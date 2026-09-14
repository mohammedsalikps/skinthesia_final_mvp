package com.skinthesia.data.repository

import com.skinthesia.data.local.SkinthesiaJson
import com.skinthesia.data.local.db.AssessmentDao
import com.skinthesia.data.local.db.AssessmentEntity
import com.skinthesia.data.local.db.MeasurementDao
import com.skinthesia.data.local.db.MeasurementSessionEntity
import com.skinthesia.data.local.db.PlanDao
import com.skinthesia.data.local.db.PlanEntity
import com.skinthesia.data.local.db.RoutineLogEntity
import com.skinthesia.data.local.db.SensorReadingEntity
import com.skinthesia.data.local.db.SessionWithReadings
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.DataSource
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.RoutineLog
import com.skinthesia.domain.model.SensorMeasurement
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomAssessmentRepository(private val dao: AssessmentDao) : AssessmentRepository {

    override fun observe(id: String): Flow<Assessment?> = dao.observe(id).map { it?.toDomain() }

    override suspend fun get(id: String): Assessment? = dao.get(id)?.toDomain()

    override suspend fun save(assessment: Assessment) = dao.upsert(assessment.toEntity())

    override suspend fun update(id: String, transform: (Assessment) -> Assessment): Assessment? {
        val current = get(id) ?: return null
        val next = transform(current)
        dao.upsert(next.toEntity())
        return next
    }

    override fun completed(): Flow<List<Assessment>> = dao.completed().map { list -> list.mapNotNull { it.toDomain() } }

    override suspend fun completedList(): List<Assessment> = dao.completedList().mapNotNull { it.toDomain() }

    override suspend fun deleteAll() = dao.deleteAll()

    private fun Assessment.toEntity() = AssessmentEntity(
        id = id,
        kind = kind.name,
        week = week,
        status = status.name,
        startedAt = startedAt,
        completedAt = completedAt,
        overall = skinPrint?.overall,
        json = SkinthesiaJson.encodeToString(Assessment.serializer(), this),
    )

    private fun AssessmentEntity.toDomain(): Assessment? =
        runCatching { SkinthesiaJson.decodeFromString(Assessment.serializer(), json) }.getOrNull()
}

class RoomSkinProbeRepository(
    private val dao: MeasurementDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : SkinProbeRepository {

    override suspend fun startSession(userId: String, assessmentId: String?, device: ProbeDevice): MeasurementSession {
        val entity = MeasurementSessionEntity(
            id = Ids.new("ses"),
            number = dao.sessionCount() + 1,
            userId = userId,
            assessmentId = assessmentId,
            deviceId = device.id,
            deviceName = device.name,
            isSimulated = device.isSimulated,
            startedAt = clock(),
            completedAt = null,
        )
        dao.insertSession(entity)
        return SessionWithReadings(entity, emptyList()).toDomain()
    }

    override suspend fun saveReadings(sessionId: String, region: MeasurementRegion, readings: List<SensorMeasurement>) {
        dao.replaceRegion(
            sessionId = sessionId,
            region = region.name,
            readings = readings.map {
                SensorReadingEntity(
                    id = it.id,
                    sessionId = sessionId,
                    userId = it.userId,
                    region = it.region.name,
                    sensor = it.sensor.name,
                    value = it.value,
                    timestamp = it.timestamp,
                    source = it.source.name,
                )
            },
        )
    }

    override suspend fun completeSession(sessionId: String) = dao.complete(sessionId, clock())

    override fun observeSession(id: String): Flow<MeasurementSession?> = dao.observe(id).map { it?.toDomain() }

    override suspend fun session(id: String): MeasurementSession? = dao.get(id)?.toDomain()

    override fun sessions(): Flow<List<MeasurementSession>> = dao.all().map { list -> list.map { it.toDomain() } }

    override suspend fun deleteAll() = dao.deleteAll()

    private fun SessionWithReadings.toDomain() = MeasurementSession(
        id = session.id,
        number = session.number,
        userId = session.userId,
        assessmentId = session.assessmentId,
        deviceId = session.deviceId,
        deviceName = session.deviceName,
        isSimulated = session.isSimulated,
        startedAt = session.startedAt,
        completedAt = session.completedAt,
        readings = readings.mapNotNull { reading ->
            val region = enumOrNull<MeasurementRegion>(reading.region) ?: return@mapNotNull null
            val sensor = enumOrNull<SensorType>(reading.sensor) ?: return@mapNotNull null
            SensorMeasurement(
                id = reading.id,
                userId = reading.userId,
                sessionId = reading.sessionId,
                region = region,
                sensor = sensor,
                value = reading.value,
                timestamp = reading.timestamp,
                source = enumOrNull<DataSource>(reading.source) ?: DataSource.SENSOR_SIMULATED,
            )
        }.sortedWith(compareBy({ it.region.ordinal }, { it.sensor.ordinal })),
    )
}

class RoomPlanRepository(
    private val dao: PlanDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : PlanRepository {

    override val currentPlan: Flow<PersonalizedPlan?> = dao.observeLatest().map { it?.toDomain() }

    override suspend fun current(): PersonalizedPlan? = dao.latest()?.toDomain()

    override fun history(): Flow<List<PersonalizedPlan>> = dao.all().map { list -> list.mapNotNull { it.toDomain() } }

    override suspend fun save(plan: PersonalizedPlan) = dao.upsert(
        PlanEntity(
            id = plan.id,
            version = plan.version,
            createdAt = plan.createdAt,
            json = SkinthesiaJson.encodeToString(PersonalizedPlan.serializer(), plan),
        ),
    )

    override fun logsOn(date: LocalDate): Flow<List<RoutineLog>> =
        dao.logsOn(date.toString()).map { list -> list.map { RoutineLog(it.date, it.stepId, it.completedAt) } }

    override fun logsBetween(from: LocalDate, to: LocalDate): Flow<List<RoutineLog>> =
        dao.logsBetween(from.toString(), to.toString()).map { list -> list.map { RoutineLog(it.date, it.stepId, it.completedAt) } }

    override suspend fun setStepDone(date: LocalDate, stepId: String, done: Boolean) {
        if (done) dao.insertLog(RoutineLogEntity(date.toString(), stepId, clock())) else dao.deleteLog(date.toString(), stepId)
    }

    override suspend fun deleteAll() {
        dao.deletePlans()
        dao.deleteLogs()
    }

    private fun PlanEntity.toDomain(): PersonalizedPlan? =
        runCatching { SkinthesiaJson.decodeFromString(PersonalizedPlan.serializer(), json) }.getOrNull()
}

internal inline fun <reified T : Enum<T>> enumOrNull(name: String): T? =
    runCatching { enumValueOf<T>(name) }.getOrNull()
