package com.seashore.zoonzoon.ui.placeholder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "$title — Coming soon",
            style = MaterialTheme.typography.titleLarge
        )
    }
}
