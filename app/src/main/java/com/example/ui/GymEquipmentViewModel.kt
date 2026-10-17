package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiCoachService
import com.example.data.GymEquipmentRepository
import com.example.data.ScienceEngine
import com.example.data.WorkoutRepository
import com.example.data.WorkoutSessionPlan
import com.example.data.local.BodyMetricDao
import com.example.data.local.BodyMetricEntity
import com.example.data.local.GymDatabase
import com.example.data.local.NutritionLogDao
import com.example.data.local.NutritionLogEntity
import com.example.data.local.UserProfileDao
import com.example.data.local.UserProfileEntity
import com.example.data.local.WorkoutSetEntity
import com.example.engine3d.Render3DMode
import com.example.model.EquipmentCategory
import com.example.model.MuscleGroup
import com.example.ui.screens.ChatMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GymUiState(
    val selectedEquipmentId: String = "bench_press",
    val selectedCategory: EquipmentCategory = EquipmentCategory.ALL,
    val selectedMuscle: MuscleGroup = MuscleGroup.ALL,
    val searchQuery: String = "",
    val bookmarkedIds: Set<String> = emptySet(),
    val isPlaying: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val animationProgress: Float = 0f,
    val cameraYaw: Float = 40f,
    val cameraPitch: Float = 22f,
    val cameraZoom: Float = 1.0f,
    val renderMode: Render3DMode = Render3DMode.SOLID,
    val selectedPinId: String? = null,
    val selectedDetailTab: Int = 0,
    // Rest Timer
    val isRestTimerActive: Boolean = false,
    val restTimeRemaining: Int = 0,
    val restTotalDuration: Int = 90,
    val isRestTimerPaused: Boolean = false,
    // 5-Tab Navigation (0: Today, 1: Workout, 2: Nutrition, 3: Analytics, 4: Coach)
    val selectedBottomTab: Int = 0,
    // Onboarding Dialog Visibility
    val showOnboardingDialog: Boolean = false,
    // AI Coach Chat
    val isCoachLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class GymEquipmentViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GymDatabase.getDatabase(application)
    private val workoutRepo = WorkoutRepository(database.workoutSetDao())
    private val userProfileDao = database.userProfileDao()
    private val nutritionLogDao = database.nutritionLogDao()
    private val bodyMetricDao = database.bodyMetricDao()

    private val _uiState = MutableStateFlow(GymUiState())
    val uiState: StateFlow<GymUiState> = _uiState.asStateFlow()

    // 1. User Profile Flow
    val userProfile: StateFlow<UserProfileEntity> = userProfileDao.getUserProfile()
        .map { it ?: UserProfileEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileEntity()
        )

    // 2. Nutrition Logs Flow for Today
    private val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayNutritionLogs: StateFlow<List<NutritionLogEntity>> = nutritionLogDao.getLogsForDate(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 3. Body Metrics Flow
    val bodyMetrics: StateFlow<List<BodyMetricEntity>> = bodyMetricDao.getAllMetrics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 4. All Workout Sets Flow
    val allWorkoutSets: StateFlow<List<WorkoutSetEntity>> = workoutRepo.getSetsForEquipment("all")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 5. Workout Sets for Selected Equipment
    val currentEquipmentSets: StateFlow<List<WorkoutSetEntity>> = _uiState
        .flatMapLatest { state ->
            workoutRepo.getSetsForEquipment(state.selectedEquipmentId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentEquipmentMaxWeight: StateFlow<Float?> = _uiState
        .flatMapLatest { state ->
            workoutRepo.getMaxWeightForEquipment(state.selectedEquipmentId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _latestCompletedSet = MutableStateFlow<WorkoutSetEntity?>(null)
    val latestCompletedSet: StateFlow<WorkoutSetEntity?> = _latestCompletedSet.asStateFlow()

    // 6. AI Coach Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // 7. Generated Periodized Split
    private val _generatedSplit = MutableStateFlow<List<WorkoutSessionPlan>>(emptyList())
    val generatedSplit: StateFlow<List<WorkoutSessionPlan>> = _generatedSplit.asStateFlow()

    private var restTimerJob: Job? = null

    init {
        startAnimationLoop()
        checkInitialOnboarding()
    }

    private fun checkInitialOnboarding() {
        viewModelScope.launch {
            val profile = userProfileDao.getUserProfileOnce()
            if (profile == null) {
                val defaultProf = UserProfileEntity()
                userProfileDao.insertOrUpdateProfile(defaultProf)
                _uiState.update { it.copy(showOnboardingDialog = true) }
                updateSplit(defaultProf)
            } else {
                if (!profile.isOnboardingCompleted) {
                    _uiState.update { it.copy(showOnboardingDialog = true) }
                }
                updateSplit(profile)
            }
        }
    }

    fun updateSplit(profile: UserProfileEntity) {
        val split = ScienceEngine.generateSplit(
            daysPerWeek = profile.workoutDaysPerWeek,
            prioritized1 = profile.prioritizedMuscle1,
            prioritized2 = profile.prioritizedMuscle2,
            experienceLevel = profile.experienceLevel,
            equipmentAccess = profile.equipmentAccess
        )
        _generatedSplit.value = split
    }

    fun saveUserProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            userProfileDao.insertOrUpdateProfile(profile)
            updateSplit(profile)
            _uiState.update { it.copy(showOnboardingDialog = false) }
        }
    }

    fun setBottomTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedBottomTab = tabIndex) }
    }

    fun openOnboardingDialog() {
        _uiState.update { it.copy(showOnboardingDialog = true) }
    }

    fun closeOnboardingDialog() {
        _uiState.update { it.copy(showOnboardingDialog = false) }
    }

    // --- Nutrition Logging ---
    fun addMealLog(name: String, type: String, cals: Int, protein: Float, carbs: Float, fat: Float) {
        viewModelScope.launch {
            val entry = NutritionLogEntity(
                dateString = todayDateString,
                mealType = type,
                foodName = name,
                calories = cals,
                proteinG = protein,
                carbsG = carbs,
                fatG = fat
            )
            nutritionLogDao.insertLog(entry)
        }
    }

    fun deleteMealLog(id: Long) {
        viewModelScope.launch {
            nutritionLogDao.deleteLogById(id)
        }
    }

    fun adjustWeeklyFlux(calorieDelta: Int) {
        viewModelScope.launch {
            val current = userProfile.value
            val newCals = (current.targetCalories + calorieDelta).coerceAtLeast(1400)
            val updated = current.copy(
                targetCalories = newCals,
                lastWeeklyCheckinTimestamp = System.currentTimeMillis()
            )
            userProfileDao.insertOrUpdateProfile(updated)
        }
    }

    // --- AI Coach Chat ---
    fun sendCoachMessage(query: String) {
        val userMsg = ChatMessage(sender = "user", text = query)
        _chatMessages.update { it + userMsg }
        _uiState.update { it.copy(isCoachLoading = true) }

        viewModelScope.launch {
            val response = GeminiCoachService.askScienceCoach(query)
            val coachMsg = ChatMessage(sender = "coach", text = response)
            _chatMessages.update { it + coachMsg }
            _uiState.update { it.copy(isCoachLoading = false) }
        }
    }

    // --- 3D Animation & Camera Controls ---
    private fun startAnimationLoop() {
        viewModelScope.launch {
            val frameTimeMs = 25L
            while (isActive) {
                delay(frameTimeMs)
                if (_uiState.value.isPlaying) {
                    val speed = _uiState.value.playbackSpeed
                    val step = (frameTimeMs / (3500f / speed))
                    _uiState.update { state ->
                        val next = (state.animationProgress + step) % 1.0f
                        state.copy(animationProgress = next)
                    }
                }
            }
        }
    }

    fun selectEquipment(id: String) {
        val equipment = GymEquipmentRepository.getEquipmentById(id)
        _uiState.update {
            it.copy(
                selectedEquipmentId = id,
                cameraYaw = equipment.defaultCameraYaw,
                cameraPitch = equipment.defaultCameraPitch,
                cameraZoom = equipment.defaultCameraZoom,
                selectedPinId = null,
                selectedDetailTab = 0
            )
        }
    }

    fun selectCategory(category: EquipmentCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectMuscle(muscle: MuscleGroup) {
        _uiState.update { it.copy(selectedMuscle = muscle) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleBookmark(id: String) {
        _uiState.update {
            val nextBookmarks = if (it.bookmarkedIds.contains(id)) {
                it.bookmarkedIds - id
            } else {
                it.bookmarkedIds + id
            }
            it.copy(bookmarkedIds = nextBookmarks)
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun setProgress(progress: Float) {
        _uiState.update { it.copy(animationProgress = progress.coerceIn(0f, 1f)) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun rotateCamera(deltaYaw: Float, deltaPitch: Float) {
        _uiState.update {
            val newYaw = (it.cameraYaw + deltaYaw) % 360f
            val newPitch = (it.cameraPitch + deltaPitch).coerceIn(-75f, 75f)
            it.copy(cameraYaw = newYaw, cameraPitch = newPitch)
        }
    }

    fun setCameraAngles(yaw: Float, pitch: Float) {
        _uiState.update { it.copy(cameraYaw = yaw, cameraPitch = pitch) }
    }

    fun setCameraZoom(zoom: Float) {
        _uiState.update { it.copy(cameraZoom = zoom.coerceIn(0.55f, 2.8f)) }
    }

    fun resetCamera() {
        val current = GymEquipmentRepository.getEquipmentById(_uiState.value.selectedEquipmentId)
        _uiState.update {
            it.copy(
                cameraYaw = current.defaultCameraYaw,
                cameraPitch = current.defaultCameraPitch,
                cameraZoom = current.defaultCameraZoom
            )
        }
    }

    fun setRenderMode(mode: Render3DMode) {
        _uiState.update { it.copy(renderMode = mode) }
    }

    fun selectPin(pinId: String?) {
        _uiState.update { it.copy(selectedPinId = pinId) }
    }

    fun selectDetailTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedDetailTab = tabIndex) }
    }

    // --- Workout Set Logging Operations ---
    fun addSet(customEquipId: String? = null) {
        viewModelScope.launch {
            val eqId = customEquipId ?: _uiState.value.selectedEquipmentId
            val equipment = GymEquipmentRepository.getEquipmentById(eqId)
            val currentSets = workoutRepo.getSetsForEquipment(eqId).first()

            val nextSetNumber = if (currentSets.isEmpty()) 1 else (currentSets.maxOf { it.setNumber } + 1)
            val previousSet = currentSets.lastOrNull() ?: workoutRepo.getLatestCompletedSet(eqId)

            val newSet = if (equipment.category == EquipmentCategory.CARDIO) {
                WorkoutSetEntity(
                    equipmentId = eqId,
                    setNumber = nextSetNumber,
                    durationSeconds = previousSet?.durationSeconds ?: 600,
                    distanceKm = previousSet?.distanceKm ?: 1.5f,
                    inclineOrResistance = previousSet?.inclineOrResistance ?: 1.0f,
                    isCompleted = false
                )
            } else {
                WorkoutSetEntity(
                    equipmentId = eqId,
                    setNumber = nextSetNumber,
                    weightKg = previousSet?.weightKg ?: 50f,
                    reps = previousSet?.reps ?: 10,
                    isCompleted = false
                )
            }

            workoutRepo.insertSet(newSet)
        }
    }

    fun updateSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            workoutRepo.updateSet(set)
        }
    }

    fun deleteSet(id: Long) {
        viewModelScope.launch {
            workoutRepo.deleteSetById(id)
        }
    }

    fun toggleCompleteSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            val updated = set.copy(isCompleted = !set.isCompleted)
            workoutRepo.updateSet(updated)

            if (updated.isCompleted) {
                _latestCompletedSet.value = updated
                startRestTimer(90)
            }
        }
    }

    // --- Rest Timer Operations ---
    fun startRestTimer(durationSeconds: Int) {
        restTimerJob?.cancel()
        _uiState.update {
            it.copy(
                isRestTimerActive = true,
                restTimeRemaining = durationSeconds,
                restTotalDuration = durationSeconds,
                isRestTimerPaused = false
            )
        }

        restTimerJob = viewModelScope.launch {
            while (isActive && _uiState.value.restTimeRemaining > 0) {
                delay(1000L)
                if (!_uiState.value.isRestTimerPaused) {
                    _uiState.update {
                        val next = it.restTimeRemaining - 1
                        if (next <= 0) {
                            it.copy(isRestTimerActive = false, restTimeRemaining = 0)
                        } else {
                            it.copy(restTimeRemaining = next)
                        }
                    }
                }
            }
        }
    }

    fun togglePauseRestTimer() {
        _uiState.update { it.copy(isRestTimerPaused = !it.isRestTimerPaused) }
    }

    fun addRestSeconds(seconds: Int) {
        _uiState.update {
            val next = it.restTimeRemaining + seconds
            it.copy(
                restTimeRemaining = next,
                restTotalDuration = maxOf(it.restTotalDuration, next)
            )
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isRestTimerActive = false, restTimeRemaining = 0) }
    }
}
