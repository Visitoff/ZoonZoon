package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Property-based tests for vibration enable/disable operations.
 * 
 * **Validates: Requirements 2.1, 2.2**
 * 
 * Property 2: Vibration Enable/Disable Commands
 * For any vibration enable or disable action, the ViewModel SHALL call the corresponding 
 * method (EnableVibration or DisableVibration) on the VibrationEngine.
 * 
 * This test validates that enable/disable operations correctly update the VibrationEngine 
 * state and trigger appropriate actions across all patterns, intensities, and state transitions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VibrationEnableDisablePropertyTest {
    
    /**
     * Property: Enable vibration must update state to enabled for all patterns and intensities
     * 
     * Tests that calling enableVibration() correctly updates the VibrationEngine state
     * to enabled=true regardless of the active pattern or intensity setting.
     */
    @Test
    fun testEnableVibrationUpdatesStateForAllPatternsAndIntensities() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 1.0f),
            VibrationPattern.Pulse(frequencyHz = 5.0f),
            VibrationPattern.Wave(frequencyHz = 0.5f),
            VibrationPattern.Wave(frequencyHz = 2.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            ),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.3f, 0.7f, 100L),
                    VibrationPattern.Custom.Frame(0.8f, 0.2f, 100L)
                ),
                loop = false
            )
        )
        
        val intensities = listOf(0.0f, 0.1f, 0.25f, 0.5f, 0.75f, 0.9f, 1.0f)
        
        for (pattern in patterns) {
            for (intensity in intensities) {
                // Reset to known state
                engine.disableVibration()
                advanceTimeBy(1)
                controller.reset()
                
                engine.setPattern(pattern)
                engine.setIntensity(intensity)
                
                assertFalse(
                    engine.vibrationState.value.enabled,
                    "Engine should start disabled for pattern=${pattern::class.simpleName}, intensity=$intensity"
                )
                
                engine.enableVibration()
                
                assertTrue(
                    engine.vibrationState.value.enabled,
                    "enableVibration() must update state to enabled for pattern=${pattern::class.simpleName}, intensity=$intensity"
                )
                assertEquals(pattern, engine.vibrationState.value.activePattern)
                assertEquals(intensity, engine.vibrationState.value.intensity)
            }
        }
        
        // Clean up
        engine.disableVibration()
        advanceTimeBy(1)
    }
    
    /**
     * Property: Disable vibration must update state to disabled for all patterns and intensities
     * 
     * Tests that calling disableVibration() correctly updates the VibrationEngine state
     * to enabled=false regardless of the active pattern or intensity setting.
     */
    @Test
    fun testDisableVibrationUpdatesStateForAllPatternsAndIntensities() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 1.0f),
            VibrationPattern.Pulse(frequencyHz = 5.0f),
            VibrationPattern.Wave(frequencyHz = 0.5f),
            VibrationPattern.Wave(frequencyHz = 2.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            ),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.3f, 0.7f, 100L),
                    VibrationPattern.Custom.Frame(0.8f, 0.2f, 100L)
                ),
                loop = false
            )
        )
        
        val intensities = listOf(0.0f, 0.1f, 0.25f, 0.5f, 0.75f, 0.9f, 1.0f)
        
        for (pattern in patterns) {
            for (intensity in intensities) {
                controller.reset()
                
                // Set pattern and intensity
                engine.setPattern(pattern)
                engine.setIntensity(intensity)
                
                // Enable vibration first
                engine.enableVibration()
                advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
                
                // Verify it's enabled
                assertTrue(
                    engine.vibrationState.value.enabled,
                    "Engine should be enabled before test for pattern=${pattern::class.simpleName}, intensity=$intensity"
                )
                
                // Disable vibration
                engine.disableVibration()
                advanceTimeBy(1)
                
                // Verify state is now disabled
                assertFalse(
                    engine.vibrationState.value.enabled,
                    "disableVibration() must update state to disabled for pattern=${pattern::class.simpleName}, intensity=$intensity"
                )
                
                // Verify pattern and intensity are preserved
                assertEquals(
                    pattern,
                    engine.vibrationState.value.activePattern,
                    "Pattern should be preserved after disableVibration()"
                )
                assertEquals(
                    intensity,
                    engine.vibrationState.value.intensity,
                    "Intensity should be preserved after disableVibration()"
                )
            }
        }
    }
    
    /**
     * Property: Enable/disable operations must be idempotent
     * 
     * Tests that calling enableVibration() multiple times or disableVibration() 
     * multiple times produces consistent state without side effects.
     */
    @Test
    fun testEnableDisableOperationsAreIdempotent() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )
        
        for (pattern in patterns) {
            engine.setPattern(pattern)
            engine.setIntensity(0.7f)
            
            // Multiple enable calls
            engine.enableVibration()
            assertTrue(engine.vibrationState.value.enabled, "First enable should work")
            
            engine.enableVibration()
            assertTrue(engine.vibrationState.value.enabled, "Second enable should maintain enabled state")
            
            engine.enableVibration()
            assertTrue(engine.vibrationState.value.enabled, "Third enable should maintain enabled state")
            
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            
            // Multiple disable calls
            engine.disableVibration()
            advanceTimeBy(1)
            assertFalse(engine.vibrationState.value.enabled, "First disable should work")
            
            engine.disableVibration()
            advanceTimeBy(1)
            assertFalse(engine.vibrationState.value.enabled, "Second disable should maintain disabled state")
            
            engine.disableVibration()
            advanceTimeBy(1)
            assertFalse(engine.vibrationState.value.enabled, "Third disable should maintain disabled state")
        }
    }
    
    /**
     * Property: Enable/disable state transitions must work correctly in any order
     * 
     * Tests various sequences of enable/disable operations to ensure state 
     * transitions are always correct.
     */
    @Test
    fun testEnableDisableStateTransitionsInAnyOrder() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(0.5f)
        
        // Test various transition sequences
        val sequences = listOf(
            listOf(true, false),
            listOf(true, true, false),
            listOf(true, false, true, false),
            listOf(false, true, false),
            listOf(false, false, true, true, false, false),
            listOf(true, false, true, true, false, true, false)
        )
        
        for (sequence in sequences) {
            // Reset to disabled state
            engine.disableVibration()
            advanceTimeBy(1)
            controller.reset()
            
            for (shouldEnable in sequence) {
                if (shouldEnable) {
                    engine.enableVibration()
                    advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS)
                    assertTrue(
                        engine.vibrationState.value.enabled,
                        "State should be enabled after enableVibration() in sequence $sequence"
                    )
                } else {
                    engine.disableVibration()
                    advanceTimeBy(1)
                    assertFalse(
                        engine.vibrationState.value.enabled,
                        "State should be disabled after disableVibration() in sequence $sequence"
                    )
                }
            }
        }
    }
    
    /**
     * Property: Enable vibration must start pattern execution
     * 
     * Tests that calling enableVibration() causes the engine to start sending 
     * vibration commands to the controller.
     */
    @Test
    fun testEnableVibrationStartsPatternExecution() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            )
        )
        
        val intensities = listOf(0.1f, 0.5f, 1.0f)
        
        for (pattern in patterns) {
            for (intensity in intensities) {
                controller.reset()
                
                engine.setPattern(pattern)
                engine.setIntensity(intensity)
                
                // Before enabling, no commands should be sent
                advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)
                assertEquals(
                    0,
                    controller.commands.size,
                    "No commands should be sent before enableVibration() for pattern=${pattern::class.simpleName}"
                )
                
                // Enable vibration
                engine.enableVibration()
                
                // After enabling, commands should be sent
                advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)
                assertTrue(
                    controller.commands.isNotEmpty(),
                    "Commands should be sent after enableVibration() for pattern=${pattern::class.simpleName}, intensity=$intensity"
                )
                
                // Clean up
                engine.disableVibration()
                advanceTimeBy(1)
            }
        }
    }
    
    /**
     * Property: Disable vibration must stop pattern execution
     * 
     * Tests that calling disableVibration() causes the engine to stop sending 
     * pattern vibration commands (only stop command with 0,0 is allowed).
     */
    @Test
    fun testDisableVibrationStopsPatternExecution() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            )
        )
        
        for (pattern in patterns) {
            controller.reset()
            
            engine.setPattern(pattern)
            engine.setIntensity(0.8f)
            engine.enableVibration()
            
            // Let it run for a bit
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)
            val commandsWhileEnabled = controller.commands.size
            assertTrue(
                commandsWhileEnabled > 0,
                "Commands should be sent while enabled for pattern=${pattern::class.simpleName}"
            )
            
            // Disable vibration
            engine.disableVibration()
            advanceTimeBy(1)
            
            val commandsAtDisable = controller.commands.size
            
            // Advance more time and verify no new pattern commands are sent
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 10)
            
            val commandsAfterDisable = controller.commands.drop(commandsAtDisable)
            val patternCommandsAfterDisable = commandsAfterDisable.filter { 
                it.leftMotor != 0f || it.rightMotor != 0f 
            }
            
            assertTrue(
                patternCommandsAfterDisable.isEmpty(),
                "No pattern commands should be sent after disableVibration() for pattern=${pattern::class.simpleName}, " +
                "but got: $patternCommandsAfterDisable"
            )
        }
    }
    
    /**
     * Property: Enable/disable operations must work correctly with pattern changes
     * 
     * Tests that enable/disable operations work correctly when patterns are 
     * changed between operations.
     */
    @Test
    fun testEnableDisableWorksWithPatternChanges() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            )
        )
        
        // Start with first pattern
        engine.setPattern(patterns[0])
        engine.setIntensity(0.5f)
        engine.enableVibration()
        assertTrue(engine.vibrationState.value.enabled, "Should be enabled")
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        
        // Change pattern while enabled
        engine.setPattern(patterns[1])
        assertTrue(
            engine.vibrationState.value.enabled,
            "Should remain enabled after pattern change"
        )
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        
        // Disable
        engine.disableVibration()
        advanceTimeBy(1)
        assertFalse(engine.vibrationState.value.enabled, "Should be disabled")
        
        // Change pattern while disabled
        engine.setPattern(patterns[2])
        assertFalse(
            engine.vibrationState.value.enabled,
            "Should remain disabled after pattern change"
        )
        
        // Enable again with new pattern
        engine.enableVibration()
        assertTrue(engine.vibrationState.value.enabled, "Should be enabled again")
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        
        // Change pattern and disable
        engine.setPattern(patterns[3])
        engine.disableVibration()
        advanceTimeBy(1)
        assertFalse(engine.vibrationState.value.enabled, "Should be disabled at end")
    }
    
    /**
     * Property: Enable/disable operations must work correctly with intensity changes
     * 
     * Tests that enable/disable operations work correctly when intensity is 
     * changed between operations.
     */
    @Test
    fun testEnableDisableWorksWithIntensityChanges() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        engine.setPattern(VibrationPattern.Constant)
        
        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        
        for (intensity in intensities) {
            // Set intensity and enable
            engine.setIntensity(intensity)
            engine.enableVibration()
            assertTrue(
                engine.vibrationState.value.enabled,
                "Should be enabled with intensity=$intensity"
            )
            assertEquals(
                intensity,
                engine.vibrationState.value.intensity,
                "Intensity should be $intensity"
            )
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            
            // Change intensity while enabled
            val newIntensity = (intensity + 0.1f).coerceIn(0.0f, 1.0f)
            engine.setIntensity(newIntensity)
            assertTrue(
                engine.vibrationState.value.enabled,
                "Should remain enabled after intensity change"
            )
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            
            // Disable
            engine.disableVibration()
            advanceTimeBy(1)
            assertFalse(
                engine.vibrationState.value.enabled,
                "Should be disabled"
            )
            assertEquals(
                newIntensity,
                engine.vibrationState.value.intensity,
                "Intensity should be preserved after disable"
            )
        }
    }
    
    /**
     * Property: Enable/disable state must be observable via StateFlow
     * 
     * Tests that the enabled state is correctly exposed through the 
     * vibrationState StateFlow for reactive observation.
     */
    @Test
    fun testEnableDisableStateIsObservable() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        // Initial state should be disabled
        assertFalse(
            engine.vibrationState.value.enabled,
            "Initial state should be disabled"
        )
        
        // Enable and verify observable state
        engine.enableVibration()
        assertTrue(
            engine.vibrationState.value.enabled,
            "Observable state should reflect enabled"
        )
        
        // Disable and verify observable state
        engine.disableVibration()
        advanceTimeBy(1)
        assertFalse(
            engine.vibrationState.value.enabled,
            "Observable state should reflect disabled"
        )
        
        // Multiple transitions
        for (i in 1..5) {
            engine.enableVibration()
            assertTrue(
                engine.vibrationState.value.enabled,
                "Observable state should reflect enabled on iteration $i"
            )
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS)
            
            engine.disableVibration()
            advanceTimeBy(1)
            assertFalse(
                engine.vibrationState.value.enabled,
                "Observable state should reflect disabled on iteration $i"
            )
        }
    }
}


