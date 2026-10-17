package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GymEquipmentRepository
import com.example.data.WorkoutRepository
import com.example.data.local.GymDatabase
import com.example.data.local.WorkoutSetEntity
import com.example.engine3d.Render3DMode
import com.example.model.Equipment
import com.example.model.EquipmentCategory
import com.example.model.MuscleGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
    val selectedDetailTab: Int = 0, // 0: 3D Guide, 1: Workout Log, 2: Setup & Pins, 3: Muscles, 4: Mistakes
    // Rest Timer State
    val isRestTimerActive: Boolean = false,
    val restTimeRemaining: Int = 0,
    val restTotalDuration: Int = 90,
    val isRestTimerPaused: Boolean = false
)

class GymEquipmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkoutRepository

    init {
        val database = GymDatabase.getDatabase(application)
        repository = WorkoutRepository(database.workoutSetDao())
    }

    private val _uiState = MutableStateFlow(GymUiState())
    val uiState: StateFlow<GymUiState> = _uiState.asStateFlow()

    // Reactive workout sets for the selected equipment
    val currentEquipmentSets: StateFlow<List<WorkoutSetEntity>> = _uiState
        .flatMapLatest { state ->
            repository.getSetsForEquipment(state.selectedEquipmentId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentEquipmentMaxWeight: StateFlow<Float?> = _uiState
        .flatMapLatest { state ->
            repository.getMaxWeightForEquipment(state.selectedEquipmentId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _latestCompletedSet = MutableStateFlow<WorkoutSetEntity?>(null)
    val latestCompletedSet: StateFlow<WorkoutSetEntity?> = _latestCompletedSet.asStateFlow()

    private var restTimerJob: Job? = null

    init {
        startAnimationLoop()
        refreshLatestCompletedSet(_uiState.value.selectedEquipmentId)
    }

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
        refreshLatestCompletedSet(id)
    }

    private fun refreshLatestCompletedSet(equipmentId: String) {
        viewModelScope.launch {
            _latestCompletedSet.value = repository.getLatestCompletedSet(equipmentId)
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

    // --- Workout Set Operations (Room) ---

    fun addSet() {
        viewModelScope.launch {
            val eqId = _uiState.value.selectedEquipmentId
            val equipment = GymEquipmentRepository.getEquipmentById(eqId)
            val currentSets = currentEquipmentSets.value

            // Find max setNumber for today
            val todayStart = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis

            val todaySets = currentSets.filter { it.timestamp >= todayStart }
            val nextSetNumber = if (todaySets.isEmpty()) 1 else (todaySets.maxOf { it.setNumber } + 1)

            // Auto-fill from previous set if available
            val previousSet = todaySets.lastOrNull() ?: _latestCompletedSet.value

            val newSet = if (equipment.category == EquipmentCategory.CARDIO) {
                WorkoutSetEntity(
                    equipmentId = eqId,
                    setNumber = nextSetNumber,
                    durationSeconds = previousSet?.durationSeconds ?: 600, // 10 minutes default
                    distanceKm = previousSet?.distanceKm ?: 1.5f,
                    inclineOrResistance = previousSet?.inclineOrResistance ?: 1.0f,
                    isCompleted = false
                )
            } else {
                WorkoutSetEntity(
                    equipmentId = eqId,
                    setNumber = nextSetNumber,
                    weightKg = previousSet?.weightKg ?: 40f,
                    reps = previousSet?.reps ?: 10,
                    isCompleted = false
                )
            }

            repository.insertSet(newSet)
        }
    }

    fun updateSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            repository.updateSet(set)
        }
    }

    fun deleteSet(id: Long) {
        viewModelScope.launch {
            repository.deleteSetById(id)
        }
    }

    fun toggleCompleteSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            val updated = set.copy(isCompleted = !set.isCompleted)
            repository.updateSet(updated)

            if (updated.isCompleted) {
                // Refresh latest completed set cache
                _latestCompletedSet.value = updated
                // Trigger 90-second rest timer automatically!
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
