package com.seashore.zoonzoon.ui.placeholder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.ui.figma.FigmaScreenTitle
import com.seashore.zoonzoon.ui.figma.FigmaTokens
import com.seashore.zoonzoon.ui.figma.figmaGilroy
import com.seashore.zoonzoon.ui.home.HomeFrameHeight
import com.seashore.zoonzoon.ui.home.HomeFrameWidth

@Composable
fun MultiplayerScreen(modifier: Modifier = Modifier) {
    PlaceholderFrame(title = "Multiplayer", modifier = modifier)
}

@Composable
fun AiScreen(modifier: Modifier = Modifier) {
    PlaceholderFrame(title = "AI", modifier = modifier)
}

@Composable
private fun PlaceholderFrame(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.requiredSize(HomeFrameWidth, HomeFrameHeight)) {
        Column(
            modifier = Modifier
                .offset(x = FigmaTokens.Spacing.s15, y = 54.dp)
                .requiredSize(345.dp, 648.dp)
        ) {
            FigmaScreenTitle(text = title)
            Spacer(Modifier.height(26.dp))
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Coming soon",
                    color = FigmaTokens.Color.white.copy(alpha = 0.5f),
                    style = figmaGilroy(size = 16)
                )
            }
        }
    }
}
