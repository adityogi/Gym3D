package com.example.engine3d

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Geometry3DResult(
    val vertices: List<Vector3>,
    val faces: List<MeshFace>,
    val lines: List<MeshLine>,
    val pinIndicators: List<Pin3DMarker> = emptyList()
)

data class Pin3DMarker(
    val id: String,
    val position: Vector3,
    val label: String
)

object EquipmentGeometries {

    // Helper to generate a 3D box (6 faces, 8 vertices)
    private fun addBox(
        vertices: MutableList<Vector3>,
        faces: MutableList<MeshFace>,
        lines: MutableList<MeshLine>,
        min: Vector3,
        max: Vector3,
        color: Color,
        wireColor: Color = Color(0x33FFFFFF)
    ) {
        val baseIndex = vertices.size
        // 8 corners
        vertices.add(Vector3(min.x, min.y, min.z)) // 0: bottom-left-front
        vertices.add(Vector3(max.x, min.y, min.z)) // 1: bottom-right-front
        vertices.add(Vector3(max.x, max.y, min.z)) // 2: top-right-front
        vertices.add(Vector3(min.x, max.y, min.z)) // 3: top-left-front
        vertices.add(Vector3(min.x, min.y, max.z)) // 4: bottom-left-back
        vertices.add(Vector3(max.x, min.y, max.z)) // 5: bottom-right-back
        vertices.add(Vector3(max.x, max.y, max.z)) // 6: top-right-back
        vertices.add(Vector3(min.x, max.y, max.z)) // 7: top-left-back

        // 6 faces (subtle shading variations for depth perception)
        val topColor = color.copy(alpha = 0.95f)
        val frontColor = color.copy(red = color.red * 0.85f, green = color.green * 0.85f, blue = color.blue * 0.85f)
        val sideColor = color.copy(red = color.red * 0.70f, green = color.green * 0.70f, blue = color.blue * 0.70f)
        val bottomColor = color.copy(red = color.red * 0.50f, green = color.green * 0.50f, blue = color.blue * 0.50f)

        // Front
        faces.add(MeshFace(listOf(baseIndex + 0, baseIndex + 1, baseIndex + 2, baseIndex + 3), frontColor, wireColor))
        // Back
        faces.add(MeshFace(listOf(baseIndex + 5, baseIndex + 4, baseIndex + 7, baseIndex + 6), frontColor, wireColor))
        // Top
        faces.add(MeshFace(listOf(baseIndex + 3, baseIndex + 2, baseIndex + 6, baseIndex + 7), topColor, wireColor))
        // Bottom
        faces.add(MeshFace(listOf(baseIndex + 4, baseIndex + 5, baseIndex + 1, baseIndex + 0), bottomColor, wireColor))
        // Left
        faces.add(MeshFace(listOf(baseIndex + 4, baseIndex + 0, baseIndex + 3, baseIndex + 7), sideColor, wireColor))
        // Right
        faces.add(MeshFace(listOf(baseIndex + 1, baseIndex + 5, baseIndex + 6, baseIndex + 2), sideColor, wireColor))

        // Outline wire edges
        lines.add(MeshLine(baseIndex + 0, baseIndex + 1, wireColor))
        lines.add(MeshLine(baseIndex + 1, baseIndex + 2, wireColor))
        lines.add(MeshLine(baseIndex + 2, baseIndex + 3, wireColor))
        lines.add(MeshLine(baseIndex + 3, baseIndex + 0, wireColor))
        lines.add(MeshLine(baseIndex + 4, baseIndex + 5, wireColor))
        lines.add(MeshLine(baseIndex + 5, baseIndex + 6, wireColor))
        lines.add(MeshLine(baseIndex + 6, baseIndex + 7, wireColor))
        lines.add(MeshLine(baseIndex + 7, baseIndex + 4, wireColor))
        lines.add(MeshLine(baseIndex + 0, baseIndex + 4, wireColor))
        lines.add(MeshLine(baseIndex + 1, baseIndex + 5, wireColor))
        lines.add(MeshLine(baseIndex + 2, baseIndex + 6, wireColor))
        lines.add(MeshLine(baseIndex + 3, baseIndex + 7, wireColor))
    }

    // Helper for polygonal cylinder/disc (e.g. weight plates, flywheels)
    private fun addCylinder(
        vertices: MutableList<Vector3>,
        faces: MutableList<MeshFace>,
        lines: MutableList<MeshLine>,
        center: Vector3,
        radius: Float,
        thickness: Float,
        axis: Char, // 'X', 'Y', 'Z'
        color: Color,
        segments: Int = 12
    ) {
        val baseIndex = vertices.size
        for (i in 0 until segments) {
            val angle = (2 * PI * i / segments).toFloat()
            val c = cos(angle) * radius
            val s = sin(angle) * radius
            when (axis) {
                'X' -> {
                    vertices.add(Vector3(center.x - thickness / 2, center.y + c, center.z + s))
                    vertices.add(Vector3(center.x + thickness / 2, center.y + c, center.z + s))
                }
                'Y' -> {
                    vertices.add(Vector3(center.x + c, center.y - thickness / 2, center.z + s))
                    vertices.add(Vector3(center.x + c, center.y + thickness / 2, center.z + s))
                }
                else -> {
                    vertices.add(Vector3(center.x + c, center.y + s, center.z - thickness / 2))
                    vertices.add(Vector3(center.x + c, center.y + s, center.z + thickness / 2))
                }
            }
        }

        // Side quads
        for (i in 0 until segments) {
            val next = (i + 1) % segments
            val i0 = baseIndex + i * 2
            val i1 = baseIndex + i * 2 + 1
            val next0 = baseIndex + next * 2
            val next1 = baseIndex + next * 2 + 1

            faces.add(MeshFace(listOf(i0, i1, next1, next0), color))
            lines.add(MeshLine(i0, next0, color.copy(alpha = 0.4f)))
            lines.add(MeshLine(i1, next1, color.copy(alpha = 0.4f)))
        }
    }

    // 1. BARBELL BENCH PRESS
    fun buildBenchPress(progress: Float): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()
        val pins = mutableListOf<Pin3DMarker>()

        val steelColor = Color(0xFF282C37)
        val padColor = Color(0xFF1E212A)
        val chromeColor = Color(0xFFCBD5E1)
        val plateColor = Color(0xFF181B20)
        val accentRed = Color(0xFFE53935)
        val pinColor = Color(0xFFFFD600)

        // Bench Legs & Base
        addBox(vertices, faces, lines, Vector3(-0.15f, -0.9f, -0.6f), Vector3(0.15f, -0.8f, 0.7f), steelColor)
        addBox(vertices, faces, lines, Vector3(-0.06f, -0.8f, 0.5f), Vector3(0.06f, -0.35f, 0.6f), steelColor)
        addBox(vertices, faces, lines, Vector3(-0.06f, -0.8f, -0.4f), Vector3(0.06f, -0.35f, -0.3f), steelColor)

        // Bench Padded Cushion
        addBox(vertices, faces, lines, Vector3(-0.22f, -0.35f, -0.6f), Vector3(0.22f, -0.27f, 0.65f), padColor)

        // Upright Vertical Catchers (Left & Right)
        addBox(vertices, faces, lines, Vector3(-0.55f, -0.9f, -0.35f), Vector3(-0.45f, 0.45f, -0.25f), steelColor)
        addBox(vertices, faces, lines, Vector3(0.45f, -0.9f, -0.35f), Vector3(0.55f, 0.45f, -0.25f), steelColor)
        // Upright crossbrace
        addBox(vertices, faces, lines, Vector3(-0.45f, -0.7f, -0.33f), Vector3(0.45f, -0.62f, -0.27f), steelColor)

        // J-Hooks / Catches
        addBox(vertices, faces, lines, Vector3(-0.56f, 0.35f, -0.25f), Vector3(-0.44f, 0.40f, -0.12f), steelColor)
        addBox(vertices, faces, lines, Vector3(0.44f, 0.35f, -0.25f), Vector3(0.56f, 0.40f, -0.12f), steelColor)

        // Safety catch pin marker
        pins.add(Pin3DMarker("safety_pin_l", Vector3(-0.5f, 0.05f, -0.2f), "Safety Notch 4"))
        pins.add(Pin3DMarker("safety_pin_r", Vector3(0.5f, 0.05f, -0.2f), "Safety Notch 4"))

        // Motion interpolation:
        // progress: 0f = lowered to chest, 1f = locked out at top
        // Cycle is sinusoidal: down to chest, pause, press up
        val cycle = (sin((progress * 2 * PI).toDouble() - PI / 2).toFloat() + 1f) / 2f
        val barY = -0.15f + cycle * 0.52f // -0.15f (chest) to 0.37f (extension)
        val barZ = -0.18f + cycle * 0.04f // slight natural bar path arc

        // Barbell Shaft
        val barStartIndex = vertices.size
        vertices.add(Vector3(-1.1f, barY, barZ))
        vertices.add(Vector3(1.1f, barY, barZ))
        lines.add(MeshLine(barStartIndex, barStartIndex + 1, chromeColor, strokeWidth = 8f))

        // Center knurling markings
        vertices.add(Vector3(-0.4f, barY, barZ))
        vertices.add(Vector3(0.4f, barY, barZ))
        lines.add(MeshLine(barStartIndex + 2, barStartIndex + 3, Color(0xFFCCFF00), strokeWidth = 3f))

        // 45lb Olympic Weight Plates on left & right
        addCylinder(vertices, faces, lines, Vector3(-0.85f, barY, barZ), radius = 0.32f, thickness = 0.06f, axis = 'X', color = plateColor)
        addCylinder(vertices, faces, lines, Vector3(-0.93f, barY, barZ), radius = 0.30f, thickness = 0.05f, axis = 'X', color = accentRed)
        addCylinder(vertices, faces, lines, Vector3(0.85f, barY, barZ), radius = 0.32f, thickness = 0.06f, axis = 'X', color = plateColor)
        addCylinder(vertices, faces, lines, Vector3(0.93f, barY, barZ), radius = 0.30f, thickness = 0.05f, axis = 'X', color = accentRed)

        // Collars
        addBox(vertices, faces, lines, Vector3(-0.80f, barY - 0.05f, barZ - 0.05f), Vector3(-0.76f, barY + 0.05f, barZ + 0.05f), pinColor)
        addBox(vertices, faces, lines, Vector3(0.76f, barY - 0.05f, barZ - 0.05f), Vector3(0.80f, barY + 0.05f, barZ + 0.05f), pinColor)

        // Lifter Ghost Torso & Arms
        val torsoY = -0.24f
        val chestVertex = vertices.size
        vertices.add(Vector3(0f, torsoY + 0.05f, -0.15f)) // Chest center
        vertices.add(Vector3(-0.35f, torsoY, -0.15f)) // Left shoulder
        vertices.add(Vector3(0.35f, torsoY, -0.15f)) // Right shoulder

        // Elbow joints moving dynamically
        val elbowY = torsoY - (1f - cycle) * 0.18f
        val elbowX_L = -0.48f - (1f - cycle) * 0.12f
        val elbowX_R = 0.48f + (1f - cycle) * 0.12f
        vertices.add(Vector3(elbowX_L, elbowY, barZ - 0.1f)) // Left elbow
        vertices.add(Vector3(elbowX_R, elbowY, barZ - 0.1f)) // Right elbow

        // Hands on bar
        vertices.add(Vector3(-0.45f, barY, barZ)) // Left hand
        vertices.add(Vector3(0.45f, barY, barZ)) // Right hand

        // Ghost arm lines
        val ghostColor = Color(0x8800E5FF)
        lines.add(MeshLine(chestVertex + 1, chestVertex + 3, ghostColor, 4f)) // Left shoulder to elbow
        lines.add(MeshLine(chestVertex + 3, chestVertex + 5, ghostColor, 4f)) // Left elbow to hand
        lines.add(MeshLine(chestVertex + 2, chestVertex + 4, ghostColor, 4f)) // Right shoulder to elbow
        lines.add(MeshLine(chestVertex + 4, chestVertex + 6, ghostColor, 4f)) // Right elbow to hand

        return Geometry3DResult(vertices, faces, lines, pins)
    }

    // 2. POWER SQUAT RACK
    fun buildSquatRack(progress: Float): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()
        val pins = mutableListOf<Pin3DMarker>()

        val steelColor = Color(0xFF232733)
        val safetyBarColor = Color(0xFFFFB300)
        val chromeBar = Color(0xFFCBD5E1)
        val plateColor = Color(0xFF15171E)
        val plateBlue = Color(0xFF1E88E5)

        // 4 Vertical Cage Pillars
        val w = 0.55f
        val d = 0.55f
        val h = 1.05f
        val pillarW = 0.06f

        // Front Left, Front Right, Back Left, Back Right
        addBox(vertices, faces, lines, Vector3(-w - pillarW, -0.9f, d - pillarW), Vector3(-w + pillarW, h, d + pillarW), steelColor)
        addBox(vertices, faces, lines, Vector3(w - pillarW, -0.9f, d - pillarW), Vector3(w + pillarW, h, d + pillarW), steelColor)
        addBox(vertices, faces, lines, Vector3(-w - pillarW, -0.9f, -d - pillarW), Vector3(-w + pillarW, h, -d + pillarW), steelColor)
        addBox(vertices, faces, lines, Vector3(w - pillarW, -0.9f, -d - pillarW), Vector3(w + pillarW, h, -d + pillarW), steelColor)

        // Base frame stabilization
        addBox(vertices, faces, lines, Vector3(-w, -0.9f, -d), Vector3(w, -0.82f, -d + 0.1f), steelColor)
        addBox(vertices, faces, lines, Vector3(-w - 0.1f, -0.9f, -d - 0.2f), Vector3(-w + 0.1f, -0.82f, d + 0.2f), steelColor)
        addBox(vertices, faces, lines, Vector3(w - 0.1f, -0.9f, -d - 0.2f), Vector3(w + 0.1f, -0.82f, d + 0.2f), steelColor)

        // Top Pull-Up Crossbar
        addBox(vertices, faces, lines, Vector3(-w, h - 0.08f, d - 0.03f), Vector3(w, h, d + 0.03f), steelColor)
        addBox(vertices, faces, lines, Vector3(-w, h - 0.08f, -d - 0.03f), Vector3(w, h, -d + 0.03f), steelColor)

        // Safety Spotter Bars (Adjustable)
        val safetyY = -0.25f
        addBox(vertices, faces, lines, Vector3(-w, safetyY, -d), Vector3(-w + 0.06f, safetyY + 0.06f, d), safetyBarColor)
        addBox(vertices, faces, lines, Vector3(w - 0.06f, safetyY, -d), Vector3(w, safetyY + 0.06f, d), safetyBarColor)

        pins.add(Pin3DMarker("safety_spotter", Vector3(-w, safetyY + 0.03f, 0f), "Safety Pin Hole #11"))

        // Squat Motion:
        // progress: 0f = standing tall, 0.5f = deep parallel squat, 1f = standing
        val squatDepth = (sin((progress * 2 * PI).toDouble() - PI / 2).toFloat() + 1f) / 2f
        val barY = 0.55f - squatDepth * 0.48f
        val barZ = -0.05f + squatDepth * 0.05f

        // Barbell
        val barIdx = vertices.size
        vertices.add(Vector3(-1.15f, barY, barZ))
        vertices.add(Vector3(1.15f, barY, barZ))
        lines.add(MeshLine(barIdx, barIdx + 1, chromeBar, 7f))

        // Weight Plates (Olympic 45lb / 20kg Blue + Black)
        addCylinder(vertices, faces, lines, Vector3(-0.90f, barY, barZ), radius = 0.35f, thickness = 0.06f, axis = 'X', color = plateBlue)
        addCylinder(vertices, faces, lines, Vector3(-0.98f, barY, barZ), radius = 0.35f, thickness = 0.06f, axis = 'X', color = plateColor)
        addCylinder(vertices, faces, lines, Vector3(0.90f, barY, barZ), radius = 0.35f, thickness = 0.06f, axis = 'X', color = plateBlue)
        addCylinder(vertices, faces, lines, Vector3(0.98f, barY, barZ), radius = 0.35f, thickness = 0.06f, axis = 'X', color = plateColor)

        // Ghost Lifter Skeleton (Hips, Knees, Feet)
        val hipY = 0.05f - squatDepth * 0.45f
        val hipZ = barZ - 0.18f - squatDepth * 0.22f // Hips sit back in hinge
        val kneeY = -0.40f - squatDepth * 0.15f
        val kneeZ = 0.12f // Knees travel slightly forward

        val ghostIdx = vertices.size
        vertices.add(Vector3(0f, barY - 0.1f, barZ)) // Upper back
        vertices.add(Vector3(0f, hipY, hipZ)) // Pelvis / Hips
        vertices.add(Vector3(-0.22f, kneeY, kneeZ)) // Left Knee
        vertices.add(Vector3(0.22f, kneeY, kneeZ)) // Right Knee
        vertices.add(Vector3(-0.25f, -0.85f, 0.05f)) // Left Foot
        vertices.add(Vector3(0.25f, -0.85f, 0.05f)) // Right Foot

        val ghostColor = Color(0xAA00E5FF)
        lines.add(MeshLine(ghostIdx + 0, ghostIdx + 1, ghostColor, 5f)) // Spine
        lines.add(MeshLine(ghostIdx + 1, ghostIdx + 2, ghostColor, 5f)) // Left Femur
        lines.add(MeshLine(ghostIdx + 1, ghostIdx + 3, ghostColor, 5f)) // Right Femur
        lines.add(MeshLine(ghostIdx + 2, ghostIdx + 4, ghostColor, 5f)) // Left Tibia
        lines.add(MeshLine(ghostIdx + 3, ghostIdx + 5, ghostColor, 5f)) // Right Tibia

        return Geometry3DResult(vertices, faces, lines, pins)
    }

    // 3. DUMBBELL INCLINE BENCH
    fun buildDumbbellBench(progress: Float): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()
        val pins = mutableListOf<Pin3DMarker>()

        val steelColor = Color(0xFF282D39)
        val padColor = Color(0xFF1B1E26)
        val dumbbellColor = Color(0xFF15181F)
        val pinColor = Color(0xFFFFD600)

        // Main Base Spine
        addBox(vertices, faces, lines, Vector3(-0.15f, -0.9f, -0.6f), Vector3(0.15f, -0.82f, 0.6f), steelColor)
        addBox(vertices, faces, lines, Vector3(-0.06f, -0.82f, 0.35f), Vector3(0.06f, -0.38f, 0.45f), steelColor)
        addBox(vertices, faces, lines, Vector3(-0.06f, -0.82f, -0.4f), Vector3(0.06f, -0.45f, -0.3f), steelColor)

        // Seat Pad (flat / slightly inclined)
        addBox(vertices, faces, lines, Vector3(-0.2f, -0.38f, 0.2f), Vector3(0.2f, -0.30f, 0.55f), padColor)

        // Incline Backrest Pad (30-45 degree incline)
        val inclineStart = Vector3(-0.2f, -0.30f, 0.15f)
        val inclineEnd = Vector3(0.2f, 0.25f, -0.45f)
        addBox(vertices, faces, lines, inclineStart, inclineEnd, padColor)

        // Incline adjustment pin selector
        pins.add(Pin3DMarker("incline_pin", Vector3(0f, -0.55f, -0.15f), "Angle Pin (30° / 45°)"))

        // Dumbbells motion cycle
        // progress: 0f = lowered to shoulders, 1f = pressed up overhead
        val pressCycle = (sin((progress * 2 * PI).toDouble() - PI / 2).toFloat() + 1f) / 2f
        val dbY = -0.05f + pressCycle * 0.58f
        val dbZ = -0.10f - pressCycle * 0.12f
        val dbXLeft = -0.52f + pressCycle * 0.25f // Converges slightly at top
        val dbXRight = 0.52f - pressCycle * 0.25f

        // Left Dumbbell (Handle + Hexagonal heads)
        val dbLineIdx = vertices.size
        vertices.add(Vector3(dbXLeft, dbY, dbZ - 0.14f))
        vertices.add(Vector3(dbXLeft, dbY, dbZ + 0.14f))
        lines.add(MeshLine(dbLineIdx, dbLineIdx + 1, Color(0xFFCBD5E1), 5f))
        addBox(vertices, faces, lines, Vector3(dbXLeft - 0.08f, dbY - 0.08f, dbZ - 0.22f), Vector3(dbXLeft + 0.08f, dbY + 0.08f, dbZ - 0.13f), dumbbellColor)
        addBox(vertices, faces, lines, Vector3(dbXLeft - 0.08f, dbY - 0.08f, dbZ + 0.13f), Vector3(dbXLeft + 0.08f, dbY + 0.08f, dbZ + 0.22f), dumbbellColor)

        // Right Dumbbell
        val dbRIdx = vertices.size
        vertices.add(Vector3(dbXRight, dbY, dbZ - 0.14f))
        vertices.add(Vector3(dbXRight, dbY, dbZ + 0.14f))
        lines.add(MeshLine(dbRIdx, dbRIdx + 1, Color(0xFFCBD5E1), 5f))
        addBox(vertices, faces, lines, Vector3(dbXRight - 0.08f, dbY - 0.08f, dbZ - 0.22f), Vector3(dbXRight + 0.08f, dbY + 0.08f, dbZ - 0.13f), dumbbellColor)
        addBox(vertices, faces, lines, Vector3(dbXRight - 0.08f, dbY - 0.08f, dbZ + 0.13f), Vector3(dbXRight + 0.08f, dbY + 0.08f, dbZ + 0.22f), dumbbellColor)

        return Geometry3DResult(vertices, faces, lines, pins)
    }

    // 4. CABLE LAT PULLDOWN
    fun buildLatPulldown(progress: Float): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()
        val pins = mutableListOf<Pin3DMarker>()

        val steelColor = Color(0xFF242834)
        val padColor = Color(0xFF191C24)
        val chromeBar = Color(0xFFCBD5E1)
        val cableColor = Color(0xFFE2E8F0)
        val weightColor = Color(0xFF15171E)
        val pinColor = Color(0xFFFFD600)

        // Tower Frame
        addBox(vertices, faces, lines, Vector3(-0.45f, -0.9f, -0.45f), Vector3(0.45f, -0.82f, 0.55f), steelColor)
        // Upright Guide Columns (Dual)
        addBox(vertices, faces, lines, Vector3(-0.16f, -0.82f, -0.3f), Vector3(-0.12f, 1.05f, -0.26f), steelColor)
        addBox(vertices, faces, lines, Vector3(0.12f, -0.82f, -0.3f), Vector3(0.16f, 1.05f, -0.26f), steelColor)

        // Overhead High Pulley Cantilever Arm
        addBox(vertices, faces, lines, Vector3(-0.08f, 0.95f, -0.35f), Vector3(0.08f, 1.05f, 0.45f), steelColor)

        // Seat & Thigh Roller Pads
        addBox(vertices, faces, lines, Vector3(-0.18f, -0.45f, -0.05f), Vector3(0.18f, -0.38f, 0.35f), padColor)
        addBox(vertices, faces, lines, Vector3(-0.25f, -0.15f, 0.15f), Vector3(0.25f, -0.08f, 0.25f), padColor)
        pins.add(Pin3DMarker("thigh_pin", Vector3(0.26f, -0.12f, 0.2f), "Thigh Pad Pin (Snug)"))

        // Pulley Wheel at front tip
        addCylinder(vertices, faces, lines, Vector3(0f, 1.0f, 0.42f), radius = 0.08f, thickness = 0.03f, axis = 'X', color = steelColor)

        // Exercise cycle:
        // progress: 0f = bar overhead (arms extended), 1f = pulled to clavicle
        val pullCycle = (sin((progress * 2 * PI).toDouble() - PI / 2).toFloat() + 1f) / 2f
        val barY = 0.85f - pullCycle * 0.55f // 0.85f down to 0.30f
        val barZ = 0.42f

        // Weight Stack Motion (Rises as bar is pulled down)
        val stackHeight = 0.45f
        val stackBottomY = -0.75f + pullCycle * 0.35f
        addBox(vertices, faces, lines, Vector3(-0.22f, stackBottomY, -0.33f), Vector3(0.22f, stackBottomY + stackHeight, -0.23f), weightColor)
        pins.add(Pin3DMarker("weight_pin", Vector3(0f, stackBottomY + 0.25f, -0.21f), "Weight Selector Pin: 60 kg"))

        // Cable Line: From Weight Stack top -> Up to overhead pulley -> Down to Lat Bar
        val cableIdx = vertices.size
        vertices.add(Vector3(0f, stackBottomY + stackHeight, -0.28f)) // Top of weight stack
        vertices.add(Vector3(0f, 1.02f, -0.28f)) // Top rear pulley
        vertices.add(Vector3(0f, 1.02f, 0.42f)) // Top front pulley
        vertices.add(Vector3(0f, barY, barZ)) // Attachment to bar
        lines.add(MeshLine(cableIdx, cableIdx + 1, cableColor, 3f))
        lines.add(MeshLine(cableIdx + 1, cableIdx + 2, cableColor, 3f))
        lines.add(MeshLine(cableIdx + 2, cableIdx + 3, cableColor, 3f))

        // Wide Grip Lat Bar (angled downward slightly at ends)
        val barIdx = vertices.size
        vertices.add(Vector3(-0.75f, barY - 0.08f, barZ))
        vertices.add(Vector3(-0.45f, barY, barZ))
        vertices.add(Vector3(0.45f, barY, barZ))
        vertices.add(Vector3(0.75f, barY - 0.08f, barZ))
        lines.add(MeshLine(barIdx, barIdx + 1, chromeBar, 6f))
        lines.add(MeshLine(barIdx + 1, barIdx + 2, chromeBar, 6f))
        lines.add(MeshLine(barIdx + 2, barIdx + 3, chromeBar, 6f))

        return Geometry3DResult(vertices, faces, lines, pins)
    }

    // 5. SEATED CABLE ROW
    fun buildSeatedRow(progress: Float): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()
        val pins = mutableListOf<Pin3DMarker>()

        val steelColor = Color(0xFF252A36)
        val padColor = Color(0xFF1A1C24)
        val cableColor = Color(0xFFE2E8F0)
        val chromeHandle = Color(0xFFCBD5E1)
        val weightColor = Color(0xFF14171E)

        // Horizontal Guide Rail & Seat
        addBox(vertices, faces, lines, Vector3(-0.18f, -0.85f, -0.7f), Vector3(0.18f, -0.75f, 0.7f), steelColor)
        addBox(vertices, faces, lines, Vector3(-0.20f, -0.42f, 0.1f), Vector3(0.20f, -0.34f, 0.5f), padColor) // Bench seat

        // Angled Footplates at front
        addBox(vertices, faces, lines, Vector3(-0.22f, -0.55f, -0.42f), Vector3(-0.06f, -0.35f, -0.35f), steelColor)
        addBox(vertices, faces, lines, Vector3(0.06f, -0.55f, -0.42f), Vector3(0.22f, -0.35f, -0.35f), steelColor)

        // Low Pulley Housing
        addCylinder(vertices, faces, lines, Vector3(0f, -0.42f, -0.52f), radius = 0.08f, thickness = 0.04f, axis = 'X', color = steelColor)

        // Vertical Weight Tower at front
        addBox(vertices, faces, lines, Vector3(-0.35f, -0.85f, -0.85f), Vector3(0.35f, 0.75f, -0.65f), steelColor)

        // Motion cycle:
        // progress: 0f = arms extended forward, 1f = pulled into abdomen
        val rowCycle = (sin((progress * 2 * PI).toDouble() - PI / 2).toFloat() + 1f) / 2f
        val handleZ = -0.25f + rowCycle * 0.42f // pulls back towards seat
        val handleY = -0.38f + rowCycle * 0.06f

        // Weight Stack Motion
        val stackY = -0.7f + rowCycle * 0.28f
        addBox(vertices, faces, lines, Vector3(-0.22f, stackY, -0.80f), Vector3(0.22f, stackY + 0.4f, -0.70f), weightColor)
        pins.add(Pin3DMarker("weight_pin", Vector3(0f, stackY + 0.2f, -0.68f), "Pin Weight: 55 kg"))

        // Cable Line from low pulley to handle
        val cableIdx = vertices.size
        vertices.add(Vector3(0f, -0.42f, -0.52f)) // Pulley
        vertices.add(Vector3(0f, handleY, handleZ)) // Handle attachment
        lines.add(MeshLine(cableIdx, cableIdx + 1, cableColor, 4f))

        // V-Bar / Close-Grip Handle
        val handleIdx = vertices.size
        vertices.add(Vector3(-0.14f, handleY - 0.06f, handleZ + 0.06f))
        vertices.add(Vector3(0f, handleY, handleZ))
        vertices.add(Vector3(0.14f, handleY - 0.06f, handleZ + 0.06f))
        lines.add(MeshLine(handleIdx, handleIdx + 1, chromeHandle, 5f))
        lines.add(MeshLine(handleIdx + 1, handleIdx + 2, chromeHandle, 5f))

        return Geometry3DResult(vertices, faces, lines, pins)
    }

    // 6. CABLE PEC FLY / FUNCTIONAL TRAINER
    fun buildCableFly(progress: Float): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()
        val pins = mutableListOf<Pin3DMarker>()

        val steelColor = Color(0xFF262A37)
        val cableColor = Color(0xFFE2E8F0)
        val handleColor = Color(0xFFCCFF00)
        val weightColor = Color(0xFF151821)
        val pinColor = Color(0xFFFFD600)

        val span = 0.85f // Width of dual cable columns
        val height = 1.1f

        // Left Tower & Right Tower
        addBox(vertices, faces, lines, Vector3(-span - 0.15f, -0.9f, -0.2f), Vector3(-span + 0.15f, height, 0.2f), steelColor)
        addBox(vertices, faces, lines, Vector3(span - 0.15f, -0.9f, -0.2f), Vector3(span + 0.15f, height, 0.2f), steelColor)

        // Top Connecting Crossbeam & Chin-up Handles
        addBox(vertices, faces, lines, Vector3(-span, height - 0.08f, -0.05f), Vector3(span, height, 0.05f), steelColor)

        // Pulley Carriages (Adjustable height notch)
        val pulleyY = 0.35f
        addBox(vertices, faces, lines, Vector3(-span - 0.18f, pulleyY - 0.08f, 0.05f), Vector3(-span + 0.18f, pulleyY + 0.08f, 0.22f), steelColor)
        addBox(vertices, faces, lines, Vector3(span - 0.18f, pulleyY - 0.08f, 0.05f), Vector3(span + 0.18f, pulleyY + 0.08f, 0.22f), steelColor)

        pins.add(Pin3DMarker("pulley_l", Vector3(-span, pulleyY, 0.23f), "Carriage Pin #8 (Chest Level)"))
        pins.add(Pin3DMarker("pulley_r", Vector3(span, pulleyY, 0.23f), "Carriage Pin #8 (Chest Level)"))

        // Exercise cycle:
        // progress: 0f = arms wide open (eccentric stretch), 1f = hands meet at center chest (peak contraction)
        val flyCycle = (sin((progress * 2 * PI).toDouble() - PI / 2).toFloat() + 1f) / 2f
        val handleX_L = -0.75f + flyCycle * 0.65f // -0.75f to -0.10f
        val handleX_R = 0.75f - flyCycle * 0.65f // 0.75f to 0.10f
        val handleZ = 0.15f + flyCycle * 0.35f // moves slightly forward as arms hug together
        val handleY = pulleyY - flyCycle * 0.10f

        // Weight Stacks rising
        val stackRise = flyCycle * 0.25f
        addBox(vertices, faces, lines, Vector3(-span - 0.10f, -0.7f + stackRise, -0.1f), Vector3(-span + 0.10f, -0.3f + stackRise, 0.1f), weightColor)
        addBox(vertices, faces, lines, Vector3(span - 0.10f, -0.7f + stackRise, -0.1f), Vector3(span + 0.10f, -0.3f + stackRise, 0.1f), weightColor)

        // Left & Right Cables
        val cL = vertices.size
        vertices.add(Vector3(-span, pulleyY, 0.2f))
        vertices.add(Vector3(handleX_L, handleY, handleZ))
        lines.add(MeshLine(cL, cL + 1, cableColor, 3f))

        val cR = vertices.size
        vertices.add(Vector3(span, pulleyY, 0.2f))
        vertices.add(Vector3(handleX_R, handleY, handleZ))
        lines.add(MeshLine(cR, cR + 1, cableColor, 3f))

        // Stirrup Handles
        addCylinder(vertices, faces, lines, Vector3(handleX_L, handleY, handleZ), radius = 0.06f, thickness = 0.02f, axis = 'Z', color = handleColor)
        addCylinder(vertices, faces, lines, Vector3(handleX_R, handleY, handleZ), radius = 0.06f, thickness = 0.02f, axis = 'Z', color = handleColor)

        return Geometry3DResult(vertices, faces, lines, pins)
    }

    // 7. COMMERCIAL TREADMILL
    fun buildTreadmill(progress: Float): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()
        val pins = mutableListOf<Pin3DMarker>()

        val deckColor = Color(0xFF1E222C)
        val beltColor = Color(0xFF121418)
        val steelColor = Color(0xFF2A2E3C)
        val displayColor = Color(0xFF00E5FF)
        val lineMarkerColor = Color(0x66CCFF00)

        // Deck Base (Incline angle calculation)
        val inclineDeg = 6f
        val deckLen = 1.3f
        val deckW = 0.45f
        addBox(vertices, faces, lines, Vector3(-deckW, -0.85f, -deckLen / 2), Vector3(deckW, -0.72f, deckLen / 2), deckColor)

        // Moving Belt Stride Surface
        addBox(vertices, faces, lines, Vector3(-deckW + 0.08f, -0.71f, -deckLen / 2 + 0.08f), Vector3(deckW - 0.08f, -0.69f, deckLen / 2 - 0.08f), beltColor)

        // Moving belt dashed line markers (continuous linear motion)
        val beltOffset = (progress * 0.4f) % 0.3f
        for (i in -2..2) {
            val markerZ = i * 0.28f + beltOffset
            val idx = vertices.size
            vertices.add(Vector3(-0.02f, -0.68f, markerZ))
            vertices.add(Vector3(0.02f, -0.68f, markerZ + 0.12f))
            lines.add(MeshLine(idx, idx + 1, lineMarkerColor, 3f))
        }

        // Side Rails & Front Console Uprights
        val frontZ = -deckLen / 2 + 0.15f
        addBox(vertices, faces, lines, Vector3(-deckW - 0.04f, -0.8f, frontZ), Vector3(-deckW + 0.04f, 0.35f, frontZ + 0.08f), steelColor)
        addBox(vertices, faces, lines, Vector3(deckW - 0.04f, -0.8f, frontZ), Vector3(deckW + 0.04f, 0.35f, frontZ + 0.08f), steelColor)

        // Side Handrails extending backward
        addBox(vertices, faces, lines, Vector3(-deckW - 0.03f, 0.15f, frontZ), Vector3(-deckW + 0.03f, 0.22f, 0.2f), steelColor)
        addBox(vertices, faces, lines, Vector3(deckW - 0.03f, 0.15f, frontZ), Vector3(deckW + 0.03f, 0.22f, 0.2f), steelColor)

        // Front Console Dashboard & Screen
        addBox(vertices, faces, lines, Vector3(-0.35f, 0.25f, frontZ - 0.05f), Vector3(0.35f, 0.45f, frontZ + 0.08f), steelColor)
        addBox(vertices, faces, lines, Vector3(-0.25f, 0.30f, frontZ + 0.081f), Vector3(0.25f, 0.42f, frontZ + 0.085f), displayColor.copy(alpha = 0.8f))

        pins.add(Pin3DMarker("safety_clip", Vector3(0f, 0.23f, frontZ + 0.09f), "Safety Magnetic Lanyard Clip"))
        pins.add(Pin3DMarker("incline_ctrl", Vector3(-0.28f, 0.35f, frontZ + 0.08f), "Incline Calibration (+12%)"))

        return Geometry3DResult(vertices, faces, lines, pins)
    }

    // 8. STATIONARY EXERCISE BIKE
    fun buildExerciseBike(progress: Float): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()
        val pins = mutableListOf<Pin3DMarker>()

        val steelColor = Color(0xFF262C3A)
        val flywheelColor = Color(0xFF64748B)
        val saddleColor = Color(0xFF14161C)
        val crankColor = Color(0xFFCBD5E1)
        val pinColor = Color(0xFFFFD600)

        // Base Stabilizers
        addBox(vertices, faces, lines, Vector3(-0.32f, -0.9f, -0.5f), Vector3(0.32f, -0.82f, -0.4f), steelColor)
        addBox(vertices, faces, lines, Vector3(-0.32f, -0.9f, 0.4f), Vector3(0.32f, -0.82f, 0.5f), steelColor)
        // Central Connecting Spine
        addBox(vertices, faces, lines, Vector3(-0.06f, -0.88f, -0.45f), Vector3(0.06f, -0.80f, 0.45f), steelColor)

        // Front Flywheel Disc (Perimeter-weighted steel wheel)
        addCylinder(vertices, faces, lines, Vector3(0f, -0.35f, -0.38f), radius = 0.32f, thickness = 0.08f, axis = 'X', color = flywheelColor)

        // Diagonal Frame Tubes
        addBox(vertices, faces, lines, Vector3(-0.05f, -0.80f, -0.05f), Vector3(0.05f, 0.15f, -0.35f), steelColor) // Front mast
        addBox(vertices, faces, lines, Vector3(-0.05f, -0.80f, 0.0f), Vector3(0.05f, 0.05f, 0.28f), steelColor) // Seat post tube

        // Adjustable Seat Post & Saddle
        addBox(vertices, faces, lines, Vector3(-0.16f, 0.06f, 0.15f), Vector3(0.16f, 0.14f, 0.42f), saddleColor)
        pins.add(Pin3DMarker("seat_pin", Vector3(0.07f, -0.15f, 0.25f), "Seat Height Pin (Level with Hip)"))

        // Bullhorn Handlebars & Console
        addBox(vertices, faces, lines, Vector3(-0.28f, 0.22f, -0.45f), Vector3(0.28f, 0.28f, -0.32f), steelColor)
        pins.add(Pin3DMarker("handle_pin", Vector3(0f, 0.05f, -0.35f), "Handlebar Reach Knob"))

        // Rotating Pedals (Continuous 360 rotation driven by progress)
        val crankAngle = progress * 2 * PI
        val crankRadius = 0.18f
        val crankCenter = Vector3(0f, -0.58f, -0.05f)

        val leftPedalY = crankCenter.y + sin(crankAngle).toFloat() * crankRadius
        val leftPedalZ = crankCenter.z + cos(crankAngle).toFloat() * crankRadius
        val rightPedalY = crankCenter.y - sin(crankAngle).toFloat() * crankRadius
        val rightPedalZ = crankCenter.z - cos(crankAngle).toFloat() * crankRadius

        // Left crank arm + pedal
        val cLIdx = vertices.size
        vertices.add(Vector3(-0.14f, crankCenter.y, crankCenter.z))
        vertices.add(Vector3(-0.14f, leftPedalY, leftPedalZ))
        lines.add(MeshLine(cLIdx, cLIdx + 1, crankColor, 4f))
        addBox(vertices, faces, lines, Vector3(-0.24f, leftPedalY - 0.03f, leftPedalZ - 0.05f), Vector3(-0.14f, leftPedalY + 0.03f, leftPedalZ + 0.05f), saddleColor)

        // Right crank arm + pedal
        val cRIdx = vertices.size
        vertices.add(Vector3(0.14f, crankCenter.y, crankCenter.z))
        vertices.add(Vector3(0.14f, rightPedalY, rightPedalZ))
        lines.add(MeshLine(cRIdx, cRIdx + 1, crankColor, 4f))
        addBox(vertices, faces, lines, Vector3(0.14f, rightPedalY - 0.03f, rightPedalZ - 0.05f), Vector3(0.24f, rightPedalY + 0.03f, rightPedalZ + 0.05f), saddleColor)

        return Geometry3DResult(vertices, faces, lines, pins)
    }

    // 9. CONCEPT ROWING MACHINE
    fun buildRowingMachine(progress: Float): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()
        val pins = mutableListOf<Pin3DMarker>()

        val steelColor = Color(0xFF272D3A)
        val monorailColor = Color(0xFF94A3B8)
        val seatColor = Color(0xFF161820)
        val chainColor = Color(0xFFCBD5E1)
        val flywheelColor = Color(0xFF1E2129)

        val railLen = 1.4f
        // Monorail Beam
        addBox(vertices, faces, lines, Vector3(-0.06f, -0.75f, -0.5f), Vector3(0.06f, -0.65f, railLen - 0.5f), monorailColor)
        // Rear Leg Base
        addBox(vertices, faces, lines, Vector3(-0.25f, -0.9f, railLen - 0.55f), Vector3(0.25f, -0.80f, railLen - 0.45f), steelColor)
        // Front Frame Support
        addBox(vertices, faces, lines, Vector3(-0.28f, -0.9f, -0.55f), Vector3(0.28f, -0.80f, -0.45f), steelColor)

        // Front Circular Flywheel Fan Housing
        addCylinder(vertices, faces, lines, Vector3(0f, -0.32f, -0.65f), radius = 0.35f, thickness = 0.16f, axis = 'X', color = flywheelColor)
        pins.add(Pin3DMarker("damper_pin", Vector3(0.18f, -0.32f, -0.65f), "Damper Lever: Setting 4-5"))

        // Footrests & Straps
        addBox(vertices, faces, lines, Vector3(-0.22f, -0.55f, -0.28f), Vector3(-0.08f, -0.30f, -0.20f), steelColor)
        addBox(vertices, faces, lines, Vector3(0.08f, -0.55f, -0.28f), Vector3(0.22f, -0.30f, -0.20f), steelColor)
        pins.add(Pin3DMarker("strap_pin", Vector3(0f, -0.40f, -0.24f), "Footplate Heel Notch 3"))

        // Stroke Motion:
        // progress: 0f = The Catch (seat forward, knees bent), 1f = The Finish (legs extended, handle pulled to ribs)
        val strokeCycle = (sin((progress * 2 * PI).toDouble() - PI / 2).toFloat() + 1f) / 2f
        val seatZ = -0.15f + strokeCycle * 0.65f // Sliding seat moves back on monorail
        val handleZ = -0.42f + strokeCycle * 0.85f // Handle pulls past seat
        val handleY = -0.25f + strokeCycle * 0.05f

        // Sliding Seat on Rollers
        addBox(vertices, faces, lines, Vector3(-0.18f, -0.62f, seatZ - 0.14f), Vector3(0.18f, -0.52f, seatZ + 0.14f), seatColor)

        // Chain / Drive Strap from Flywheel to Handle
        val chainIdx = vertices.size
        vertices.add(Vector3(0f, -0.28f, -0.62f)) // Sprocket outlet
        vertices.add(Vector3(0f, handleY, handleZ)) // Wooden/Ergonomic handle
        lines.add(MeshLine(chainIdx, chainIdx + 1, chainColor, 3f))

        // Rowing Handle
        val hIdx = vertices.size
        vertices.add(Vector3(-0.25f, handleY, handleZ))
        vertices.add(Vector3(0.25f, handleY, handleZ))
        lines.add(MeshLine(hIdx, hIdx + 1, Color(0xFFCCFF00), 6f))

        return Geometry3DResult(vertices, faces, lines, pins)
    }

    fun getGeometry(equipmentId: String, progress: Float): Geometry3DResult {
        return when (equipmentId) {
            "bench_press" -> buildBenchPress(progress)
            "squat_rack" -> buildSquatRack(progress)
            "incline_dumbbell" -> buildDumbbellBench(progress)
            "lat_pulldown" -> buildLatPulldown(progress)
            "seated_row" -> buildSeatedRow(progress)
            "cable_fly" -> buildCableFly(progress)
            "treadmill" -> buildTreadmill(progress)
            "exercise_bike" -> buildExerciseBike(progress)
            "rowing_machine" -> buildRowingMachine(progress)
            else -> buildBenchPress(progress)
        }
    }
}
