package com.seashore.zoonzoon.gamepad.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.VibrationPattern

private data class PatternOption(
    val label: String,
    val description: String,
    val emoji: String,
    val pattern: VibrationPattern
)

private val patternOptions = listOf(
    PatternOption(
        label = "Constant",
        description = "Steady vibration at fixed intensity",
        emoji = "▬",
        pattern = VibrationPattern.Constant
    ),
    PatternOption(
        label = "Pulse",
        description = "Rhythmic on/off pulses",
        emoji = "◉",
        pattern = VibrationPattern.Pulse()
    ),
    PatternOption(
        label = "Wave",
        description = "Smooth sine wave modulation",
        emoji = "〜",
        pattern = VibrationPattern.Wave()
    ),
    PatternOption(
        label = "Custom",
        description = "User-defined frame sequence",
        emoji = "✦",
        pattern = VibrationPattern.Custom(emptyList())
    )
)

/**
 * PatternSelectionCard — grid of selectable pattern cards.
 *
 * Each card shows the pattern name, a visual hint, and a short description.
 * The active pattern is highlighted with the primary color.
 *
 * **Validates: Requirement 7.7**
 */
@Composable
fun PatternSelectionCard(
    activePattern: VibrationPattern,
    onPatternSelected: (VibrationPattern) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Vibration Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2-column grid of pattern cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(patternOptions) { option ->
                    PatternCard(
                        option = option,
                        isSelected = activePattern::class == option.pattern::class,
                        onClick = { onPatternSelected(option.pattern) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PatternCard(
    option: PatternOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val border = if (isSelected)
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    else
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Visual hint
            Text(
                text = option.emoji,
                style = MaterialTheme.typography.headlineMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else contentColor
            )

            // Pattern name
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            // Description
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
