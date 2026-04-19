# Gamepad Vibrator Theme

This package contains the pink pastel Material3 theme implementation for the Gamepad Vibration Controller app.

## Files

### Color.kt
Defines the pink pastel color palette and Material3 color schemes:
- **Pink Pastel Colors**: 11 shades from very light pink (PinkPastel10) to dark red (PinkPastel99)
- **Purple Pastel Colors**: 9 complementary purple shades for secondary colors
- **Neutral Pastel Colors**: 11 neutral shades for backgrounds and surfaces
- **GamepadVibratorLightColorScheme**: Light theme color scheme using pink pastels
- **GamepadVibratorDarkColorScheme**: Dark theme color scheme using pink pastels

### Theme.kt
Contains the main theme composable:
- **GamepadVibratorTheme**: The primary theme composable that applies the pink pastel color scheme
- Automatically adapts to system dark/light mode preferences
- Uses Material3 default typography and shapes

### ThemeUsageExample.kt
Demonstrates how to use the theme with various Material3 components:
- Shows primary, secondary, and tertiary color containers
- Demonstrates buttons, cards, and surfaces with theme colors
- Serves as a visual reference for the color scheme

## Usage

```kotlin
@Composable
fun MyApp() {
    GamepadVibratorTheme {
        // Your app content here
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            // UI components will automatically use the pink pastel theme
        }
    }
}
```

## Color Scheme Details

### Light Theme
- **Primary**: Hot Pink (PinkPastel70) - #FF69B4
- **Secondary**: Purple (PurplePastel60) - #805AD5  
- **Tertiary**: Classic Pink (PinkPastel50) - #FFC0CB
- **Background**: Very Light Neutral (NeutralPastel10) - #FFFBFF

### Dark Theme
- **Primary**: Light Pink (PinkPastel40) - #FFB6C1
- **Secondary**: Light Purple (PurplePastel40) - #B794F6
- **Tertiary**: Soft Pink (PinkPastel30) - #FFD1DC
- **Background**: Dark Neutral (NeutralPastel95) - #141218

## Requirements Satisfied

This implementation satisfies the following requirements:
- **Requirement 7.1**: UI Layer built using Compose Multiplatform ✓
- **Requirement 7.2**: UI Layer uses Material3 components ✓
- **Requirement 7.3**: UI Layer implements pink pastel color theme ✓

## Testing

The theme includes comprehensive unit tests in `ThemeTest.kt` and `ColorValidationTest.kt` that verify:
- Color values match pink pastel specifications
- All colors are in valid ARGB ranges
- Light and dark themes have proper contrast relationships
- Color schemes are complete and properly defined