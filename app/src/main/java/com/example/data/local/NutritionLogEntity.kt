package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "nutrition_logs",
    indices = [
        Index(value = ["dateString"]),
        Index(value = ["timestamp"])
    ]
)
data class NutritionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val foodName: String,
    val calories: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val timestamp: Long = System.currentTimeMillis()
)
