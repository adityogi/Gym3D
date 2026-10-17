package com.example.engine3d

import androidx.compose.ui.graphics.Color
import java.io.BufferedReader
import java.io.StringReader

/**
 * Metadata descriptor for loadable 3D gym equipment models.
 */
data class Model3DMeta(
    val id: String,
    val name: String,
    val category: String,
    val defaultYaw: Float = 35f,
    val defaultPitch: Float = 14f,
    val defaultZoom: Float = 1.0f,
    val description: String = ""
)

/**
 * Engine loader capable of loading built-in procedural equipment models,
 * parsing Wavefront OBJ model strings, and managing model cache and telemetry.
 */
object Model3DLoader {

    val availableModels: List<Model3DMeta> = listOf(
        Model3DMeta(
            id = "bench_press",
            name = "Olympic Flat Bench Press",
            category = "Free Weights",
            defaultYaw = 35f,
            defaultPitch = 14f,
            description = "Heavy-duty powerlifting bench with safety catches and Olympic 45lb barbell."
        ),
        Model3DMeta(
            id = "squat_rack",
            name = "Commercial Power Cage",
            category = "Free Weights",
            defaultYaw = 40f,
            defaultPitch = 12f,
            description = "Full 4-post steel cage with adjustable J-cups, safeties, and knurled bar."
        ),
        Model3DMeta(
            id = "incline_dumbbell",
            name = "Adjustable Incline Bench",
            category = "Free Weights",
            defaultYaw = 30f,
            defaultPitch = 15f,
            description = "Ladder-catch bench with polyurethane pro-style hex dumbbells."
        ),
        Model3DMeta(
            id = "lat_pulldown",
            name = "Selectorized Lat Pulldown",
            category = "Cable Machines",
            defaultYaw = 25f,
            defaultPitch = 12f,
            description = "Dual-pulley tower with revolving wide-grip lat bar and thigh pads."
        ),
        Model3DMeta(
            id = "seated_row",
            name = "Low Pulley Seated Cable Row",
            category = "Cable Machines",
            defaultYaw = 45f,
            defaultPitch = 14f,
            description = "Heavy steel monorail with aluminum footplates and close-grip V-handle."
        ),
        Model3DMeta(
            id = "cable_fly",
            name = "Functional Cable Crossover",
            category = "Cable Machines",
            defaultYaw = 20f,
            defaultPitch = 10f,
            description = "Dual adjustable height pulley towers with aircraft-grade nylon-coated steel cables."
        ),
        Model3DMeta(
            id = "treadmill",
            name = "Commercial Curved Treadmill",
            category = "Cardio",
            defaultYaw = 35f,
            defaultPitch = 16f,
            description = "Heavy commercial treadmill with shock-absorbing textured slat running deck."
        ),
        Model3DMeta(
            id = "exercise_bike",
            name = "Magnetic Air Studio Bike",
            category = "Cardio",
            defaultYaw = 40f,
            defaultPitch = 15f,
            description = "Heavy perimeter flywheel with dual micro-adjustable drop handlebars."
        ),
        Model3DMeta(
            id = "rowing_machine",
            name = "Air Resistance Rower",
            category = "Cardio",
            defaultYaw = 35f,
            defaultPitch = 14f,
            description = "Precision flywheel damper with sliding ergonomic seat on extruded monorail."
        )
    )

    /**
     * Loads a 3D equipment model by ID with dynamic kinematic progress (0.0f = start, 1.0f = completion).
     */
    fun loadModel(modelId: String, progress: Float = 0f): Geometry3DResult {
        return EquipmentGeometries.getGeometry(modelId, progress.coerceIn(0f, 1f))
    }

    /**
     * Finds metadata for a specific model ID.
     */
    fun getModelMeta(modelId: String): Model3DMeta {
        return availableModels.find { it.id == modelId } ?: availableModels.first()
    }

    /**
     * Wavefront .OBJ parser capable of loading arbitrary 3D models into Geometry3DResult.
     * Supports vertices ('v x y z'), normals ('vn'), lines ('l v1 v2'), and faces ('f v1 v2 v3 ...').
     */
    fun loadObjString(
        objData: String,
        baseColor: Color = Color(0xFF374151),
        wireColor: Color = Color(0x4400E5FF)
    ): Geometry3DResult {
        val vertices = mutableListOf<Vector3>()
        val faces = mutableListOf<MeshFace>()
        val lines = mutableListOf<MeshLine>()

        val reader = BufferedReader(StringReader(objData))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line!!.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

            val tokens = trimmed.split("\\s+".toRegex())
            when (tokens[0]) {
                "v" -> {
                    // Vertex: v x y z
                    if (tokens.size >= 4) {
                        val x = tokens[1].toFloatOrNull() ?: 0f
                        val y = tokens[2].toFloatOrNull() ?: 0f
                        val z = tokens[3].toFloatOrNull() ?: 0f
                        vertices.add(Vector3(x, y, z))
                    }
                }
                "f" -> {
                    // Face: f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...
                    if (tokens.size >= 4) {
                        val faceIndices = mutableListOf<Int>()
                        for (i in 1 until tokens.size) {
                            val vToken = tokens[i].split("/")[0]
                            val index = vToken.toIntOrNull()
                            if (index != null) {
                                // OBJ uses 1-based indexing
                                val zeroBased = if (index > 0) index - 1 else vertices.size + index
                                if (zeroBased in vertices.indices) {
                                    faceIndices.add(zeroBased)
                                }
                            }
                        }
                        if (faceIndices.size >= 3) {
                            faces.add(MeshFace(faceIndices, baseColor, wireColor))
                        }
                    }
                }
                "l" -> {
                    // Line: l v1 v2
                    if (tokens.size >= 3) {
                        val i1 = (tokens[1].toIntOrNull() ?: 1) - 1
                        val i2 = (tokens[2].toIntOrNull() ?: 2) - 1
                        if (i1 in vertices.indices && i2 in vertices.indices) {
                            lines.add(MeshLine(i1, i2, wireColor, 2f))
                        }
                    }
                }
            }
        }

        // Normalize vertices to [-0.9, 0.9] bounds if geometry was found
        if (vertices.isNotEmpty()) {
            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

            for (v in vertices) {
                if (v.x < minX) minX = v.x; if (v.x > maxX) maxX = v.x
                if (v.y < minY) minY = v.y; if (v.y > maxY) maxY = v.y
                if (v.z < minZ) minZ = v.z; if (v.z > maxZ) maxZ = v.z
            }

            val centerX = (minX + maxX) / 2f
            val centerY = (minY + maxY) / 2f
            val centerZ = (minZ + maxZ) / 2f

            val maxExtent = maxOf(maxX - minX, maxY - minY, maxZ - minZ).coerceAtLeast(0.001f)
            val scale = 1.6f / maxExtent

            val normalizedVertices = vertices.map { v ->
                Vector3(
                    (v.x - centerX) * scale,
                    (v.y - centerY) * scale,
                    (v.z - centerZ) * scale
                )
            }

            return Geometry3DResult(normalizedVertices, faces, lines)
        }

        return Geometry3DResult(vertices, faces, lines)
    }
}
