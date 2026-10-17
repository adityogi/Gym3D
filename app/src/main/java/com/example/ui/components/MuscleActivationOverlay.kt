package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.example.ui.theme.DarkBackground
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Filter mode for muscle activation display.
 */
enum class MuscleFilterMode(val label: String) {
    ALL("All Muscles"),
    PRIMARY_ONLY("Primary Agonists"),
    SECONDARY_ONLY("Secondary & Stabilizers")
}

/**
 * Visual overlay component for 3D equipment viewer that highlights the primary and
 * secondary muscles activated by the currently selected gym equipment.
 *
 * Supports:
 * - Compact Floating HUD pill docked in the 3D viewport with quick glance at primary and secondary activations.
 * - Expanded Holographic Overlay pane featuring a biomechanical anatomy silhouette with highlighted muscle zones,
 *   role-based color coding (Electric Lime for Primary Agonists, Cyber Emerald/Cyan for Secondary Synergists & Stabilizers),
 *   real-time rep-cycle activation meters, and interactive muscle cards.
 */
@Composable
fun MuscleActivationOverlay(
    activations: List<MuscleActivation>,
    animationProgress: Float = 0f,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
    onVisibilityChanged: ((Boolean) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    var selectedFilter by remember { mutableStateOf(MuscleFilterMode.ALL) }
    var selectedMuscleGroup by remember { mutableStateOf<MuscleGroup?>(null) }
    var anatomyView by remember { mutableStateOf("front") } // "front" or "back"

    val primaryActivations = remember(activations) {
        activations.filter { it.role == MuscleRole.PRIMARY_AGONIST }
    }
    val secondaryActivations = remember(activations) {
        activations.filter { it.role == MuscleRole.SYNERGIST || it.role == MuscleRole.STABILIZER }
    }

    val filteredActivations = remember(activations, selectedFilter) {
        when (selectedFilter) {
            MuscleFilterMode.ALL -> activations
            MuscleFilterMode.PRIMARY_ONLY -> primaryActivations
            MuscleFilterMode.SECONDARY_ONLY -> secondaryActivations
        }
    }

    // Dynamic rep contraction pulse (0.75 - 1.25 factor)
    val repPulse = (sin(animationProgress * 2 * Math.PI) * 0.25f + 0.75f).toFloat()

    // Infinite breathing glow for primary agonist highlights
    val infiniteTransition = rememberInfiniteTransition(label = "primary_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier.testTag("muscle_activation_overlay")
    ) {
        if (!isExpanded) {
            // ----------------------------------------------------
            // COMPACT FLOATING HUD PILL (Docked on 3D Viewport)
            // ----------------------------------------------------
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                ElectricLime.copy(alpha = 0.7f),
                                CyberEmerald.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        isExpanded = true
                        onVisibilityChanged?.invoke(true)
                    }
                    .testTag("muscle_overlay_compact_hud"),
                color = DarkSurface.copy(alpha = 0.90f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pulsing muscle activity indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ElectricLime.copy(alpha = glowAlpha))
                    )

                    Column {
                        // Primary Mover Quick Glance
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PRIMARY: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricLime,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                            val primaryNames = primaryActivations.joinToString(", ") { it.muscle.displayName }
                            Text(
                                text = if (primaryNames.isNotEmpty()) primaryNames else "Full Body",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }

                        // Secondary Movers Quick Glance
                        if (secondaryActivations.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SECONDARY: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                                val secondaryNames = secondaryActivations.take(2).joinToString(", ") { it.muscle.displayName }
                                Text(
                                    text = secondaryNames + if (secondaryActivations.size > 2) " +${secondaryActivations.size - 2}" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Expand Icon Button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = "Expand Muscle Activation Overlay",
                            tint = ElectricLime,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            // ----------------------------------------------------
            // EXPANDED HOLOGRAPHIC OVERLAY PANEL
            // ----------------------------------------------------
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("muscle_overlay_expanded_panel"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurface.copy(alpha = 0.95f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ElectricLime.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // 1. Overlay Header Bar with Close/Minimize
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ElectricLime.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "Muscles Overlay",
                                    tint = ElectricLime,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MUSCLE ACTIVATION OVERLAY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricLime,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Biomechanical Target Recruitment",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Close button to collapse overlay
                        IconButton(
                            onClick = {
                                isExpanded = false
                                onVisibilityChanged?.invoke(false)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceHigh)
                                .testTag("muscle_overlay_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Minimize Muscle Overlay",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Primary vs Secondary Visual Legend & Filter Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterModeChip(
                            label = "All (${activations.size})",
                            isSelected = selectedFilter == MuscleFilterMode.ALL,
                            color = TextPrimary,
                            onClick = { selectedFilter = MuscleFilterMode.ALL },
                            tag = "filter_all_muscles"
                        )
                        FilterModeChip(
                            label = "Primary (${primaryActivations.size})",
                            isSelected = selectedFilter == MuscleFilterMode.PRIMARY_ONLY,
                            color = ElectricLime,
                            onClick = { selectedFilter = MuscleFilterMode.PRIMARY_ONLY },
                            tag = "filter_primary_muscles"
                        )
                        FilterModeChip(
                            label = "Secondary (${secondaryActivations.size})",
                            isSelected = selectedFilter == MuscleFilterMode.SECONDARY_ONLY,
                            color = CyberEmerald,
                            onClick = { selectedFilter = MuscleFilterMode.SECONDARY_ONLY },
                            tag = "filter_secondary_muscles"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Main Split View: Anatomical Silhouette Map + Highlight Cards
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left Pane: Biomechanical Mannequin Silhouette with glowing muscle highlights
                        Column(
                            modifier = Modifier
                                .weight(0.9f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceHigh.copy(alpha = 0.7f))
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // View Switcher (Front / Back)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(2.dp)
                                    .testTag("anatomy_toggle_front_back")
                            ) {
                                SmallViewPill(
                                    label = "Front",
                                    isSelected = anatomyView == "front",
                                    onClick = { anatomyView = "front" }
                                )
                                SmallViewPill(
                                    label = "Back",
                                    isSelected = anatomyView == "back",
                                    onClick = { anatomyView = "back" }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive Silhouette Canvas
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawOverlayMannequin(
                                        w = size.width,
                                        h = size.height,
                                        isFront = anatomyView == "front",
                                        activations = activations,
                                        selectedFilter = selectedFilter,
                                        selectedMuscle = selectedMuscleGroup,
                                        repPulse = repPulse,
                                        glowAlpha = glowAlpha
                                    )
                                }
                            }

                            // Dynamic Rep Phase EMG Meter
                            val phaseText = when {
                                animationProgress < 0.35f -> "Concentric Ramp"
                                animationProgress < 0.65f -> "Peak Contraction"
                                else -> "Eccentric Return"
                            }
                            Text(
                                text = "EMG: $phaseText (${(repPulse * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricLime,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Right Pane: Categorized Muscle Cards (Primary Agonists first, then Secondary Synergists)
                        Column(
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Section: Primary Agonists (Highlighted prominently)
                            if (selectedFilter != MuscleFilterMode.SECONDARY_ONLY && primaryActivations.isNotEmpty()) {
                                SectionHeaderBadge(
                                    title = "PRIMARY AGONIST (MAIN MOVER)",
                                    badgeColor = ElectricLime,
                                    icon = Icons.Default.FlashOn
                                )
                                primaryActivations.forEach { act ->
                                    PrimaryMuscleHighlightCard(
                                        activation = act,
                                        repPulse = repPulse,
                                        isSelected = selectedMuscleGroup == act.muscle,
                                        onClick = {
                                            selectedMuscleGroup = if (selectedMuscleGroup == act.muscle) null else act.muscle
                                        }
                                    )
                                }
                            }

                            // Section: Secondary Synergists & Stabilizers
                            if (selectedFilter != MuscleFilterMode.PRIMARY_ONLY && secondaryActivations.isNotEmpty()) {
                                SectionHeaderBadge(
                                    title = "SECONDARY & STABILIZERS",
                                    badgeColor = CyberEmerald,
                                    icon = Icons.Default.Shield
                                )
                                secondaryActivations.forEach { act ->
                                    SecondaryMuscleHighlightCard(
                                        activation = act,
                                        repPulse = repPulse,
                                        isSelected = selectedMuscleGroup == act.muscle,
                                        onClick = {
                                            selectedMuscleGroup = if (selectedMuscleGroup == act.muscle) null else act.muscle
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Bottom Action Footer / Quick Cue
                    val activePrimary = primaryActivations.firstOrNull()
                    if (activePrimary != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ElectricLime,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Target focus: Initiate drive with ${activePrimary.muscle.displayName} before synergists take over.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderBadge(
    title: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = badgeColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * High-emphasis card for Primary Agonist muscles.
 * Features an Electric Lime glowing border, high-contrast intensity badge, and anatomical cue.
 */
@Composable
fun PrimaryMuscleHighlightCard(
    activation: MuscleActivation,
    repPulse: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dynamicIntensity = ((activation.intensityPercent * repPulse).toInt()).coerceIn(40, 100)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) BrightAmber else ElectricLime.copy(alpha = 0.8f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .testTag("primary_muscle_card_${activation.muscle.name.lowercase()}"),
        color = if (isSelected) DarkSurfaceHigh else DarkSurface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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
                            .background(ElectricLime)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activation.muscle.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Primary Badge & Intensity readout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectricLime.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRIMARY MOVER",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricLime,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 8.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$dynamicIntensity%",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Animated high-intensity progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(DarkBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(dynamicIntensity / 100f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElectricLime, BrightAmber)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = activation.anatomicalDetail,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

/**
 * Card for Secondary Synergist and Stabilizer muscles.
 * Features a Cyber Emerald / Neon Cyan badge and support role explanation.
 */
@Composable
fun SecondaryMuscleHighlightCard(
    activation: MuscleActivation,
    repPulse: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isSynergist = activation.role == MuscleRole.SYNERGIST
    val roleColor = if (isSynergist) CyberEmerald else NeonCyan
    val roleBadge = if (isSynergist) "SYNERGIST (ASSIST)" else "STABILIZER (FIXATOR)"
    val dynamicIntensity = ((activation.intensityPercent * (repPulse * 0.9f)).toInt()).coerceIn(20, 95)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) roleColor else DarkBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .testTag("secondary_muscle_card_${activation.muscle.name.lowercase()}"),
        color = if (isSelected) DarkSurfaceHigh else DarkSurface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(roleColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activation.muscle.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(roleColor.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = roleBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = roleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$dynamicIntensity%",
                        style = MaterialTheme.typography.labelSmall,
                        color = roleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Subdued secondary progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(dynamicIntensity / 100f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(roleColor)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = activation.anatomicalDetail,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 9.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun FilterModeChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        color = if (isSelected) color.copy(alpha = 0.2f) else DarkSurfaceHigh,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else DarkBorder
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) color else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SmallViewPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ElectricLime else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFF0F1115) else TextSecondary,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = 9.sp
        )
    }
}

/**
 * Draws an anatomical body silhouette with highlighted primary and secondary muscle zones.
 */
private fun DrawScope.drawOverlayMannequin(
    w: Float,
    h: Float,
    isFront: Boolean,
    activations: List<MuscleActivation>,
    selectedFilter: MuscleFilterMode,
    selectedMuscle: MuscleGroup?,
    repPulse: Float,
    glowAlpha: Float
) {
    val neutralBody = Color(0xFF1E232F)
    val cx = w / 2f
    val scale = (h / 240f).coerceAtLeast(0.5f)

    fun getMuscleColor(group: MuscleGroup): Color {
        // If a specific muscle is selected by user, dim other muscles
        if (selectedMuscle != null && selectedMuscle != group && group != MuscleGroup.FULL_BODY) {
            return neutralBody
        }

        val match = activations.firstOrNull { it.muscle == group || it.muscle == MuscleGroup.FULL_BODY }
            ?: return neutralBody

        if (selectedFilter == MuscleFilterMode.PRIMARY_ONLY && match.role != MuscleRole.PRIMARY_AGONIST) {
            return neutralBody
        }
        if (selectedFilter == MuscleFilterMode.SECONDARY_ONLY && match.role == MuscleRole.PRIMARY_AGONIST) {
            return neutralBody
        }

        return when (match.role) {
            MuscleRole.PRIMARY_AGONIST -> ElectricLime.copy(alpha = glowAlpha)
            MuscleRole.SYNERGIST -> CyberEmerald.copy(alpha = 0.9f)
            MuscleRole.STABILIZER -> NeonCyan.copy(alpha = 0.8f)
        }
    }

    // 1. Head
    drawOval(
        color = neutralBody,
        topLeft = Offset(cx - 12f * scale, 8f * scale),
        size = Size(24f * scale, 30f * scale)
    )

    // 2. Neck
    drawRect(
        color = neutralBody,
        topLeft = Offset(cx - 6f * scale, 36f * scale),
        size = Size(12f * scale, 10f * scale)
    )

    if (isFront) {
        // ================= FRONT VIEW =================
        // Shoulders / Deltoids
        val deltColor = getMuscleColor(MuscleGroup.SHOULDERS)
        drawOval(deltColor, Offset(cx - 38f * scale, 44f * scale), Size(18f * scale, 22f * scale))
        drawOval(deltColor, Offset(cx + 20f * scale, 44f * scale), Size(18f * scale, 22f * scale))

        // Pectorals (Chest)
        val chestColor = getMuscleColor(MuscleGroup.CHEST)
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(cx - 22f * scale, 46f * scale),
            size = Size(20f * scale, 18f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale)
        )
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(cx + 2f * scale, 46f * scale),
            size = Size(20f * scale, 18f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale)
        )

        // Abdominals (Core)
        val absColor = getMuscleColor(MuscleGroup.CORE)
        for (row in 0..2) {
            drawRoundRect(
                color = absColor,
                topLeft = Offset(cx - 12f * scale, (68f + row * 10f) * scale),
                size = Size(10f * scale, 8f * scale),
                cornerRadius = CornerRadius(2f * scale, 2f * scale)
            )
            drawRoundRect(
                color = absColor,
                topLeft = Offset(cx + 2f * scale, (68f + row * 10f) * scale),
                size = Size(10f * scale, 8f * scale),
                cornerRadius = CornerRadius(2f * scale, 2f * scale)
            )
        }

        // Biceps (Arms)
        val armColor = getMuscleColor(MuscleGroup.ARMS)
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx - 42f * scale, 64f * scale),
            size = Size(14f * scale, 24f * scale),
            cornerRadius = CornerRadius(5f * scale, 5f * scale)
        )
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx + 28f * scale, 64f * scale),
            size = Size(14f * scale, 24f * scale),
            cornerRadius = CornerRadius(5f * scale, 5f * scale)
        )

        // Forearms
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx - 44f * scale, 90f * scale),
            size = Size(12f * scale, 26f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale)
        )
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx + 32f * scale, 90f * scale),
            size = Size(12f * scale, 26f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale)
        )

        // Quadriceps (Legs)
        val quadColor = getMuscleColor(MuscleGroup.LEGS)
        drawRoundRect(
            color = quadColor,
            topLeft = Offset(cx - 24f * scale, 102f * scale),
            size = Size(20f * scale, 48f * scale),
            cornerRadius = CornerRadius(7f * scale, 7f * scale)
        )
        drawRoundRect(
            color = quadColor,
            topLeft = Offset(cx + 4f * scale, 102f * scale),
            size = Size(20f * scale, 48f * scale),
            cornerRadius = CornerRadius(7f * scale, 7f * scale)
        )

        // Calves (Shins/Tibialis)
        drawRoundRect(
            color = quadColor,
            topLeft = Offset(cx - 20f * scale, 156f * scale),
            size = Size(16f * scale, 42f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )
        drawRoundRect(
            color = quadColor,
            topLeft = Offset(cx + 4f * scale, 156f * scale),
            size = Size(16f * scale, 42f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )
    } else {
        // ================= BACK VIEW =================
        // Trapezius / Upper Back
        val backColor = getMuscleColor(MuscleGroup.BACK)
        val trapPath = Path().apply {
            moveTo(cx, 38f * scale)
            lineTo(cx - 28f * scale, 56f * scale)
            lineTo(cx, 76f * scale)
            lineTo(cx + 28f * scale, 56f * scale)
            close()
        }
        drawPath(trapPath, backColor)

        // Latissimus Dorsi (Lats)
        drawRoundRect(
            color = backColor,
            topLeft = Offset(cx - 30f * scale, 64f * scale),
            size = Size(16f * scale, 34f * scale),
            cornerRadius = CornerRadius(5f * scale, 5f * scale)
        )
        drawRoundRect(
            color = backColor,
            topLeft = Offset(cx + 14f * scale, 64f * scale),
            size = Size(16f * scale, 34f * scale),
            cornerRadius = CornerRadius(5f * scale, 5f * scale)
        )

        // Triceps (Back Arms)
        val tricepColor = getMuscleColor(MuscleGroup.ARMS)
        drawRoundRect(
            color = tricepColor,
            topLeft = Offset(cx - 42f * scale, 56f * scale),
            size = Size(14f * scale, 28f * scale),
            cornerRadius = CornerRadius(5f * scale, 5f * scale)
        )
        drawRoundRect(
            color = tricepColor,
            topLeft = Offset(cx + 28f * scale, 56f * scale),
            size = Size(14f * scale, 28f * scale),
            cornerRadius = CornerRadius(5f * scale, 5f * scale)
        )

        // Glutes
        val gluteColor = getMuscleColor(MuscleGroup.LEGS)
        drawOval(gluteColor, Offset(cx - 24f * scale, 98f * scale), Size(22f * scale, 24f * scale))
        drawOval(gluteColor, Offset(cx + 2f * scale, 98f * scale), Size(22f * scale, 24f * scale))

        // Hamstrings
        drawRoundRect(
            color = gluteColor,
            topLeft = Offset(cx - 24f * scale, 124f * scale),
            size = Size(18f * scale, 34f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )
        drawRoundRect(
            color = gluteColor,
            topLeft = Offset(cx + 6f * scale, 124f * scale),
            size = Size(18f * scale, 34f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )

        // Calves (Gastrocnemius)
        drawRoundRect(
            color = gluteColor,
            topLeft = Offset(cx - 22f * scale, 162f * scale),
            size = Size(18f * scale, 38f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )
        drawRoundRect(
            color = gluteColor,
            topLeft = Offset(cx + 4f * scale, 162f * scale),
            size = Size(18f * scale, 38f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )
    }
}
