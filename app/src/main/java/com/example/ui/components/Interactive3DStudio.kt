package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.engine3d.Gym3DCanvas
import com.example.engine3d.Render3DMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.example.model.Equipment
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
fun Interactive3DStudio(
    equipment: Equipment,
    animationProgress: Float,
    isPlaying: Boolean,
    playbackSpeed: Float,
    yaw: Float,
    pitch: Float,
    zoom: Float,
    renderMode: Render3DMode,
    selectedPinId: String?,
    onTogglePlay: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onRotate: (deltaYaw: Float, deltaPitch: Float) -> Unit,
    onZoomChange: (Float) -> Unit,
    onSetCameraAngles: (yaw: Float, pitch: Float) -> Unit,
    onResetCamera: () -> Unit,
    onSetRenderMode: (Render3DMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAutoSpinning by remember { mutableStateOf(false) }

    LaunchedEffect(isAutoSpinning) {
        while (isAutoSpinning && isActive) {
            delay(16L)
            onRotate(0.4f, 0f)
        }
    }

    // Determine movement phase
    val phaseName = when {
        animationProgress < 0.35f -> "Concentric Drive (Lifting)"
        animationProgress < 0.65f -> "Peak Contraction (Hold)"
        else -> "Eccentric Return (Controlled Negative)"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkBackground)
            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
            .testTag("interactive_3d_studio")
    ) {
        // 1. Top HUD Bar (Title, 360 Badge, Render Mode Switcher)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                    contentDescription = "360 Orbit",
                    tint = ElectricLime,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "360° REAL-TIME 3D VIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Swipe to rotate • Pinch to zoom",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Mode Selector Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(2.dp)
            ) {
                RenderModePill(
                    label = "Solid",
                    isSelected = renderMode == Render3DMode.SOLID,
                    onClick = { onSetRenderMode(Render3DMode.SOLID) }
                )
                RenderModePill(
                    label = "Blueprint",
                    isSelected = renderMode == Render3DMode.WIREFRAME,
                    onClick = { onSetRenderMode(Render3DMode.WIREFRAME) }
                )
                RenderModePill(
                    label = "Ghost",
                    isSelected = renderMode == Render3DMode.ANATOMY,
                    onClick = { onSetRenderMode(Render3DMode.ANATOMY) }
                )
            }
        }

        // 2. 3D Canvas Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(Color(0xFF090A0D))
        ) {
            // The 3D Engine Canvas
            Gym3DCanvas(
                equipmentId = equipment.id,
                animationProgress = animationProgress,
                yaw = yaw,
                pitch = pitch,
                zoom = zoom,
                selectedPinId = selectedPinId,
                renderMode = renderMode,
                onRotate = onRotate,
                onZoomChange = onZoomChange,
                onResetCamera = onResetCamera,
                modifier = Modifier.fillMaxSize()
            )

            // Top-left Camera Telemetry HUD
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface.copy(alpha = 0.85f))
                    .border(1.dp, DarkBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "360° YAW: ${yaw.toInt()}° | PITCH: ${pitch.toInt()}°",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    text = "ZOOM: ${(zoom * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            var isMuscleOverlayExpanded by remember { mutableStateOf(false) }

            // Top-right Preset Camera Buttons (Side 90, Front, Iso, Top, Back, Auto-Spin, Muscle Overlay)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetCameraBtn(if (isMuscleOverlayExpanded) "HIDE OVERLAY" else "MUSCLES OVERLAY") {
                    isMuscleOverlayExpanded = !isMuscleOverlayExpanded
                }
                PresetCameraBtn(if (isAutoSpinning) "SPINNING" else "360° AUTO") { isAutoSpinning = !isAutoSpinning }
                PresetCameraBtn("ISO") { onSetCameraAngles(equipment.defaultCameraYaw, equipment.defaultCameraPitch) }
                PresetCameraBtn("FRONT 0°") { onSetCameraAngles(0f, 12f) }
                PresetCameraBtn("SIDE 90°") { onSetCameraAngles(90f, 10f) }
                PresetCameraBtn("BACK 180°") { onSetCameraAngles(180f, 12f) }
                PresetCameraBtn("TOP 75°") { onSetCameraAngles(0f, 75f) }
                PresetCameraBtn("RESET") { onResetCamera() }
            }

            // Bottom Floating Phase Indicator
            if (!isMuscleOverlayExpanded) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface.copy(alpha = 0.9f))
                        .border(1.dp, ElectricLime.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricLime)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = phaseName,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Primary & Secondary Muscle Activation Visual Overlay
            if (isMuscleOverlayExpanded) {
                MuscleActivationOverlay(
                    activations = equipment.muscleActivations,
                    animationProgress = animationProgress,
                    initialExpanded = true,
                    onVisibilityChanged = { isMuscleOverlayExpanded = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                )
            } else {
                MuscleActivationOverlay(
                    activations = equipment.muscleActivations,
                    animationProgress = animationProgress,
                    initialExpanded = false,
                    onVisibilityChanged = { isMuscleOverlayExpanded = it },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 44.dp)
                )
            }
        }

        // 3. Animation Controls Panel (Play/Pause, Scrubber, Speed Pills)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(14.dp)
        ) {
            // Timeline scrubber slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "0%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )

                Slider(
                    value = animationProgress,
                    onValueChange = onProgressChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("exercise_timeline_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricLime,
                        activeTrackColor = ElectricLime,
                        inactiveTrackColor = DarkBorder
                    )
                )

                Text(
                    text = "${(animationProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Play/Pause + Speed Pills + Zoom quick triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ElectricLime)
                            .clickable(onClick = onTogglePlay)
                            .testTag("play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color(0xFF0F1115),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (isPlaying) "Playing Form Loop" else "Paused (Scrub to inspect)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }

                // Speed Selectors (0.5x Slow-Mo, 1.0x, 1.5x)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(2.dp)
                ) {
                    SpeedPill(
                        label = "0.5x",
                        isSelected = playbackSpeed == 0.5f,
                        onClick = { onSpeedChange(0.5f) }
                    )
                    SpeedPill(
                        label = "1.0x",
                        isSelected = playbackSpeed == 1.0f,
                        onClick = { onSpeedChange(1.0f) }
                    )
                    SpeedPill(
                        label = "1.5x",
                        isSelected = playbackSpeed == 1.5f,
                        onClick = { onSpeedChange(1.5f) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderModePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ElectricLime else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFF0F1115) else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun PresetCameraBtn(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurface.copy(alpha = 0.85f))
            .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SpeedPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberEmerald else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFF0F1115) else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 10.sp
        )
    }
}
