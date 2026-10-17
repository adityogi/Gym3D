package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.DoubleProgressionStatus
import com.example.data.GymEquipmentRepository
import com.example.data.ScienceEngine
import com.example.data.WorkoutRepository
import com.example.data.local.GymDatabase
import com.example.data.local.NutritionLogEntity
import com.example.data.local.UserProfileEntity
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

        repository.deleteSetById(insertedId1)
        val remainingSets = repository.getSetsForEquipment("bench_press").first()
        assertEquals(1, remainingSets.size)
    }

    @Test
    fun `verify science engine nutrition calculation`() {
        val (targetCals, macros, _) = ScienceEngine.calculateNutritionTargets(
            isMale = true,
            age = 25,
            heightCm = 180f,
            weightKg = 80f,
            daysPerWeek = 4,
            goal = "Hypertrophy (Lean Bulk)"
        )

        assertTrue(targetCals > 2000)
        // Protein should be ~ 80 * 2.2 = 176g
        assertEquals(176, macros.first)
        assertTrue(macros.second > 150) // Carbs
        assertTrue(macros.third > 40) // Fat
    }

    @Test
    fun `verify double progression rule evaluation`() {
        val setsHitTop = listOf(Pair(80f, 12), Pair(80f, 12), Pair(80f, 12))
        val evalTop = ScienceEngine.evaluateDoubleProgression(12, setsHitTop)
        assertTrue(evalTop is DoubleProgressionStatus.IncreaseLoad)

        val setsBelowTop = listOf(Pair(80f, 12), Pair(80f, 10), Pair(80f, 9))
        val evalBelow = ScienceEngine.evaluateDoubleProgression(12, setsBelowTop)
        assertTrue(evalBelow is DoubleProgressionStatus.MaintainLoad)
    }

    @Test
    fun `verify periodized split generation with prioritized muscles`() {
        val split4Day = ScienceEngine.generateSplit(4, "Upper Chest", "Side Delts")
        assertEquals(4, split4Day.size)
        // Check that Upper 1 has bench press
        val upper1 = split4Day.first()
        assertTrue(upper1.exercises.any { it.equipmentId == "bench_press" })
        // Check biomechanical swaps exist
        assertTrue(upper1.exercises.first().biomechanicalSwaps.isNotEmpty())
    }

    @Test
    fun `verify user profile and nutrition log Room persistence`() = runBlocking {
        val userProfileDao = db.userProfileDao()
        val nutritionLogDao = db.nutritionLogDao()

        val profile = UserProfileEntity(
            age = 28,
            weightKg = 82f,
            goal = "Fat Loss (Cut)",
            targetCalories = 2200
        )
        userProfileDao.insertOrUpdateProfile(profile)
        val loadedProfile = userProfileDao.getUserProfileOnce()
        assertNotNull(loadedProfile)
        assertEquals(28, loadedProfile?.age)
        assertEquals(2200, loadedProfile?.targetCalories)

        val meal = NutritionLogEntity(
            dateString = "2026-10-17",
            mealType = "Lunch",
            foodName = "Grilled Chicken & Rice",
            calories = 550,
            proteinG = 50f,
            carbsG = 60f,
            fatG = 12f
        )
        nutritionLogDao.insertLog(meal)
        val logs = nutritionLogDao.getLogsForDate("2026-10-17").first()
        assertEquals(1, logs.size)
        assertEquals("Grilled Chicken & Rice", logs.first().foodName)
    }

    @Test
    fun `verify diagnostic onboarding telemetry calculations`() {
        val bmi = ScienceEngine.calculateBmi(180f, 80f)
        assertEquals(24.69f, bmi, 0.1f)

        val bmrMale = ScienceEngine.calculateBmr(isMale = true, age = 25, heightCm = 180f, weightKg = 80f)
        // 10*80 + 6.25*180 - 5*25 + 5 = 800 + 1125 - 125 + 5 = 1805
        assertEquals(1805f, bmrMale, 1.0f)

        val bmrFemale = ScienceEngine.calculateBmr(isMale = false, age = 25, heightCm = 165f, weightKg = 60f)
        // 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 - 161 = 1345.25
        assertEquals(1345.25f, bmrFemale, 1.0f)

        val desc3 = ScienceEngine.getSplitDescription(3)
        assertTrue(desc3.contains("Full Body"))

        val desc5 = ScienceEngine.getSplitDescription(5)
        assertTrue(desc5.contains("5-Day"))
    }

    @Test
    fun `verify workout generation engine equipment adaptation`() {
        // Home Dumbbells adaptation
        val splitDumbbells = ScienceEngine.generateSplit(
            daysPerWeek = 4,
            prioritized1 = "Upper Chest",
            prioritized2 = "Side Delts",
            experienceLevel = "Intermediate (1-3 yrs)",
            equipmentAccess = "Home Dumbbells"
        )
        assertEquals(4, splitDumbbells.size)
        val upper1 = splitDumbbells.first()
        assertTrue(upper1.exercises.any { it.exerciseName.contains("Dumbbell") })

        // 5-day split generation
        val split5Day = ScienceEngine.generateSplit(5, "Upper Chest", "Side Delts")
        assertEquals(5, split5Day.size)
        assertEquals("Push 1", split5Day[0].dayName)
        assertEquals("Pull 1", split5Day[1].dayName)
        assertEquals("Legs 1", split5Day[2].dayName)
        assertEquals("Upper Hypertrophy", split5Day[3].dayName)
        assertEquals("Lower Hypertrophy", split5Day[4].dayName)
    }

    @Test
    fun `verify 3D equipment visualizer view component and 360 interaction`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val visualizerView = com.example.engine3d.EquipmentVisualizerView(context)

        // Verify initial state
        assertEquals("bench_press", visualizerView.currentModelId)
        assertEquals(35f, visualizerView.yaw, 0.01f)
        assertEquals(14f, visualizerView.pitch, 0.01f)
        assertEquals(1.0f, visualizerView.zoom, 0.01f)

        // 360-degree rotation wrapping test
        visualizerView.yaw = 370f
        assertEquals(10f, visualizerView.yaw, 0.01f)

        visualizerView.yaw = -45f
        assertEquals(315f, visualizerView.yaw, 0.01f)

        // Pitch clamping test
        visualizerView.pitch = 95f
        assertEquals(80f, visualizerView.pitch, 0.01f)

        visualizerView.pitch = -100f
        assertEquals(-80f, visualizerView.pitch, 0.01f)

        // Zoom clamping test
        visualizerView.zoom = 5.0f
        assertEquals(3.0f, visualizerView.zoom, 0.01f)

        // Model switching & loading test
        visualizerView.loadModel("squat_rack", 0.5f)
        assertEquals("squat_rack", visualizerView.currentModelId)
        assertEquals(0.5f, visualizerView.currentProgress, 0.01f)

        // Model3DLoader test
        val squatMeta = com.example.engine3d.Model3DLoader.getModelMeta("squat_rack")
        assertNotNull(squatMeta)
        assertEquals("Commercial Power Cage", squatMeta.name)

        // Wavefront OBJ parsing test
        val testObj = """
            # Simple 3D Pyramid
            v 0.0 1.0 0.0
            v -1.0 -1.0 1.0
            v 1.0 -1.0 1.0
            v 1.0 -1.0 -1.0
            v -1.0 -1.0 -1.0
            f 1 2 3
            f 1 3 4
            f 1 4 5
            f 1 5 2
        """.trimIndent()
        val parsedModel = com.example.engine3d.Model3DLoader.loadObjString(testObj)
        assertEquals(5, parsedModel.vertices.size)
        assertEquals(4, parsedModel.faces.size)
    }
}
