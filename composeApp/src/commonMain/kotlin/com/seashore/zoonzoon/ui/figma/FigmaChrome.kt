package com.seashore.zoonzoon.ui.figma

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.fig_home_indicator
import com.seashore.zoonzoon.generated.resources.fig_status_bar
import org.jetbrains.compose.resources.painterResource

/** Figma `Screen Components / Status Bar` — 375×44 @ y=0. */
@Composable
fun FigmaStatusBar(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.fig_status_bar),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    )
}

/** Figma `Screen Components / Home Indicator` — 375×34 @ y=778. */
@Composable
fun FigmaHomeIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(Res.drawable.fig_home_indicator),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .requiredSize(375.dp, 34.dp)
        )
    }
}
