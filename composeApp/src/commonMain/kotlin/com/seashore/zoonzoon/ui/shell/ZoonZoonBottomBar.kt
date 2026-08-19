package com.seashore.zoonzoon.ui.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.fig_bb_bg
import com.seashore.zoonzoon.generated.resources.nav_ic_ai
import com.seashore.zoonzoon.generated.resources.nav_ic_duo
import com.seashore.zoonzoon.generated.resources.nav_ic_home
import com.seashore.zoonzoon.generated.resources.nav_ic_patterns
import com.seashore.zoonzoon.generated.resources.nav_ic_settings
import com.seashore.zoonzoon.ui.figma.FigmaCircleButton
import com.seashore.zoonzoon.ui.figma.FigmaFill
import com.seashore.zoonzoon.ui.figma.FigmaIconIdle
import com.seashore.zoonzoon.ui.figma.FigmaTokens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Figma `-bottom-bar`: 330×70 at (23, 703), buttons 60×60 with 5dp inset/gap. */
@Composable
fun BottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(330.dp, 70.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.fig_bb_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(330.dp, 70.dp)
        )
        Row(
            modifier = Modifier.size(320.dp, 60.dp),
            horizontalArrangement = Arrangement.spacedBy(FigmaTokens.Spacing.s5),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                FigmaCircleButton(
                    onClick = { onTabSelected(tab) },
                    size = 60.dp,
                    fill = if (selected) FigmaFill.Coral else FigmaFill.Dark
                ) {
                    Image(
                        painter = painterResource(tab.icon()),
                        contentDescription = tab.label,
                        modifier = Modifier.size(
                            width = if (tab == AppTab.Home) 16.dp else 18.dp,
                            height = if (tab == AppTab.Home) 16.dp else 18.dp
                        ),
                        colorFilter = ColorFilter.tint(
                            if (selected) FigmaTokens.Color.white else FigmaIconIdle
                        )
                    )
                }
            }
        }
    }
}

private fun AppTab.icon(): DrawableResource = when (this) {
    AppTab.Multiplayer -> Res.drawable.nav_ic_duo
    AppTab.Ai -> Res.drawable.nav_ic_ai
    AppTab.Home -> Res.drawable.nav_ic_home
    AppTab.Patterns -> Res.drawable.nav_ic_patterns
    AppTab.Settings -> Res.drawable.nav_ic_settings
}
