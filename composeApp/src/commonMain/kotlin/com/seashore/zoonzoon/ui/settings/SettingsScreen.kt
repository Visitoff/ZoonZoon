package com.seashore.zoonzoon.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.platform.openStoreReviewPage
import com.seashore.zoonzoon.settings.AppThemeMode
import com.seashore.zoonzoon.settings.VibrationTarget
import com.seashore.zoonzoon.ui.components.IosAccentButton
import com.seashore.zoonzoon.ui.components.IosGroupedCard
import com.seashore.zoonzoon.ui.components.IosGroupedDivider
import com.seashore.zoonzoon.ui.components.IosLargeTitle
import com.seashore.zoonzoon.ui.components.IosListRow
import com.seashore.zoonzoon.ui.components.IosScreenBackground
import com.seashore.zoonzoon.ui.components.IosSectionHeader
import com.seashore.zoonzoon.ui.components.IosSegmentedControl

@Composable
fun SettingsScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val vibrationTarget by viewModel.vibrationTarget.collectAsState()
    val language by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val strings = remember(language) { stringsFor(language) }

    IosScreenBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { IosLargeTitle(text = strings.settings) }

            item { IosSectionHeader(text = strings.vibrationTarget) }
            item {
                IosGroupedCard {
                    VibrationTarget.entries.forEachIndexed { index, target ->
                        val label = when (target) {
                            VibrationTarget.PHONE_ONLY -> strings.phoneOnly
                            VibrationTarget.GAMEPAD_ONLY -> strings.gamepadOnly
                            VibrationTarget.GAMEPAD_AND_PHONE -> strings.gamepadAndPhone
                        }
                        IosListRow(
                            title = label,
                            selected = vibrationTarget == target,
                            onClick = { viewModel.setVibrationTarget(target) },
                            trailing = {
                                if (vibrationTarget == target) {
                                    Text(
                                        text = "✓",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        )
                        if (index < VibrationTarget.entries.lastIndex) {
                            IosGroupedDivider()
                        }
                    }
                }
            }

            item { IosSectionHeader(text = strings.language) }
            item {
                IosGroupedCard {
                    IosSegmentedControl(
                        options = AppLanguage.entries.map { it.displayName },
                        selectedIndex = AppLanguage.entries.indexOf(language),
                        onSelected = { index -> viewModel.setLanguage(AppLanguage.entries[index]) },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            item { IosSectionHeader(text = strings.theme) }
            item {
                IosGroupedCard {
                    IosSegmentedControl(
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
                        },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            item { IosSectionHeader(text = strings.leaveReview) }
            item {
                IosGroupedCard {
                    IosAccentButton(
                        text = strings.leaveReview,
                        onClick = { openStoreReviewPage() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }

            item { IosSectionHeader(text = "About") }
            item {
                IosGroupedCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "ZoonZoon 2",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Version 1.4 · ${strings.comingSoon} (Subscription)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
