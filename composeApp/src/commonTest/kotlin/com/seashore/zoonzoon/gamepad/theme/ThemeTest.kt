package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemeTest {

    @Test
    fun `light theme primary colors should use ZoonZoon brand tokens`() {
        val lightScheme = GamepadVibratorLightColorScheme

        assertEquals(ZoonZoonAccentStart, lightScheme.primary)
        assertEquals(Color.White, lightScheme.onPrimary)
        assertEquals(Color(0xFFF3E5FF), lightScheme.primaryContainer)
        assertEquals(ZoonZoonAccentEnd, lightScheme.onPrimaryContainer)
    }

    @Test
    fun `dark theme primary colors should use ZoonZoon brand tokens`() {
        val darkScheme = GamepadVibratorDarkColorScheme

        assertEquals(ZoonZoonAccentStart, darkScheme.primary)
        assertEquals(Color.White, darkScheme.onPrimary)
        assertEquals(Color(0xFF4A2D66), darkScheme.primaryContainer)
        assertEquals(Color(0xFFF3E5FF), darkScheme.onPrimaryContainer)
    }

    @Test
    fun `light theme secondary colors should use ZoonZoon accent gradient end`() {
        val lightScheme = GamepadVibratorLightColorScheme

        assertEquals(ZoonZoonAccentEnd, lightScheme.secondary)
        assertEquals(Color.White, lightScheme.onSecondary)
        assertEquals(Color(0xFFEDE8FF), lightScheme.secondaryContainer)
        assertEquals(ZoonZoonAccentEnd, lightScheme.onSecondaryContainer)
    }

    @Test
    fun `dark theme secondary colors should use ZoonZoon accent end`() {
        val darkScheme = GamepadVibratorDarkColorScheme

        assertEquals(ZoonZoonAccentEnd, darkScheme.secondary)
        assertEquals(Color.White, darkScheme.onSecondary)
    }

    @Test
    fun `light theme tertiary colors should use ZoonZoon accent tokens`() {
        val lightScheme = GamepadVibratorLightColorScheme

        assertEquals(ZoonZoonAccentStart, lightScheme.tertiary)
        assertEquals(Color.White, lightScheme.onTertiary)
        assertEquals(Color(0xFFF8F0FF), lightScheme.tertiaryContainer)
        assertEquals(ZoonZoonAccentEnd, lightScheme.onTertiaryContainer)
    }

    @Test
    fun `dark theme tertiary colors should use ZoonZoon accent tokens`() {
        val darkScheme = GamepadVibratorDarkColorScheme

        assertEquals(ZoonZoonAccentStart, darkScheme.tertiary)
        assertEquals(Color.White, darkScheme.onTertiary)
    }

    @Test
    fun `pink pastel colors should be in valid range`() {
        val pinkColors = listOf(
            PinkPastel10, PinkPastel20, PinkPastel30, PinkPastel40, PinkPastel50,
            PinkPastel60, PinkPastel70, PinkPastel80, PinkPastel90, PinkPastel95, PinkPastel99
        )
        
        pinkColors.forEach { color ->
            // All color components should be in valid range [0.0, 1.0]
            assertTrue(color.red >= 0f && color.red <= 1f, "Red component out of range for $color")
            assertTrue(color.green >= 0f && color.green <= 1f, "Green component out of range for $color")
            assertTrue(color.blue >= 0f && color.blue <= 1f, "Blue component out of range for $color")
            assertTrue(color.alpha >= 0f && color.alpha <= 1f, "Alpha component out of range for $color")
        }
    }

    @Test
    fun `purple pastel colors should be in valid range`() {
        val purpleColors = listOf(
            PurplePastel10, PurplePastel20, PurplePastel30, PurplePastel40, PurplePastel50,
            PurplePastel60, PurplePastel70, PurplePastel80, PurplePastel90
        )
        
        purpleColors.forEach { color ->
            // All color components should be in valid range [0.0, 1.0]
            assertTrue(color.red >= 0f && color.red <= 1f, "Red component out of range for $color")
            assertTrue(color.green >= 0f && color.green <= 1f, "Green component out of range for $color")
            assertTrue(color.blue >= 0f && color.blue <= 1f, "Blue component out of range for $color")
            assertTrue(color.alpha >= 0f && color.alpha <= 1f, "Alpha component out of range for $color")
        }
    }

    @Test
    fun `neutral pastel colors should be in valid range`() {
        val neutralColors = listOf(
            NeutralPastel10, NeutralPastel20, NeutralPastel30, NeutralPastel40, NeutralPastel50,
            NeutralPastel60, NeutralPastel70, NeutralPastel80, NeutralPastel90, NeutralPastel95, NeutralPastel99
        )
        
        neutralColors.forEach { color ->
            // All color components should be in valid range [0.0, 1.0]
            assertTrue(color.red >= 0f && color.red <= 1f, "Red component out of range for $color")
            assertTrue(color.green >= 0f && color.green <= 1f, "Green component out of range for $color")
            assertTrue(color.blue >= 0f && color.blue <= 1f, "Blue component out of range for $color")
            assertTrue(color.alpha >= 0f && color.alpha <= 1f, "Alpha component out of range for $color")
        }
    }

    @Test
    fun `pink pastel colors should have pink characteristics`() {
        // PinkPastel50 (classic pink) should have high red and moderate green/blue
        assertTrue(PinkPastel50.red > 0.8f, "Pink should have high red component")
        assertTrue(PinkPastel50.green > 0.5f, "Pink should have moderate green component")
        assertTrue(PinkPastel50.blue > 0.5f, "Pink should have moderate blue component")
        
        // PinkPastel70 (hot pink) should be more saturated
        assertTrue(PinkPastel70.red > 0.8f, "Hot pink should have high red component")
        assertTrue(PinkPastel70.green < PinkPastel70.red, "Hot pink should have less green than red")
        assertTrue(PinkPastel70.blue < PinkPastel70.red, "Hot pink should have less blue than red")
    }

    @Test
    fun `color schemes should have proper contrast relationships`() {
        val lightScheme = GamepadVibratorLightColorScheme
        val darkScheme = GamepadVibratorDarkColorScheme
        
        // Light theme should have dark text on light backgrounds
        assertTrue(lightScheme.onBackground.red < 0.5f, "Light theme text should be dark")
        assertTrue(lightScheme.background.red > 0.8f, "Light theme background should be light")
        
        // Dark theme should have light text on dark backgrounds
        assertTrue(darkScheme.onBackground.red > 0.5f, "Dark theme text should be light")
        assertTrue(darkScheme.background.red < 0.3f, "Dark theme background should be dark")
    }

    @Test
    fun `both color schemes should be complete`() {
        val lightScheme = GamepadVibratorLightColorScheme
        val darkScheme = GamepadVibratorDarkColorScheme
        
        // Verify that all essential colors are defined (non-null and not transparent)
        listOf(lightScheme, darkScheme).forEach { scheme ->
            assertTrue(scheme.primary.alpha == 1f, "Primary color should be opaque")
            assertTrue(scheme.secondary.alpha == 1f, "Secondary color should be opaque")
            assertTrue(scheme.tertiary.alpha == 1f, "Tertiary color should be opaque")
            assertTrue(scheme.background.alpha == 1f, "Background color should be opaque")
            assertTrue(scheme.surface.alpha == 1f, "Surface color should be opaque")
        }
    }
}