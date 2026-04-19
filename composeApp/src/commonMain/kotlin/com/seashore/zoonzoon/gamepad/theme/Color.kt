package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

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

// Light theme color scheme with pink pastels
val GamepadVibratorLightColorScheme = lightColorScheme(
    primary = PinkPastel70,
    onPrimary = Color.White,
    primaryContainer = PinkPastel20,
    onPrimaryContainer = PinkPastel90,
    
    secondary = PurplePastel60,
    onSecondary = Color.White,
    secondaryContainer = PurplePastel20,
    onSecondaryContainer = PurplePastel80,
    
    tertiary = PinkPastel50,
    onTertiary = Color.White,
    tertiaryContainer = PinkPastel10,
    onTertiaryContainer = PinkPastel80,
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = NeutralPastel10,
    onBackground = NeutralPastel90,
    surface = NeutralPastel10,
    onSurface = NeutralPastel90,
    surfaceVariant = NeutralPastel30,
    onSurfaceVariant = NeutralPastel70,
    
    outline = NeutralPastel50,
    outlineVariant = NeutralPastel40,
    scrim = Color.Black,
    
    inverseSurface = NeutralPastel90,
    inverseOnSurface = NeutralPastel20,
    inversePrimary = PinkPastel40,
    
    surfaceDim = NeutralPastel20,
    surfaceBright = NeutralPastel10,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = NeutralPastel20,
    surfaceContainer = NeutralPastel30,
    surfaceContainerHigh = NeutralPastel40,
    surfaceContainerHighest = NeutralPastel50
)

// Dark theme color scheme with pink pastels
val GamepadVibratorDarkColorScheme = darkColorScheme(
    primary = PinkPastel40,
    onPrimary = PinkPastel90,
    primaryContainer = PinkPastel80,
    onPrimaryContainer = PinkPastel20,
    
    secondary = PurplePastel40,
    onSecondary = PurplePastel80,
    secondaryContainer = PurplePastel70,
    onSecondaryContainer = PurplePastel20,
    
    tertiary = PinkPastel30,
    onTertiary = PinkPastel80,
    tertiaryContainer = PinkPastel70,
    onTertiaryContainer = PinkPastel10,
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = NeutralPastel95,
    onBackground = NeutralPastel20,
    surface = NeutralPastel95,
    onSurface = NeutralPastel20,
    surfaceVariant = NeutralPastel70,
    onSurfaceVariant = NeutralPastel40,
    
    outline = NeutralPastel60,
    outlineVariant = NeutralPastel70,
    scrim = Color.Black,
    
    inverseSurface = NeutralPastel20,
    inverseOnSurface = NeutralPastel90,
    inversePrimary = PinkPastel70,
    
    surfaceDim = NeutralPastel95,
    surfaceBright = NeutralPastel80,
    surfaceContainerLowest = NeutralPastel99,
    surfaceContainerLow = NeutralPastel90,
    surfaceContainer = NeutralPastel80,
    surfaceContainerHigh = NeutralPastel70,
    surfaceContainerHighest = NeutralPastel60
)