package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GymEquipmentViewModel
import com.example.ui.components.OnboardingDiagnosticDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CoachScreen
import com.example.ui.screens.EquipmentCatalogScreen
import com.example.ui.screens.EquipmentDetailScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.WorkoutTabScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Gym3DMainApp()
            }
        }
    }
}

@Composable
fun Gym3DMainApp(
    viewModel: GymEquipmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val todayLogs by viewModel.todayNutritionLogs.collectAsState()
    val bodyMetrics by viewModel.bodyMetrics.collectAsState()
    val generatedSplit by viewModel.generatedSplit.collectAsState()
    val allWorkoutSets by viewModel.allWorkoutSets.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    var activeEquipmentDetailId by remember { mutableStateOf<String?>(null) }
    var showFull3DCatalog by remember { mutableStateOf(false) }

    val todaySession = generatedSplit.firstOrNull() ?: com.example.data.WorkoutSessionPlan(
        dayName = "Workout A",
        splitTitle = "Full Body Foundation",
        targetMuscles = listOf("Pectorals", "Quads", "Lats"),
        exercises = emptyList()
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        if (activeEquipmentDetailId != null) {
            // Equipment Detail View (3D Canvas + Setup Pins + Muscles + Mistakes + Log)
            BackHandler {
                activeEquipmentDetailId = null
            }
            EquipmentDetailScreen(
                equipmentId = activeEquipmentDetailId!!,
                viewModel = viewModel,
                onNavigateBack = { activeEquipmentDetailId = null }
            )
        } else if (showFull3DCatalog) {
            // Full 3D Equipment Catalog View
            BackHandler {
                showFull3DCatalog = false
            }
            EquipmentCatalogScreen(
                viewModel = viewModel,
                onSelectEquipment = { id ->
                    viewModel.selectEquipment(id)
                    activeEquipmentDetailId = id
                },
                onNavigateBack = {
                    showFull3DCatalog = false
                }
            )
        } else {
            // Main 5-Tab Navigation Shell
            Scaffold(
                containerColor = DarkBackground,
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("main_bottom_nav_bar")
                    ) {
                        val items = listOf(
                            Triple(0, "Today", Icons.Default.CalendarToday),
                            Triple(1, "Workout", Icons.Default.FitnessCenter),
                            Triple(2, "Nutrition", Icons.Default.Restaurant),
                            Triple(3, "Analytics", Icons.Default.AutoGraph),
                            Triple(4, "Coach", Icons.Default.Psychology)
                        )

                        items.forEach { (index, label, icon) ->
                            val isSelected = uiState.selectedBottomTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.setBottomTab(index) },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) Color(0xFF0F1115) else TextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        color = if (isSelected) ElectricLime else TextMuted,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = ElectricLime
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (uiState.selectedBottomTab) {
                        0 -> TodayScreen(
                            userProfile = userProfile,
                            todaySession = todaySession,
                            nutritionLogs = todayLogs,
                            onStartWorkout = { viewModel.setBottomTab(1) },
                            onOpenQuiz = { viewModel.openOnboardingDialog() },
                            onNavigateToNutrition = { viewModel.setBottomTab(2) }
                        )

                        1 -> WorkoutTabScreen(
                            generatedSplit = generatedSplit,
                            allWorkoutSets = allWorkoutSets,
                            onOpen3DEquipment = { equipId ->
                                viewModel.selectEquipment(equipId)
                                activeEquipmentDetailId = equipId
                            },
                            onOpenFullCatalog = { showFull3DCatalog = true },
                            onAddSet = { equipId -> viewModel.addSet(equipId) },
                            onUpdateSet = { viewModel.updateSet(it) },
                            onDeleteSet = { viewModel.deleteSet(it) },
                            onToggleComplete = { viewModel.toggleCompleteSet(it) }
                        )

                        2 -> NutritionScreen(
                            userProfile = userProfile,
                            todayLogs = todayLogs,
                            onAddMealLog = { name, type, cals, p, c, f ->
                                viewModel.addMealLog(name, type, cals, p, c, f)
                            },
                            onDeleteMealLog = { viewModel.deleteMealLog(it) },
                            onWeeklyFluxAdjust = { delta -> viewModel.adjustWeeklyFlux(delta) }
                        )

                        3 -> AnalyticsScreen(
                            userProfile = userProfile,
                            metrics = bodyMetrics
                        )

                        4 -> CoachScreen(
                            messages = chatMessages,
                            isLoading = uiState.isCoachLoading,
                            onSendMessage = { query -> viewModel.sendCoachMessage(query) }
                        )
                    }
                }
            }
        }

        // Onboarding Diagnostic Dialog
        if (uiState.showOnboardingDialog) {
            OnboardingDiagnosticDialog(
                initialProfile = userProfile,
                onSaveProfile = { newProfile ->
                    viewModel.saveUserProfile(newProfile)
                },
                onDismiss = {
                    viewModel.closeOnboardingDialog()
                }
            )
        }
    }
}
