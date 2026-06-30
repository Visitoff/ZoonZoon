package com.seashore.zoonzoon.ui.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentEnd
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentStart
import com.seashore.zoonzoon.ui.components.IosGroupedCard
import com.seashore.zoonzoon.ui.components.IosLargeTitle
import com.seashore.zoonzoon.ui.components.IosScreenBackground

@Composable
fun MultiplayerScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        emoji = "👥",
        title = "Multiplayer",
        badge = "Coming soon",
        subtitle = "Host a private or public room and let one guest control your vibration in real time.",
        modifier = modifier
    )
}

@Composable
fun AiScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        emoji = "🤖",
        title = "AI Companion",
        badge = "In development",
        subtitle = "An in-app AI companion that crafts and adapts vibration patterns to your mood.",
        modifier = modifier
    )
}

@Composable
private fun PlaceholderScreen(
    emoji: String,
    title: String,
    badge: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    IosScreenBackground(modifier = modifier, accentGlow = true) {
        Column(modifier = Modifier.fillMaxSize()) {
            IosLargeTitle(text = title)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                IosGroupedCard(strong = true) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            ZoonZoonAccentStart.copy(alpha = 0.25f),
                                            ZoonZoonAccentEnd.copy(alpha = 0.25f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 34.sp)
                        }
                        Text(
                            text = badge.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = ZoonZoonAccentStart,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
