package com.seashore.zoonzoon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** @deprecated Use [IosScreenBackground] — kept for call-site compatibility. */
@Composable
fun ZoonZoonBackground(
    modifier: Modifier = Modifier,
    showRipples: Boolean = false,
    content: @Composable () -> Unit
) {
    IosScreenBackground(
        modifier = modifier,
        accentGlow = showRipples,
        content = content
    )
}
