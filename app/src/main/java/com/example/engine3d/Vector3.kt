package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

data class Vector3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)

    fun rotateX(deg: Float): Vector3 {
        val rad = Math.toRadians(deg.toDouble()).toFloat()
        val c = cos(rad)
        val s = sin(rad)
        return Vector3(x, y * c - z * s, y * s + z * c)
    }

    fun rotateY(deg: Float): Vector3 {
        val rad = Math.toRadians(deg.toDouble()).toFloat()
        val c = cos(rad)
        val s = sin(rad)
        return Vector3(x * c + z * s, y, -x * s + z * c)
    }

    fun rotateZ(deg: Float): Vector3 {
        val rad = Math.toRadians(deg.toDouble()).toFloat()
        val c = cos(rad)
        val s = sin(rad)
        return Vector3(x * c - y * s, x * s + y * c, z)
    }

    fun project(
        screenWidth: Float,
        screenHeight: Float,
        cameraDistance: Float = 600f,
        scale: Float = 250f,
        panX: Float = 0f,
        panY: Float = 0f
    ): ProjectedPoint {
        val effectiveZ = z + cameraDistance
        val clampedZ = if (effectiveZ <= 10f) 10f else effectiveZ
        val perspective = cameraDistance / clampedZ

        val screenX = screenWidth / 2f + panX + (x * scale * perspective)
        val screenY = screenHeight / 2f + panY - (y * scale * perspective) // Invert Y for screen coords

        return ProjectedPoint(
            screenPos = Offset(screenX, screenY),
            depthZ = effectiveZ
        )
    }
}

data class ProjectedPoint(
    val screenPos: Offset,
    val depthZ: Float
)

data class MeshFace(
    val indices: List<Int>,
    val baseColor: Color,
    val wireColor: Color = Color(0x55FFFFFF)
)

data class MeshLine(
    val startIndex: Int,
    val endIndex: Int,
    val color: Color,
    val strokeWidth: Float = 2.5f
)

data class ProjectedFace(
    val points: List<Offset>,
    val avgDepth: Float,
    val color: Color,
    val wireColor: Color
)

data class ProjectedLine(
    val start: Offset,
    val end: Offset,
    val avgDepth: Float,
    val color: Color,
    val strokeWidth: Float
)
