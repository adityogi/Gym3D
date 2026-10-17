package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorkoutSetEntity::class,
        UserProfileEntity::class,
        NutritionLogEntity::class,
        BodyMetricEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {

    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun nutritionLogDao(): NutritionLogDao
    abstract fun bodyMetricDao(): BodyMetricDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_3d_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
