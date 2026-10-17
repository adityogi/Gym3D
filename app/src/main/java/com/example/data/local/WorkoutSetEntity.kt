package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    indices = [
        Index(value = ["equipmentId"]),
        Index(value = ["equipmentId", "timestamp"])
    ]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val equipmentId: String,
    val setNumber: Int,
    val weightKg: Float = 0f,
    val reps: Int = 0,
    val durationSeconds: Int = 0,
    val distanceKm: Float = 0f,
    val inclineOrResistance: Float = 0f,
    val rpe: Float = 8f,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
