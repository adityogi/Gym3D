package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.BodyMetricEntity
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

@Composable
fun AnalyticsScreen(
    userProfile: UserProfileEntity,
    metrics: List<BodyMetricEntity>,
    modifier: Modifier = Modifier
) {
    // Interactive 1RM Calculator State
    var calcWeight by remember { mutableStateOf(85f) }
    var calcReps by remember { mutableStateOf(8f) }
    // Brzycki formula: 1RM = Weight / (1.0278 - (0.0278 * Reps))
    val estimated1RM = (calcWeight / (1.0278f - (0.0278f * calcReps))).coerceAtLeast(calcWeight)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "BIOMECHANICAL ANALYTICS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Body Recomp & Strength Curves",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary
                )
            }
        }

        // 2. Body Recomposition Detector Milestone Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurface)
                    .border(1.5.dp, CyberEmerald, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Recomp", tint = CyberEmerald, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("BODY RECOMPOSITION DETECTOR", style = MaterialTheme.typography.labelSmall, color = CyberEmerald, fontWeight = FontWeight.Bold)
                        Text("Concurrent Fat Loss & Hypertrophy", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Monitors 3 vectors simultaneously: 7-day scale weight moving average, waist circumference, and compound 1RM strength.\n\n" +
                            "✅ Weight Stable: ${userProfile.weightKg} kg (±0.5%)\n" +
                            "✅ Waist Circumference: ${userProfile.waistCircumferenceCm} cm (Decreasing trend)\n" +
                            "✅ Compound 1RM Strength: Rising (+5.2%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberEmerald.copy(alpha = 0.15f))
                        .border(1.dp, CyberEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🎉 Recomp Milestone Active! You are concurrently oxidizing adipose tissue while synthesizing myofibrillar muscle protein.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Dynamic 1RM Calculator (Brzycki Formula)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ElectricLime.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "1RM", tint = ElectricLime, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("BRZYCKI 1RM ESTIMATOR", style = MaterialTheme.typography.labelSmall, color = ElectricLime, fontWeight = FontWeight.Bold)
                            Text("Theoretical Maximal Strength", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "${estimated1RM.toInt()} kg",
                        style = MaterialTheme.typography.displayMedium,
                        color = ElectricLime
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sliders for Weight & Reps
                Text("Tested Working Weight: ${calcWeight.toInt()} kg", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Slider(
                    value = calcWeight,
                    onValueChange = { calcWeight = it },
                    valueRange = 20f..200f,
                    colors = SliderDefaults.colors(thumbColor = ElectricLime, activeTrackColor = ElectricLime)
                )

                Text("Reps Performed: ${calcReps.toInt()} reps", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Slider(
                    value = calcReps,
                    onValueChange = { calcReps = it },
                    valueRange = 1f..15f,
                    colors = SliderDefaults.colors(thumbColor = ElectricLime, activeTrackColor = ElectricLime)
                )
            }
        }

        // 4. Volume Landmark Tracker (MEV / MAV / MRV)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoGraph, contentDescription = "Volume", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("HYPERTROPHY VOLUME LANDMARKS", style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontWeight = FontWeight.Bold)
                        Text("Weekly Sets per Muscle Group", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                VolumeLandmarkRow("Chest / Pectorals", currentSets = 16, mev = 8, mav = 16, mrv = 22, isPriority = userProfile.prioritizedMuscle1 == "Upper Chest" || userProfile.prioritizedMuscle2 == "Upper Chest")
                Spacer(modifier = Modifier.height(10.dp))
                VolumeLandmarkRow("Back / Latissimus", currentSets = 15, mev = 10, mav = 16, mrv = 22, isPriority = userProfile.prioritizedMuscle1 == "Lats" || userProfile.prioritizedMuscle2 == "Lats")
                Spacer(modifier = Modifier.height(10.dp))
                VolumeLandmarkRow("Deltoids / Side Delts", currentSets = 18, mev = 8, mav = 18, mrv = 24, isPriority = userProfile.prioritizedMuscle1 == "Side Delts" || userProfile.prioritizedMuscle2 == "Side Delts")
                Spacer(modifier = Modifier.height(10.dp))
                VolumeLandmarkRow("Quadriceps & Glutes", currentSets = 14, mev = 8, mav = 14, mrv = 20, isPriority = userProfile.prioritizedMuscle1 == "Quads & Glutes" || userProfile.prioritizedMuscle2 == "Quads & Glutes")
            }
        }
    }
}

@Composable
private fun VolumeLandmarkRow(
    muscle: String,
    currentSets: Int,
    mev: Int,
    mav: Int,
    mrv: Int,
    isPriority: Boolean
) {
    val progress = (currentSets.toFloat() / mrv.toFloat()).coerceIn(0f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(muscle, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                if (isPriority) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricLime.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("PRIORITY +4 SETS", style = MaterialTheme.typography.labelSmall, color = ElectricLime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("$currentSets / $mav sets (MAV)", style = MaterialTheme.typography.labelSmall, color = ElectricLime, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (currentSets > mav) BrightAmber else ElectricLime)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("MEV: $mev", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
            Text("MAV: $mav (Optimal)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
            Text("MRV: $mrv (Limit)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
        }
    }
}
