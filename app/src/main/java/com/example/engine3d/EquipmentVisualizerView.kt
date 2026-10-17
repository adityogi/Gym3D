package com.example.engine3d

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Custom Android View component capable of loading and rotating 3D gym equipment models.
 * Implements full 360-degree rotational gesture handling (continuous drag, fling momentum,
 * pinch-to-zoom, double-tap reset, and pin selection).
 */
class EquipmentVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 3D Model State
    var currentModelId: String = "bench_press"
        private set
    var currentProgress: Float = 0.0f
        private set
    private var geometry: Geometry3DResult = Model3DLoader.loadModel(currentModelId, currentProgress)

    // Camera & 360 Rotation State
    var yaw: Float = 35f
        set(value) {
            field = ((value % 360f) + 360f) % 360f // Always normalize to [0, 360)
            invalidate()
            notifyCameraChanged()
        }

    var pitch: Float = 14f
        set(value) {
            field = value.coerceIn(-80f, 80f)
            invalidate()
            notifyCameraChanged()
        }

    var zoom: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.45f, 3.0f)
            invalidate()
            notifyCameraChanged()
        }

    var renderMode: Render3DMode = Render3DMode.SOLID
        set(value) {
            field = value
            invalidate()
        }

    var selectedPinId: String? = null
        set(value) {
            field = value
            invalidate()
        }

    var isTurntableEnabled: Boolean = false
        set(value) {
            field = value
            if (value) postInvalidateOnAnimation()
        }

    // Default Camera Presets
    var defaultYaw: Float = 35f
    var defaultPitch: Float = 14f
    var defaultZoom: Float = 1.0f

    // Flung Rotational Momentum / Inertia
    private var flingVelocityYaw: Float = 0f
    private var flingVelocityPitch: Float = 0f
    private val friction = 0.93f
    private val minVelocity = 0.15f

    // Listeners
    var onCameraChangedListener: ((yaw: Float, pitch: Float, zoom: Float) -> Unit)? = null
    var onPinSelectedListener: ((pinId: String?, label: String?) -> Unit)? = null

    // Cache projected pin markers for hit testing
    private val cachedPinScreenPositions = mutableListOf<Triple<String, String, Offset>>()

    // Paints
    private val faceFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val wireStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = AndroidColor.argb(35, 255, 255, 255)
        strokeWidth = 1.5f
    }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = AndroidColor.argb(45, 204, 255, 0)
        strokeWidth = 2f
    }
    private val hudTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(220, 255, 255, 255)
        textSize = 28f
    }
    private val accentTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(204, 255, 0)
        textSize = 30f
        isFakeBoldText = true
    }

    // Gesture Detectors
    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            // Stop any ongoing inertia
            flingVelocityYaw = 0f
            flingVelocityPitch = 0f
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Horizontal drag rotates Yaw 360-degrees smoothly
            val deltaYaw = -distanceX * 0.42f
            // Vertical drag tilts Pitch
            val deltaPitch = distanceY * 0.42f

            yaw = ((yaw + deltaYaw) % 360f + 360f) % 360f
            pitch = (pitch + deltaPitch).coerceIn(-80f, 80f)
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // Launch rotational inertia
            flingVelocityYaw = -velocityX * 0.015f
            flingVelocityPitch = velocityY * 0.012f
            postInvalidateOnAnimation()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            animateCameraTo(defaultYaw, defaultPitch, defaultZoom)
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val tapX = e.x
            val tapY = e.y
            val hitRadius = 48f

            var tappedPin: Triple<String, String, Offset>? = null
            for (pin in cachedPinScreenPositions) {
                val dist = hypot((pin.third.x - tapX).toDouble(), (pin.third.y - tapY).toDouble()).toFloat()
                if (dist <= hitRadius) {
                    tappedPin = pin
                    break
                }
            }

            if (tappedPin != null) {
                selectedPinId = if (selectedPinId == tappedPin.first) null else tappedPin.first
                onPinSelectedListener?.invoke(selectedPinId, if (selectedPinId != null) tappedPin.second else null)
            } else {
                if (selectedPinId != null) {
                    selectedPinId = null
                    onPinSelectedListener?.invoke(null, null)
                }
            }
            return true
        }
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            zoom = (zoom * detector.scaleFactor).coerceIn(0.45f, 3.0f)
            return true
        }
    }

    private val gestureDetector = GestureDetector(context, gestureListener)
    private val scaleGestureDetector = ScaleGestureDetector(context, scaleListener)

    init {
        // Enable touch and hardware acceleration
        isClickable = true
        isFocusable = true
    }

    /**
     * Loads a built-in 3D model by ID.
     */
    fun loadModel(modelId: String, progress: Float = currentProgress) {
        currentModelId = modelId
        currentProgress = progress
        val meta = Model3DLoader.getModelMeta(modelId)
        defaultYaw = meta.defaultYaw
        defaultPitch = meta.defaultPitch
        defaultZoom = meta.defaultZoom
        geometry = Model3DLoader.loadModel(modelId, progress)
        selectedPinId = null
        invalidate()
    }

    /**
     * Loads a custom Geometry3DResult directly into the visualizer.
     */
    fun loadGeometry(newGeometry: Geometry3DResult) {
        geometry = newGeometry
        selectedPinId = null
        invalidate()
    }

    /**
     * Loads a Wavefront .OBJ 3D model string.
     */
    fun loadObj(objData: String) {
        geometry = Model3DLoader.loadObjString(objData)
        selectedPinId = null
        invalidate()
    }

    /**
     * Updates kinematics / animation progress of current model [0.0f..1.0f].
     */
    fun setProgress(progress: Float) {
        currentProgress = progress.coerceIn(0f, 1f)
        geometry = Model3DLoader.loadModel(currentModelId, currentProgress)
        invalidate()
    }

    /**
     * Animate camera smoothly to target Yaw, Pitch, and Zoom.
     */
    fun animateCameraTo(targetYaw: Float, targetPitch: Float, targetZoom: Float = zoom) {
        val startYaw = yaw
        val startPitch = pitch
        val startZoom = zoom

        // Calculate shortest arc for yaw
        var diffYaw = (targetYaw - startYaw) % 360f
        if (diffYaw > 180f) diffYaw -= 360f
        if (diffYaw < -180f) diffYaw += 360f

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val frac = animator.animatedValue as Float
                yaw = ((startYaw + diffYaw * frac) % 360f + 360f) % 360f
                pitch = startPitch + (targetPitch - startPitch) * frac
                zoom = startZoom + (targetZoom - startZoom) * frac
            }
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = scaleGestureDetector.onTouchEvent(event)
        handled = gestureDetector.onTouchEvent(event) || handled

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (abs(flingVelocityYaw) > minVelocity || abs(flingVelocityPitch) > minVelocity) {
                postInvalidateOnAnimation()
            }
        }
        return handled || super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Handle active inertia or turntable continuous auto-rotation
        if (isTurntableEnabled) {
            yaw = ((yaw + 0.35f) % 360f + 360f) % 360f
            postInvalidateOnAnimation()
        } else if (abs(flingVelocityYaw) > minVelocity || abs(flingVelocityPitch) > minVelocity) {
            yaw = ((yaw + flingVelocityYaw) % 360f + 360f) % 360f
            pitch = (pitch + flingVelocityPitch).coerceIn(-80f, 80f)

            flingVelocityYaw *= friction
            flingVelocityPitch *= friction

            if (abs(flingVelocityYaw) > minVelocity || abs(flingVelocityPitch) > minVelocity) {
                postInvalidateOnAnimation()
            } else {
                flingVelocityYaw = 0f
                flingVelocityPitch = 0f
            }
        }

        val width = width.toFloat()
        val height = height.toFloat()
        if (width <= 0f || height <= 0f) return

        val baseScale = (minOf(width, height) * 0.46f) * zoom

        // 1. Draw 3D Ground Grid
        drawGroundGrid(canvas, width, height, baseScale)

        // 2. Transform all vertices
        val transformedVertices = geometry.vertices.map { v ->
            v.rotateY(yaw).rotateX(pitch)
        }

        // Project 3D vertices to 2D screen positions
        val projectedPoints = transformedVertices.map { v ->
            v.project(screenWidth = width, screenHeight = height, cameraDistance = 900f, scale = baseScale)
        }

        // 3. Depth-sort and render faces
        val facePath = AndroidPath()
        val sortedFaces = geometry.faces.mapNotNull { face ->
            if (face.indices.any { it >= projectedPoints.size }) return@mapNotNull null
            val pts = face.indices.map { projectedPoints[it].screenPos }
            val avgDepth = face.indices.map { projectedPoints[it].depthZ }.average().toFloat()

            // Back-face culling for triangles & quads
            val isFrontFacing = if (pts.size >= 3) {
                val p0 = pts[0]; val p1 = pts[1]; val p2 = pts[2]
                ((p1.x - p0.x) * (p2.y - p0.y) - (p1.y - p0.y) * (p2.x - p0.x)) < 0
            } else true

            if (!isFrontFacing && renderMode == Render3DMode.SOLID) return@mapNotNull null

            Triple(face, pts, avgDepth)
        }.sortedByDescending { it.third }

        for ((face, pts, _) in sortedFaces) {
            if (pts.size < 3) continue
            facePath.reset()
            facePath.moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) {
                facePath.lineTo(pts[i].x, pts[i].y)
            }
            facePath.close()

            if (renderMode != Render3DMode.WIREFRAME) {
                val fillColor = when (renderMode) {
                    Render3DMode.ANATOMY -> face.baseColor.copy(alpha = 0.22f).toArgb()
                    else -> face.baseColor.toArgb()
                }
                faceFillPaint.color = fillColor
                canvas.drawPath(facePath, faceFillPaint)
            }

            // Wire edges
            val wireColor = when (renderMode) {
                Render3DMode.WIREFRAME -> AndroidColor.rgb(0, 229, 255)
                else -> face.wireColor.toArgb()
            }
            wireStrokePaint.color = wireColor
            wireStrokePaint.strokeWidth = if (renderMode == Render3DMode.WIREFRAME) 2.2f else 1.0f
            canvas.drawPath(facePath, wireStrokePaint)
        }

        // 4. Render lines (cables, bars, linkage)
        val sortedLines = geometry.lines.mapNotNull { line ->
            if (line.startIndex >= projectedPoints.size || line.endIndex >= projectedPoints.size) return@mapNotNull null
            val p1 = projectedPoints[line.startIndex]
            val p2 = projectedPoints[line.endIndex]
            val avgDepth = (p1.depthZ + p2.depthZ) / 2f
            Triple(line, p1.screenPos to p2.screenPos, avgDepth)
        }.sortedByDescending { it.third }

        for ((line, pts, _) in sortedLines) {
            linePaint.color = line.color.toArgb()
            linePaint.strokeWidth = line.strokeWidth
            canvas.drawLine(pts.first.x, pts.first.y, pts.second.x, pts.second.y, linePaint)
        }

        // 5. Render interactive Pin Markers & update hit-test cache
        cachedPinScreenPositions.clear()
        for (pin in geometry.pinIndicators) {
            val transformedPin = pin.position.rotateY(yaw).rotateX(pitch)
            val projectedPin = transformedPin.project(width, height, cameraDistance = 900f, scale = baseScale)
            val isSelected = pin.id == selectedPinId
            cachedPinScreenPositions.add(Triple(pin.id, pin.label, projectedPin.screenPos))

            drawPinMarker(canvas, projectedPin.screenPos, isSelected)
        }

        // 6. Draw 360-degree HUD & Mini Compass
        draw360Hud(canvas, width, height)
    }

    private fun drawGroundGrid(canvas: Canvas, width: Float, height: Float, scale: Float) {
        val groundY = -0.92f
        val gridSize = 1.4f
        val step = 0.35f

        var x = -gridSize
        while (x <= gridSize + 0.001f) {
            val p1 = Vector3(x, groundY, -gridSize).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)
            val p2 = Vector3(x, groundY, gridSize).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)
            val p3 = Vector3(-gridSize, groundY, x).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)
            val p4 = Vector3(gridSize, groundY, x).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)

            canvas.drawLine(p1.screenPos.x, p1.screenPos.y, p2.screenPos.x, p2.screenPos.y, gridPaint)
            canvas.drawLine(p3.screenPos.x, p3.screenPos.y, p4.screenPos.x, p4.screenPos.y, gridPaint)
            x += step
        }

        val originCenter = Vector3(0f, groundY, 0f).rotateY(yaw).rotateX(pitch).project(width, height, 900f, scale)
        val ringRadius = scale * 0.85f * (900f / originCenter.depthZ)
        canvas.drawCircle(originCenter.screenPos.x, originCenter.screenPos.y, ringRadius, ringPaint)
    }

    private fun drawPinMarker(canvas: Canvas, pos: Offset, isSelected: Boolean) {
        val outerRadius = if (isSelected) 22f else 14f
        val innerRadius = if (isSelected) 10f else 6f
        val colorInt = if (isSelected) AndroidColor.rgb(255, 179, 0) else AndroidColor.rgb(204, 255, 0)

        // Pulsing glow halo
        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(70, AndroidColor.red(colorInt), AndroidColor.green(colorInt), AndroidColor.blue(colorInt))
            style = Paint.Style.FILL
        }
        canvas.drawCircle(pos.x, pos.y, outerRadius * 1.5f, haloPaint)

        // Ring
        val pinRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(pos.x, pos.y, outerRadius, pinRingPaint)

        // Inner solid core
        val pinCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.FILL
        }
        canvas.drawCircle(pos.x, pos.y, innerRadius, pinCorePaint)
    }

    private fun draw360Hud(canvas: Canvas, width: Float, height: Float) {
        // Mini 360-Degree Compass / Orientation Gizmo in Top-Right
        val compassCenterX = width - 64f
        val compassCenterY = 64f
        val compassRadius = 28f

        val compassRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.argb(80, 204, 255, 0)
            strokeWidth = 2f
        }
        canvas.drawCircle(compassCenterX, compassCenterY, compassRadius, compassRingPaint)

        // Draw heading needle according to Yaw
        val rad = Math.toRadians((yaw - 90.0).toDouble())
        val needleX = compassCenterX + (compassRadius - 6f) * kotlin.math.cos(rad).toFloat()
        val needleY = compassCenterY + (compassRadius - 6f) * kotlin.math.sin(rad).toFloat()

        val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.rgb(204, 255, 0)
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(compassCenterX, compassCenterY, needleX, needleY, needlePaint)

        // Draw North pip
        val northPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 75, 75)
            textSize = 18f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("N", compassCenterX, compassCenterY - compassRadius - 6f, northPaint)

        // Telemetry readout
        val hudYawText = "360° ORBIT: ${yaw.toInt()}°"
        val hudPitchText = "PITCH: ${pitch.toInt()}° | ZOOM: ${(zoom * 100).toInt()}%"

        canvas.drawText(hudYawText, 30f, 50f, accentTextPaint)
        canvas.drawText(hudPitchText, 30f, 85f, hudTextPaint)
    }

    private fun notifyCameraChanged() {
        onCameraChangedListener?.invoke(yaw, pitch, zoom)
    }
}
