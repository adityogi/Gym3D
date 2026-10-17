package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.BiomechanicalSwap
import com.example.data.PlannedExercise
import com.example.data.ScienceEngine
import com.example.data.WorkoutSessionPlan
import com.example.data.local.WorkoutSetEntity
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

@Composable
fun WorkoutTabScreen(
    generatedSplit: List<WorkoutSessionPlan>,
    allWorkoutSets: List<WorkoutSetEntity>,
    onOpen3DEquipment: (String) -> Unit,
    onOpenFullCatalog: () -> Unit,
    onAddSet: (String) -> Unit,
    onUpdateSet: (WorkoutSetEntity) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onToggleComplete: (WorkoutSetEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSessionIndex by remember { mutableStateOf(0) }
    val currentSession = generatedSplit.getOrNull(selectedSessionIndex) ?: generatedSplit.first()

    var swapTargetExercise by remember { mutableStateOf<PlannedExercise?>(null) }
    var activeSwaps by remember { mutableStateOf(mapOf<String, BiomechanicalSwap>()) }
    var isWorkoutShortened by remember { mutableStateOf(false) }
    var showDeloadDialog by remember { mutableStateOf(false) }

    val displayedExercises = remember(currentSession, isWorkoutShortened) {
        if (isWorkoutShortened) {
            currentSession.exercises.take(3)
        } else {
            currentSession.exercises
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("workout_tab_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with Catalog Switcher & Workout Shortener
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PERIODIZED TRAINING ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentSession.splitTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick Browse All 3D Equipment button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenFullCatalog)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Full Catalog",
                            tint = ElectricLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "All 3D Models",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Day Switcher Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                generatedSplit.forEachIndexed { idx, plan ->
                    val isSelected = selectedSessionIndex == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ElectricLime else DarkSurface)
                            .border(1.dp, if (isSelected) ElectricLime else DarkBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedSessionIndex = idx }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = plan.dayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color(0xFF0F1115) else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Workout Shortener Tool & Plateau / Deload Controls
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WORKOUT SHORTENER TOOL",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isWorkoutShortened) BrightAmber else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isWorkoutShortened) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BrightAmber.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "35 MIN CONDENSED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrightAmber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isWorkoutShortened) "Condensing session to 3 core compound lifts" else "Full periodized routine (~60 min)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isWorkoutShortened) BrightAmber else DarkSurfaceVariant)
                            .clickable { isWorkoutShortened = !isWorkoutShortened }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("workout_shortener_toggle")
                    ) {
                        Text(
                            text = if (isWorkoutShortened) "Disable" else "Shorten Session",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isWorkoutShortened) Color(0xFF0F1115) else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stalled for 3 consecutive sessions?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { showDeloadDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Plateau Deload Protocol",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Autonomous Double Progression Protocol Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF152219))
                    .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Double Progression",
                        tint = CyberEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DOUBLE PROGRESSION ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "When you complete the top rep boundary across all prescribed sets at RIR ≥ 1, the app instructs: 'Increase weight by +2.5 kg / 5 lbs next session.'",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 5. Session Exercises List
        items(displayedExercises) { baseExercise ->
            val activeSwap = activeSwaps[baseExercise.equipmentId]
            val effectiveEquipId = activeSwap?.equipmentId ?: baseExercise.equipmentId
            val effectiveName = activeSwap?.name ?: baseExercise.exerciseName

            val exerciseSets = allWorkoutSets.filter { it.equipmentId == effectiveEquipId }

            ExerciseCard(
                exercise = baseExercise,
                effectiveName = effectiveName,
                effectiveEquipId = effectiveEquipId,
                sets = exerciseSets,
                onOpen3D = { onOpen3DEquipment(effectiveEquipId) },
                onSwap = { swapTargetExercise = baseExercise },
                onAddSet = { onAddSet(effectiveEquipId) },
                onUpdateSet = onUpdateSet,
                onDeleteSet = onDeleteSet,
                onToggleComplete = onToggleComplete
            )
        }
    }

    // Biomechanical Exercise Swap Dialog
    if (swapTargetExercise != null) {
        val exercise = swapTargetExercise!!
        AlertDialog(
            onDismissRequest = { swapTargetExercise = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Biomechanical Exercise Swap",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Swap '${exercise.exerciseName}' with verified biomechanical alternatives sharing matching resistance profiles and muscular targets:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    exercise.biomechanicalSwaps.forEach { swap ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    activeSwaps = activeSwaps + (exercise.equipmentId to swap)
                                    swapTargetExercise = null
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(swap.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text(swap.targetDivision, style = MaterialTheme.typography.labelSmall, color = ElectricLime)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(swap.rationale, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeSwaps = activeSwaps - exercise.equipmentId
                        swapTargetExercise = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Text("Reset to Original", color = TextSecondary)
                }
            }
        )
    }

    // Plateau Deload Protocol Dialog
    if (showDeloadDialog) {
        AlertDialog(
            onDismissRequest = { showDeloadDialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Deload",
                        tint = BrightAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Plateau Deload Protocol",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "When compound progression stalls for 3 consecutive weeks, accumulated systemic fatigue attenuates muscle protein synthesis.\n\n" +
                                "Evidence-Based 1-Week Protocol:\n" +
                                "• Volume: -50% reduction (perform 2 sets instead of 3-4)\n" +
                                "• Load: -10% lighter (maintain RIR 3-4)\n" +
                                "• Goal: Connective tissue recovery and neuromuscular resensitization.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDeloadDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald)
                ) {
                    Text("Apply Deload Protocol", color = Color(0xFF0F1115), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: PlannedExercise,
    effectiveName: String,
    effectiveEquipId: String,
    sets: List<WorkoutSetEntity>,
    onOpen3D: () -> Unit,
    onSwap: () -> Unit,
    onAddSet: () -> Unit,
    onUpdateSet: (WorkoutSetEntity) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onToggleComplete: (WorkoutSetEntity) -> Unit
) {
    val upperRepLimit = remember(exercise.targetRepRange) {
        exercise.targetRepRange.split("-").lastOrNull()?.trim()?.toIntOrNull() ?: 12
    }
    val isProgressionAchieved = sets.isNotEmpty() && sets.all { it.isCompleted && it.reps >= upperRepLimit }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, if (isProgressionAchieved) CyberEmerald else DarkBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Exercise Title & Biomechanical Swap Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = effectiveName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${exercise.prescribedSets} Sets • ${exercise.targetRepRange} Reps • ${exercise.targetRir} • Rest: ${exercise.restSeconds}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Swap Button
                IconButton(onClick = onSwap) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Exercise",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 3D View Button
                IconButton(onClick = onOpen3D) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "3D View",
                        tint = ElectricLime,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Progressive Overload Alert Banner
        if (isProgressionAchieved) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberEmerald.copy(alpha = 0.15f))
                    .border(1.dp, CyberEmerald, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Progress",
                        tint = CyberEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROGRESSION UNLOCKED: Hit $upperRepLimit reps on all sets! Increase weight by +2.5 kg / 5 lbs next session.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Set Logging Table
        sets.forEach { setItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (setItem.isCompleted) Color(0xFF14241B) else DarkSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Set ${setItem.setNumber}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text("${setItem.weightKg} kg × ${setItem.reps} reps", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (setItem.isCompleted) CyberEmerald else DarkSurfaceHigh)
                            .clickable { onToggleComplete(setItem) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete",
                            tint = if (setItem.isCompleted) Color(0xFF0F1115) else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { onDeleteSet(setItem.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // + Add Set Quick Action
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceVariant)
                .clickable(onClick = onAddSet)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = ElectricLime, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Set",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
