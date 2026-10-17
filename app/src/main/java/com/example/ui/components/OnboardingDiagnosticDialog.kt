package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ScienceEngine
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.BrightAmber
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingDiagnosticDialog(
    initialProfile: UserProfileEntity,
    onSaveProfile: (UserProfileEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // User Form State
    var gender by remember { mutableStateOf(initialProfile.gender) }
    var age by remember { mutableStateOf(initialProfile.age.toFloat()) }
    var heightCm by remember { mutableStateOf(initialProfile.heightCm) }
    var weightKg by remember { mutableStateOf(initialProfile.weightKg) }
    var bodyFatPct by remember { mutableStateOf(initialProfile.bodyFatPct) }
    var goal by remember { mutableStateOf(initialProfile.goal) }
    var experience by remember { mutableStateOf(initialProfile.experienceLevel) }
    var equipmentAccess by remember { mutableStateOf(initialProfile.equipmentAccess) }
    var daysPerWeek by remember { mutableStateOf(initialProfile.workoutDaysPerWeek) }
    var prioritized1 by remember { mutableStateOf(initialProfile.prioritizedMuscle1) }
    var prioritized2 by remember { mutableStateOf(initialProfile.prioritizedMuscle2) }

    // Live Telemetry Calculations
    val bmi = remember(heightCm, weightKg) {
        ScienceEngine.calculateBmi(heightCm, weightKg)
    }
    val bmr = remember(gender, age, heightCm, weightKg) {
        ScienceEngine.calculateBmr(gender == "Male", age.toInt(), heightCm, weightKg)
    }
    val leanMassKg = remember(weightKg, bodyFatPct) {
        weightKg * (1f - (bodyFatPct / 100f))
    }
    val fatMassKg = remember(weightKg, bodyFatPct) {
        weightKg * (bodyFatPct / 100f)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
                .testTag("onboarding_diagnostic_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Progress Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DIAGNOSTIC ONBOARDING",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Step $step of 5: " + when (step) {
                                1 -> "Biometric Baseline"
                                2 -> "Body Fat & Lean Mass"
                                3 -> "Primary Fitness Goal"
                                4 -> "Schedule & Equipment"
                                else -> "Muscle Prioritizer"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("onboarding_step_indicator")
                    ) {
                        Text(
                            text = "${(step * 20)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stepper Visual Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..5) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        i <= step -> ElectricLime
                                        else -> DarkSurfaceHigh
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Step Content Animated
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "step_content"
                ) { currentStep ->
                    when (currentStep) {
                        1 -> Step1Biometrics(
                            gender = gender,
                            onGenderChange = { gender = it },
                            age = age,
                            onAgeChange = { age = it },
                            heightCm = heightCm,
                            onHeightChange = { heightCm = it },
                            weightKg = weightKg,
                            onWeightChange = { weightKg = it },
                            bmi = bmi,
                            bmr = bmr
                        )

                        2 -> Step2BodyFat(
                            bodyFatPct = bodyFatPct,
                            onSelectBf = { bodyFatPct = it },
                            weightKg = weightKg,
                            leanMassKg = leanMassKg,
                            fatMassKg = fatMassKg
                        )

                        3 -> Step3FitnessGoal(
                            goal = goal,
                            onSelectGoal = { goal = it },
                            weightKg = weightKg,
                            heightCm = heightCm,
                            age = age.toInt(),
                            isMale = gender == "Male",
                            daysPerWeek = daysPerWeek
                        )

                        4 -> Step4ScheduleAndEquipment(
                            experience = experience,
                            onSelectExperience = { experience = it },
                            equipmentAccess = equipmentAccess,
                            onSelectEquipment = { equipmentAccess = it },
                            daysPerWeek = daysPerWeek,
                            onSelectDays = { daysPerWeek = it }
                        )

                        5 -> Step5MusclePrioritizer(
                            prioritized1 = prioritized1,
                            prioritized2 = prioritized2,
                            onToggleMuscle = { m ->
                                val isChosen = prioritized1 == m || prioritized2 == m
                                if (isChosen) {
                                    if (prioritized1 == m) prioritized1 = "" else prioritized2 = ""
                                } else {
                                    if (prioritized1.isEmpty()) prioritized1 = m else prioritized2 = m
                                }
                            },
                            daysPerWeek = daysPerWeek,
                            experience = experience,
                            equipmentAccess = equipmentAccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation Controls (Back & Next / Generate Plan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier.testTag("onboarding_btn_back")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(60.dp))
                    }

                    Button(
                        onClick = {
                            if (step < 5) {
                                step++
                            } else {
                                // Calculate TDEE and macros using Mifflin-St Jeor
                                val (cals, macros, _) = ScienceEngine.calculateNutritionTargets(
                                    isMale = gender == "Male",
                                    age = age.toInt(),
                                    heightCm = heightCm,
                                    weightKg = weightKg,
                                    daysPerWeek = daysPerWeek,
                                    goal = goal
                                )

                                val updated = initialProfile.copy(
                                    gender = gender,
                                    age = age.toInt(),
                                    heightCm = heightCm,
                                    weightKg = weightKg,
                                    bodyFatPct = bodyFatPct,
                                    goal = goal,
                                    experienceLevel = experience,
                                    equipmentAccess = equipmentAccess,
                                    workoutDaysPerWeek = daysPerWeek,
                                    prioritizedMuscle1 = prioritized1.ifEmpty { "Upper Chest" },
                                    prioritizedMuscle2 = prioritized2.ifEmpty { "Side Delts" },
                                    targetCalories = cals,
                                    targetProteinG = macros.first,
                                    targetCarbsG = macros.second,
                                    targetFatG = macros.third,
                                    isOnboardingCompleted = true
                                )
                                onSaveProfile(updated)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime),
                        modifier = Modifier.testTag(if (step < 5) "onboarding_btn_next" else "onboarding_finish_btn")
                    ) {
                        Text(
                            text = if (step < 5) "Next Step" else "Generate Science Plan",
                            color = Color(0xFF0F1115),
                            fontWeight = FontWeight.Bold
                        )
                        if (step < 5) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color(0xFF0F1115), modifier = Modifier.size(16.dp))
                        } else {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Generate", tint = Color(0xFF0F1115), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1: Biometric Baseline
// -------------------------------------------------------------
@Composable
private fun Step1Biometrics(
    gender: String,
    onGenderChange: (String) -> Unit,
    age: Float,
    onAgeChange: (Float) -> Unit,
    heightCm: Float,
    onHeightChange: (Float) -> Unit,
    weightKg: Float,
    onWeightChange: (Float) -> Unit,
    bmi: Float,
    bmr: Float
) {
    Column {
        Text("Biological Sex", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectableChip(
                label = "Male",
                isSelected = gender == "Male",
                tag = "onboarding_gender_male"
            ) { onGenderChange("Male") }

            SelectableChip(
                label = "Female",
                isSelected = gender == "Female",
                tag = "onboarding_gender_female"
            ) { onGenderChange("Female") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Age with Steppers
        ValueSliderWithSteppers(
            title = "Age",
            displayValue = "${age.toInt()} years",
            value = age,
            range = 16f..75f,
            stepDelta = 1f,
            onValueChange = onAgeChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Height with dual cm / ft'in" display
        val feet = (heightCm / 30.48f).toInt()
        val inches = kotlin.math.round((heightCm / 2.54f) % 12f).toInt()
        ValueSliderWithSteppers(
            title = "Height",
            displayValue = "${heightCm.toInt()} cm ($feet' $inches\")",
            value = heightCm,
            range = 140f..215f,
            stepDelta = 1f,
            onValueChange = onHeightChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Weight with dual kg / lbs display
        val lbs = weightKg * 2.20462f
        ValueSliderWithSteppers(
            title = "Bodyweight",
            displayValue = String.format(Locale.US, "%.1f kg (%.1f lbs)", weightKg, lbs),
            value = weightKg,
            range = 40f..160f,
            stepDelta = 0.5f,
            onValueChange = onWeightChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Diagnostic Telemetry Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Calculated BMI", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format(Locale.US, "%.1f", bmi),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val classification = when {
                            bmi < 18.5f -> "Underweight"
                            bmi < 25f -> "Normal"
                            bmi < 30f -> "Athletic / Heavy"
                            else -> "Overweight"
                        }
                        Text(
                            text = "($classification)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (bmi in 18.5f..26f) CyberEmerald else BrightAmber
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Resting BMR (Mifflin-St Jeor)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "${bmr.toInt()} kcal/day",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2: Body Fat Estimation & Lean Mass Profile
// -------------------------------------------------------------
@Composable
private fun Step2BodyFat(
    bodyFatPct: Float,
    onSelectBf: (Float) -> Unit,
    weightKg: Float,
    leanMassKg: Float,
    fatMassKg: Float
) {
    Column {
        Text("Estimated Body Fat Percentage", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(
            text = "Select the range that mirrors your abdominal definition and vascularity for precise lean mass targeting.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        val bfRanges = listOf(
            Triple("8% – 10%", "Shredded", "Striations visible, deep abdominal cuts & veins"),
            Triple("11% – 14%", "Athletic", "Clear 6-pack abs outline, vascularity on arms"),
            Triple("15% – 19%", "Moderate", "Top 2-4 abs visible, lean athletic foundation"),
            Triple("20% – 24%", "Soft", "Flat stomach, minimal definition visible"),
            Triple("25% – 29%", "Higher", "Soft midsection, waist overhang beginning"),
            Triple("30%+", "High", "Significant adiposity, minimal muscular outline")
        )

        bfRanges.forEachIndexed { idx, (rangeText, tag, desc) ->
            val midVal = when (idx) {
                0 -> 9f
                1 -> 12.5f
                2 -> 17f
                3 -> 22f
                4 -> 27f
                else -> 32f
            }
            val isSelected = kotlin.math.abs(bodyFatPct - midVal) < 2.5f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) DarkSurfaceHigh else DarkSurfaceVariant)
                    .border(1.dp, if (isSelected) ElectricLime else DarkBorder, RoundedCornerShape(12.dp))
                    .clickable { onSelectBf(midVal) }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(rangeText, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) ElectricLime.copy(alpha = 0.2f) else DarkBorder)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(tag, style = MaterialTheme.typography.labelSmall, color = if (isSelected) ElectricLime else TextSecondary, fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = ElectricLime, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lean Mass vs Fat Mass Telemetry Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberEmerald.copy(alpha = 0.1f))
                .border(1.dp, CyberEmerald.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Estimated Lean Body Mass", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = String.format(Locale.US, "%.1f kg (%.1f lbs)", leanMassKg, leanMassKg * 2.20462f),
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Estimated Fat Mass", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = String.format(Locale.US, "%.1f kg", fatMassKg),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 3: Primary Fitness Goal
// -------------------------------------------------------------
@Composable
private fun Step3FitnessGoal(
    goal: String,
    onSelectGoal: (String) -> Unit,
    weightKg: Float,
    heightCm: Float,
    age: Int,
    isMale: Boolean,
    daysPerWeek: Int
) {
    val (targetCals, macros, maintenanceTdee) = remember(goal, weightKg, heightCm, age, isMale, daysPerWeek) {
        ScienceEngine.calculateNutritionTargets(
            isMale = isMale,
            age = age,
            heightCm = heightCm,
            weightKg = weightKg,
            daysPerWeek = daysPerWeek,
            goal = goal
        )
    }

    Column {
        Text("Select Primary Fitness Objective", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(
            text = "Caloric and macronutrient distribution adapt dynamically based on your chosen metabolic path.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        val goals = listOf(
            Triple(
                "Hypertrophy (Lean Bulk)",
                "+250 kcal caloric surplus to maximize rate of muscle protein synthesis while keeping adipose accretion minimal.",
                CyberEmerald
            ),
            Triple(
                "Fat Loss (Cut)",
                "-450 kcal deficit targeting 0.5%–1.0% bodyweight loss/week with high protein (2.2g/kg) to preserve lean mass.",
                ElectricLime
            ),
            Triple(
                "Body Recomposition",
                "-75 kcal slight deficit to simultaneously build muscle and mobilize fatty acids. Ideal for beginners or >15% body fat.",
                BrightAmber
            )
        )

        goals.forEach { (gTitle, gDesc, gColor) ->
            val isSelected = goal == gTitle
            val tag = when (gTitle) {
                "Hypertrophy (Lean Bulk)" -> "onboarding_goal_hypertrophy"
                "Fat Loss (Cut)" -> "onboarding_goal_fatloss"
                else -> "onboarding_goal_recomp"
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) DarkSurfaceHigh else DarkSurfaceVariant)
                    .border(1.5.dp, if (isSelected) gColor else DarkBorder, RoundedCornerShape(14.dp))
                    .clickable { onSelectGoal(gTitle) }
                    .padding(14.dp)
                    .testTag(tag)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(gTitle, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        if (isSelected) Icon(Icons.Default.Check, contentDescription = "Selected", tint = gColor)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(gDesc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic Targets Preview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Target Calories", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        "$targetCals kcal / day",
                        style = MaterialTheme.typography.titleSmall,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Protein Target (2.2g/kg)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        "${macros.first}g / day",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Maintenance Baseline", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        "$maintenanceTdee kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 4: Schedule, Experience & Equipment
// -------------------------------------------------------------
@Composable
private fun Step4ScheduleAndEquipment(
    experience: String,
    onSelectExperience: (String) -> Unit,
    equipmentAccess: String,
    onSelectEquipment: (String) -> Unit,
    daysPerWeek: Int,
    onSelectDays: (Int) -> Unit
) {
    Column {
        // Experience Level
        Text("Training Experience Level", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        val expList = listOf(
            Pair("Beginner (<1 yr)", "2–3 RIR • Linear progression focus"),
            Pair("Intermediate (1-3 yrs)", "1–2 RIR • Double progression model"),
            Pair("Advanced (3+ yrs)", "0–1 RIR • High volume & drop sets")
        )
        expList.forEach { (exp, desc) ->
            val isSelected = experience == exp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) DarkSurfaceHigh else DarkSurfaceVariant)
                    .border(1.dp, if (isSelected) CyberEmerald else DarkBorder, RoundedCornerShape(10.dp))
                    .clickable { onSelectExperience(exp) }
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(exp, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                    }
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = "Selected", tint = CyberEmerald, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Equipment Access
        Text("Equipment & Training Environment", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        val equipList = listOf(
            Triple("Commercial Gym", "Barbells, Squat Racks, Cables, Machines, Ergometers", "onboarding_equipment_gym"),
            Triple("Home Dumbbells", "Adjustable Dumbbells, Bench & Bodyweight movements", "onboarding_equipment_dumbbells"),
            Triple("Bodyweight / Calisthenics", "Doorway pull-up bar, floor, dips, and gravity load", "onboarding_equipment_bodyweight")
        )
        equipList.forEach { (equip, details, testTag) ->
            val isSelected = equipmentAccess == equip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) DarkSurfaceHigh else DarkSurfaceVariant)
                    .border(1.dp, if (isSelected) NeonCyan else DarkBorder, RoundedCornerShape(10.dp))
                    .clickable { onSelectEquipment(equip) }
                    .padding(10.dp)
                    .testTag(testTag)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(equip, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(details, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                    }
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = "Selected", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Training Frequency
        Text("Weekly Training Frequency", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = ScienceEngine.getSplitDescription(daysPerWeek),
            style = MaterialTheme.typography.bodySmall,
            color = ElectricLime
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(3, 4, 5, 6).forEach { d ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (daysPerWeek == d) ElectricLime else DarkSurfaceVariant)
                        .border(1.dp, if (daysPerWeek == d) ElectricLime else DarkBorder, RoundedCornerShape(10.dp))
                        .clickable { onSelectDays(d) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$d Days",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (daysPerWeek == d) Color(0xFF0F1115) else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 5: Muscle Prioritizer & Workout Generation Preview
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step5MusclePrioritizer(
    prioritized1: String,
    prioritized2: String,
    onToggleMuscle: (String) -> Unit,
    daysPerWeek: Int,
    experience: String,
    equipmentAccess: String
) {
    Column {
        Text("Muscle Group Prioritizer", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(
            text = "Select 1 to 2 lagging muscle groups. The workout generation engine automatically allocates +3–4 extra weekly sets to push them toward Maximum Adaptive Volume (MAV).",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        val muscles = listOf("Upper Chest", "Side Delts", "Lats", "Quads & Glutes", "Arms")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            muscles.forEach { m ->
                val isChosen = prioritized1 == m || prioritized2 == m
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isChosen) ElectricLime else DarkSurfaceVariant)
                        .border(1.dp, if (isChosen) ElectricLime else DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { onToggleMuscle(m) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("onboarding_muscle_chip_${m.replace(" ", "_")}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = m,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isChosen) Color(0xFF0F1115) else TextPrimary,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium
                        )
                        if (isChosen) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Check, contentDescription = "Active", tint = Color(0xFF0F1115), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Workout Generation Engine Readout Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Engine", tint = CyberEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Workout Engine Calibration",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Program: ${ScienceEngine.getSplitDescription(daysPerWeek)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                Text(
                    text = "• Environment: $equipmentAccess",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "• Experience: $experience",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                val focusStr = listOf(prioritized1, prioritized2).filter { it.isNotEmpty() }.joinToString(" + ")
                Text(
                    text = if (focusStr.isNotEmpty()) "• Prioritized Volume: $focusStr (+3-4 sets allocated)" else "• Prioritized Volume: Balanced systemic volume",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricLime,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER: Reusable Slider with - / + Stepper Buttons
// -------------------------------------------------------------
@Composable
private fun ValueSliderWithSteppers(
    title: String,
    displayValue: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    stepDelta: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(displayValue, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onValueChange((value - stepDelta).coerceIn(range)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = ElectricLime, modifier = Modifier.size(18.dp))
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(thumbColor = ElectricLime, activeTrackColor = ElectricLime),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { onValueChange((value + stepDelta).coerceIn(range)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = ElectricLime, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    tag: String = "",
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) CyberEmerald else DarkSurfaceVariant)
            .border(1.dp, if (isSelected) CyberEmerald else DarkBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFF0F1115) else TextPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
