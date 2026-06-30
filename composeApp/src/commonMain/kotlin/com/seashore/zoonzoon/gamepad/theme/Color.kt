package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand accent gradient (#CA5AFF → #7E6BF3)
val ZoonZoonAccentStart = Color(0xFFCA5AFF)
val ZoonZoonAccentEnd = Color(0xFF7E6BF3)
val ZoonZoonAccentMuted = Color(0xFFCA5AFF).copy(alpha = 0.14f)

// iOS system surfaces (HIG)
val IosGroupedBackgroundLight = Color(0xFFF2F2F7)
val IosSecondaryGroupedLight = Color(0xFFFFFFFF)
val IosLabelSecondaryLight = Color(0x993C3C43)
val IosSeparatorLight = Color(0x335C3C43)

val IosGroupedBackgroundDark = Color(0xFF000000)
val IosSecondaryGroupedDark = Color(0xFF1C1C1E)
val IosTertiaryGroupedDark = Color(0xFF2C2C2E)
val IosLabelSecondaryDark = Color(0x99EBEBF5)
val IosSeparatorDark = Color(0x33EBEBF5)

// Legacy tokens (Home glow only)
val ZoonZoonBackgroundStart = Color(0xFFF7E7FF)
val ZoonZoonBackgroundEnd = Color(0xFFECF0F3)
val ZoonZoonBackgroundFallback = Color(0xFFF0F0F3)
val ZoonZoonRippleRing = Color(0x80E3E6EC)

// Pink Pastel Color Palette
val PinkPastel10 = Color(0xFFFFF0F5)  // Very light pink
val PinkPastel20 = Color(0xFFFFE4E8)  // Light pink
val PinkPastel30 = Color(0xFFFFD1DC)  // Soft pink
val PinkPastel40 = Color(0xFFFFB6C1)  // Light pink
val PinkPastel50 = Color(0xFFFFC0CB)  // Classic pink
val PinkPastel60 = Color(0xFFFF91A4)  // Medium pink
val PinkPastel70 = Color(0xFFFF69B4)  // Hot pink
val PinkPastel80 = Color(0xFFFF1493)  // Deep pink
val PinkPastel90 = Color(0xFFDC143C)  // Crimson
val PinkPastel95 = Color(0xFFB22222)  // Fire brick
val PinkPastel99 = Color(0xFF8B0000)  // Dark red

// Complementary colors for better contrast
val PurplePastel10 = Color(0xFFF8F0FF)
val PurplePastel20 = Color(0xFFE8D5FF)
val PurplePastel30 = Color(0xFFD1B3FF)
val PurplePastel40 = Color(0xFFB794F6)
val PurplePastel50 = Color(0xFF9F7AEA)
val PurplePastel60 = Color(0xFF805AD5)
val PurplePastel70 = Color(0xFF6B46C1)
val PurplePastel80 = Color(0xFF553C9A)
val PurplePastel90 = Color(0xFF44337A)

// Neutral colors for backgrounds and surfaces
val NeutralPastel10 = Color(0xFFFFFBFF)
val NeutralPastel20 = Color(0xFFF7F2FA)
val NeutralPastel30 = Color(0xFFECE6F0)
val NeutralPastel40 = Color(0xFFCAC4D0)
val NeutralPastel50 = Color(0xFF79747E)
val NeutralPastel60 = Color(0xFF605D64)
val NeutralPastel70 = Color(0xFF484649)
val NeutralPastel80 = Color(0xFF313033)
val NeutralPastel90 = Color(0xFF1C1B1F)
val NeutralPastel95 = Color(0xFF141218)
val NeutralPastel99 = Color(0xFF0A0A0A)

// iOS-native light theme + ZoonZoon accent
val GamepadVibratorLightColorScheme = lightColorScheme(
    primary = ZoonZoonAccentStart,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E5FF),
    onPrimaryContainer = ZoonZoonAccentEnd,

    secondary = ZoonZoonAccentEnd,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE8FF),
    onSecondaryContainer = ZoonZoonAccentEnd,

    tertiary = ZoonZoonAccentStart,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8F0FF),
    onTertiaryContainer = ZoonZoonAccentEnd,

    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEA),
    onErrorContainer = Color(0xFFBA1A1A),

    background = IosGroupedBackgroundLight,
    onBackground = Color(0xFF000000),
    surface = IosSecondaryGroupedLight,
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF8E8E93),

    outline = IosSeparatorLight,
    outlineVariant = Color(0xFFD1D1D6),
    scrim = Color.Black,

    inverseSurface = Color(0xFF1C1C1E),
    inverseOnSurface = Color(0xFFF2F2F7),
    inversePrimary = ZoonZoonAccentEnd,

    surfaceDim = Color(0xFFE5E5EA),
    surfaceBright = IosSecondaryGroupedLight,
    surfaceContainerLowest = IosSecondaryGroupedLight,
    surfaceContainerLow = Color(0xFFF9F9FB),
    surfaceContainer = IosSecondaryGroupedLight,
    surfaceContainerHigh = Color(0xFFECECF0),
    surfaceContainerHighest = Color(0xFFE5E5EA)
)

// iOS-native dark theme + ZoonZoon accent
val GamepadVibratorDarkColorScheme = darkColorScheme(
    primary = ZoonZoonAccentStart,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A2D66),
    onPrimaryContainer = Color(0xFFF3E5FF),

    secondary = ZoonZoonAccentEnd,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D3566),
    onSecondaryContainer = Color(0xFFE8E0FF),

    tertiary = ZoonZoonAccentStart,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF352848),
    onTertiaryContainer = Color(0xFFF3E5FF),

    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = IosGroupedBackgroundDark,
    onBackground = Color(0xFFFFFFFF),
    surface = IosSecondaryGroupedDark,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = IosTertiaryGroupedDark,
    onSurfaceVariant = Color(0xFF8E8E93),

    outline = IosSeparatorDark,
    outlineVariant = Color(0xFF38383A),
    scrim = Color.Black,

    inverseSurface = Color(0xFFF2F2F7),
    inverseOnSurface = Color(0xFF1C1C1E),
    inversePrimary = ZoonZoonAccentEnd,

    surfaceDim = IosGroupedBackgroundDark,
    surfaceBright = IosSecondaryGroupedDark,
    surfaceContainerLowest = IosGroupedBackgroundDark,
    surfaceContainerLow = IosSecondaryGroupedDark,
    surfaceContainer = IosTertiaryGroupedDark,
    surfaceContainerHigh = Color(0xFF3A3A3C),
    surfaceContainerHighest = Color(0xFF48484A)
)