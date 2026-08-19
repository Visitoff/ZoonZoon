package com.seashore.zoonzoon.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.fig_ic_gamepad
import com.seashore.zoonzoon.generated.resources.fig_ic_gamepad_and_phone
import com.seashore.zoonzoon.generated.resources.fig_ic_phone
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.platform.openStoreReviewPage
import com.seashore.zoonzoon.settings.AppThemeMode
import com.seashore.zoonzoon.settings.VibrationTarget
import com.seashore.zoonzoon.ui.figma.FigmaCircleButton
import com.seashore.zoonzoon.ui.figma.FigmaFill
import com.seashore.zoonzoon.ui.figma.FigmaScreenTitle
import com.seashore.zoonzoon.ui.figma.FigmaSectionTitle
import com.seashore.zoonzoon.ui.figma.FigmaSelectableItem
import com.seashore.zoonzoon.ui.figma.FigmaSelector
import com.seashore.zoonzoon.ui.figma.FigmaTokens
import com.seashore.zoonzoon.ui.home.HomeFrameHeight
import com.seashore.zoonzoon.ui.home.HomeFrameWidth
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SettingsScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val vibrationTarget by viewModel.vibrationTarget.collectAsState()
    val language by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val strings = remember(language) { stringsFor(language) }

    Box(modifier = modifier.requiredSize(HomeFrameWidth, HomeFrameHeight)) {
        LazyColumn(
            modifier = Modifier
                .offset(x = FigmaTokens.Spacing.s15, y = 54.dp)
                .requiredSize(345.dp, 648.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { FigmaScreenTitle(text = strings.settings) }
            item { Spacer(Modifier.height(26.dp)) }

            item { FigmaSectionTitle(text = strings.vibrationTarget) }
            item {
                Row(
                    modifier = Modifier.requiredSize(345.dp, 70.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VibrationTarget.entries.forEach { target ->
                        val selected = vibrationTarget == target
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            FigmaCircleButton(
                                onClick = { viewModel.setVibrationTarget(target) },
                                size = 70.dp,
                                fill = if (selected) FigmaFill.Coral else FigmaFill.Dark
                            ) {
                                Image(
                                    painter = painterResource(target.icon()),
                                    contentDescription = null,
                                    modifier = Modifier.size(35.dp),
                                    colorFilter = ColorFilter.tint(FigmaTokens.Color.white)
                                )
                            }
                        }
                    }
                }
            }

            item { FigmaSectionTitle(text = strings.language) }
            item {
                FigmaSelector(
                    options = AppLanguage.entries.map { it.displayName },
                    selectedIndex = AppLanguage.entries.indexOf(language),
                    onSelected = { index -> viewModel.setLanguage(AppLanguage.entries[index]) }
                )
            }

            item { FigmaSectionTitle(text = strings.theme) }
            item {
                FigmaSelector(
                    options = listOf(strings.themeLight, strings.themeDark, strings.themeSystem),
                    selectedIndex = when (themeMode) {
                        AppThemeMode.LIGHT -> 0
                        AppThemeMode.DARK -> 1
                        AppThemeMode.SYSTEM -> 2
                    },
                    onSelected = { index ->
                        viewModel.setThemeMode(
                            when (index) {
                                0 -> AppThemeMode.LIGHT
                                1 -> AppThemeMode.DARK
                                else -> AppThemeMode.SYSTEM
                            }
                        )
                    }
                )
            }

            item {
                FigmaSelectableItem(
                    emoji = "⭐",
                    title = strings.leaveReview,
                    subtitle = "ZoonZoon 2",
                    selected = false,
                    showRadio = false,
                    signSelected = false,
                    onClick = { openStoreReviewPage() }
                )
            }
        }
    }
}

private fun VibrationTarget.icon(): DrawableResource = when (this) {
    VibrationTarget.PHONE_ONLY -> Res.drawable.fig_ic_phone
    VibrationTarget.GAMEPAD_ONLY -> Res.drawable.fig_ic_gamepad
    VibrationTarget.GAMEPAD_AND_PHONE -> Res.drawable.fig_ic_gamepad_and_phone
}
