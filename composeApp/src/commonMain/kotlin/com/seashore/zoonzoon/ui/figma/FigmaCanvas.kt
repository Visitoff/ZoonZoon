package com.seashore.zoonzoon.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun FigmaCanvas(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = rememberHazeState()
    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(FigmaBlack)
                .hazeSource(state = hazeState),
            content = content
        )
    }
}

@Composable
fun BoxScope.FigmaScreenGlow() {
    FigmaGlowLo(
        modifier = Modifier
            .offset(x = GlowOffsetX, y = GlowTopY)
            .size(GlowWidth, GlowHeight)
    )
    FigmaGlowHi(
        modifier = Modifier
            .offset(x = GlowOffsetX, y = GlowBottomY)
            .size(GlowWidth, GlowHeight)
    )
}

private val GlowOffsetX = (-85.704).dp
private val GlowTopY = 79.5.dp
private val GlowBottomY = 474.dp
private val GlowWidth = 553.944.dp
private val GlowHeight = 433.038.dp
