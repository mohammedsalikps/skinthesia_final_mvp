package com.skinthesia.domain.repository

import com.skinthesia.domain.model.AppSettings
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.Booking
import com.skinthesia.domain.model.CartItem
import com.skinthesia.domain.model.CartLine
import com.skinthesia.domain.model.CommunityComment
import com.skinthesia.domain.model.CommunityPost
import com.skinthesia.domain.model.CommunitySection
import com.skinthesia.domain.model.ConsultationExpert
import com.skinthesia.domain.model.ConsultationSlot
import com.skinthesia.domain.model.ConsultationType
import com.skinthesia.domain.model.DeliveryDetails
import com.skinthesia.domain.model.LearningArticle
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.Order
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.RoutineLog
import com.skinthesia.domain.model.SensorMeasurement
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/*
 * Repository contracts. Every implementation today is local (DataStore, Room or
 * bundled seed data), so the demonstration works fully offline. Each interface is
 * the seam where a backend (auth, cloud profile, catalogue, payments, scheduling,
 * community) plugs in later without touching the UI.
 */

interface UserProfileRepository {
    val profile: Flow<UserProfile>
    suspend fun current(): UserProfile
    suspend fun update(transform: (UserProfile) -> UserProfile): UserProfile
    suspend fun clear()
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun current(): AppSettings
    suspend fun update(transform: (AppSettings) -> AppSettings)
    suspend fun clear()
}

interface AssessmentRepository {
    fun observe(id: String): Flow<Assessment?>
    suspend fun get(id: String): Assessment?
    suspend fun save(assessment: Assessment)
    suspend fun update(id: String, transform: (Assessment) -> Assessment): Assessment?

    /** Completed assessments, oldest first. */
    fun completed(): Flow<List<Assessment>>
    suspend fun completedList(): List<Assessment>
    suspend fun deleteAll()
}

/** Persists probe sessions and their readings (user, session, region, sensor, time). */
interface SkinProbeRepository {
    suspend fun startSession(userId: String, assessmentId: String?, device: ProbeDevice): MeasurementSession
    suspend fun saveReadings(sessionId: String, region: MeasurementRegion, readings: List<SensorMeasurement>)
    suspend fun completeSession(sessionId: String)
    fun observeSession(id: String): Flow<MeasurementSession?>
    suspend fun session(id: String): MeasurementSession?

    /** All sessions, newest first. */
    fun sessions(): Flow<List<MeasurementSession>>
    suspend fun deleteAll()
}

interface PlanRepository {
    val currentPlan: Flow<PersonalizedPlan?>
    suspend fun current(): PersonalizedPlan?

    /** Every plan version, newest first. */
    fun history(): Flow<List<PersonalizedPlan>>
    suspend fun save(plan: PersonalizedPlan)
    fun logsOn(date: LocalDate): Flow<List<RoutineLog>>
    fun logsBetween(from: LocalDate, to: LocalDate): Flow<List<RoutineLog>>
    suspend fun setStepDone(date: LocalDate, stepId: String, done: Boolean)
    suspend fun deleteAll()
}

interface ProductCatalogRepository {
    val products: Flow<List<Product>>
    suspend fun all(): List<Product>
    suspend fun byId(id: String): Product?
}

interface CartRepository {
    val items: Flow<List<CartItem>>
    suspend fun add(productId: String, quantity: Int = 1)
    suspend fun setQuantity(productId: String, quantity: Int)
    suspend fun remove(productId: String)
    suspend fun clear()
}

/** Simulated checkout: no payment provider is connected in the demonstration build. */
interface OrderRepository {
    val orders: Flow<List<Order>>
    suspend fun placeOrder(lines: List<CartLine>, delivery: DeliveryDetails): Order
    suspend fun order(id: String): Order?
    suspend fun deleteAll()
}

interface ConsultationRepository {
    val experts: Flow<List<ConsultationExpert>>
    suspend fun expert(id: String): ConsultationExpert?
    suspend fun slots(expertId: String, date: LocalDate): List<ConsultationSlot>
    val bookings: Flow<List<Booking>>
    suspend fun booking(id: String): Booking?
    suspend fun book(
        expert: ConsultationExpert,
        slot: ConsultationSlot,
        type: ConsultationType,
        notes: String,
        shareSkinPrint: Boolean,
    ): Booking
    suspend fun cancel(bookingId: String)
    suspend fun deleteAll()
}

interface LearningRepository {
    val articles: Flow<List<LearningArticle>>
    suspend fun article(id: String): LearningArticle?
    val savedIds: Flow<Set<String>>
    suspend fun toggleSaved(id: String)
}

interface CommunityRepository {
    /** Posts for a section. FOR_YOU is personalised around [goals]. */
    fun feed(section: CommunitySection, goals: Set<SkinGoal>): Flow<List<CommunityPost>>
    fun post(id: String): Flow<CommunityPost?>
    suspend fun toggleLike(postId: String)
    suspend fun toggleCommentLike(commentId: String)
    suspend fun addComment(postId: String, body: String): CommunityComment
    suspend fun createPost(section: CommunitySection, title: String, body: String, tags: List<String>): CommunityPost
    suspend fun deleteUserContent()
}
