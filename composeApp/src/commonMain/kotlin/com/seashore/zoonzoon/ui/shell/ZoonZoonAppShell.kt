package com.seashore.zoonzoon.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.ui.fig.FigCanvas
import com.seashore.zoonzoon.ui.fig.FigScreenGlow
import com.seashore.zoonzoon.ui.figma.LocalHazeState
import com.seashore.zoonzoon.ui.glass.ProvideGlassBackdrop
import com.seashore.zoonzoon.ui.glass.glassSource
import com.seashore.zoonzoon.ui.home.HomeScreen
import com.seashore.zoonzoon.ui.patterns.PatternsScreen
import com.seashore.zoonzoon.ui.placeholder.AiScreen
import com.seashore.zoonzoon.ui.placeholder.MultiplayerScreen
import com.seashore.zoonzoon.ui.settings.SettingsScreen
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun ZoonZoonAppShell(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Home) }
    val hazeState = rememberHazeState()

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        ProvideGlassBackdrop {
            FigCanvas(modifier = modifier.hazeSource(state = hazeState)) { frameHeight ->
                Box(Modifier.fillMaxSize().glassSource()) {
                    FigScreenGlow()
                    if (selectedTab == AppTab.Home) {
                        HomeScreen(
                            viewModel = viewModel,
                            showStage = true,
                            showChrome = false
                        )
                    }
                }
                if (selectedTab == AppTab.Home) {
                    HomeScreen(
                        viewModel = viewModel,
                        showStage = false,
                        showChrome = true
                    )
                } else {
                    Box(Modifier.fillMaxSize()) {
                        when (selectedTab) {
                            AppTab.Home -> Unit
                            AppTab.Multiplayer -> MultiplayerScreen(Modifier.fillMaxSize())
                            AppTab.Ai -> AiScreen(Modifier.fillMaxSize())
                            AppTab.Patterns -> PatternsScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            AppTab.Settings -> SettingsScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                ZoonZoonBottomBar(
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
                    modifier = Modifier
                        .zIndex(1f)
                        .offset(
                            x = BottomBarX,
                            y = frameHeight - BottomBarBottomMargin - BottomBarHeight
                        )
                )
            }
        }
    }
}
