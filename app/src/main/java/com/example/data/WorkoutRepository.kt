package com.example.data

import com.example.data.local.WorkoutSetDao
import com.example.data.local.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val dao: WorkoutSetDao) {

    fun getSetsForEquipment(equipmentId: String): Flow<List<WorkoutSetEntity>> {
        return dao.getSetsForEquipment(equipmentId)
    }

    fun getMaxWeightForEquipment(equipmentId: String): Flow<Float?> {
        return dao.getMaxWeightForEquipment(equipmentId)
    }

    suspend fun getLatestCompletedSet(equipmentId: String): WorkoutSetEntity? {
        return dao.getLatestCompletedSet(equipmentId)
    }

    suspend fun insertSet(set: WorkoutSetEntity): Long {
        return dao.insertSet(set)
    }

    suspend fun updateSet(set: WorkoutSetEntity) {
        dao.updateSet(set)
    }

    suspend fun deleteSetById(id: Long) {
        dao.deleteSetById(id)
    }

    suspend fun clearSetsForEquipment(equipmentId: String) {
        dao.clearSetsForEquipment(equipmentId)
    }
}
