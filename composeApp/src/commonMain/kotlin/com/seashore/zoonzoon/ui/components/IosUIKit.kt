package com.seashore.zoonzoon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.theme.GlassButtonCorner
import com.seashore.zoonzoon.gamepad.theme.GlassCardCorner
import com.seashore.zoonzoon.gamepad.theme.GlassControlCorner
import com.seashore.zoonzoon.gamepad.theme.LocalGlassTokens
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentEnd
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentStart
import com.seashore.zoonzoon.gamepad.theme.glassSurface
import com.seashore.zoonzoon.gamepad.theme.zoonZoonAccentBrush

/**
 * Transparent screen container. The gradient backdrop is provided once at the shell
 * level, so screens just lay content over it. [accentGlow] adds a soft accent halo
 * near the top (used on Home).
 */
@Composable
fun IosScreenBackground(
    modifier: Modifier = Modifier,
    accentGlow: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (accentGlow) {
                    Modifier.drawBehind {
                        val radius = size.minDimension * 0.62f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ZoonZoonAccentStart.copy(alpha = 0.30f),
                                    ZoonZoonAccentEnd.copy(alpha = 0.10f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height * 0.34f),
                                radius = radius
                            ),
                            radius = radius,
                            center = Offset(size.width / 2f, size.height * 0.34f)
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

@Composable
fun IosLargeTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
    )
}

@Composable
fun IosSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 8.dp)
    )
}

/** Frosted-glass grouped container. */
@Composable
fun IosGroupedCard(
    modifier: Modifier = Modifier,
    strong: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalGlassTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glassSurface(shape = GlassCardCorner, tokens = tokens, strong = strong, elevation = 16.dp),
        content = content
    )
}

@Composable
fun IosGroupedDivider() {
    val tokens = LocalGlassTokens.current
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        thickness = 0.5.dp,
        color = tokens.hairline
    )
}

@Composable
fun IosListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .background(
                if (selected) ZoonZoonAccentStart.copy(alpha = 0.16f) else Color.Transparent
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) ZoonZoonAccentStart else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) {
            Box(modifier = Modifier.padding(start = 12.dp)) { trailing() }
        }
    }
}

/** iOS-style segmented control with a frosted track and a liquid-glass accent thumb. */
@Composable
fun IosSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(GlassControlCorner)
            .background(
                if (tokens.dark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.35f)
            )
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedThumbShape)
                    .then(
                        if (selected) Modifier.background(zoonZoonAccentBrush()) else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelected(index) }
                    )
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private val RoundedThumbShape = androidx.compose.foundation.shape.RoundedCornerShape(11.dp)

/** Primary action — filled with the brand accent gradient and an accent glow. */
@Composable
fun IosAccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier.shadow(
                        elevation = 14.dp,
                        shape = GlassButtonCorner,
                        clip = false,
                        ambientColor = ZoonZoonAccentStart.copy(alpha = 0.5f),
                        spotColor = ZoonZoonAccentEnd.copy(alpha = 0.55f)
                    )
                } else {
                    Modifier
                }
            )
            .clip(GlassButtonCorner)
            .then(
                if (enabled) {
                    Modifier.background(zoonZoonAccentBrush())
                } else {
                    Modifier.background(Color.Gray.copy(alpha = 0.25f))
                }
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = if (enabled) 0.5f else 0f), Color.Transparent)
                    )
                ),
                GlassButtonCorner
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Secondary action — frosted glass pill with accent label. */
@Composable
fun IosSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .clip(GlassButtonCorner)
            .glassSurface(shape = GlassButtonCorner, tokens = tokens, strong = false, elevation = 6.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (enabled) ZoonZoonAccentStart else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun IosDestructiveTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp)
    )
}
