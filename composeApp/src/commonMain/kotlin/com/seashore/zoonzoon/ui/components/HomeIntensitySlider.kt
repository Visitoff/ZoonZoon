package com.seashore.zoonzoon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentStart
import kotlin.math.roundToInt

@Composable
fun HomeIntensitySlider(
    intensity: Float,
    onIntensityChanged: (Float) -> Unit,
    label: String = "Intensity",
    modifier: Modifier = Modifier
) {
    IosGroupedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(intensity * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ZoonZoonAccentStart
                )
            }

            Slider(
                value = intensity,
                onValueChange = onIntensityChanged,
                valueRange = 0f..1f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = ZoonZoonAccentStart,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                    activeTickColor = Color.White.copy(alpha = 0.7f),
                    inactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                )
            )
        }
    }
}
