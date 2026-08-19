package com.seashore.zoonzoon.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.ui.figma.FigmaCanvas
import com.seashore.zoonzoon.ui.figma.FigmaScreenGlow
import com.seashore.zoonzoon.ui.home.HomeFrameHeight
import com.seashore.zoonzoon.ui.home.HomeFrameWidth
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

    FigmaCanvas(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val scale = maxWidth / HomeFrameWidth
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
                    .requiredSize(HomeFrameWidth, HomeFrameHeight)
            ) {
                FigmaScreenGlow()
                when (selectedTab) {
                    AppTab.Home -> HomeScreen(viewModel = viewModel)
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
                ZoonZoonBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 22.dp, y = 701.dp)
                )
            }
        }
    }
}
