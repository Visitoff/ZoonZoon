package com.seashore.zoonzoon.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.DisplayError
import com.seashore.zoonzoon.gamepad.model.ErrorSeverity

@Composable
fun ErrorDisplay(
    error: DisplayError?,
    modifier: Modifier = Modifier
) {
    if (error == null) return

    val containerColor = when (error.severity) {
        ErrorSeverity.ERROR -> MaterialTheme.colorScheme.errorContainer
        ErrorSeverity.WARNING -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (error.severity) {
        ErrorSeverity.ERROR -> MaterialTheme.colorScheme.onErrorContainer
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
