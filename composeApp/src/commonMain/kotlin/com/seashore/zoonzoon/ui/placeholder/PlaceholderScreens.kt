package com.seashore.zoonzoon.ui.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import com.seashore.zoonzoon.ui.figma.FigmaBlack
import com.seashore.zoonzoon.ui.figma.FigmaTokens
import com.seashore.zoonzoon.ui.figma.figmaGilroy

@Composable
fun MultiplayerScreen(modifier: Modifier = Modifier) {
    PlaceholderContent(title = "Multiplayer", modifier = modifier)
}

@Composable
fun AiScreen(modifier: Modifier = Modifier) {
    PlaceholderContent(title = "AI", modifier = modifier)
}

@Composable
private fun PlaceholderContent(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FigmaBlack),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title — Coming soon",
            color = FigmaTokens.Color.white.copy(alpha = 0.5f),
            style = figmaGilroy(size = 18)
        )
    }
}
