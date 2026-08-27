package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.home_logo
import com.seashore.zoonzoon.generated.resources.ic_plus
import com.seashore.zoonzoon.generated.resources.ic_unlock
import com.seashore.zoonzoon.ui.fig.FigAsset
import com.seashore.zoonzoon.ui.fig.FigCircleButton
import com.seashore.zoonzoon.ui.fig.FigColor
import com.seashore.zoonzoon.ui.fig.figText

/** Figma `-top-bar` (4028:2871): 345×50 at (15, 54). */
val TopBarWidth = 345.dp
val TopBarHeight = 50.dp
val TopBarX = 15.dp
val TopBarY = 54.dp

@Composable
fun TopBar(
    onLockClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.size(TopBarWidth, TopBarHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigCircleButton(onClick = onLockClick, diameter = 45.dp) {
            FigAsset(
                resource = Res.drawable.ic_unlock,
                width = 11.6667.dp,
                height = 15.dp,
                modifier = Modifier.offset(x = (-0.17).dp, y = 0.5.dp)
            )
        }
        Logo()
        FigCircleButton(onClick = onAddClick, diameter = 45.dp) {
            FigAsset(
                resource = Res.drawable.ic_plus,
                width = 9.33333.dp,
                height = 8.66667.dp
            )
        }
    }
}

/** Figma `-state-sign` (2097:11859): a 50×50 mark with the ™ hanging off its top-right corner. */
@Composable
private fun Logo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(50.dp)) {
        FigAsset(resource = Res.drawable.home_logo, width = 50.dp, height = 50.dp)
        Text(
            text = "™",
            color = FigColor.white,
            style = figText(size = 18),
            modifier = Modifier
                .offset(x = 50.dp)
                .wrapContentSize(align = Alignment.TopStart, unbounded = true)
        )
    }
}
