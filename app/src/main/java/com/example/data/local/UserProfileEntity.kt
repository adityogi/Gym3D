package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val gender: String = "Male", // "Male" or "Female"
    val age: Int = 26,
    val heightCm: Float = 178f,
    val weightKg: Float = 78f,
    val bodyFatPct: Float = 16f,
    val goal: String = "Hypertrophy (Lean Bulk)", // "Hypertrophy (Lean Bulk)", "Fat Loss (Cut)", "Body Recomposition"
    val experienceLevel: String = "Intermediate (1-3 yrs)", // "Beginner (<1 yr)", "Intermediate (1-3 yrs)", "Advanced (3+ yrs)"
    val workoutDaysPerWeek: Int = 4, // 3, 4, 5, 6
    val prioritizedMuscle1: String = "Upper Chest",
    val prioritizedMuscle2: String = "Side Delts",
    val targetCalories: Int = 2650,
    val targetProteinG: Int = 175,
    val targetCarbsG: Int = 300,
    val targetFatG: Int = 70,
    val waistCircumferenceCm: Float = 82f,
    val isOnboardingCompleted: Boolean = false,
    val lastWeeklyCheckinTimestamp: Long = System.currentTimeMillis()
)
