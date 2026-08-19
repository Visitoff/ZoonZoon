package com.seashore.zoonzoon.ui.figma

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.seashore.zoonzoon.generated.resources.Res
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

@Composable
fun FigmaEqImage(modifier: Modifier = Modifier) {
    FigmaAsset(Res.drawable.fig_eq, modifier, ContentScale.Crop)
}

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
