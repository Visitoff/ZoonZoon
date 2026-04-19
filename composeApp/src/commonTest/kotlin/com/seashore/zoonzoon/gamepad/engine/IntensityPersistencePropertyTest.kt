package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Property-based tests for intensity persistence across pattern changes.
 *
 * **Validates: Requirement 4.4**
 *
 * Property 10: Intensity Persistence Across Pattern Changes
 * For any intensity setting and pattern change, the intensity SHALL remain
 * unchanged after the pattern change.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntensityPersistencePropertyTest {

    private class FakeGamepadController : GamepadController {
        override suspend fun sendVibrationCommand(leftMotor: Float, rightMotor: Float) = Result.success(Unit)
    }

    /**
     * Property: Intensity is preserved when pattern changes while vibration is disabled.
     */
    @Test
    fun testIntensityPreservedAcrossPatternChangesWhileDisabled() = runTest {
        val engine = VibrationEngine(FakePlatformGamepadController(), this)

        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(2.0f),
            VibrationPattern.Wave(1.0f),
            VibrationPattern.Custom(listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)), loop = true)
        )

        for (intensity in intensities) {
            engine.setIntensity(intensity)

            for (pattern in patterns) {
                engine.setPattern(pattern)
                assertEquals(
                    intensity,
                    engine.vibrationState.value.intensity,
                    "Intensity must remain $intensity after switching to ${pattern::class.simpleName}"
                )
            }
        }
    }

    /**
     * Property: Intensity is preserved when pattern changes while vibration is enabled.
     */
    @Test
    fun testIntensityPreservedAcrossPatternChangesWhileEnabled() = runTest {
        val engine = VibrationEngine(FakePlatformGamepadController(), this)

        val intensities = listOf(0.2f, 0.5f, 0.9f)
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(2.0f),
            VibrationPattern.Wave(1.0f)
        )

        for (intensity in intensities) {
            engine.setIntensity(intensity)
            engine.enableVibration()

            for (pattern in patterns) {
                engine.setPattern(pattern)
                advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS)

                assertEquals(
                    intensity,
                    engine.vibrationState.value.intensity,
                    "Intensity must remain $intensity after switching to ${pattern::class.simpleName} while enabled"
                )
            }

            engine.disableVibration()
            advanceTimeBy(1)
        }
    }

    /**
     * Property: setPattern() does not modify the intensity field in VibrationState.
     */
    @Test
    fun testSetPatternDoesNotModifyIntensity() = runTest {
        val engine = VibrationEngine(FakePlatformGamepadController(), this)

        val targetIntensity = 0.65f
        engine.setIntensity(targetIntensity)

        // Change pattern many times
        val patterns = listOf(
            VibrationPattern.Pulse(1.0f),
            VibrationPattern.Wave(2.0f),
            VibrationPattern.Constant,
            VibrationPattern.Custom(listOf(VibrationPattern.Custom.Frame(0.3f, 0.7f, 50L)), loop = false),
            VibrationPattern.Pulse(5.0f),
            VibrationPattern.Wave(0.5f)
        )

        for (pattern in patterns) {
            engine.setPattern(pattern)
            assertEquals(
                targetIntensity,
                engine.vibrationState.value.intensity,
                0.0001f,
                "setPattern() must not change intensity, expected $targetIntensity"
            )
        }
    }
}


