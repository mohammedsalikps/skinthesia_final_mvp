package com.skinthesia.data.local.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/*
 * Room schema. History-shaped data (assessments, probe sessions and readings,
 * plan versions, routine logs, orders, bookings, community activity) lives here.
 * Rich, evolving aggregates are stored as JSON columns next to the few fields we
 * query on; probe readings are fully relational (user, session, region, sensor, time).
 */

@Entity(tableName = "assessments", indices = [Index("status"), Index("week")])
data class AssessmentEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val week: Int,
    val status: String,
    val startedAt: Long,
    val completedAt: Long?,
    val overall: Int?,
    val json: String,
)

@Entity(tableName = "measurement_sessions", indices = [Index("assessmentId")])
data class MeasurementSessionEntity(
    @PrimaryKey val id: String,
    val number: Int,
    val userId: String,
    val assessmentId: String?,
    val deviceId: String,
    val deviceName: String,
    val isSimulated: Boolean,
    val startedAt: Long,
    val completedAt: Long?,
)

@Entity(
    tableName = "sensor_readings",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId"), Index("userId")],
)
data class SensorReadingEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val userId: String,
    val region: String,
    val sensor: String,
    val value: Double,
    val timestamp: Long,
    val source: String,
)

data class SessionWithReadings(
    @Embedded val session: MeasurementSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val readings: List<SensorReadingEntity>,
)

@Entity(tableName = "plans", indices = [Index("version")])
data class PlanEntity(
    @PrimaryKey val id: String,
    val version: Int,
    val createdAt: Long,
    val json: String,
)

@Entity(tableName = "routine_logs", primaryKeys = ["date", "stepId"])
data class RoutineLogEntity(
    val date: String,
    val stepId: String,
    val completedAt: Long,
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int,
    val addedAt: Long,
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val placedAt: Long,
    val json: String,
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val expertId: String,
    val startAt: Long,
    val status: String,
    val json: String,
)

@Entity(tableName = "user_posts")
data class UserPostEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val json: String,
)

@Entity(tableName = "user_comments", indices = [Index("postId")])
data class UserCommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val createdAt: Long,
    val json: String,
)

@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey val targetId: String,
    val createdAt: Long,
)

@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey val articleId: String,
    val savedAt: Long,
)
