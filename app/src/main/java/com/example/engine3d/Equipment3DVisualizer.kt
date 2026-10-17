package com.example.engine3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BrightAmber
import com.example.ui.theme.CanvasGridLine
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Equipment Visualizer View Component capable of loading and rotating 3D models.
 * Features 360-degree orbital gesture interaction, rotational inertia momentum,
 * pinch-to-zoom, auto-spin turntable mode, preset camera snapping, and dual Compose/AndroidView backends.
 */
@Composable
fun Equipment3DVisualizer(
    initialModelId: String = "bench_press",
    initialProgress: Float = 0.0f,
    initialYaw: Float = 35f,
    initialPitch: Float = 14f,
    initialZoom: Float = 1.0f,
    initialRenderMode: Render3DMode = Render3DMode.SOLID,
    selectedPinId: String? = null,
    showModelSwitcher: Boolean = true,
    showCompassHud: Boolean = true,
    showPresetSnaps: Boolean = true,
    onPinSelected: ((String?) -> Unit)? = null,
    onModelChanged: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var currentModelId by remember(initialModelId) { mutableStateOf(initialModelId) }
    var progress by remember(initialProgress) { mutableFloatStateOf(initialProgress) }
    var yaw by remember(initialYaw) { mutableFloatStateOf(initialYaw) }
    var pitch by remember(initialPitch) { mutableFloatStateOf(initialPitch) }
    var zoom by remember(initialZoom) { mutableFloatStateOf(initialZoom) }
    var renderMode by remember(initialRenderMode) { mutableStateOf(initialRenderMode) }
    var isTurntableActive by remember { mutableStateOf(false) }
    var useAndroidViewBackend by remember { mutableStateOf(false) }
    var isModelLoading by remember { mutableStateOf(false) }

    // Rotational Momentum state
    var velocityYaw by remember { mutableFloatStateOf(0f) }
    var velocityPitch by remember { mutableFloatStateOf(0f) }

    // Load active 3D model geometry
    val currentMeta = remember(currentModelId) { Model3DLoader.getModelMeta(currentModelId) }
    val geometry = remember(currentModelId, progress) {
        Model3DLoader.loadModel(currentModelId, progress)
    }

    // Auto-spin turntable or rotational momentum decay loop
    LaunchedEffect(isTurntableActive, velocityYaw, velocityPitch) {
        while (isActive) {
            delay(16L) // ~60 FPS
            if (isTurntableActive) {
                yaw = ((yaw + 0.4f) % 360f + 360f) % 360f
            } else if (abs(velocityYaw) > 0.1f || abs(velocityPitch) > 0.1f) {
                yaw = ((yaw + velocityYaw) % 360f + 360f) % 360f
                pitch = (pitch + velocityPitch).coerceIn(-80f, 80f)
                velocityYaw *= 0.92f
                velocityPitch *= 0.92f
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkBackground)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .testTag("equipment_3d_visualizer")
    ) {
        // 1. Top HUD Bar (Title, 360 Badge, Render Mode Pills, Backend Toggle)
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
                    contentDescription = "360 Visualizer",
                    tint = ElectricLime,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "360° 3D EQUIPMENT VISUALIZER",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentMeta.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            // Mode Selector Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VisualizerPill(
                    label = "Solid",
                    isSelected = renderMode == Render3DMode.SOLID,
                    onClick = { renderMode = Render3DMode.SOLID }
                )
                VisualizerPill(
                    label = "CAD Wire",
                    isSelected = renderMode == Render3DMode.WIREFRAME,
                    onClick = { renderMode = Render3DMode.WIREFRAME }
                )
                VisualizerPill(
                    label = "Ghost",
                    isSelected = renderMode == Render3DMode.ANATOMY,
                    onClick = { renderMode = Render3DMode.ANATOMY }
                )
            }
        }

        // 2. Model Loading Selector Strip (Horizontal carousel)
        if (showModelSwitcher) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceHigh)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Model3DLoader.availableModels.forEach { modelMeta ->
                    val isSelected = modelMeta.id == currentModelId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricLime else DarkSurfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ElectricLime else DarkBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (currentModelId != modelMeta.id) {
                                    currentModelId = modelMeta.id
                                    yaw = modelMeta.defaultYaw
                                    pitch = modelMeta.defaultPitch
                                    zoom = modelMeta.defaultZoom
                                    onModelChanged?.invoke(modelMeta.id)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("model_chip_${modelMeta.id}")
                    ) {
                        Text(
                            text = modelMeta.name.split(" ").take(2).joinToString(" "),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color(0xFF0F1115) else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 3. The 3D Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(Color(0xFF07080B))
        ) {
            if (useAndroidViewBackend) {
                // Native Android View backend
                AndroidView(
                    factory = { ctx ->
                        EquipmentVisualizerView(ctx).apply {
                            loadModel(currentModelId, progress)
                            this.yaw = yaw
                            this.pitch = pitch
                            this.zoom = zoom
                            this.renderMode = renderMode
                            this.isTurntableEnabled = isTurntableActive
                            this.onCameraChangedListener = { y, p, z ->
                                yaw = y
                                pitch = p
                                zoom = z
                            }
                            this.onPinSelectedListener = { pId, _ ->
                                onPinSelected?.invoke(pId)
                            }
                        }
                    },
                    update = { view ->
                        if (view.currentModelId != currentModelId) {
                            view.loadModel(currentModelId, progress)
                        } else {
                            view.setProgress(progress)
                        }
                        view.yaw = yaw
                        view.pitch = pitch
                        view.zoom = zoom
                        view.renderMode = renderMode
                        view.isTurntableEnabled = isTurntableActive
                        view.selectedPinId = selectedPinId
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("native_3d_view_backend")
                )
            } else {
                // Jetpack Compose Hardware Canvas Backend with full 360-degree touch gestures
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("equipment_3d_canvas")
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    yaw = currentMeta.defaultYaw
                                    pitch = currentMeta.defaultPitch
                                    zoom = currentMeta.defaultZoom
                                    velocityYaw = 0f
                                    velocityPitch = 0f
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                // Single-finger & two-finger continuous 360 drag
                                val deltaYaw = -pan.x * 0.45f
                                val deltaPitch = pan.y * 0.45f
                                yaw = ((yaw + deltaYaw) % 360f + 360f) % 360f
                                pitch = (pitch + deltaPitch).coerceIn(-80f, 80f)

                                if (gestureZoom != 1.0f) {
                                    zoom = (zoom * gestureZoom).coerceIn(0.45f, 3.0f)
                                }

                                // Store recent motion for momentum
                                velocityYaw = -pan.x * 0.12f
                                velocityPitch = pan.y * 0.10f
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val baseScale = (w.coerceAtMost(h) * 0.46f) * zoom

                    // 1. Draw 3D Ground Grid
                    drawVisualizerGroundGrid(yaw, pitch, w, h, baseScale)

                    // 2. Transform vertices by camera (Yaw & Pitch)
                    val transformedVertices = geometry.vertices.map { v ->
                        v.rotateY(yaw).rotateX(pitch)
                    }

                    // Project vertices to screen coordinates
                    val projectedPoints = transformedVertices.map { v ->
                        v.project(w, h, cameraDistance = 900f, scale = baseScale)
                    }

                    // 3. Depth-sort and render faces (Painter's Algorithm)
                    val projectedFaces = geometry.faces.mapNotNull { face ->
                        if (face.indices.any { it >= projectedPoints.size }) return@mapNotNull null
                        val pts = face.indices.map { projectedPoints[it].screenPos }
                        val avgDepth = face.indices.map { projectedPoints[it].depthZ }.average().toFloat()

                        val isFrontFacing = if (pts.size >= 3) {
                            val p0 = pts[0]; val p1 = pts[1]; val p2 = pts[2]
                            ((p1.x - p0.x) * (p2.y - p0.y) - (p1.y - p0.y) * (p2.x - p0.x)) < 0
                        } else true

                        if (!isFrontFacing && renderMode == Render3DMode.SOLID) return@mapNotNull null

                        ProjectedFace(
                            points = pts,
                            avgDepth = avgDepth,
                            color = when (renderMode) {
                                Render3DMode.WIREFRAME -> Color.Transparent
                                Render3DMode.ANATOMY -> face.baseColor.copy(alpha = 0.22f)
                                Render3DMode.SOLID -> face.baseColor
                            },
                            wireColor = when (renderMode) {
                                Render3DMode.WIREFRAME -> NeonCyan.copy(alpha = 0.88f)
                                else -> face.wireColor
                            }
                        )
                    }.sortedByDescending { it.avgDepth }

                    val facePath = Path()
                    for (face in projectedFaces) {
                        if (face.points.size >= 3) {
                            facePath.reset()
                            facePath.moveTo(face.points[0].x, face.points[0].y)
                            for (i in 1 until face.points.size) {
                                facePath.lineTo(face.points[i].x, face.points[i].y)
                            }
                            facePath.close()

                            if (face.color != Color.Transparent) {
                                drawPath(facePath, color = face.color, style = Fill)
                            }
                            if (face.wireColor.alpha > 0.05f) {
                                drawPath(
                                    facePath,
                                    color = face.wireColor,
                                    style = Stroke(width = if (renderMode == Render3DMode.WIREFRAME) 2.0f else 1.0f)
                                )
                            }
                        }
                    }

                    // 4. Render 3D Lines (cables, bars, chains)
                    val projectedLines = geometry.lines.mapNotNull { line ->
                        if (line.startIndex >= projectedPoints.size || line.endIndex >= projectedPoints.size) return@mapNotNull null
                        val p1 = projectedPoints[line.startIndex]
                        val p2 = projectedPoints[line.endIndex]
                        ProjectedLine(
                            start = p1.screenPos,
                            end = p2.screenPos,
                            avgDepth = (p1.depthZ + p2.depthZ) / 2f,
                            color = if (renderMode == Render3DMode.ANATOMY && line.color.alpha > 0.4f) ElectricLime else line.color,
                            strokeWidth = line.strokeWidth
                        )
                    }.sortedByDescending { it.avgDepth }

                    for (line in projectedLines) {
                        drawLine(
                            color = line.color,
                            start = line.start,
                            end = line.end,
                            strokeWidth = line.strokeWidth
                        )
                    }

                    // 5. Render interactive Pin Markers
                    for (pin in geometry.pinIndicators) {
                        val transformedPin = pin.position.rotateY(yaw).rotateX(pitch)
                        val projectedPin = transformedPin.project(w, h, cameraDistance = 900f, scale = baseScale)
                        val isSelected = pin.id == selectedPinId
                        drawVisualizerPin(projectedPin.screenPos, isSelected)
                    }

                    // 6. Draw 360 Orientation Compass in top-right
                    if (showCompassHud) {
                        drawCompassGizmo(yaw, w)
                    }
                }
            }

            // Top-Left Telemetry HUD (360 Orbit, Yaw, Pitch, Polygon Count)
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface.copy(alpha = 0.88f))
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ElectricLime)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "360° ORBIT: ${yaw.toInt()}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "PITCH: ${pitch.toInt()}° | ZOOM: ${(zoom * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = "${geometry.vertices.size} Verts • ${geometry.faces.size} Faces",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }

            // Top-Right Controls: Turntable 360° Auto-spin + Backend Toggle
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Turntable Auto-spin button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isTurntableActive) ElectricLime else DarkSurface.copy(alpha = 0.85f))
                        .border(1.dp, if (isTurntableActive) ElectricLime else DarkBorder, RoundedCornerShape(8.dp))
                        .clickable { isTurntableActive = !isTurntableActive }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("turntable_toggle_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cached,
                            contentDescription = "Auto-Spin 360",
                            tint = if (isTurntableActive) Color(0xFF0F1115) else TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTurntableActive) "SPINNING" else "360° AUTO",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTurntableActive) Color(0xFF0F1115) else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Reset Camera
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface.copy(alpha = 0.85f))
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            yaw = currentMeta.defaultYaw
                            pitch = currentMeta.defaultPitch
                            zoom = currentMeta.defaultZoom
                            velocityYaw = 0f
                            velocityPitch = 0f
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("reset_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Camera",
                        tint = TextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Bottom Interaction Prompt Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface.copy(alpha = 0.85f))
                    .border(1.dp, DarkBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Drag to rotate 360° • Pinch to zoom • Double-tap to reset",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        // 4. Preset Angle Snapping Strip (FRONT 0°, RIGHT 90°, BACK 180°, LEFT 270°, TOP 75°, ISO 35°)
        if (showPresetSnaps) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AngleSnapChip("FRONT 0°") { yaw = 0f; pitch = 10f }
                    AngleSnapChip("SIDE 90°") { yaw = 90f; pitch = 10f }
                    AngleSnapChip("BACK 180°") { yaw = 180f; pitch = 10f }
                    AngleSnapChip("LEFT 270°") { yaw = 270f; pitch = 10f }
                    AngleSnapChip("ISO 45°") { yaw = 45f; pitch = 15f }
                    AngleSnapChip("TOP 75°") { yaw = 0f; pitch = 75f }
                }
            }
        }
    }
}

@Composable
private fun VisualizerPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ElectricLime else Color.Transparent)
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

@Composable
private fun AngleSnapChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun DrawScope.drawVisualizerGroundGrid(
    yaw: Float,
    pitch: Float,
    width: Float,
    height: Float,
    scale: Float
) {
    val groundY = -0.92f
    val gridSize = 1.4f
    val step = 0.35f

    var x = -gridSize
    while (x <= gridSize + 0.001f) {
        val p1 = Vector3(x, groundY, -gridSize).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)
        val p2 = Vector3(x, groundY, gridSize).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)
        val p3 = Vector3(-gridSize, groundY, x).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)
        val p4 = Vector3(gridSize, groundY, x).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)

        drawLine(CanvasGridLine, p1.screenPos, p2.screenPos, strokeWidth = 1f)
        drawLine(CanvasGridLine, p3.screenPos, p4.screenPos, strokeWidth = 1f)
        x += step
    }

    val originCenter = Vector3(0f, groundY, 0f).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)
    drawCircle(
        color = ElectricLime.copy(alpha = 0.15f),
        radius = scale * 0.85f * (900f / originCenter.depthZ),
        center = originCenter.screenPos,
        style = Stroke(width = 1.5f)
    )
}

private fun DrawScope.drawVisualizerPin(pos: Offset, isSelected: Boolean) {
    val outerRadius = if (isSelected) 18f else 12f
    val innerRadius = if (isSelected) 8f else 5f
    val color = if (isSelected) BrightAmber else ElectricLime

    drawCircle(color = color.copy(alpha = 0.35f), radius = outerRadius * 1.6f, center = pos)
    drawCircle(color = color, radius = outerRadius, center = pos, style = Stroke(width = 2.5f))
    drawCircle(color = Color(0xFF0F1115), radius = outerRadius - 2f, center = pos)
    drawCircle(color = color, radius = innerRadius, center = pos)
}

private fun DrawScope.drawCompassGizmo(yaw: Float, width: Float) {
    val center = Offset(width - 45f, 90f)
    val radius = 18f

    drawCircle(
        color = ElectricLime.copy(alpha = 0.25f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5f)
    )

    // Heading needle (North is at 0 degrees)
    val rad = Math.toRadians((yaw - 90.0).toDouble())
    val needleEnd = Offset(
        center.x + (radius - 3f) * cos(rad).toFloat(),
        center.y + (radius - 3f) * sin(rad).toFloat()
    )

    drawLine(
        color = ElectricLime,
        start = center,
        end = needleEnd,
        strokeWidth = 2.5f
    )
}
