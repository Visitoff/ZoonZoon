package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Example usage of the GamepadVibratorTheme to demonstrate the pink pastel color scheme.
 * This composable shows how the theme colors are applied to various Material3 components.
 */
@Composable
fun ThemeUsageExample() {
    GamepadVibratorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary color demonstration
                Card(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Primary Container",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                
                // Secondary color demonstration
                Card(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Secondary Container",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                
                // Tertiary color demonstration
                Card(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Tertiary Container",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                
                // Button with primary color
                Button(
                    onClick = { /* Demo button */ },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Primary Button")
                }
                
                // Surface variant demonstration
                Surface(
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Surface Variant",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}