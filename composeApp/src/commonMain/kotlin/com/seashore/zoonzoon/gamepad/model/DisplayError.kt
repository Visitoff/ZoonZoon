package com.seashore.zoonzoon.gamepad.model

enum class ErrorSeverity { WARNING, ERROR }

data class DisplayError(
    val message: String,
    val severity: ErrorSeverity = ErrorSeverity.ERROR,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

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
