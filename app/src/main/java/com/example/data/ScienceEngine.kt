package com.example.data

import com.example.model.Equipment
import com.example.model.MuscleGroup
import java.util.Calendar

data class WorkoutSessionPlan(
    val dayName: String,
    val splitTitle: String,
    val targetMuscles: List<String>,
    val exercises: List<PlannedExercise>
)

data class PlannedExercise(
    val equipmentId: String,
    val exerciseName: String,
    val prescribedSets: Int,
    val targetRepRange: String, // e.g. "8–12"
    val targetRir: String, // e.g. "1–2 RIR"
    val restSeconds: Int,
    val volumeLandmark: String, // "MEV (6-10)", "MAV (12-20)", "MRV (22+)"
    val biomechanicalSwaps: List<BiomechanicalSwap>
)

data class BiomechanicalSwap(
    val name: String,
    val targetDivision: String,
    val equipmentId: String,
    val rationale: String
)

data class ScienceArticle(
    val dayNumber: Int,
    val title: String,
    val citation: String,
    val takeaway: String,
    val fullText: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val correctIndex: Int
)

data class ScienceQAPair(
    val question: String,
    val answer: String,
    val scientificCitations: String
)

sealed class DoubleProgressionStatus {
    data class IncreaseLoad(val recommendation: String, val suggestedDeltaKg: Float) : DoubleProgressionStatus()
    data class MaintainLoad(val recommendation: String) : DoubleProgressionStatus()
    data class DeloadAlert(val recommendation: String) : DoubleProgressionStatus()
}

object ScienceEngine {

    // 1. Mifflin-St Jeor TDEE & Macro Calculator
    fun calculateNutritionTargets(
        isMale: Boolean,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        daysPerWeek: Int,
        goal: String // "Hypertrophy (Lean Bulk)", "Fat Loss (Cut)", "Body Recomposition"
    ): Triple<Int, Triple<Int, Int, Int>, Int> {
        // Mifflin-St Jeor BMR
        val bmr = (10f * weightKg) + (6.25f * heightCm) - (5f * age) + (if (isMale) 5f else -161f)

        // Activity multiplier
        val activityMultiplier = when {
            daysPerWeek <= 3 -> 1.40f
            daysPerWeek <= 4 -> 1.50f
            daysPerWeek <= 5 -> 1.60f
            else -> 1.70f
        }

        val maintenanceTdee = (bmr * activityMultiplier).toInt()

        val targetCalories = when (goal) {
            "Fat Loss (Cut)" -> (maintenanceTdee - 450).coerceAtLeast(1400)
            "Hypertrophy (Lean Bulk)" -> maintenanceTdee + 275
            else -> maintenanceTdee - 75 // Body Recomposition
        }

        // Protein: 2.2g per kg bodyweight
        val proteinG = (weightKg * 2.2f).toInt()
        val proteinCalories = proteinG * 4

        // Fat: 25% of target calories
        val fatCalories = (targetCalories * 0.25f).toInt()
        val fatG = (fatCalories / 9f).toInt()

        // Carbs: Remainder of calories
        val carbCalories = (targetCalories - (proteinCalories + fatCalories)).coerceAtLeast(400)
        val carbsG = (carbCalories / 4f).toInt()

        return Triple(targetCalories, Triple(proteinG, carbsG, fatG), maintenanceTdee)
    }

    // Helpers for Diagnostic Onboarding Telemetry
    fun calculateBmi(heightCm: Float, weightKg: Float): Float {
        val heightM = heightCm / 100f
        return if (heightM > 0f) weightKg / (heightM * heightM) else 0f
    }

    fun calculateBmr(isMale: Boolean, age: Int, heightCm: Float, weightKg: Float): Float {
        return (10f * weightKg) + (6.25f * heightCm) - (5f * age) + (if (isMale) 5f else -161f)
    }

    fun getSplitDescription(daysPerWeek: Int): String {
        return when (daysPerWeek) {
            3 -> "3-Day Full Body Foundation (A / B / C)"
            4 -> "4-Day Upper / Lower Periodized Split"
            5 -> "5-Day Push / Pull / Legs / Upper / Lower"
            else -> "6-Day Push / Pull / Legs (PPL x 2)"
        }
    }

    // 2. Periodized Split Allocation Engine
    fun generateSplit(
        daysPerWeek: Int,
        prioritized1: String = "Upper Chest",
        prioritized2: String = "Side Delts",
        experienceLevel: String = "Intermediate (1-3 yrs)",
        equipmentAccess: String = "Commercial Gym"
    ): List<WorkoutSessionPlan> {
        val extraChest = prioritized1 == "Upper Chest" || prioritized2 == "Upper Chest"
        val extraDelts = prioritized1 == "Side Delts" || prioritized2 == "Side Delts"
        val extraLats = prioritized1 == "Lats" || prioritized2 == "Lats"
        val extraQuads = prioritized1 == "Quads & Glutes" || prioritized2 == "Quads & Glutes"
        val extraArms = prioritized1 == "Arms" || prioritized2 == "Arms"

        val isBeginner = experienceLevel.contains("Beginner", ignoreCase = true)
        val isAdvanced = experienceLevel.contains("Advanced", ignoreCase = true)

        val defaultRir = when {
            isBeginner -> "2–3 RIR"
            isAdvanced -> "0–1 RIR"
            else -> "1–2 RIR"
        }

        val baseSets = when {
            isBeginner -> 3
            isAdvanced -> 4
            else -> 3
        }

        val isDumbbellsOnly = equipmentAccess.contains("Dumbbell", ignoreCase = true)
        val isBodyweight = equipmentAccess.contains("Bodyweight", ignoreCase = true)

        // Equipment resolution helpers
        fun chestPressName() = when {
            isBodyweight -> "Deficit Tempo Push-ups"
            isDumbbellsOnly -> "Flat Dumbbell Press"
            else -> "Barbell Flat Bench Press"
        }
        fun chestPressEquip() = if (isDumbbellsOnly) "incline_dumbbell" else "bench_press"

        fun squatName() = when {
            isBodyweight -> "Bodyweight Bulgarian Split Squats"
            isDumbbellsOnly -> "Dumbbell Goblet Squat"
            else -> "Barbell Back Squat"
        }
        fun squatEquip() = if (isDumbbellsOnly) "incline_dumbbell" else "squat_rack"

        fun pullName() = when {
            isBodyweight -> "Doorway Inverted Rows"
            isDumbbellsOnly -> "Incline Chest-Supported DB Row"
            else -> "Cable Lat Pulldown"
        }
        fun pullEquip() = if (isDumbbellsOnly) "incline_dumbbell" else "lat_pulldown"

        fun rowName() = when {
            isBodyweight -> "Scapular Inverted Rows"
            isDumbbellsOnly -> "One-Arm Dumbbell Row"
            else -> "Seated Cable Row"
        }
        fun rowEquip() = if (isDumbbellsOnly) "incline_dumbbell" else "seated_row"

        fun flyName() = when {
            isBodyweight -> "Floor Sliding Flyes"
            isDumbbellsOnly -> "Dumbbell Incline Fly"
            else -> "Dual Cable Pec Fly"
        }
        fun flyEquip() = if (isDumbbellsOnly) "incline_dumbbell" else "cable_fly"

        return when (daysPerWeek) {
            3 -> listOf(
                WorkoutSessionPlan(
                    dayName = "Workout A",
                    splitTitle = "Full Body Foundation",
                    targetMuscles = listOf("Pectorals", "Quadriceps", "Latissimus Dorsi", "Deltoids"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = chestPressEquip(),
                            exerciseName = chestPressName(),
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = if (isBeginner) "8–12" else "6–10",
                            targetRir = defaultRir,
                            restSeconds = if (isBeginner) 120 else 150,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Primary Compound)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Incline Dumbbell Press", "Clavicular Pec Head", "incline_dumbbell", "Easier on shoulder joint, deeper stretch"),
                                BiomechanicalSwap("Dual Cable Pec Fly", "Sternal Pec Fibers", "cable_fly", "Continuous horizontal adduction tension")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = squatEquip(),
                            exerciseName = squatName(),
                            prescribedSets = if (extraQuads) baseSets + 1 else baseSets,
                            targetRepRange = if (isBeginner) "8–10" else "6–8",
                            targetRir = defaultRir,
                            restSeconds = 180,
                            volumeLandmark = if (extraQuads) "MRV Focus (+3-4 Sets Priority)" else "MAV (Quadriceps & Glute)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Dumbbell Goblet Squat", "Quadriceps Focus", "incline_dumbbell", "Reduced axial spinal compression"),
                                BiomechanicalSwap("Rowing Ergometer Sprint", "Lower Body Power", "rowing_machine", "Aerobic leg drive power")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = pullEquip(),
                            exerciseName = pullName(),
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Back Width)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Seated Cable Row", "Mid-Back Thickness", "seated_row", "Horizontal scapular retraction emphasis")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Workout B",
                    splitTitle = "Full Body Hypertrophy",
                    targetMuscles = listOf("Latissimus Dorsi", "Shoulders", "Chest", "Core"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = rowEquip(),
                            exerciseName = rowName(),
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Rhomboids & Lats)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Lat Pulldown", "Vertical Lat Width", "lat_pulldown", "Alternative vertical plane pull")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = if (isBodyweight) "Feet-Elevated Push-ups" else "Dumbbell Incline Bench Press",
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Upper Chest)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Pec Crossover", "Clavicular Pec", "cable_fly", "Low-to-high fly matching upper chest fibers")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "rowing_machine",
                            exerciseName = "Concept Air Rower Intervals",
                            prescribedSets = 3,
                            targetRepRange = "500m Intervals",
                            targetRir = "Zone 4 Cardio",
                            restSeconds = 90,
                            volumeLandmark = "Conditioning (Total Body)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Commercial Treadmill Incline", "Cardio Conditioning", "treadmill", "Low joint impact incline walk")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Workout C",
                    splitTitle = "Full Body Finish & Delts",
                    targetMuscles = listOf("Deltoids", "Pectorals", "Back", "Legs"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = flyEquip(),
                            exerciseName = flyName(),
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "10–15",
                            targetRir = defaultRir,
                            restSeconds = 90,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Chest Isolation)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Dumbbell Incline Press", "Chest Hypertrophy", "incline_dumbbell", "Compound pressing variation")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = "Dumbbell Lateral Raise & Delts",
                            prescribedSets = if (extraDelts) baseSets + 2 else baseSets,
                            targetRepRange = "12–15",
                            targetRir = defaultRir,
                            restSeconds = 75,
                            volumeLandmark = if (extraDelts) "MRV Focus (+3-4 Sets Priority)" else "MAV (Side Delts)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Lateral Raise", "Lateral Deltoid", "cable_fly", "Continuous tension curve")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "exercise_bike",
                            exerciseName = "Ergometer Bike Intervals",
                            prescribedSets = 3,
                            targetRepRange = "3 Min Rounds",
                            targetRir = "Zone 3/4",
                            restSeconds = 90,
                            volumeLandmark = "Aerobic Engine",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Treadmill Incline Run", "Aerobic Conditioning", "treadmill", "High calorie expenditure")
                            )
                        )
                    )
                )
            )

            4 -> listOf(
                WorkoutSessionPlan(
                    dayName = "Upper 1",
                    splitTitle = "Upper Body Strength & Chest",
                    targetMuscles = listOf("Pectorals", "Latissimus Dorsi", "Deltoids", "Triceps"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = chestPressEquip(),
                            exerciseName = chestPressName(),
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = if (isBeginner) "8–10" else "6–8",
                            targetRir = defaultRir,
                            restSeconds = 150,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Chest Compound)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Incline Dumbbell Press", "Upper Chest Focus", "incline_dumbbell", "Greater unilateral freedom")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = pullEquip(),
                            exerciseName = pullName(),
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Latissimus Dorsi)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Seated Cable Row", "Horizontal Back", "seated_row", "Thickness and scapular retraction")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = flyEquip(),
                            exerciseName = flyName(),
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "10–15",
                            targetRir = defaultRir,
                            restSeconds = 90,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Pec Peak Tension)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Incline Dumbbell Press", "Clavicular Pec", "incline_dumbbell", "Heavy dumbbell overload")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Lower 1",
                    splitTitle = "Lower Body Quad & Posterior Chain",
                    targetMuscles = listOf("Quadriceps", "Glutes", "Hamstrings", "Calves"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = squatEquip(),
                            exerciseName = squatName(),
                            prescribedSets = if (extraQuads) baseSets + 1 else baseSets,
                            targetRepRange = if (isBeginner) "8–10" else "6–10",
                            targetRir = defaultRir,
                            restSeconds = 180,
                            volumeLandmark = if (extraQuads) "MRV Focus (+3-4 Sets Priority)" else "MAV (Quad Compound)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Dumbbell Squats", "Quad Focus", "incline_dumbbell", "Zero spine compressive load")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "rowing_machine",
                            exerciseName = "Concept Rowing Ergometer",
                            prescribedSets = 3,
                            targetRepRange = "500m Pace",
                            targetRir = "RPE 8",
                            restSeconds = 90,
                            volumeLandmark = "Posterior Chain Conditioning",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Treadmill Incline Hike", "Posterior Chain", "treadmill", "Glute & hamstring slope work")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Upper 2",
                    splitTitle = "Upper Body Hypertrophy & Back",
                    targetMuscles = listOf("Latissimus Dorsi", "Shoulders", "Chest", "Biceps"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = rowEquip(),
                            exerciseName = rowName(),
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Mid-Back)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Lat Pulldown", "Vertical Back", "lat_pulldown", "Latissimus width emphasis")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = if (isBodyweight) "Incline Push-ups" else "Incline Dumbbell Press",
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Upper Chest)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Barbell Flat Bench", "Sternal Chest", "bench_press", "Heavy barbell loading")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = "Lateral Raises & Rear Delts",
                            prescribedSets = if (extraDelts) baseSets + 2 else baseSets,
                            targetRepRange = "12–15",
                            targetRir = defaultRir,
                            restSeconds = 75,
                            volumeLandmark = if (extraDelts) "MRV Focus (+3-4 Sets Priority)" else "MAV (Lateral Delts)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Lateral Raise", "Lateral Delts", "cable_fly", "Constant cable tension")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Lower 2",
                    splitTitle = "Lower Body & Conditioning",
                    targetMuscles = listOf("Quadriceps", "Hamstrings", "Cardiovascular"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = "exercise_bike",
                            exerciseName = "Stationary Ergometer Bike",
                            prescribedSets = 4,
                            targetRepRange = "4 Min Tabata",
                            targetRir = "Zone 4",
                            restSeconds = 90,
                            volumeLandmark = "Aerobic Leg Drive",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Treadmill Tempo Run", "Leg Conditioning", "treadmill", "Weight-bearing cardio")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "treadmill",
                            exerciseName = "Treadmill Incline Walk",
                            prescribedSets = 3,
                            targetRepRange = "10 Min at 8%",
                            targetRir = "Zone 2",
                            restSeconds = 60,
                            volumeLandmark = "Fat Oxidation Cardio",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Concept Air Rower", "Full Body Aerobic", "rowing_machine", "Upper/lower blended work")
                            )
                        )
                    )
                )
            )

            5 -> listOf(
                WorkoutSessionPlan(
                    dayName = "Push 1",
                    splitTitle = "Chest, Shoulders & Triceps Strength",
                    targetMuscles = listOf("Pectorals", "Anterior Deltoids", "Triceps"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = chestPressEquip(),
                            exerciseName = chestPressName(),
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = if (isBeginner) "8–10" else "6–8",
                            targetRir = defaultRir,
                            restSeconds = 150,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Primary Push)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Incline Dumbbell Press", "Upper Chest", "incline_dumbbell", "Clavicular head isolation")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = if (isBodyweight) "Feet-Elevated Push-ups" else "Incline Dumbbell Press",
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Upper Chest)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Pec Crossover", "Inner Chest", "cable_fly", "Constant cable tension")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = flyEquip(),
                            exerciseName = flyName(),
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "10–15",
                            targetRir = defaultRir,
                            restSeconds = 90,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Chest Isolation)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Incline Dumbbell Fly", "Chest Stretch", "incline_dumbbell", "Loaded eccentric stretch")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Pull 1",
                    splitTitle = "Back, Rear Delts & Biceps Strength",
                    targetMuscles = listOf("Latissimus Dorsi", "Trapezius", "Rhomboids", "Biceps"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = pullEquip(),
                            exerciseName = pullName(),
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Vertical Pull)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Seated Cable Row", "Horizontal Pull", "seated_row", "Thickness and scapular retraction")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = rowEquip(),
                            exerciseName = rowName(),
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Horizontal Pull)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Lat Pulldown", "Vertical Width", "lat_pulldown", "Wide grip lat stimulation")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "rowing_machine",
                            exerciseName = "Concept Air Rower Finish",
                            prescribedSets = 3,
                            targetRepRange = "500m Sprint",
                            targetRir = "Zone 4",
                            restSeconds = 90,
                            volumeLandmark = "Posterior Conditioning",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Bike Ergometer", "Conditioning", "exercise_bike", "Low impact cardio alternative")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Legs 1",
                    splitTitle = "Quads, Hamstrings & Calves",
                    targetMuscles = listOf("Quadriceps", "Gluteus Maximus", "Hamstrings", "Calves"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = squatEquip(),
                            exerciseName = squatName(),
                            prescribedSets = if (extraQuads) baseSets + 1 else baseSets,
                            targetRepRange = if (isBeginner) "8–10" else "6–8",
                            targetRir = defaultRir,
                            restSeconds = 180,
                            volumeLandmark = if (extraQuads) "MRV Focus (+3-4 Sets Priority)" else "MAV (Quad Anchor)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Dumbbell Goblet Squat", "Quad Focus", "incline_dumbbell", "Lower spine relief")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "exercise_bike",
                            exerciseName = "Stationary Ergometer Bike",
                            prescribedSets = 3,
                            targetRepRange = "5 Min Intervals",
                            targetRir = "Zone 3",
                            restSeconds = 90,
                            volumeLandmark = "Quad Flush Conditioning",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Treadmill Incline Walk", "Glute Focus", "treadmill", "Incline walking")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Upper Hypertrophy",
                    splitTitle = "Upper Body Pump & Delts Focus",
                    targetMuscles = listOf("Deltoids", "Pectorals", "Latissimus Dorsi", "Arms"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = "Incline Dumbbell Hex Press",
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "10–12",
                            targetRir = defaultRir,
                            restSeconds = 90,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Upper Chest Hypertrophy)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Dual Cable Pec Fly", "Chest Isolation", "cable_fly", "Horizontal adduction tension")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = "Dumbbell Lateral Raise Constant Tension",
                            prescribedSets = if (extraDelts) baseSets + 2 else baseSets,
                            targetRepRange = "12–15",
                            targetRir = defaultRir,
                            restSeconds = 60,
                            volumeLandmark = if (extraDelts) "MRV Focus (+3-4 Sets Priority)" else "MAV (Side Delts)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Y-Raise", "Lateral Deltoids", "cable_fly", "Scapular plane lateral elevation")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = rowEquip(),
                            exerciseName = "Close-Grip High Cable / DB Row",
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "10–12",
                            targetRir = defaultRir,
                            restSeconds = 90,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Upper Back)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Pulldown", "Lats", "lat_pulldown", "Vertical plane")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Lower Hypertrophy",
                    splitTitle = "Glute, Hamstrings & Cardio Flush",
                    targetMuscles = listOf("Glutes", "Hamstrings", "Quadriceps", "Calves"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = squatEquip(),
                            exerciseName = if (isBodyweight) "Walking Lunges & Jump Squats" else "Dumbbell Romanian Deadlift",
                            prescribedSets = if (extraQuads) baseSets + 1 else baseSets,
                            targetRepRange = "10–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraQuads) "MRV Focus (+3-4 Sets Priority)" else "MAV (Posterior Hypertrophy)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Goblet Squat", "Quads", "incline_dumbbell", "Knee flexion focus")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "treadmill",
                            exerciseName = "Treadmill High Incline Power Walk",
                            prescribedSets = 3,
                            targetRepRange = "12 Min at 10%",
                            targetRir = "Zone 2",
                            restSeconds = 60,
                            volumeLandmark = "Aerobic & Glute Endurance",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Concept Air Rower", "Full Body", "rowing_machine", "Aerobic engine")
                            )
                        )
                    )
                )
            )

            else -> listOf(
                WorkoutSessionPlan(
                    dayName = "Push 1",
                    splitTitle = "Chest, Anterior Delts & Triceps (Heavy)",
                    targetMuscles = listOf("Pectorals", "Anterior Deltoids", "Triceps"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = chestPressEquip(),
                            exerciseName = chestPressName(),
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = if (isBeginner) "8–10" else "6–8",
                            targetRir = defaultRir,
                            restSeconds = 150,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Primary Push)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Incline Dumbbell Press", "Upper Chest", "incline_dumbbell", "Clavicular head isolation")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = if (isBodyweight) "Incline Push-ups" else "Incline Dumbbell Press",
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Upper Chest)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Pec Crossover", "Inner Chest", "cable_fly", "Constant cable tension")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = flyEquip(),
                            exerciseName = flyName(),
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "10–15",
                            targetRir = defaultRir,
                            restSeconds = 90,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Chest Isolation)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Incline Dumbbell Fly", "Chest Stretch", "incline_dumbbell", "Loaded eccentric stretch")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Pull 1",
                    splitTitle = "Back Width & Biceps (Vertical Pull)",
                    targetMuscles = listOf("Latissimus Dorsi", "Biceps", "Rear Deltoids"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = pullEquip(),
                            exerciseName = pullName(),
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Vertical Pull)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Seated Cable Row", "Horizontal Pull", "seated_row", "Thickness and scapular retraction")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = rowEquip(),
                            exerciseName = rowName(),
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Horizontal Pull)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Lat Pulldown", "Vertical Width", "lat_pulldown", "Wide grip lat stimulation")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "rowing_machine",
                            exerciseName = "Concept Air Rower Finish",
                            prescribedSets = 3,
                            targetRepRange = "500m Sprint",
                            targetRir = "Zone 4",
                            restSeconds = 90,
                            volumeLandmark = "Posterior Conditioning",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Bike Ergometer", "Conditioning", "exercise_bike", "Low impact cardio alternative")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Legs 1",
                    splitTitle = "Quads & Calves (Knee Flexion Heavy)",
                    targetMuscles = listOf("Quadriceps", "Gluteus Maximus", "Calves"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = squatEquip(),
                            exerciseName = squatName(),
                            prescribedSets = if (extraQuads) baseSets + 1 else baseSets,
                            targetRepRange = if (isBeginner) "8–10" else "6–8",
                            targetRir = defaultRir,
                            restSeconds = 180,
                            volumeLandmark = if (extraQuads) "MRV Focus (+3-4 Sets Priority)" else "MAV (Quad Anchor)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Dumbbell Goblet Squat", "Quad Focus", "incline_dumbbell", "Lower spine relief")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "exercise_bike",
                            exerciseName = "Stationary Ergometer Bike",
                            prescribedSets = 3,
                            targetRepRange = "5 Min Intervals",
                            targetRir = "Zone 3",
                            restSeconds = 90,
                            volumeLandmark = "Quad Flush Conditioning",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Treadmill Incline Walk", "Glute Focus", "treadmill", "Incline walking")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Push 2",
                    splitTitle = "Delts & Upper Chest Hypertrophy",
                    targetMuscles = listOf("Deltoids", "Pectorals", "Triceps"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = "Incline Dumbbell Press",
                            prescribedSets = if (extraChest) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraChest) "MRV Focus (+3-4 Sets Priority)" else "MAV (Clavicular Pec)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Barbell Flat Bench", "Sternal Pec", "bench_press", "Heavy press")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "incline_dumbbell",
                            exerciseName = "Dumbbell Lateral Raise Strict Form",
                            prescribedSets = if (extraDelts) baseSets + 2 else baseSets,
                            targetRepRange = "12–15",
                            targetRir = defaultRir,
                            restSeconds = 60,
                            volumeLandmark = if (extraDelts) "MRV Focus (+3-4 Sets Priority)" else "MAV (Lateral Deltoid)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Lateral Fly", "Side Delts", "cable_fly", "Smooth constant tension")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Pull 2",
                    splitTitle = "Mid-Back Thickness & Biceps",
                    targetMuscles = listOf("Latissimus Dorsi", "Rhomboids", "Biceps"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = rowEquip(),
                            exerciseName = rowName(),
                            prescribedSets = if (extraLats) baseSets + 1 else baseSets,
                            targetRepRange = "8–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraLats) "MRV Focus (+3-4 Sets Priority)" else "MAV (Scapular Retraction)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Cable Lat Pulldown", "Vertical Pull", "lat_pulldown", "Lat width")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = pullEquip(),
                            exerciseName = "Underhand Grip Lat Pulldown / Row",
                            prescribedSets = if (extraArms) baseSets + 1 else baseSets,
                            targetRepRange = "10–12",
                            targetRir = defaultRir,
                            restSeconds = 90,
                            volumeLandmark = if (extraArms) "MRV Focus (+3-4 Sets Priority)" else "MAV (Biceps & Lats)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Seated Row", "Mid Back", "seated_row", "Thickness")
                            )
                        )
                    )
                ),
                WorkoutSessionPlan(
                    dayName = "Legs 2",
                    splitTitle = "Posterior Chain, Glutes & Hamstrings",
                    targetMuscles = listOf("Glutes", "Hamstrings", "Quadriceps"),
                    exercises = listOf(
                        PlannedExercise(
                            equipmentId = squatEquip(),
                            exerciseName = if (isBodyweight) "Bulgarian Split Squats" else "Dumbbell Goblet Squat",
                            prescribedSets = if (extraQuads) baseSets + 1 else baseSets,
                            targetRepRange = "10–12",
                            targetRir = defaultRir,
                            restSeconds = 120,
                            volumeLandmark = if (extraQuads) "MRV Focus (+3-4 Sets Priority)" else "MAV (Unilateral Leg)",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Barbell Squat", "Quad Anchor", "squat_rack", "Heavy bilateral squat")
                            )
                        ),
                        PlannedExercise(
                            equipmentId = "treadmill",
                            exerciseName = "Incline Treadmill Walk",
                            prescribedSets = 3,
                            targetRepRange = "15 Min at 8%",
                            targetRir = "Zone 2",
                            restSeconds = 60,
                            volumeLandmark = "Posterior Chain Oxidation",
                            biomechanicalSwaps = listOf(
                                BiomechanicalSwap("Ergometer Bike", "Leg Flush", "exercise_bike", "Low impact")
                            )
                        )
                    )
                )
            )
        }
    }

    // 3. Autonomous Double Progression Evaluator
    fun evaluateDoubleProgression(
        targetTopRep: Int,
        completedSets: List<Pair<Float, Int>> // (Weight, Reps)
    ): DoubleProgressionStatus {
        if (completedSets.isEmpty()) {
            return DoubleProgressionStatus.MaintainLoad("Log sets to analyze progression.")
        }

        val allHitTopRep = completedSets.all { it.second >= targetTopRep }
        return if (allHitTopRep) {
            DoubleProgressionStatus.IncreaseLoad(
                recommendation = "Target Achieved! You hit $targetTopRep reps across all sets. Increase weight by +2.5 kg (+5 lbs) for next session.",
                suggestedDeltaKg = 2.5f
            )
        } else {
            val minReps = completedSets.minOf { it.second }
            DoubleProgressionStatus.MaintainLoad(
                recommendation = "Maintain current load. Work toward completing $targetTopRep reps on all sets before increasing weight (Lowest set: $minReps reps)."
            )
        }
    }

    // 4. Daily Science Knowledge Boosters (Peer-reviewed micro-lessons)
    val dailyScienceArticles = listOf(
        ScienceArticle(
            dayNumber = 1,
            title = "The Science of Progressive Overload & Mechanical Tension",
            citation = "Schoenfeld, B. J. (2010). The mechanisms of muscle hypertrophy. J. Strength Cond. Res.",
            takeaway = "Mechanical tension is the primary driver of muscle hypertrophy. Muscles grow when subjected to progressively higher loads over time.",
            fullText = "Muscle hypertrophy is driven primarily by mechanotransduction—the conversion of mechanical strain into molecular signaling cascades (mTORC1). While metabolic stress and muscle damage contribute, progressive mechanical tension across full ranges of motion is the non-negotiable stimulus.",
            quizQuestion = "What is the primary physiological driver of muscle hypertrophy?",
            quizOptions = listOf("Mechanical Tension", "Severe Muscle Soreness (DOMS)", "Lactic Acid Burn", "Cardio Sweating"),
            correctIndex = 0
        ),
        ScienceArticle(
            dayNumber = 2,
            title = "Rest Intervals: Why 2–3 Minutes Outperforms 1 Minute",
            citation = "Schoenfeld et al. (2016). Longer inter-set rest periods enhance muscle strength and hypertrophy.",
            takeaway = "Resting 2-3 minutes between compound sets produces superior hypertrophy compared to 1 minute by allowing higher volume load.",
            fullText = "In randomized controlled trials comparing 1-minute vs 3-minute inter-set rest intervals in trained men, the 3-minute group demonstrated significantly greater quadriceps and triceps thickness. Short rest truncated total volume load due to central fatigue.",
            quizQuestion = "Why do 2-3 minute rest intervals lead to greater muscle growth on compound lifts?",
            quizOptions = listOf("They allow higher total volume and load recovery", "They cool down the muscles completely", "They burn fewer total calories", "They elevate cortisol"),
            correctIndex = 0
        ),
        ScienceArticle(
            dayNumber = 3,
            title = "Protein Distribution: The Leucine Threshold",
            citation = "Morton et al. (2018). A systematic review and meta-analysis of protein supplementation.",
            takeaway = "Consuming 1.6 to 2.2 g/kg (0.8–1.0 g/lb) of protein daily maximized muscle protein synthesis across 49 studies.",
            fullText = "Meta-analyses demonstrate that total daily protein intake is the most impactful nutritional variable for lean mass accretion. Intakes beyond 1.62 g/kg show diminishing returns, with 2.2 g/kg providing a safe upper ceiling during hypocaloric dieting to spare lean tissue.",
            quizQuestion = "What is the evidence-based optimal protein range for natural lifters?",
            quizOptions = listOf("1.6 to 2.2 g per kg of bodyweight", "0.5 g per kg", "4.0 g per kg", "No protein needed if eating carbs"),
            correctIndex = 0
        ),
        ScienceArticle(
            dayNumber = 4,
            title = "Volume Landmarks: Understanding MEV, MAV, and MRV",
            citation = "Israetel, M., & Helms, E. (2019). Scientific Principles of Hypertrophy Training.",
            takeaway = "Weekly set volume per muscle group should cycle between Minimum Effective Volume (MEV: 6-10 sets) and Maximum Adaptive Volume (MAV: 12-20 sets).",
            fullText = "Volume follows an inverted U-curve. Training below MEV produces no adaptation; training above MRV (Maximum Recoverable Volume, >22 sets) leads to systemic overreaching and blunted growth. Aim for the MAV sweet spot of 12-18 weekly sets per muscle.",
            quizQuestion = "What happens when training volume exceeds your Maximum Recoverable Volume (MRV)?",
            quizOptions = listOf("Recovery fails, performance drops, and growth stops", "Growth doubles exponentially", "Fat is burned faster", "Tendons become indestructible"),
            correctIndex = 0
        )
    )

    // 5. Pre-compiled Science Q&A (Offline Coach Engine)
    val offlineScienceQA = listOf(
        ScienceQAPair(
            question = "How do I break a bench press plateau?",
            answer = "1. Double Progression Check: Ensure you hit the upper rep boundary (e.g. 3x8-10) before jumping weight.\n2. Deload Protocol: If stalled for 3+ weeks, cut volume by 50% and intensity by 10% for 7 days to shed systemic neural fatigue.\n3. Biomechanical Swap: Shift to Incline Dumbbell Press or Low-to-High Cable Crossover for 4 weeks to recruit fresh motor units.\n4. Micro-loading: Use 1.25 kg / 2.5 lb fractional plates rather than 5 kg jumps.",
            scientificCitations = "Helms et al. (2015), Schoenfeld et al. (2016)"
        ),
        ScienceQAPair(
            question = "Should I eat before morning workouts?",
            answer = "Consuming 25-40g of fast-digesting carbohydrates (e.g., banana, rice cakes, oatmeal) and 20g protein 30-60 minutes before morning training significantly improves mechanical output and reduces perceived exertion compared to fasted lifting.",
            scientificCitations = "Aragon & Schoenfeld (2013). Nutrient timing revisited."
        ),
        ScienceQAPair(
            question = "How to optimize shoulder hypertrophy with side delts?",
            answer = "The lateral head of the deltoid possesses predominantly type I & IIa fibers and responds exceptionally well to moderate loads with high weekly frequency (16-22 sets/week). Perform cable lateral raises with the pulley set at wrist height to align the peak resistance curve with the lateral delt leverage profile.",
            scientificCitations = "Schoenfeld (2016). Science and Development of Muscle Hypertrophy."
        ),
        ScienceQAPair(
            question = "Am I in a true caloric deficit?",
            answer = "Track your 7-day rolling morning bodyweight average after voiding bladder. Day-to-day fluctuations reflect sodium, glycogen, and water retention. If the 7-day average has not dropped by 0.25-0.5% over a 2-week span, reduce daily intake by 125 kcal or add 1,500 daily steps.",
            scientificCitations = "Hall et al. (2011). Quantification of the effect of energy imbalance."
        ),
        ScienceQAPair(
            question = "What is the optimal rest time between sets?",
            answer = "For heavy multi-joint compound movements (Squats, Bench Press, Rows), rest 2 to 3.5 minutes. For single-joint isolation exercises (Lateral Raises, Bicep Curls), 90 seconds is sufficient. Shorter rest truncates motor unit recruitment on subsequent sets.",
            scientificCitations = "de Salles et al. (2009), Schoenfeld et al. (2016)"
        )
    )
}
