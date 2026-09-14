package com.skinthesia.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessments WHERE id = :id")
    fun observe(id: String): Flow<AssessmentEntity?>

    @Query("SELECT * FROM assessments WHERE id = :id")
    suspend fun get(id: String): AssessmentEntity?

    @Upsert
    suspend fun upsert(entity: AssessmentEntity)

    @Query("SELECT * FROM assessments WHERE status = 'COMPLETE' ORDER BY week ASC, startedAt ASC")
    fun completed(): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE status = 'COMPLETE' ORDER BY week ASC, startedAt ASC")
    suspend fun completedList(): List<AssessmentEntity>

    @Query("DELETE FROM assessments")
    suspend fun deleteAll()
}

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insertSession(session: MeasurementSessionEntity)

    @Query("SELECT COUNT(*) FROM measurement_sessions")
    suspend fun sessionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<SensorReadingEntity>)

    @Query("DELETE FROM sensor_readings WHERE sessionId = :sessionId AND region = :region")
    suspend fun deleteRegion(sessionId: String, region: String)

    @Transaction
    suspend fun replaceRegion(sessionId: String, region: String, readings: List<SensorReadingEntity>) {
        deleteRegion(sessionId, region)
        insertReadings(readings)
    }

    @Query("UPDATE measurement_sessions SET completedAt = :completedAt WHERE id = :id")
    suspend fun complete(id: String, completedAt: Long)

    @Transaction
    @Query("SELECT * FROM measurement_sessions WHERE id = :id")
    fun observe(id: String): Flow<SessionWithReadings?>

    @Transaction
    @Query("SELECT * FROM measurement_sessions WHERE id = :id")
    suspend fun get(id: String): SessionWithReadings?

    @Transaction
    @Query("SELECT * FROM measurement_sessions ORDER BY startedAt DESC")
    fun all(): Flow<List<SessionWithReadings>>

    @Query("DELETE FROM measurement_sessions")
    suspend fun deleteAll()
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY version DESC LIMIT 1")
    fun observeLatest(): Flow<PlanEntity?>

    @Query("SELECT * FROM plans ORDER BY version DESC LIMIT 1")
    suspend fun latest(): PlanEntity?

    @Query("SELECT * FROM plans ORDER BY version DESC")
    fun all(): Flow<List<PlanEntity>>

    @Upsert
    suspend fun upsert(plan: PlanEntity)

    @Query("SELECT * FROM routine_logs WHERE date = :date")
    fun logsOn(date: String): Flow<List<RoutineLogEntity>>

    @Query("SELECT * FROM routine_logs WHERE date BETWEEN :from AND :to")
    fun logsBetween(from: String, to: String): Flow<List<RoutineLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: RoutineLogEntity)

    @Query("DELETE FROM routine_logs WHERE date = :date AND stepId = :stepId")
    suspend fun deleteLog(date: String, stepId: String)

    @Query("DELETE FROM plans")
    suspend fun deletePlans()

    @Query("DELETE FROM routine_logs")
    suspend fun deleteLogs()
}

@Dao
interface CommerceDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAt ASC")
    fun cart(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun cartItem(productId: String): CartItemEntity?

    @Upsert
    suspend fun upsertCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun removeCartItem(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT * FROM orders ORDER BY placedAt DESC")
    fun orders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun order(id: String): OrderEntity?

    @Insert
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun orderCount(): Int

    @Query("DELETE FROM orders")
    suspend fun deleteOrders()
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY startAt ASC")
    fun all(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun get(id: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE expertId = :expertId AND status = 'CONFIRMED'")
    suspend fun confirmedFor(expertId: String): List<BookingEntity>

    @Upsert
    suspend fun upsert(booking: BookingEntity)

    @Query("DELETE FROM bookings")
    suspend fun deleteAll()
}

@Dao
interface CommunityDao {
    @Query("SELECT * FROM user_posts ORDER BY createdAt DESC")
    fun posts(): Flow<List<UserPostEntity>>

    @Insert
    suspend fun insertPost(post: UserPostEntity)

    @Query("SELECT * FROM user_comments ORDER BY createdAt ASC")
    fun comments(): Flow<List<UserCommentEntity>>

    @Insert
    suspend fun insertComment(comment: UserCommentEntity)

    @Query("SELECT * FROM likes")
    fun likes(): Flow<List<LikeEntity>>

    @Query("SELECT COUNT(*) FROM likes WHERE targetId = :targetId")
    suspend fun isLiked(targetId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun like(like: LikeEntity)

    @Query("DELETE FROM likes WHERE targetId = :targetId")
    suspend fun unlike(targetId: String)

    @Query("DELETE FROM user_posts")
    suspend fun deletePosts()

    @Query("DELETE FROM user_comments")
    suspend fun deleteComments()

    @Query("DELETE FROM likes")
    suspend fun deleteLikes()
}

@Dao
interface LibraryDao {
    @Query("SELECT articleId FROM saved_articles")
    fun savedIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM saved_articles WHERE articleId = :id")
    suspend fun isSaved(id: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE articleId = :id")
    suspend fun remove(id: String)

    @Query("DELETE FROM saved_articles")
    suspend fun deleteAll()
}
