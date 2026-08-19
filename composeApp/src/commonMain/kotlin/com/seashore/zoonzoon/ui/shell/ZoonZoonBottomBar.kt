package com.seashore.zoonzoon.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.ui.figma.FigmaCircleButton
import com.seashore.zoonzoon.ui.figma.FigmaFill
import com.seashore.zoonzoon.ui.figma.FigmaNavBarBackground
import com.seashore.zoonzoon.ui.figma.FigmaNavIcon
import com.seashore.zoonzoon.ui.figma.FigmaTokens

/** Figma `-bottom-bar` (4025:2853): 330×70 @ y=701. */
@Composable
fun ZoonZoonBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(330.dp, 70.dp),
        contentAlignment = Alignment.Center
    ) {
        FigmaNavBarBackground(Modifier.size(330.dp, 70.dp))
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
                    FigmaNavIcon(
                        tab = tab,
                        modifier = Modifier.size(if (tab == AppTab.Home) 16.dp else 18.dp)
                    )
                }
            }
        }
    }
}
