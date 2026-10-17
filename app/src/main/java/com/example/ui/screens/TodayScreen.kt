package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScienceEngine
import com.example.data.WorkoutSessionPlan
import com.example.data.local.NutritionLogEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.BrightAmber
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TodayScreen(
    userProfile: UserProfileEntity,
    todaySession: WorkoutSessionPlan,
    nutritionLogs: List<NutritionLogEntity>,
    onStartWorkout: () -> Unit,
    onOpenQuiz: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalLoggedCals = nutritionLogs.sumOf { it.calories }
    val totalLoggedProtein = nutritionLogs.sumOf { it.proteinG.toDouble() }.toFloat()
    val totalLoggedCarbs = nutritionLogs.sumOf { it.carbsG.toDouble() }.toFloat()
    val totalLoggedFat = nutritionLogs.sumOf { it.fatG.toDouble() }.toFloat()

    // Daily Science Booster State
    val dailyArticle = ScienceEngine.dailyScienceArticles.first()
    var selectedQuizOption by remember { mutableStateOf<Int?>(null) }
    var hasAnsweredQuiz by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("today_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with greeting and profile badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "Gym3D",
                                tint = Color(0xFF0F1115),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "BUILT WITH SCIENCE+",
                            style = MaterialTheme.typography.displayMedium,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${userProfile.goal} • ${userProfile.experienceLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime
                    )
                }

                IconButton(
                    onClick = onOpenQuiz,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Edit Profile",
                        tint = TextSecondary
                    )
                }
            }
        }

        // 2. Scheduled Workout Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurface)
                    .border(1.dp, ElectricLime.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricLime.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TODAY'S SCHEDULED SESSION",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "${todaySession.exercises.size} Exercises • ~45 min",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${todaySession.dayName}: ${todaySession.splitTitle}",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Muscles Targeted: " + todaySession.targetMuscles.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Start Workout Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricLime)
                        .clickable(onClick = onStartWorkout)
                        .padding(vertical = 12.dp)
                        .testTag("start_today_workout_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = Color(0xFF0F1115),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Launch Workout Session & 3D Guide",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF0F1115),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Daily Adaptive Nutrition Overview Ring Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                    .clickable(onClick = onNavigateToNutrition)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ADAPTIVE NUTRITION TARGETS",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberEmerald,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Mifflin-St Jeor Energy Balance",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Details",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Caloric Progress Ring + Numbers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val calsProgress = (totalLoggedCals.toFloat() / userProfile.targetCalories.toFloat()).coerceIn(0f, 1f)
                        CircularProgressIndicator(
                            progress = { calsProgress },
                            color = CyberEmerald,
                            trackColor = DarkSurfaceHigh,
                            strokeWidth = 7.dp,
                            modifier = Modifier.size(80.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalLoggedCals",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "kcal",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Macro Breakdown Columns
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MacroColumnItem("Protein", "${totalLoggedProtein.toInt()}g", "${userProfile.targetProteinG}g", ElectricLime)
                        MacroColumnItem("Carbs", "${totalLoggedCarbs.toInt()}g", "${userProfile.targetCarbsG}g", NeonCyan)
                        MacroColumnItem("Fat", "${totalLoggedFat.toInt()}g", "${userProfile.targetFatG}g", BrightAmber)
                    }
                }
            }
        }

        // 4. Daily 2-Minute Science Booster & Literacy Check
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Science",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DAILY 2-MINUTE SCIENCE BOOSTER",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = dailyArticle.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = dailyArticle.takeaway,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "📚 ${dailyArticle.citation}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quiz Check
                Text(
                    text = "Q: " + dailyArticle.quizQuestion,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                dailyArticle.quizOptions.forEachIndexed { optIdx, optText ->
                    val isChosen = selectedQuizOption == optIdx
                    val isCorrect = optIdx == dailyArticle.correctIndex
                    val boxColor = if (hasAnsweredQuiz) {
                        if (isCorrect) CyberEmerald.copy(alpha = 0.25f)
                        else if (isChosen) Color(0x33FF4848)
                        else DarkSurfaceVariant
                    } else if (isChosen) {
                        DarkSurfaceHigh
                    } else {
                        DarkSurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(boxColor)
                            .border(
                                1.dp,
                                if (hasAnsweredQuiz && isCorrect) CyberEmerald else DarkBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = !hasAnsweredQuiz) {
                                selectedQuizOption = optIdx
                                hasAnsweredQuiz = true
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            if (hasAnsweredQuiz && isCorrect) {
                                Icon(Icons.Default.Check, contentDescription = "Correct", tint = CyberEmerald, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun MacroColumnItem(
    label: String,
    logged: String,
    target: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = logged, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(text = "of $target", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
    }
}
