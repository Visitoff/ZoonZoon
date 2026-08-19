package com.seashore.zoonzoon.ui.figma

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.fig_eq_wave_hi
import com.seashore.zoonzoon.generated.resources.fig_eq_wave_lo
import com.seashore.zoonzoon.generated.resources.fig_logo
import com.seashore.zoonzoon.generated.resources.home_bg_glow_hi
import com.seashore.zoonzoon.generated.resources.home_bg_glow_lo
import com.seashore.zoonzoon.generated.resources.home_ic_arrows
import com.seashore.zoonzoon.generated.resources.home_ic_plus
import com.seashore.zoonzoon.generated.resources.home_ic_power
import com.seashore.zoonzoon.generated.resources.home_ic_unlock
import com.seashore.zoonzoon.generated.resources.home_intensity_notch
import com.seashore.zoonzoon.generated.resources.nav_bb_bg
import com.seashore.zoonzoon.generated.resources.nav_ic_ai
import com.seashore.zoonzoon.generated.resources.nav_ic_duo
import com.seashore.zoonzoon.generated.resources.nav_ic_home
import com.seashore.zoonzoon.generated.resources.nav_ic_patterns
import com.seashore.zoonzoon.generated.resources.nav_ic_settings
import com.seashore.zoonzoon.ui.shell.AppTab
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
private fun FigmaAsset(
    resource: DrawableResource,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun FigmaGlowLo(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.home_bg_glow_lo, modifier, ContentScale.FillBounds)
}

@Composable
fun FigmaGlowHi(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.home_bg_glow_hi, modifier, ContentScale.FillBounds)
}

@Composable
fun FigmaLogoImage(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.fig_logo, modifier)
}

/** Figma `-Equalizer` wave layers (`2097:12036`, `2097:12037`) — waves only, no bottom UI. */
@Composable
fun FigmaEqWaves(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        FigmaAsset(
            Res.drawable.fig_eq_wave_hi,
            Modifier
                .offset(x = EqWaveHiX, y = EqWaveHiY)
                .size(EqWaveHiW, EqWaveHiH),
            ContentScale.FillBounds
        )
        FigmaAsset(
            Res.drawable.fig_eq_wave_lo,
            Modifier
                .offset(x = EqWaveLoX, y = EqWaveLoY)
                .size(EqWaveLoW, EqWaveLoH),
            ContentScale.FillBounds
        )
    }
}

private val EqWaveHiX = (-59.04).dp
private val EqWaveHiY = 11.981.dp
private val EqWaveHiW = 473.907.dp
private val EqWaveHiH = 433.038.dp

private val EqWaveLoX = (-59).dp
private val EqWaveLoY = (-382).dp
private val EqWaveLoW = 510.dp
private val EqWaveLoH = 433.dp

@Composable
fun FigmaNavBarBackground(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.nav_bb_bg, modifier, ContentScale.FillBounds)
}

@Composable
fun FigmaUnlockIcon(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.home_ic_unlock, modifier)
}

@Composable
fun FigmaPlusIcon(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.home_ic_plus, modifier)
}

@Composable
fun FigmaPowerIcon(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.home_ic_power, modifier)
}

@Composable
fun FigmaArrowsIcon(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.home_ic_arrows, modifier)
}

@Composable
fun FigmaIntensityNotch(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.home_intensity_notch, modifier, ContentScale.FillBounds)
}

@Composable
fun FigmaNavIcon(tab: AppTab, modifier: Modifier = Modifier) {
    val resource = when (tab) {
        AppTab.Multiplayer -> Res.drawable.nav_ic_duo
        AppTab.Ai -> Res.drawable.nav_ic_ai
        AppTab.Home -> Res.drawable.nav_ic_home
        AppTab.Patterns -> Res.drawable.nav_ic_patterns
        AppTab.Settings -> Res.drawable.nav_ic_settings
    }
    FigmaAsset(resource, modifier)
}
