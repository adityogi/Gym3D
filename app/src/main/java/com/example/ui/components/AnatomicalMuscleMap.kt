package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MuscleActivation
import com.example.model.MuscleGroup
import com.example.model.MuscleRole
import com.example.ui.theme.BrightAmber
import com.example.ui.theme.CyberEmerald
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
fun AnatomicalMuscleMap(
    activations: List<MuscleActivation>,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    var selectedView by remember { mutableStateOf("front") } // "front" or "back"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("anatomical_muscle_map")
    ) {
        // Header & View Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TARGETED MUSCLE RECRUITMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Biomechanical Activation Map",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            // Front/Back Switcher Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceVariant)
                    .padding(3.dp)
            ) {
                ViewPill(
                    label = "Front View",
                    isSelected = selectedView == "front",
                    onClick = { selectedView = "front" }
                )
                ViewPill(
                    label = "Back View",
                    isSelected = selectedView == "back",
                    onClick = { selectedView = "back" }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center Anatomy Silhouette Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            // Pulse opacity driven by exercise cycle
            val pulse = (kotlin.math.sin(animationProgress * 2 * kotlin.math.PI) * 0.25f + 0.75f).toFloat()

            Canvas(modifier = Modifier.size(190.dp, 230.dp)) {
                val w = size.width
                val h = size.height

                val isFront = selectedView == "front"

                // Base mannequin body outline
                drawAnatomicalBody(
                    w = w,
                    h = h,
                    isFront = isFront,
                    activations = activations,
                    pulse = pulse
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Role Legend Tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = ElectricLime, label = "Primary Agonist")
            LegendItem(color = CyberEmerald, label = "Synergist")
            LegendItem(color = NeonCyan, label = "Stabilizer")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of Muscle Activations with Progress Bars
        Text(
            text = "Active Muscular Chain Details",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        activations.forEach { act ->
            MuscleItemCard(act, animationProgress)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ViewPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) ElectricLime else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFF0F1115) else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun MuscleItemCard(act: MuscleActivation, animationProgress: Float) {
    val roleColor = when (act.role) {
        MuscleRole.PRIMARY_AGONIST -> ElectricLime
        MuscleRole.SYNERGIST -> CyberEmerald
        MuscleRole.STABILIZER -> NeonCyan
    }

    val animatedFill by animateFloatAsState(
        targetValue = (act.intensityPercent / 100f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "fill"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(roleColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = act.muscle.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${act.intensityPercent}%",
                style = MaterialTheme.typography.titleSmall,
                color = roleColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFill)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(roleColor)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = act.anatomicalDetail,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAnatomicalBody(
    w: Float,
    h: Float,
    isFront: Boolean,
    activations: List<MuscleActivation>,
    pulse: Float
) {
    val neutralBody = Color(0xFF222733)
    val cx = w / 2f

    fun getMuscleColor(group: MuscleGroup): Color {
        val match = activations.firstOrNull { it.muscle == group || it.muscle == MuscleGroup.FULL_BODY }
            ?: return neutralBody
        val baseColor = when (match.role) {
            MuscleRole.PRIMARY_AGONIST -> ElectricLime
            MuscleRole.SYNERGIST -> CyberEmerald
            MuscleRole.STABILIZER -> NeonCyan
        }
        return baseColor.copy(alpha = if (match.role == MuscleRole.PRIMARY_AGONIST) pulse else 0.85f)
    }

    // 1. Head
    drawOval(
        color = neutralBody,
        topLeft = Offset(cx - 16f, 10f),
        size = Size(32f, 38f)
    )

    // 2. Neck
    drawRect(
        color = neutralBody,
        topLeft = Offset(cx - 8f, 44f),
        size = Size(16f, 12f)
    )

    if (isFront) {
        // FRONT VIEW:
        // Deltoids (Left & Right)
        val deltColor = getMuscleColor(MuscleGroup.SHOULDERS)
        drawOval(deltColor, Offset(cx - 48f, 54f), Size(22f, 28f))
        drawOval(deltColor, Offset(cx + 26f, 54f), Size(22f, 28f))

        // Pectorals (Chest)
        val chestColor = getMuscleColor(MuscleGroup.CHEST)
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(cx - 28f, 58f),
            size = Size(26f, 24f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(cx + 2f, 58f),
            size = Size(26f, 24f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Abdominals (Core)
        val absColor = getMuscleColor(MuscleGroup.CORE)
        for (row in 0..2) {
            val yOffset = 86f + row * 11f
            drawRoundRect(absColor, Offset(cx - 14f, yOffset), Size(12f, 9f), CornerRadius(3f, 3f))
            drawRoundRect(absColor, Offset(cx + 2f, yOffset), Size(12f, 9f), CornerRadius(3f, 3f))
        }

        // Biceps (Arms)
        val armColor = getMuscleColor(MuscleGroup.ARMS)
        drawOval(armColor, Offset(cx - 46f, 82f), Size(16f, 30f))
        drawOval(armColor, Offset(cx + 30f, 82f), Size(16f, 30f))

        // Forearms
        drawOval(neutralBody, Offset(cx - 44f, 114f), Size(14f, 32f))
        drawOval(neutralBody, Offset(cx + 30f, 114f), Size(14f, 32f))

        // Pelvis
        drawRoundRect(neutralBody, Offset(cx - 22f, 120f), Size(44f, 20f), CornerRadius(4f, 4f))

        // Quadriceps (Front Thighs)
        val quadColor = getMuscleColor(MuscleGroup.LEGS)
        drawRoundRect(quadColor, Offset(cx - 24f, 142f), Size(20f, 44f), CornerRadius(8f, 8f))
        drawRoundRect(quadColor, Offset(cx + 4f, 142f), Size(20f, 44f), CornerRadius(8f, 8f))

        // Calves & Shins
        drawRoundRect(neutralBody, Offset(cx - 20f, 190f), Size(15f, 36f), CornerRadius(6f, 6f))
        drawRoundRect(neutralBody, Offset(cx + 5f, 190f), Size(15f, 36f), CornerRadius(6f, 6f))

    } else {
        // BACK VIEW:
        // Trapezius
        val trapColor = getMuscleColor(MuscleGroup.BACK)
        val trapPath = Path().apply {
            moveTo(cx, 48f)
            lineTo(cx - 36f, 62f)
            lineTo(cx, 88f)
            lineTo(cx + 36f, 62f)
            close()
        }
        drawPath(trapPath, trapColor)

        // Rear Deltoids
        val deltColor = getMuscleColor(MuscleGroup.SHOULDERS)
        drawOval(deltColor, Offset(cx - 48f, 54f), Size(22f, 26f))
        drawOval(deltColor, Offset(cx + 26f, 54f), Size(22f, 26f))

        // Latissimus Dorsi (V-taper wings)
        val latColor = getMuscleColor(MuscleGroup.BACK)
        drawRoundRect(latColor, Offset(cx - 30f, 75f), Size(20f, 38f), CornerRadius(8f, 8f))
        drawRoundRect(latColor, Offset(cx + 10f, 75f), Size(20f, 38f), CornerRadius(8f, 8f))

        // Triceps (Arms)
        val tricepColor = getMuscleColor(MuscleGroup.ARMS)
        drawOval(tricepColor, Offset(cx - 46f, 80f), Size(16f, 30f))
        drawOval(tricepColor, Offset(cx + 30f, 80f), Size(16f, 30f))

        // Gluteus Maximus
        val gluteColor = getMuscleColor(MuscleGroup.LEGS)
        drawOval(gluteColor, Offset(cx - 24f, 120f), Size(22f, 24f))
        drawOval(gluteColor, Offset(cx + 2f, 120f), Size(22f, 24f))

        // Hamstrings (Back of Thighs)
        val hamColor = getMuscleColor(MuscleGroup.LEGS)
        drawRoundRect(hamColor, Offset(cx - 23f, 146f), Size(19f, 40f), CornerRadius(8f, 8f))
        drawRoundRect(hamColor, Offset(cx + 4f, 146f), Size(19f, 40f), CornerRadius(8f, 8f))

        // Gastrocnemius (Calves)
        drawRoundRect(gluteColor.copy(alpha = 0.6f), Offset(cx - 20f, 190f), Size(15f, 36f), CornerRadius(6f, 6f))
        drawRoundRect(gluteColor.copy(alpha = 0.6f), Offset(cx + 5f, 190f), Size(15f, 36f), CornerRadius(6f, 6f))
    }
}
