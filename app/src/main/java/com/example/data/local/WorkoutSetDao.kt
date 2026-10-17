package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {

    @Query("SELECT * FROM workout_sets WHERE equipmentId = :equipmentId ORDER BY timestamp DESC, setNumber ASC")
    fun getSetsForEquipment(equipmentId: String): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets ORDER BY timestamp DESC")
    fun getAllSets(): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE equipmentId = :equipmentId AND isCompleted = 1 ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun getLatestCompletedSet(equipmentId: String): WorkoutSetEntity?

    @Query("SELECT MAX(weightKg) FROM workout_sets WHERE equipmentId = :equipmentId AND isCompleted = 1")
    fun getMaxWeightForEquipment(equipmentId: String): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Delete
    suspend fun deleteSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSetById(id: Long)

    @Query("DELETE FROM workout_sets WHERE equipmentId = :equipmentId")
    suspend fun clearSetsForEquipment(equipmentId: String)
}
