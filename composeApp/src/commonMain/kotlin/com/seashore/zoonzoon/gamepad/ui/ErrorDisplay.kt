package com.seashore.zoonzoon.gamepad.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class ErrorSeverity { WARNING, ERROR }

data class DisplayError(
    val message: String,
    val severity: ErrorSeverity = ErrorSeverity.ERROR,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

/**
 * ErrorDisplay shows error messages to the user.
 *
 * **Validates: Requirements 10.1, 10.4**
 */
@Composable
fun ErrorDisplay(
    error: DisplayError?,
    modifier: Modifier = Modifier
) {
    if (error == null) return

    val containerColor = when (error.severity) {
        ErrorSeverity.ERROR   -> MaterialTheme.colorScheme.errorContainer
        ErrorSeverity.WARNING -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (error.severity) {
        ErrorSeverity.ERROR   -> MaterialTheme.colorScheme.onErrorContainer
        ErrorSeverity.WARNING -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (error.severity == ErrorSeverity.ERROR) "⚠ Error" else "⚠ Warning",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            if (error.actionLabel != null && error.onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = error.onAction) {
                    Text(error.actionLabel)
                }
            }
        }
    }
}

object CommonErrors {
    fun controllerDiscoveryFailed(onRetry: () -> Unit) = DisplayError(
        message = "Failed to discover controllers. Ensure your controller is in pairing mode.",
        severity = ErrorSeverity.ERROR,
        actionLabel = "Retry",
        onAction = onRetry
    )

    val mfiOnlyRestriction = DisplayError(
        message = "Only MFi-certified controllers are supported on iOS.",
        severity = ErrorSeverity.WARNING
    )

    val connectionLost = DisplayError(
        message = "Controller connection lost. Vibration has been stopped.",
        severity = ErrorSeverity.ERROR
    )
}
