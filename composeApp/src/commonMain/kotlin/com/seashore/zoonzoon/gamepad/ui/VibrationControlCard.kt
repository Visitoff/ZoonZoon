package com.seashore.zoonzoon.gamepad.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.VibrationState

/**
 * VibrationControlCard provides the main power button for enabling/disabling vibration.
 *
 * **Validates: Requirements 7.5, 7.6**
 */
@Composable
fun VibrationControlCard(
    vibrationState: VibrationState,
    onVibrationToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Vibration Control",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            val statusText  = if (vibrationState.enabled) "Enabled"  else "Disabled"
            val statusColor = if (vibrationState.enabled) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.onSurfaceVariant

            Text(
                text = "Status: $statusText",
                style = MaterialTheme.typography.bodyLarge,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onVibrationToggle(!vibrationState.enabled) },
                modifier = Modifier.size(width = 160.dp, height = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vibrationState.enabled) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor   = if (vibrationState.enabled) MaterialTheme.colorScheme.onPrimary
                                     else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = if (vibrationState.enabled) "Turn Off" else "Turn On",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
