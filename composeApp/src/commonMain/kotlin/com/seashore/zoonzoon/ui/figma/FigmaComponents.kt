package com.seashore.zoonzoon.ui.figma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.seashore.zoonzoon.generated.resources.Res
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun FigmaScreenTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = FigmaTokens.Color.white,
            style = figmaGilroy(size = 30, lineHeight = 24)
        )
    }
}

@Composable
fun FigmaSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = FigmaTokens.Color.white,
            style = figmaGilroy(size = 18, lineHeight = 24)
        )
    }
}

/** Figma `-sign-states`: 50×50 logo + ™. */
@Composable
fun FigmaSignLogo(
    modifier: Modifier = Modifier,
    showTm: Boolean = true
) {
    Box(modifier = modifier.size(50.dp)) {
        FigmaLogoImage(modifier = Modifier.fillMaxSize())
        if (showTm) {
            Text(
                text = "™",
                color = FigmaTokens.Color.white,
                style = figmaGilroy(size = 18),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(x = 9.dp.roundToPx(), y = 0) }
            )
        }
    }
}

/** Figma `-sign` emoji button with heart border. */
@Composable
fun FigmaSignButton(
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(60.dp), contentAlignment = Alignment.Center) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(FigmaGradients.SignActiveGlow)
                    .border(1.dp, FigmaTokens.Color.heartBorder, CircleShape)
            )
        }
        FigmaCircleButton(
            onClick = onClick,
            size = 60.dp,
            fill = FigmaFill.Dark,
            stroke = FigmaStroke.Heart
        ) {
            Text(
                text = emoji,
                style = figmaGilroy(size = 24, weight = FontWeight.Normal)
            )
        }
    }
}

@Composable
fun FigmaRadio(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(FigmaFillDark)
            .then(
                if (selected) Modifier.border(1.dp, FigmaCoralStart, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Text(
                text = "✓",
                color = FigmaTokens.Color.white,
                style = figmaGilroy(size = 16)
            )
        }
    }
}

@Composable
fun FigmaSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(RoundedCornerShape(FigmaTokens.Radius.pill))
            .background(FigmaFillDark)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            FigmaPillButton(
                onClick = { onSelected(index) },
                fill = if (selected) FigmaFill.Coral else FigmaFill.Dark,
                modifier = Modifier.weight(1f).height(45.dp)
            ) {
                Text(
                    text = label,
                    color = if (selected) FigmaTokens.Color.white else FigmaIconIdle,
                    style = figmaGilroy(size = 14),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FigmaSelectableItem(
    emoji: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showRadio: Boolean = true,
    signSelected: Boolean = selected,
    forceDarkFill: Boolean = false,
    onLongPress: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(FigmaTokens.Radius.pill))
            .background(
                when {
                    forceDarkFill -> FigmaFillDark
                    selected -> FigmaTokens.Color.surfaceDark
                    else -> FigmaFillDark
                }
            )
            .then(
                if (selected && !forceDarkFill) {
                    Modifier.border(1.dp, FigmaTokens.Color.heartBorder, RoundedCornerShape(FigmaTokens.Radius.pill))
                } else {
                    Modifier
                }
            )
            .pointerInput(onClick, onLongPress) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress?.invoke() }
                )
            }
            .padding(start = 5.dp, end = 20.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            FigmaSignButton(
                emoji = emoji,
                selected = signSelected,
                onClick = onClick
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(60.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    color = FigmaTokens.Color.white,
                    style = figmaGilroy(size = 16),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = FigmaTokens.Color.white.copy(alpha = 0.5f),
                    style = figmaGilroy(size = 14),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (showRadio) {
            FigmaRadio(selected = selected)
        }
    }
}
