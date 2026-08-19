package com.seashore.zoonzoon.ui.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.ui.home.HomeScreen
import com.seashore.zoonzoon.ui.patterns.PatternsScreen
import com.seashore.zoonzoon.ui.placeholder.AiScreen
import com.seashore.zoonzoon.ui.placeholder.MultiplayerScreen
import com.seashore.zoonzoon.ui.settings.SettingsScreen

@Composable
fun ZoonZoonAppShell(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Home) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.emoji) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            AppTab.Home -> HomeScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            AppTab.Multiplayer -> MultiplayerScreen(
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            AppTab.Ai -> AiScreen(
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            AppTab.Patterns -> PatternsScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            AppTab.Settings -> SettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        }
    }
}
