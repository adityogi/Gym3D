package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GymEquipmentViewModel
import com.example.ui.screens.EquipmentCatalogScreen
import com.example.ui.screens.EquipmentDetailScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Gym3DApp()
            }
        }
    }
}

@Composable
fun Gym3DApp(
    viewModel: GymEquipmentViewModel = viewModel()
) {
    var activeEquipmentId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (activeEquipmentId == null) {
            EquipmentCatalogScreen(
                viewModel = viewModel,
                onSelectEquipment = { id ->
                    viewModel.selectEquipment(id)
                    activeEquipmentId = id
                }
            )
        } else {
            EquipmentDetailScreen(
                equipmentId = activeEquipmentId!!,
                viewModel = viewModel,
                onNavigateBack = { activeEquipmentId = null }
            )
        }
    }
}
