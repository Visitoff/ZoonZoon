package com.seashore.zoonzoon.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.ic_ai
import com.seashore.zoonzoon.generated.resources.ic_duo
import com.seashore.zoonzoon.generated.resources.ic_home
import com.seashore.zoonzoon.generated.resources.ic_patterns
import com.seashore.zoonzoon.generated.resources.ic_settings
import com.seashore.zoonzoon.generated.resources.nav_bg
import com.seashore.zoonzoon.ui.fig.FigAsset
import com.seashore.zoonzoon.ui.fig.FigCircleButton
import com.seashore.zoonzoon.ui.fig.FigColor
import com.seashore.zoonzoon.ui.glass.GlassTintLight
import com.seashore.zoonzoon.ui.glass.isLiquidGlassEnabled
import org.jetbrains.compose.resources.DrawableResource

/** Figma `-bottom-bar` (4025:2853): 330×70 at (22, 701) — 41 above the frame's bottom edge. */
val BottomBarWidth = 330.dp
val BottomBarHeight = 70.dp
val BottomBarX = 22.dp
val BottomBarBottomMargin = 41.dp

private val BarPadding = 5.dp
private val ButtonSize = 60.dp
private val ButtonGap = 5.dp

private data class NavIcon(
    val resource: DrawableResource,
    val width: Dp,
    val height: Dp
)

private val NavIcons: Map<AppTab, NavIcon> = mapOf(
    AppTab.Multiplayer to NavIcon(Res.drawable.ic_duo, 16.931.dp, 12.3727.dp),
    AppTab.Ai to NavIcon(Res.drawable.ic_ai, 18.dp, 18.dp),
    AppTab.Home to NavIcon(Res.drawable.ic_home, 13.344.dp, 13.2906.dp),
    AppTab.Patterns to NavIcon(Res.drawable.ic_patterns, 15.1.dp, 16.6.dp),
    AppTab.Settings to NavIcon(Res.drawable.ic_settings, 17.4771.dp, 19.6497.dp)
)

@Composable
fun ZoonZoonBottomBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(BottomBarWidth, BottomBarHeight)) {
        FigAsset(
            resource = Res.drawable.nav_bg,
            width = BottomBarWidth,
            height = BottomBarHeight
        )
        Row(
            modifier = Modifier.offset(x = BarPadding, y = BarPadding),
            horizontalArrangement = Arrangement.spacedBy(ButtonGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { tab ->
                val icon = NavIcons.getValue(tab)
                FigCircleButton(
                    onClick = { onSelect(tab) },
                    diameter = ButtonSize,
                    fill = if (isLiquidGlassEnabled()) GlassTintLight else FigColor.buttonDark,
                    coral = tab == selected,
                    selected = tab == selected
                ) {
                    FigAsset(
                        resource = icon.resource,
                        width = icon.width,
                        height = icon.height
                    )
                }
            }
        }
    }
}
