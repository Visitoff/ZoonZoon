package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.ui.figma.FigmaCircleButton
import com.seashore.zoonzoon.ui.figma.FigmaFill
import com.seashore.zoonzoon.ui.figma.FigmaPlusIcon
import com.seashore.zoonzoon.ui.figma.FigmaSignLogo
import com.seashore.zoonzoon.ui.figma.FigmaUnlockIcon

/** Figma `-top-bar` (4028:2871): 345×50 @ y=79. */
@Composable
fun TopBar(
    onLockClick: () -> Unit,
    onPlusClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(345.dp)
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigmaCircleButton(onClick = onLockClick, size = 45.dp, fill = FigmaFill.Dark) {
            FigmaUnlockIcon(Modifier.size(16.dp))
        }
        FigmaSignLogo()
        FigmaCircleButton(onClick = onPlusClick, size = 45.dp, fill = FigmaFill.Dark) {
            FigmaPlusIcon(Modifier.size(16.dp))
        }
    }
}
