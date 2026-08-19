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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.fig_ic_check
import com.seashore.zoonzoon.generated.resources.fig_logo
import org.jetbrains.compose.resources.painterResource

private val HeartBorder = Color(0xFFF4555C)
private val RadioBorder = Color(0xFFF1535E)

/** Figma screen title: Gilroy SemiBold 30 at (15, 54). */
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

/** Figma section label: 345×50 (e.g. "Language", "Vibration target"). */
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

/** Figma `-playlist` Open: header row + nested pattern rows (10dp gap). */
@Composable
fun FigmaPlaylistCard(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier,
    onHeaderLongPress: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FigmaSelectableItem(
            emoji = "💗",
            title = title,
            subtitle = subtitle,
            selected = expanded,
            onClick = onHeaderClick,
            onLongPress = onHeaderLongPress,
            showRadio = expanded
        )
        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                content()
            }
        }
    }
}

/** Nested row inside an open playlist — dark fill, no border/radio. */
@Composable
fun FigmaNestedSelectableItem(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null
) {
    FigmaSelectableItem(
        emoji = emoji,
        title = title,
        subtitle = subtitle,
        selected = false,
        onClick = onClick,
        onLongPress = onLongPress,
        modifier = modifier,
        showRadio = false,
        signSelected = false,
        forceDarkFill = true
    )
}

/** Figma `-sign-states`: 50×50 logo with optional ™. */
@Composable
fun FigmaSignLogo(
    modifier: Modifier = Modifier,
    showTm: Boolean = true
) {
    Box(modifier = modifier.size(50.dp)) {
        Image(
            painter = painterResource(Res.drawable.fig_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
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

/** Figma `-sign` emoji button inside lists / equalizer. */
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
                    .border(1.dp, HeartBorder, CircleShape)
            )
        }
        FigmaCircleButton(
            onClick = onClick,
            size = 60.dp,
            fill = FigmaFill.Dark,
            stroke = FigmaStroke.Heart
        ) {
            Text(text = emoji, style = figmaGilroy(size = 24, weight = androidx.compose.ui.text.font.FontWeight.Normal))
        }
    }
}

/** Figma `-radio`: 30dp circle with optional check. */
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
                if (selected) Modifier.border(1.dp, RadioBorder, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Image(
                painter = painterResource(Res.drawable.fig_ic_check),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(FigmaTokens.Color.white)
            )
        }
    }
}

/** Figma `-selector`: 345×55 pill with 3 segments. */
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
            .clip(RoundedCornerShape(99.dp))
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

/** Figma `-selectable-item`: 345×70 pill row. */
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
            .clip(RoundedCornerShape(99.dp))
            .background(
                when {
                    forceDarkFill -> FigmaFillDark
                    selected -> FigmaTokens.Color.surfaceDark
                    else -> FigmaFillDark
                }
            )
            .then(
                if (selected && !forceDarkFill) Modifier.border(1.dp, HeartBorder, RoundedCornerShape(99.dp))
                else Modifier
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
