package com.seashore.zoonzoon.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seashore.zoonzoon.gamepad.theme.LocalGlassTokens
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentEnd
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentStart
import com.seashore.zoonzoon.gamepad.theme.glassSurface

/**
 * Floating "Liquid Glass" tab bar — a frosted pill hovering above the gradient,
 * with the centered Home tab elevated into an accent-gradient orb.
 */
@Composable
fun ZoonZoonBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassSurface(
                    shape = RoundedCornerShape(30.dp),
                    tokens = tokens,
                    strong = true,
                    elevation = 22.dp
                )
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { tab ->
                IosTabItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    enlarged = tab == AppTab.Home,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun IosTabItem(
    tab: AppTab,
    selected: Boolean,
    enlarged: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val accentBrush = Brush.linearGradient(listOf(ZoonZoonAccentStart, ZoonZoonAccentEnd))
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant
    val tint = if (selected) ZoonZoonAccentStart else inactive

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        if (enlarged) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .then(
                        if (selected) {
                            Modifier.shadow(10.dp, CircleShape, clip = false, spotColor = ZoonZoonAccentEnd)
                        } else {
                            Modifier
                        }
                    )
                    .clip(CircleShape)
                    .then(
                        if (selected) {
                            Modifier.background(accentBrush)
                        } else {
                            Modifier.background(inactive.copy(alpha = 0.18f))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.emoji,
                    fontSize = 18.sp,
                    color = if (selected) androidx.compose.ui.graphics.Color.White else inactive
                )
            }
        } else {
            Text(text = tab.emoji, fontSize = 20.sp, color = tint)
        }
        Text(
            text = tab.label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = tint
        )
    }
}
