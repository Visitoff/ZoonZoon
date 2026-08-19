package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.home_ic_unlock
import com.seashore.zoonzoon.ui.figma.FigmaCircleButton
import com.seashore.zoonzoon.ui.figma.FigmaFill
import com.seashore.zoonzoon.ui.figma.FigmaSignLogo
import org.jetbrains.compose.resources.painterResource

/** Figma `-top-bar`: 345×50 — lock, logo, lock. */
@Composable
fun TopBar(
    onLockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(345.dp)
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigmaCircleButton(
            onClick = onLockClick,
            size = 45.dp,
            fill = FigmaFill.Dark
        ) {
            Image(
                painter = painterResource(Res.drawable.home_ic_unlock),
                contentDescription = null,
                modifier = Modifier.size(12.dp, 15.dp)
            )
        }
        FigmaSignLogo()
        FigmaCircleButton(
            onClick = onLockClick,
            size = 45.dp,
            fill = FigmaFill.Dark
        ) {
            Image(
                painter = painterResource(Res.drawable.home_ic_unlock),
                contentDescription = null,
                modifier = Modifier.size(12.dp, 15.dp)
            )
        }
    }
}
