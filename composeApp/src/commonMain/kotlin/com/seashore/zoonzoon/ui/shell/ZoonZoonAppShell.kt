package com.seashore.zoonzoon.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.seashore.zoonzoon.gamepad.theme.GlassBackground
import com.seashore.zoonzoon.gamepad.theme.LocalAppDarkTheme
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
    val dark = LocalAppDarkTheme.current

    GlassBackground(dark = dark, modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    AppTab.Multiplayer -> MultiplayerScreen(Modifier.fillMaxSize())
                    AppTab.Ai -> AiScreen(Modifier.fillMaxSize())
                    AppTab.Home -> HomeScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                    AppTab.Patterns -> PatternsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                    AppTab.Settings -> SettingsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                }
            }

            ZoonZoonBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    }
}
