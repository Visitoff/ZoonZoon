package com.seashore.zoonzoon.gamepad.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.ConnectionState

/**
 * ConnectionStatusCard displays the current controller connection status.
 *
 * **Validates: Requirement 7.4**
 */
@Composable
fun ConnectionStatusCard(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (connectionState) {
        is ConnectionState.Disconnected -> "Disconnected" to MaterialTheme.colorScheme.error
        is ConnectionState.Scanning     -> "Scanning..."  to MaterialTheme.colorScheme.primary
        is ConnectionState.Connected    -> "Connected"    to MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Controller Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )

            if (connectionState is ConnectionState.Connected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = connectionState.controllerType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
