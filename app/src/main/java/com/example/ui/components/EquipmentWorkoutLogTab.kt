package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.data.local.WorkoutSetEntity
import com.example.model.Equipment
import com.example.model.EquipmentCategory
import com.example.ui.theme.BrightAmber
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.HazardRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EquipmentWorkoutLogTab(
    equipment: Equipment,
    sets: List<WorkoutSetEntity>,
    maxWeight: Float?,
    latestCompletedSet: WorkoutSetEntity?,
    isRestTimerActive: Boolean,
    restTimeRemaining: Int,
    restTotalDuration: Int,
    isRestTimerPaused: Boolean,
    onAddSet: () -> Unit,
    onUpdateSet: (WorkoutSetEntity) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onToggleComplete: (WorkoutSetEntity) -> Unit,
    onStartRestTimer: (Int) -> Unit,
    onTogglePauseRestTimer: () -> Unit,
    onAddRestSeconds: (Int) -> Unit,
    onSkipRestTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCardio = equipment.category == EquipmentCategory.CARDIO

    // Split sets into today's session vs past history
    val todayStart = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val todaySets = sets.filter { it.timestamp >= todayStart }.sortedBy { it.setNumber }
    val pastSets = sets.filter { it.timestamp < todayStart }

    var showHistory by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("equipment_workout_log_tab")
    ) {
        // 1. Stats Header (PR, Total Volume, Sets Completed)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // PR Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BrightAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Personal Record",
                            tint = BrightAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isCardio) "BEST DURATION" else "PERSONAL BEST",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                        Text(
                            text = if (isCardio) {
                                val maxDur = sets.maxOfOrNull { it.durationSeconds } ?: 0
                                if (maxDur > 0) "${maxDur / 60}m ${maxDur % 60}s" else "No Data"
                            } else {
                                if (maxWeight != null && maxWeight > 0f) "${maxWeight} kg" else "No Data"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = BrightAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Total Volume / Distance
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ElectricLime.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Volume",
                            tint = ElectricLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isCardio) "TODAY'S DIST" else "TODAY'S VOLUME",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                        Text(
                            text = if (isCardio) {
                                val totalDist = todaySets.filter { it.isCompleted }.sumOf { it.distanceKm.toDouble() }
                                String.format(Locale.US, "%.1f km", totalDist)
                            } else {
                                val totalTonnage = todaySets.filter { it.isCompleted }.sumOf { (it.weightKg * it.reps).toDouble() }
                                "${totalTonnage.toInt()} kg"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Active Rest Timer Widget
        AnimatedVisibility(visible = isRestTimerActive) {
            RestTimerWidget(
                secondsRemaining = restTimeRemaining,
                totalDuration = restTotalDuration,
                isPaused = isRestTimerPaused,
                onTogglePause = onTogglePauseRestTimer,
                onAddSeconds = { onAddRestSeconds(30) },
                onSkip = onSkipRestTimer
            )
        }

        if (isRestTimerActive) {
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Active Sets Table Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SESSION WORKOUT LOG",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isCardio) "Intervals & Distance" else "Sets, Weight & Reps",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                // Quick Rest Timer Preset Trigger
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rest: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "60s",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onStartRestTimer(60) }
                            .padding(horizontal = 4.dp)
                    )
                    Text(text = "•", color = TextMuted)
                    Text(
                        text = "90s",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onStartRestTimer(90) }
                            .padding(horizontal = 4.dp)
                    )
                    Text(text = "•", color = TextMuted)
                    Text(
                        text = "120s",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onStartRestTimer(120) }
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Column Header Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SET",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.width(36.dp),
                    fontSize = 11.sp
                )
                Text(
                    text = if (isCardio) "DURATION" else "WEIGHT (KG)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp
                )
                Text(
                    text = if (isCardio) "DIST (KM)" else "REPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp
                )
                Text(
                    text = "DONE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.width(44.dp),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(28.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Empty State
            if (todaySets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No sets logged yet for today",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (latestCompletedSet != null) {
                                if (isCardio) "Previous: ${latestCompletedSet.durationSeconds / 60}m • ${latestCompletedSet.distanceKm}km"
                                else "Previous: ${latestCompletedSet.weightKg} kg × ${latestCompletedSet.reps} reps"
                            } else {
                                "Tap '+ Add Set' below to start logging"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                todaySets.forEach { setItem ->
                    SetRowItem(
                        set = setItem,
                        isCardio = isCardio,
                        onUpdate = onUpdateSet,
                        onDelete = { onDeleteSet(setItem.id) },
                        onToggleComplete = { onToggleComplete(setItem) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: + Add Set & Auto-Fill from Previous
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // + Add Set Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricLime)
                        .clickable(onClick = onAddSet)
                        .padding(vertical = 12.dp)
                        .testTag("add_set_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Set",
                            tint = Color(0xFF0F1115),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCardio) "Add Interval" else "Add Set",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF0F1115),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Past Workout History (Expandable)
        if (pastSets.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHistory = !showHistory },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PREVIOUS SESSIONS (${pastSets.groupBy { it.timestamp / 86400000 }.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = if (showHistory) "Hide" else "View All",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showHistory) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val groupedByDay = pastSets.groupBy {
                        SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(it.timestamp))
                    }

                    groupedByDay.forEach { (dateStr, daySets) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberEmerald,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            daySets.forEach { s ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Set ${s.setNumber}: ${s.weightKg} kg × ${s.reps} reps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    if (s.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Done",
                                            tint = CyberEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RestTimerWidget(
    secondsRemaining: Int,
    totalDuration: Int,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onAddSeconds: () -> Unit,
    onSkip: () -> Unit
) {
    val progress = if (totalDuration > 0) secondsRemaining.toFloat() / totalDuration.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timerProgress")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.5.dp, NeonCyan, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    color = NeonCyan,
                    trackColor = DarkBorder,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "$secondsRemaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "REST INTERVAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    text = if (isPaused) "Paused" else "Resting...",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
            }
        }

        // Timer action buttons: Pause, +30s, Skip
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Pause / Resume
            IconButton(
                onClick = onTogglePause,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = "Toggle Rest",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // +30s
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .clickable(onClick = onAddSeconds)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "+30s",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Skip
            IconButton(
                onClick = onSkip,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Skip Rest",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SetRowItem(
    set: WorkoutSetEntity,
    isCardio: Boolean,
    onUpdate: (WorkoutSetEntity) -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (set.isCompleted) Color(0xFF14241B) else DarkSurfaceVariant)
            .border(
                width = 1.dp,
                color = if (set.isCompleted) CyberEmerald.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set # Badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (set.isCompleted) CyberEmerald else DarkSurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${set.setNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = if (set.isCompleted) Color(0xFF0F1115) else TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // First Metric: Weight (kg) or Duration (min)
        if (isCardio) {
            StepperField(
                valueText = "${set.durationSeconds / 60}m",
                onDecrement = {
                    val next = (set.durationSeconds - 60).coerceAtLeast(60)
                    onUpdate(set.copy(durationSeconds = next))
                },
                onIncrement = {
                    val next = set.durationSeconds + 60
                    onUpdate(set.copy(durationSeconds = next))
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            StepperField(
                valueText = "${set.weightKg} kg",
                onDecrement = {
                    val next = (set.weightKg - 2.5f).coerceAtLeast(0f)
                    onUpdate(set.copy(weightKg = next))
                },
                onIncrement = {
                    val next = set.weightKg + 2.5f
                    onUpdate(set.copy(weightKg = next))
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Second Metric: Reps or Distance
        if (isCardio) {
            StepperField(
                valueText = String.format(Locale.US, "%.1f km", set.distanceKm),
                onDecrement = {
                    val next = (set.distanceKm - 0.5f).coerceAtLeast(0f)
                    onUpdate(set.copy(distanceKm = next))
                },
                onIncrement = {
                    val next = set.distanceKm + 0.5f
                    onUpdate(set.copy(distanceKm = next))
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            StepperField(
                valueText = "${set.reps} reps",
                onDecrement = {
                    val next = (set.reps - 1).coerceAtLeast(1)
                    onUpdate(set.copy(reps = next))
                },
                onIncrement = {
                    val next = set.reps + 1
                    onUpdate(set.copy(reps = next))
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Complete Checkbox Button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (set.isCompleted) CyberEmerald else DarkSurfaceHigh)
                .clickable(onClick = onToggleComplete)
                .testTag("complete_set_${set.setNumber}"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Complete Set",
                tint = if (set.isCompleted) Color(0xFF0F1115) else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Delete Set Button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Set",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun StepperField(
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceHigh)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // -
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .clickable(onClick = onDecrement),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "−", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Text(
            text = valueText,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        // +
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .clickable(onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
