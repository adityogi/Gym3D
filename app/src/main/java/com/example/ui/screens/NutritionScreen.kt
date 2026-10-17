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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NutritionScreen(
    userProfile: UserProfileEntity,
    todayLogs: List<NutritionLogEntity>,
    onAddMealLog: (String, String, Int, Float, Float, Float) -> Unit,
    onDeleteMealLog: (Long) -> Unit,
    onWeeklyFluxAdjust: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalLoggedCals = todayLogs.sumOf { it.calories }
    val totalLoggedProtein = todayLogs.sumOf { it.proteinG.toDouble() }.toFloat()
    val totalLoggedCarbs = todayLogs.sumOf { it.carbsG.toDouble() }.toFloat()
    val totalLoggedFat = todayLogs.sumOf { it.fatG.toDouble() }.toFloat()

    var showAddMealDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("nutrition_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "ADAPTIVE NUTRITION COACH",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberEmerald,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Dynamic Energy Expenditure",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary
                )
            }
        }

        // 2. Calorie Balance Card (Mifflin-St Jeor Engine)
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calories", tint = CyberEmerald, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("TARGET DAILY ENERGY", style = MaterialTheme.typography.labelSmall, color = CyberEmerald, fontWeight = FontWeight.Bold)
                            Text("${userProfile.targetCalories} kcal", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("REMAINING", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        val remaining = userProfile.targetCalories - totalLoggedCals
                        Text(
                            text = "$remaining kcal",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (remaining >= 0) ElectricLime else BrightAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Macro Bars
                MacroProgressBar("Protein", totalLoggedProtein, userProfile.targetProteinG.toFloat(), "g", ElectricLime)
                Spacer(modifier = Modifier.height(8.dp))
                MacroProgressBar("Carbohydrates", totalLoggedCarbs, userProfile.targetCarbsG.toFloat(), "g", NeonCyan)
                Spacer(modifier = Modifier.height(8.dp))
                MacroProgressBar("Fats", totalLoggedFat, userProfile.targetFatG.toFloat(), "g", BrightAmber)
            }
        }

        // 3. Weekly Dynamic Caloric Flux Check-In Card
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(BrightAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Flux", tint = BrightAmber, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "WEEKLY MACRO FLUX ALGORITHM",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrightAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Monitors 7-day rolling bodyweight. If rate of progress stalls below target (-0.5%/week on cut), calories auto-decrement by 125 kcal. If losing too fast (>1.2%/week), calories increase by 100 kcal to protect muscle tissue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { onWeeklyFluxAdjust(-125) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-125 kcal (Stall Fix)", style = MaterialTheme.typography.labelSmall, color = BrightAmber, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { onWeeklyFluxAdjust(100) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+100 kcal (Sparing)", style = MaterialTheme.typography.labelSmall, color = CyberEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Meal Log Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S FOOD INTAKE (${todayLogs.size} MEALS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberEmerald)
                        .clickable { showAddMealDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF0F1115), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Meal", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0F1115), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (todayLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Restaurant, contentDescription = "Meals", tint = TextMuted, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No meals logged yet today", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Tap 'Log Meal' above to record protein and calories", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        } else {
            items(todayLogs) { meal ->
                MealCard(meal = meal, onDelete = { onDeleteMealLog(meal.id) })
            }
        }
    }

    // Add Meal Dialog
    if (showAddMealDialog) {
        var mealName by remember { mutableStateOf("") }
        var mealType by remember { mutableStateOf("Lunch") }
        var calsStr by remember { mutableStateOf("450") }
        var proteinStr by remember { mutableStateOf("40") }
        var carbsStr by remember { mutableStateOf("45") }
        var fatStr by remember { mutableStateOf("12") }

        AlertDialog(
            onDismissRequest = { showAddMealDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Log Food & Macronutrients", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mealName,
                        onValueChange = { mealName = it },
                        label = { Text("Food / Meal Name") },
                        placeholder = { Text("e.g. Grilled Chicken & Rice") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = calsStr,
                        onValueChange = { calsStr = it },
                        label = { Text("Calories (kcal)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = proteinStr,
                            onValueChange = { proteinStr = it },
                            label = { Text("Protein (g)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                        OutlinedTextField(
                            value = carbsStr,
                            onValueChange = { carbsStr = it },
                            label = { Text("Carbs (g)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                        OutlinedTextField(
                            value = fatStr,
                            onValueChange = { fatStr = it },
                            label = { Text("Fat (g)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cals = calsStr.toIntOrNull() ?: 400
                        val p = proteinStr.toFloatOrNull() ?: 35f
                        val c = carbsStr.toFloatOrNull() ?: 40f
                        val f = fatStr.toFloatOrNull() ?: 10f
                        val name = mealName.ifBlank { "Custom Meal" }

                        onAddMealLog(name, mealType, cals, p, c, f)
                        showAddMealDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald)
                ) {
                    Text("Save Meal", color = Color(0xFF0F1115), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAddMealDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MacroProgressBar(
    label: String,
    current: Float,
    target: Float,
    unit: String,
    color: Color
) {
    val pct = (current / target.coerceAtLeast(1f)).coerceIn(0f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("${current.toInt()} / ${target.toInt()} $unit", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun MealCard(
    meal: NutritionLogEntity,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(meal.foodName, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${meal.calories} kcal • P: ${meal.proteinG.toInt()}g • C: ${meal.carbsG.toInt()}g • F: ${meal.fatG.toInt()}g",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}
