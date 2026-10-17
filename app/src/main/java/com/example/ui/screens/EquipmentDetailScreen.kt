package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GymEquipmentRepository
import com.example.model.Equipment
import com.example.ui.GymEquipmentViewModel
import com.example.ui.components.AnatomicalMuscleMap
import com.example.ui.components.EquipmentSetupGuide
import com.example.ui.components.EquipmentWorkoutLogTab
import com.example.ui.components.FormMistakesChecklist
import com.example.ui.components.Interactive3DStudio
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentDetailScreen(
    equipmentId: String,
    viewModel: GymEquipmentViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept Android hardware / gesture back button
    BackHandler { onNavigateBack() }

    val uiState by viewModel.uiState.collectAsState()
    val equipment = GymEquipmentRepository.getEquipmentById(equipmentId)
    val isBookmarked = uiState.bookmarkedIds.contains(equipment.id)

    val currentSets by viewModel.currentEquipmentSets.collectAsState()
    val maxWeight by viewModel.currentEquipmentMaxWeight.collectAsState()
    val latestCompletedSet by viewModel.latestCompletedSet.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("equipment_detail_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = equipment.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Catalog",
                        tint = TextPrimary
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.toggleBookmark(equipment.id) }
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) ElectricLime else TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface
            )
        )

        // Main Scrolling Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. The Interactive 3D Studio & Player
            item {
                Interactive3DStudio(
                    equipment = equipment,
                    animationProgress = uiState.animationProgress,
                    isPlaying = uiState.isPlaying,
                    playbackSpeed = uiState.playbackSpeed,
                    yaw = uiState.cameraYaw,
                    pitch = uiState.cameraPitch,
                    zoom = uiState.cameraZoom,
                    renderMode = uiState.renderMode,
                    selectedPinId = uiState.selectedPinId,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onProgressChange = { viewModel.setProgress(it) },
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                    onRotate = { dy, dp -> viewModel.rotateCamera(dy, dp) },
                    onZoomChange = { viewModel.setCameraZoom(it) },
                    onSetCameraAngles = { y, p -> viewModel.setCameraAngles(y, p) },
                    onResetCamera = { viewModel.resetCamera() },
                    onSetRenderMode = { viewModel.setRenderMode(it) },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // 2. Tab Navigation Row (including the Workout Log tab!)
            item {
                val tabTitles = listOf(
                    "3D Guide",
                    "Log (${currentSets.size})",
                    "Setup & Pins (${equipment.pins.size})",
                    "Muscles (${equipment.muscleActivations.size})",
                    "Mistakes (${equipment.mistakes.size})"
                )

                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedDetailTab,
                    containerColor = DarkSurface,
                    contentColor = ElectricLime,
                    edgePadding = 14.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedDetailTab]),
                            color = ElectricLime,
                            height = 3.dp
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedDetailTab == index,
                            onClick = { viewModel.selectDetailTab(index) },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (uiState.selectedDetailTab == index) ElectricLime else TextSecondary,
                                    fontWeight = if (uiState.selectedDetailTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // 3. Tab Content
            item {
                Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                    when (uiState.selectedDetailTab) {
                        0 -> DetailOverviewSection(equipment)
                        1 -> EquipmentWorkoutLogTab(
                            equipment = equipment,
                            sets = currentSets,
                            maxWeight = maxWeight,
                            latestCompletedSet = latestCompletedSet,
                            isRestTimerActive = uiState.isRestTimerActive,
                            restTimeRemaining = uiState.restTimeRemaining,
                            restTotalDuration = uiState.restTotalDuration,
                            isRestTimerPaused = uiState.isRestTimerPaused,
                            onAddSet = { viewModel.addSet() },
                            onUpdateSet = { viewModel.updateSet(it) },
                            onDeleteSet = { viewModel.deleteSet(it) },
                            onToggleComplete = { viewModel.toggleCompleteSet(it) },
                            onStartRestTimer = { viewModel.startRestTimer(it) },
                            onTogglePauseRestTimer = { viewModel.togglePauseRestTimer() },
                            onAddRestSeconds = { viewModel.addRestSeconds(it) },
                            onSkipRestTimer = { viewModel.skipRestTimer() }
                        )
                        2 -> EquipmentSetupGuide(
                            equipment = equipment,
                            selectedPinId = uiState.selectedPinId,
                            onSelectPin = { viewModel.selectPin(it) }
                        )
                        3 -> AnatomicalMuscleMap(
                            activations = equipment.muscleActivations,
                            animationProgress = uiState.animationProgress
                        )
                        4 -> FormMistakesChecklist(
                            mistakes = equipment.mistakes
                        )
                    }
                }
            }

            // Bottom Spacing for navigation safe area
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailOverviewSection(equipment: Equipment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "EQUIPMENT BIOMECHANICS OVERVIEW",
            style = MaterialTheme.typography.labelSmall,
            color = ElectricLime,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = equipment.description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCard(
                label = "Difficulty",
                value = equipment.difficulty,
                color = CyberEmerald,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                label = "Target Mover",
                value = equipment.primaryMuscle.displayName,
                color = ElectricLime,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Primary and Secondary Muscle Recruitment Summary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ElectricLime))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PRIMARY MOVER: ${equipment.primaryMuscle.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            if (equipment.secondaryMuscles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberEmerald))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SECONDARY SYNERGISTS: ${equipment.secondaryMuscles.joinToString(", ") { it.displayName }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberEmerald,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interaction Hint
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tip",
                    tint = CyberEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tip: Check the 'Log' tab to record completed sets, track your personal best, and start rest timers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(12.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
