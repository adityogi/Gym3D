package com.example.engine3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.BrightAmber
import com.example.ui.theme.CanvasGridLine
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeonCyan

enum class Render3DMode(val label: String) {
    SOLID("Textured Solid"),
    WIREFRAME("Blueprint Wireframe"),
    ANATOMY("Ghost Anatomy Focus")
}

@Composable
fun Gym3DCanvas(
    equipmentId: String,
    animationProgress: Float,
    yaw: Float,
    pitch: Float,
    zoom: Float,
    selectedPinId: String? = null,
    renderMode: Render3DMode = Render3DMode.SOLID,
    onRotate: (deltaYaw: Float, deltaPitch: Float) -> Unit,
    onZoomChange: (newZoom: Float) -> Unit,
    onResetCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate geometry for current equipment and frame
    val geometry = remember(equipmentId, animationProgress) {
        EquipmentGeometries.getGeometry(equipmentId, animationProgress)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("gym_3d_canvas")
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onResetCamera() }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    // Rotate on drag
                    onRotate(pan.x * 0.45f, -pan.y * 0.45f)
                    // Zoom
                    if (gestureZoom != 1.0f) {
                        onZoomChange((zoom * gestureZoom).coerceIn(0.55f, 2.8f))
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val baseScale = (width.coerceAtMost(height) * 0.46f) * zoom

        // 1. Draw 3D Ground Grid for spatial depth
        drawGroundGrid(yaw, pitch, width, height, baseScale)

        // 2. Transform all vertices by camera rotation (Yaw, Pitch)
        val transformedVertices = geometry.vertices.map { v ->
            v.rotateY(yaw).rotateX(pitch)
        }

        // Project all vertices to screen 2D points
        val projectedPoints = transformedVertices.map { v ->
            v.project(screenWidth = width, screenHeight = height, cameraDistance = 900f, scale = baseScale)
        }

        // 3. Prepare and sort faces by depth (Painter's Algorithm)
        val projectedFaces = geometry.faces.mapNotNull { face ->
            if (face.indices.any { it >= projectedPoints.size }) return@mapNotNull null
            val pts = face.indices.map { projectedPoints[it].screenPos }
            val avgDepth = face.indices.map { projectedPoints[it].depthZ }.average().toFloat()

            // Back-face culling for triangles/quads: calculate signed 2D area
            val isFrontFacing = if (pts.size >= 3) {
                val p0 = pts[0]
                val p1 = pts[1]
                val p2 = pts[2]
                ((p1.x - p0.x) * (p2.y - p0.y) - (p1.y - p0.y) * (p2.x - p0.x)) < 0
            } else true

            ProjectedFace(
                points = pts,
                avgDepth = avgDepth,
                color = if (renderMode == Render3DMode.WIREFRAME) {
                    Color.Transparent
                } else if (renderMode == Render3DMode.ANATOMY) {
                    face.baseColor.copy(alpha = 0.25f)
                } else {
                    face.baseColor
                },
                wireColor = if (renderMode == Render3DMode.WIREFRAME) {
                    NeonCyan.copy(alpha = 0.85f)
                } else {
                    face.wireColor
                }
            )
        }.sortedByDescending { it.avgDepth }

        // Draw sorted faces
        for (face in projectedFaces) {
            if (face.points.size >= 3) {
                val path = Path().apply {
                    moveTo(face.points[0].x, face.points[0].y)
                    for (i in 1 until face.points.size) {
                        lineTo(face.points[i].x, face.points[i].y)
                    }
                    close()
                }

                // Fill face
                if (face.color != Color.Transparent) {
                    drawPath(path, color = face.color, style = Fill)
                }

                // Stroke wire edges
                val strokeColor = face.wireColor
                if (strokeColor.alpha > 0.05f) {
                    drawPath(path, color = strokeColor, style = Stroke(width = if (renderMode == Render3DMode.WIREFRAME) 1.8f else 1.0f))
                }
            }
        }

        // 4. Draw Lines (Cables, shafts, chains, lifter links)
        val projectedLines = geometry.lines.mapNotNull { line ->
            if (line.startIndex >= projectedPoints.size || line.endIndex >= projectedPoints.size) return@mapNotNull null
            val p1 = projectedPoints[line.startIndex]
            val p2 = projectedPoints[line.endIndex]
            val avgDepth = (p1.depthZ + p2.depthZ) / 2f
            ProjectedLine(
                start = p1.screenPos,
                end = p2.screenPos,
                avgDepth = avgDepth,
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

        // 5. Draw interactive Pin Indicators / Adjustment Points
        for (pin in geometry.pinIndicators) {
            val transformedPin = pin.position.rotateY(yaw).rotateX(pitch)
            val projectedPin = transformedPin.project(width, height, cameraDistance = 900f, scale = baseScale)
            val isSelected = pin.id == selectedPinId

            drawPinMarker(
                pos = projectedPin.screenPos,
                label = pin.label,
                isSelected = isSelected
            )
        }
    }
}

private fun DrawScope.drawGroundGrid(
    yaw: Float,
    pitch: Float,
    width: Float,
    height: Float,
    scale: Float
) {
    val groundY = -0.92f
    val gridSize = 1.4f
    val step = 0.35f

    // Draw concentric ring or isometric grid on ground
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

    // Origin circular pedestal
    val originCenter = Vector3(0f, groundY, 0f).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)
    drawCircle(
        color = ElectricLime.copy(alpha = 0.15f),
        radius = scale * 0.85f * (900f / originCenter.depthZ),
        center = originCenter.screenPos,
        style = Stroke(width = 1.5f)
    )
}

private fun DrawScope.drawPinMarker(
    pos: Offset,
    label: String,
    isSelected: Boolean
) {
    val outerRadius = if (isSelected) 18f else 12f
    val innerRadius = if (isSelected) 8f else 5f
    val color = if (isSelected) BrightAmber else ElectricLime

    // Pulsing outer halo
    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = outerRadius * 1.6f,
        center = pos
    )
    // Outer ring
    drawCircle(
        color = color,
        radius = outerRadius,
        center = pos,
        style = Stroke(width = 2.5f)
    )
    // Solid core
    drawCircle(
        color = Color(0xFF0F1115),
        radius = outerRadius - 2f,
        center = pos
    )
    drawCircle(
        color = color,
        radius = innerRadius,
        center = pos
    )
}
