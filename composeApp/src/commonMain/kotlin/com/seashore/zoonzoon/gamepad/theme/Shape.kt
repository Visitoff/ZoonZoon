package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** iOS-style continuous corner radii. */
val ZoonZoonShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

val IosGroupedCorner = RoundedCornerShape(12.dp)
val IosButtonCorner = RoundedCornerShape(14.dp)

// Liquid-glass radii (aggressive, soft — rounded-2xl / rounded-3xl)
val GlassCardCorner = RoundedCornerShape(24.dp)
val GlassPanelCorner = RoundedCornerShape(28.dp)
val GlassButtonCorner = RoundedCornerShape(16.dp)
val GlassControlCorner = RoundedCornerShape(14.dp)
