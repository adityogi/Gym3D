package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}

@Dao
interface NutritionLogDao {

    @Query("SELECT * FROM nutrition_logs WHERE dateString = :dateString ORDER BY timestamp ASC")
    fun getLogsForDate(dateString: String): Flow<List<NutritionLogEntity>>

    @Query("SELECT * FROM nutrition_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<NutritionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NutritionLogEntity): Long

    @Delete
    suspend fun deleteLog(log: NutritionLogEntity)

    @Query("DELETE FROM nutrition_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}

@Dao
interface BodyMetricDao {

    @Query("SELECT * FROM body_metrics ORDER BY timestamp DESC")
    fun getAllMetrics(): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metrics ORDER BY timestamp DESC LIMIT 14")
    fun getRecentMetrics(): Flow<List<BodyMetricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: BodyMetricEntity): Long
}
