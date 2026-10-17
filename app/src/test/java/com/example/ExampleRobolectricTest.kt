package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.GymEquipmentRepository
import com.example.data.WorkoutRepository
import com.example.data.local.GymDatabase
import com.example.data.local.WorkoutSetEntity
import com.example.engine3d.EquipmentGeometries
import com.example.engine3d.Vector3
import com.example.model.EquipmentCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: GymDatabase
    private lateinit var repository: WorkoutRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GymDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WorkoutRepository(db.workoutSetDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Gym3D", appName)
    }

    @Test
    fun `verify equipment catalog coverage`() {
        val equipment = GymEquipmentRepository.allEquipment
        assertEquals(9, equipment.size)

        assertTrue(equipment.any { it.category == EquipmentCategory.FREE_WEIGHTS })
        assertTrue(equipment.any { it.category == EquipmentCategory.CABLE_MACHINES })
        assertTrue(equipment.any { it.category == EquipmentCategory.CARDIO })

        equipment.forEach { eq ->
            assertTrue("Equipment ${eq.id} should have setup steps", eq.setupSteps.isNotEmpty())
            assertTrue("Equipment ${eq.id} should have mistakes checklist", eq.mistakes.isNotEmpty())
            assertTrue("Equipment ${eq.id} should have muscle activations", eq.muscleActivations.isNotEmpty())
        }
    }

    @Test
    fun `verify 3D geometry generation for all equipment`() {
        val equipmentIds = listOf(
            "bench_press",
            "squat_rack",
            "incline_dumbbell",
            "lat_pulldown",
            "seated_row",
            "cable_fly",
            "treadmill",
            "exercise_bike",
            "rowing_machine"
        )

        for (id in equipmentIds) {
            val geomAtStart = EquipmentGeometries.getGeometry(id, 0.0f)
            val geomAtPeak = EquipmentGeometries.getGeometry(id, 0.5f)

            assertTrue("Geometry $id at start should have vertices", geomAtStart.vertices.isNotEmpty())
            assertTrue("Geometry $id at peak should have vertices", geomAtPeak.vertices.isNotEmpty())
            assertTrue("Geometry $id should have lines or faces", geomAtStart.faces.isNotEmpty() || geomAtStart.lines.isNotEmpty())
        }
    }

    @Test
    fun `verify 3D vector transformations`() {
        val vec = Vector3(1f, 0f, 0f)
        val rotated = vec.rotateY(90f)
        assertEquals(0f, rotated.x, 0.001f)
        assertEquals(-1f, rotated.z, 0.001f)

        val projected = vec.project(screenWidth = 800f, screenHeight = 600f, cameraDistance = 900f, scale = 200f)
        assertNotNull(projected)
        assertTrue(projected.screenPos.x > 0f)
        assertTrue(projected.screenPos.y > 0f)
    }

    @Test
    fun `verify Room database workout set logging and PR calculation`() = runBlocking {
        val set1 = WorkoutSetEntity(
            equipmentId = "bench_press",
            setNumber = 1,
            weightKg = 80f,
            reps = 10,
            isCompleted = true
        )
        val set2 = WorkoutSetEntity(
            equipmentId = "bench_press",
            setNumber = 2,
            weightKg = 90f,
            reps = 8,
            isCompleted = true
        )
        val insertedId1 = repository.insertSet(set1)
        val insertedId2 = repository.insertSet(set2)
        assertTrue(insertedId1 > 0)
        assertTrue(insertedId2 > 0)

        val sets = repository.getSetsForEquipment("bench_press").first()
        assertEquals(2, sets.size)

        val maxWeight = repository.getMaxWeightForEquipment("bench_press").first()
        assertEquals(90f, maxWeight ?: 0f, 0.01f)

        val latest = repository.getLatestCompletedSet("bench_press")
        assertNotNull(latest)
        assertEquals(90f, latest?.weightKg ?: 0f, 0.01f)

        // Test delete
        repository.deleteSetById(insertedId1)
        val remainingSets = repository.getSetsForEquipment("bench_press").first()
        assertEquals(1, remainingSets.size)
        assertEquals(90f, remainingSets.first().weightKg, 0.01f)
    }

    @Test
    fun `verify Room database cardio intervals`() = runBlocking {
        val cardioSet = WorkoutSetEntity(
            equipmentId = "treadmill",
            setNumber = 1,
            durationSeconds = 900,
            distanceKm = 2.5f,
            isCompleted = true
        )
        val id = repository.insertSet(cardioSet)
        assertTrue(id > 0)

        val retrieved = repository.getSetsForEquipment("treadmill").first()
        assertEquals(1, retrieved.size)
        assertEquals(900, retrieved.first().durationSeconds)
        assertEquals(2.5f, retrieved.first().distanceKm, 0.01f)
    }
}
