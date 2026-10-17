package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_metrics")
data class BodyMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val weightKg: Float,
    val waistCircumferenceCm: Float,
    val estimatedBench1RM: Float = 0f,
    val estimatedSquat1RM: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
