package com.seashore.zoonzoon.gamepad.model

import com.seashore.zoonzoon.gamepad.engine.FakePlatformGamepadController
import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Property tests for custom pattern support.
 *
 * **Property 6: Custom Pattern Support**
 * **Validates: Requirement 3.5**
 *
 * For any valid custom pattern definition, the PatternManager SHALL
 * support and execute that pattern.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CustomPatternSupportPropertyTest {

    // -------------------------------------------------------------------------
    // PatternManager tests
    // -------------------------------------------------------------------------

    @Test
    fun testPatternManagerRegistersCustomPattern() {
        val manager = PatternManager()
        val pattern = VibrationPattern.Custom(
            frames = listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 200L)),
            loop = true
        )

        manager.registerCustomPattern("test", pattern)
        val retrieved = manager.getCustomPattern("test")

        assertNotNull(retrieved)
        assertEquals(pattern, retrieved)
    }

    @Test
    fun testPatternManagerReturnsNullForUnknownPattern() {
        val manager = PatternManager()
        assertNull(manager.getCustomPattern("nonexistent"))
    }

    @Test
    fun testPatternManagerRemovesCustomPattern() {
        val manager = PatternManager()
        val pattern = VibrationPattern.Custom(emptyList())
        manager.registerCustomPattern("removable", pattern)

        val removed = manager.removeCustomPattern("removable")
        assertTrue(removed)
        assertNull(manager.getCustomPattern("removable"))
    }

    @Test
    fun testPatternManagerListsAllPatterns() {
        val manager = PatternManager()
        val custom1 = VibrationPattern.Custom(listOf(VibrationPattern.Custom.Frame(0.3f, 0.7f, 100L)))
        val custom2 = VibrationPattern.Custom(listOf(VibrationPattern.Custom.Frame(0.8f, 0.2f, 150L)))

        manager.registerCustomPattern("pattern1", custom1)
        manager.registerCustomPattern("pattern2", custom2)

        val all = manager.getAllPatterns()
        // Should include Constant, Pulse, Wave + 2 custom
        assertTrue(all.size >= 5, "Should have at least 5 patterns (3 built-in + 2 custom)")
        assertTrue(all.any { it is VibrationPattern.Custom })
    }

    @Test
    fun testCustomPatternFramesExecuteCorrectly() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.0f, durationMs = 100L),
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 1.0f, durationMs = 100L)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = true)

        // At time 0 — first frame
        val (left0, right0) = pattern.calculateFrame(0L, 1.0f)
        assertEquals(1.0f, left0, 0.001f)
        assertEquals(0.0f, right0, 0.001f)

        // At time 100 — second frame
        val (left100, right100) = pattern.calculateFrame(100L, 1.0f)
        assertEquals(0.0f, left100, 0.001f)
        assertEquals(1.0f, right100, 0.001f)

        // At time 200 — loops back to first frame
        val (left200, right200) = pattern.calculateFrame(200L, 1.0f)
        assertEquals(1.0f, left200, 0.001f)
        assertEquals(0.0f, right200, 0.001f)
    }

    @Test
    fun testCustomPatternIntensityScaling() {
        val frames = listOf(VibrationPattern.Custom.Frame(1.0f, 1.0f, 100L))
        val pattern = VibrationPattern.Custom(frames = frames, loop = true)

        val intensities = listOf(0.0f, 0.5f, 0.8f, 1.0f)
        for (intensity in intensities) {
            val (left, right) = pattern.calculateFrame(0L, intensity)
            assertEquals(intensity, left, 0.001f, "Left motor should scale with intensity $intensity")
            assertEquals(intensity, right, 0.001f, "Right motor should scale with intensity $intensity")
        }
    }

    @Test
    fun testCustomPatternEmptyFramesReturnsZero() {
        val pattern = VibrationPattern.Custom(frames = emptyList(), loop = true)
        val (left, right) = pattern.calculateFrame(0L, 1.0f)
        assertEquals(0.0f, left)
        assertEquals(0.0f, right)
    }

    // -------------------------------------------------------------------------
    // ViewModel integration
    // -------------------------------------------------------------------------

    @Test
    fun testViewModelSaveAndApplyCustomPattern() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val frames = listOf(
            VibrationPattern.Custom.Frame(0.7f, 0.3f, 150L),
            VibrationPattern.Custom.Frame(0.3f, 0.7f, 150L)
        )

        viewModel.saveAndApplyCustomPattern("myPattern", frames, loop = true)

        val activePattern = viewModel.vibrationState.value.activePattern
        assertTrue(activePattern is VibrationPattern.Custom)
        assertEquals(frames, (activePattern as VibrationPattern.Custom).frames)
    }

    @Test
    fun testViewModelCustomPatternExecutesDuringVibration() = runTest {
        val controller = FakePlatformGamepadController()
        controller.setConnectionState(ConnectionState.Connected("Test Pad"))
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val frames = listOf(
            VibrationPattern.Custom.Frame(0.8f, 0.2f, 100L)
        )
        viewModel.saveAndApplyCustomPattern("exec", frames, loop = true)
        viewModel.setIntensity(1.0f)
        viewModel.setVibrationEnabled(true)

        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)

        assertTrue(controller.commands.isNotEmpty(), "Custom pattern should send commands")

        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }
}
