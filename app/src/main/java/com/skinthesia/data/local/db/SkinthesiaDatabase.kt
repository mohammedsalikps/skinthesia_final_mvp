package com.skinthesia.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AssessmentEntity::class,
        MeasurementSessionEntity::class,
        SensorReadingEntity::class,
        PlanEntity::class,
        RoutineLogEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        BookingEntity::class,
        UserPostEntity::class,
        UserCommentEntity::class,
        LikeEntity::class,
        SavedArticleEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class SkinthesiaDatabase : RoomDatabase() {
    abstract fun assessments(): AssessmentDao
    abstract fun measurements(): MeasurementDao
    abstract fun plans(): PlanDao
    abstract fun commerce(): CommerceDao
    abstract fun bookings(): BookingDao
    abstract fun community(): CommunityDao
    abstract fun library(): LibraryDao

    companion object {
        private const val NAME = "skinthesia.db"

        fun build(context: Context): SkinthesiaDatabase =
            Room.databaseBuilder(context.applicationContext, SkinthesiaDatabase::class.java, NAME).build()
    }
}
