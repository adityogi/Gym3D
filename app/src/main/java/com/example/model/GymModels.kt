package com.example.model

enum class EquipmentCategory(val displayName: String, val iconDescription: String) {
    ALL("All Equipment", "All gym gear"),
    FREE_WEIGHTS("Free Weights", "Barbells, benches & dumbbells"),
    CABLE_MACHINES("Cable & Pin Machines", "Selectorized stacks & cables"),
    CARDIO("Cardio Machines", "Ergometers, treadmills & rowers")
}

enum class MuscleGroup(val displayName: String, val bodyRegion: String) {
    ALL("All Muscles", "Full body"),
    CHEST("Pectorals", "Upper Body Front"),
    BACK("Latissimus & Traps", "Upper Body Back"),
    LEGS("Quads & Hamstrings", "Lower Body"),
    SHOULDERS("Deltoids", "Upper Body"),
    ARMS("Biceps & Triceps", "Arms"),
    CORE("Abs & Obliques", "Midsection"),
    FULL_BODY("Full Body Cardio", "Systemic")
}

enum class MuscleRole(val label: String) {
    PRIMARY_AGONIST("Primary Mover"),
    SYNERGIST("Assisting Synergist"),
    STABILIZER("Core Stabilizer")
}

data class MuscleActivation(
    val muscle: MuscleGroup,
    val role: MuscleRole,
    val intensityPercent: Int,
    val anatomicalDetail: String
)

data class SetupStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val pinId: String? = null,
    val recommendedValue: String
)

data class FormMistake(
    val title: String,
    val severity: String, // "High Risk", "Moderate Risk", "Efficiency Loss"
    val mistakeDetail: String,
    val correctiveAction: String,
    val injuryRisk: String
)

data class PinAdjustment(
    val pinId: String,
    val name: String,
    val currentSetting: String,
    val range: String,
    val guidance: String,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val posZ: Float = 0f
)

data class Equipment(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: EquipmentCategory,
    val difficulty: String, // "Beginner Friendly", "Intermediate", "Advanced"
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup>,
    val description: String,
    val setupSteps: List<SetupStep>,
    val mistakes: List<FormMistake>,
    val muscleActivations: List<MuscleActivation>,
    val pins: List<PinAdjustment>,
    val defaultCameraYaw: Float = 35f,
    val defaultCameraPitch: Float = 20f,
    val defaultCameraZoom: Float = 1.0f
)
