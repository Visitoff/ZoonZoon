package com.seashore.zoonzoon.ui.figma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.home_bg_glow_hi
import com.seashore.zoonzoon.generated.resources.home_bg_glow_lo
import org.jetbrains.compose.resources.painterResource

@Composable
fun FigmaCanvas(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier.fillMaxSize().background(FigmaBlack), content = content)
}

/** Figma Screen 23 background vectors — glow only, not the full UI frame. */
@Composable
fun BoxScope.FigmaScreenGlow() {
    Image(
        painter = painterResource(Res.drawable.home_bg_glow_lo),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .offset(x = GlowOffsetX, y = GlowBottomY)
            .size(GlowWidth, GlowHeight)
    )
    Image(
        painter = painterResource(Res.drawable.home_bg_glow_hi),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .offset(x = GlowOffsetX, y = GlowTopY)
            .size(GlowWidth, GlowHeight)
    )
}

private val GlowOffsetX = (-85.704).dp
private val GlowTopY = 79.5.dp
private val GlowBottomY = 474.dp
private val GlowWidth = 553.944.dp
private val GlowHeight = 433.038.dp
