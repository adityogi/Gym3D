package com.example.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingDiagnosticDialog(
    initialProfile: UserProfileEntity,
    onSaveProfile: (UserProfileEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    var gender by remember { mutableStateOf(initialProfile.gender) }
    var age by remember { mutableStateOf(initialProfile.age.toFloat()) }
    var heightCm by remember { mutableStateOf(initialProfile.heightCm) }
    var weightKg by remember { mutableStateOf(initialProfile.weightKg) }
    var bodyFatPct by remember { mutableStateOf(initialProfile.bodyFatPct) }
    var goal by remember { mutableStateOf(initialProfile.goal) }
    var experience by remember { mutableStateOf(initialProfile.experienceLevel) }
    var daysPerWeek by remember { mutableStateOf(initialProfile.workoutDaysPerWeek) }
    var prioritized1 by remember { mutableStateOf(initialProfile.prioritizedMuscle1) }
    var prioritized2 by remember { mutableStateOf(initialProfile.prioritizedMuscle2) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("onboarding_diagnostic_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Progress indicator
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
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Step $step of 5: " + when (step) {
                                1 -> "Biometric Baseline"
                                2 -> "Body Fat Estimation"
                                3 -> "Primary Fitness Goal"
                                4 -> "Schedule & Experience"
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
                    ) {
                        Text(
                            text = "${(step * 20)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Step Contents
                when (step) {
                    1 -> {
                        // Biological Sex
                        Text("Biological Sex", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SelectableChip("Male", isSelected = gender == "Male") { gender = "Male" }
                            SelectableChip("Female", isSelected = gender == "Female") { gender = "Female" }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Age
                        Text("Age: ${age.toInt()} years", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Slider(
                            value = age,
                            onValueChange = { age = it },
                            valueRange = 16f..75f,
                            colors = SliderDefaults.colors(thumbColor = ElectricLime, activeTrackColor = ElectricLime)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Height
                        Text("Height: ${heightCm.toInt()} cm", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Slider(
                            value = heightCm,
                            onValueChange = { heightCm = it },
                            valueRange = 140f..215f,
                            colors = SliderDefaults.colors(thumbColor = ElectricLime, activeTrackColor = ElectricLime)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Weight
                        Text("Weight: ${String.format(java.util.Locale.US, "%.1f", weightKg)} kg", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Slider(
                            value = weightKg,
                            onValueChange = { weightKg = it },
                            valueRange = 40f..160f,
                            colors = SliderDefaults.colors(thumbColor = ElectricLime, activeTrackColor = ElectricLime)
                        )
                    }

                    2 -> {
                        Text(
                            text = "Estimated Body Fat Percentage",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Select the range that most closely mirrors your current abdominal definition and vascularity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val bfRanges = listOf(
                            Pair("8% – 10%", "Shredded / Striations visible"),
                            Pair("11% – 14%", "Athletic / Clear 6-pack abs"),
                            Pair("15% – 19%", "Moderate / Top 2-4 abs visible"),
                            Pair("20% – 24%", "Soft / Flat stomach, no abs"),
                            Pair("25% – 29%", "Higher / Waist overhang"),
                            Pair("30%+", "High / Significant adiposity")
                        )

                        bfRanges.forEachIndexed { idx, pair ->
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
                                    .clickable { bodyFatPct = midVal }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(pair.first, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text(pair.second, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = ElectricLime)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    3 -> {
                        Text("Select Primary Focus Goal", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Caloric and macronutrient equations adapt strictly based on this physiological objective.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val goals = listOf(
                            Triple("Hypertrophy (Lean Bulk)", "+250 kcal caloric surplus to maximize rate of muscle protein synthesis with minimal fat gain.", CyberEmerald),
                            Triple("Fat Loss (Cut)", "-450 kcal caloric deficit targeting 0.5%–1.0% bodyweight loss/week while sparing lean mass.", ElectricLime),
                            Triple("Body Recomposition", "Slight deficit (-75 kcal) with high protein (2.2g/kg) to simultaneously build muscle and burn fat.", BrightAmber)
                        )

                        goals.forEach { (gTitle, gDesc, gColor) ->
                            val isSelected = goal == gTitle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) DarkSurfaceHigh else DarkSurfaceVariant)
                                    .border(1.5.dp, if (isSelected) gColor else DarkBorder, RoundedCornerShape(14.dp))
                                    .clickable { goal = gTitle }
                                    .padding(14.dp)
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
                    }

                    4 -> {
                        Text("Lifting Experience", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        val expList = listOf("Beginner (<1 yr)", "Intermediate (1-3 yrs)", "Advanced (3+ yrs)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            expList.forEach { exp ->
                                SelectableChip(exp, isSelected = experience == exp) { experience = exp }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Weekly Training Frequency: $daysPerWeek Days/Week", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = when (daysPerWeek) {
                                3 -> "Algorithm: 3-Day Full Body Split (A / B / C)"
                                4 -> "Algorithm: 4-Day Upper / Lower Periodized Split"
                                5 -> "Algorithm: 5-Day Push / Pull / Legs / Upper / Lower"
                                else -> "Algorithm: 6-Day Push / Pull / Legs (PPL x 2)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricLime
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(3, 4, 5, 6).forEach { d ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (daysPerWeek == d) ElectricLime else DarkSurfaceVariant)
                                        .clickable { daysPerWeek = d }
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

                    5 -> {
                        Text("Muscle Group Prioritizer", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select 1 to 2 lagging muscle groups. The algorithm injects +3-4 extra weekly sets to prioritize them.",
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
                                    .clickable {
                                        if (isChosen) {
                                            if (prioritized1 == m) prioritized1 = "" else prioritized2 = ""
                                        } else {
                                            if (prioritized1.isEmpty()) prioritized1 = m else prioritized2 = m
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = m,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isChosen) Color(0xFF0F1115) else TextPrimary,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Navigation Buttons (Back & Next / Finish)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        Button(
                            onClick = { step-- },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Text("Back", color = TextPrimary)
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
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime)
                    ) {
                        Text(
                            text = if (step < 5) "Next Step" else "Generate Science Plan",
                            color = Color(0xFF0F1115),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) CyberEmerald else DarkSurfaceVariant)
            .border(1.dp, if (isSelected) CyberEmerald else DarkBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFF0F1115) else TextPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
