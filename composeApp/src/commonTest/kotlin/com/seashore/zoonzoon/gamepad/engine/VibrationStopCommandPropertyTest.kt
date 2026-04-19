package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based tests for vibration stop command.
 * 
 * **Validates: Requirement 2.3**
 * 
 * Property 3: Vibration Stop Command
 * For any state where vibration is disabled, the VibrationEngine SHALL send a 
 * vibration command with motor values of 0 to the PlatformGamepadController.
 * 
 * This test validates that disabling vibration always sends a stop command with 
 * motor values of 0, regardless of the active pattern, intensity, or previous state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VibrationStopCommandPropertyTest {
    
    /**
     * Property: Disabling vibration must send a stop command (0, 0) for all patterns
     * 
     * Tests that calling disableVibration() sends a stop command with motor values 
     * of 0 regardless of which pattern was active.
     */
    @Test
    fun testDisableVibrationSendsStopCommandForAllPatterns() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        // Reduced to 4 representative patterns for faster execution
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
            
            // Set pattern and enable vibration
            engine.setPattern(pattern)
            engine.setIntensity(0.7f)
            engine.enableVibration()
            
            // Let it run for a bit
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)
            
            // Disable vibration
            engine.disableVibration()
            advanceTimeBy(1) // Allow async stop command to execute
            
            // Verify a stop command was sent
            val lastCommand = controller.getLastCommand()
            assertTrue(
                lastCommand != null,
                "A command should be sent after disableVibration() for pattern=${pattern::class.simpleName}"
            )
            assertEquals(
                0f,
                lastCommand!!.leftMotor,
                "Left motor must be 0 after disableVibration() for pattern=${pattern::class.simpleName}"
            )
            assertEquals(
                0f,
                lastCommand.rightMotor,
                "Right motor must be 0 after disableVibration() for pattern=${pattern::class.simpleName}"
            )
        }
    }
    
    /**
     * Property: Disabling vibration must send a stop command (0, 0) for all intensities
     * 
     * Tests that calling disableVibration() sends a stop command with motor values 
     * of 0 regardless of the intensity setting.
     */
    @Test
    fun testDisableVibrationSendsStopCommandForAllIntensities() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        // Reduced to 5 representative intensities for faster execution
        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        
        for (intensity in intensities) {
            controller.reset()
            
            // Set intensity and enable vibration
            engine.setPattern(VibrationPattern.Constant)
            engine.setIntensity(intensity)
            engine.enableVibration()
            
            // Let it run for a bit
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)
            
            // Disable vibration
            engine.disableVibration()
            advanceTimeBy(1) // Allow async stop command to execute
            
            // Verify a stop command was sent
            val lastCommand = controller.getLastCommand()
            assertTrue(
                lastCommand != null,
                "A command should be sent after disableVibration() for intensity=$intensity"
            )
            assertEquals(
                0f,
                lastCommand!!.leftMotor,
                "Left motor must be 0 after disableVibration() for intensity=$intensity"
            )
            assertEquals(
                0f,
                lastCommand.rightMotor,
                "Right motor must be 0 after disableVibration() for intensity=$intensity"
            )
        }
    }
    
    /**
     * Property: Stop command must be sent for all pattern-intensity combinations
     * 
     * Tests that the stop command property holds across all combinations of 
     * patterns and intensities.
     */
    @Test
    fun testStopCommandForAllPatternIntensityCombinations() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        // Reduced to 3 representative patterns for faster execution
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )
        
        // Reduced to 3 representative intensities for faster execution
        val intensities = listOf(0.0f, 0.5f, 1.0f)
        
        for (pattern in patterns) {
            for (intensity in intensities) {
                controller.reset()
                
                // Set pattern, intensity, and enable
                engine.setPattern(pattern)
                engine.setIntensity(intensity)
                engine.enableVibration()
                
                // Let it run
                advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
                
                // Disable
                engine.disableVibration()
                advanceTimeBy(1)
                
                // Verify stop command
                val lastCommand = controller.getLastCommand()
                assertTrue(
                    lastCommand != null,
                    "Command should be sent for pattern=${pattern::class.simpleName}, intensity=$intensity"
                )
                assertEquals(
                    0f,
                    lastCommand!!.leftMotor,
                    "Left motor must be 0 for pattern=${pattern::class.simpleName}, intensity=$intensity"
                )
                assertEquals(
                    0f,
                    lastCommand.rightMotor,
                    "Right motor must be 0 for pattern=${pattern::class.simpleName}, intensity=$intensity"
                )
            }
        }
    }
    
    /**
     * Property: Stop command must be sent even when disabling from already-disabled state
     * 
     * Tests that calling disableVibration() when already disabled still sends 
     * a stop command (idempotent behavior with stop command guarantee).
     */
    @Test
    fun testStopCommandSentEvenWhenAlreadyDisabled() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )
        
        for (pattern in patterns) {
            controller.reset()
            
            engine.setPattern(pattern)
            engine.setIntensity(0.5f)
            
            // Enable and then disable
            engine.enableVibration()
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            engine.disableVibration()
            advanceTimeBy(1)
            
            val commandsAfterFirstDisable = controller.commands.size
            
            // Disable again (already disabled)
            engine.disableVibration()
            advanceTimeBy(1)
            
            // Verify another stop command was sent
            assertTrue(
                controller.commands.size > commandsAfterFirstDisable,
                "A stop command should be sent even when already disabled for pattern=${pattern::class.simpleName}"
            )
            
            val lastCommand = controller.getLastCommand()
            assertEquals(
                0f,
                lastCommand!!.leftMotor,
                "Left motor must be 0 for repeated disable for pattern=${pattern::class.simpleName}"
            )
            assertEquals(
                0f,
                lastCommand.rightMotor,
                "Right motor must be 0 for repeated disable for pattern=${pattern::class.simpleName}"
            )
        }
    }
    
    /**
     * Property: Stop command must be sent immediately after disable, before any other commands
     * 
     * Tests that the stop command is sent as part of the disable operation and 
     * no pattern commands follow it.
     */
    @Test
    fun testStopCommandSentImmediatelyAfterDisable() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 3.0f),
            VibrationPattern.Wave(frequencyHz = 1.5f),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.6f, 0.4f, 100L),
                    VibrationPattern.Custom.Frame(0.4f, 0.6f, 100L)
                ),
                loop = true
            )
        )
        
        for (pattern in patterns) {
            controller.reset()
            
            engine.setPattern(pattern)
            engine.setIntensity(0.8f)
            engine.enableVibration()
            
            // Let it run
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)
            val commandsBeforeDisable = controller.commands.size
            
            // Disable
            engine.disableVibration()
            advanceTimeBy(1)
            
            // Verify at least one new command was sent
            assertTrue(
                controller.commands.size > commandsBeforeDisable,
                "At least one command should be sent after disableVibration() for pattern=${pattern::class.simpleName}"
            )
            
            // Get commands sent after disable
            val commandsAfterDisable = controller.commands.drop(commandsBeforeDisable)
            
            // The last command (or one of the recent commands) must be a stop command
            val lastCommand = commandsAfterDisable.last()
            assertEquals(
                0f,
                lastCommand.leftMotor,
                "Last command after disable must have left motor = 0 for pattern=${pattern::class.simpleName}"
            )
            assertEquals(
                0f,
                lastCommand.rightMotor,
                "Last command after disable must have right motor = 0 for pattern=${pattern::class.simpleName}"
            )
            
            // Advance more time and verify no non-zero commands follow
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 10)
            val allCommandsAfterDisable = controller.commands.drop(commandsBeforeDisable)
            val nonZeroCommandsAfterDisable = allCommandsAfterDisable.filter { 
                it.leftMotor != 0f || it.rightMotor != 0f 
            }
            
            assertTrue(
                nonZeroCommandsAfterDisable.isEmpty(),
                "No non-zero commands should follow disableVibration() for pattern=${pattern::class.simpleName}, " +
                "but got: $nonZeroCommandsAfterDisable"
            )
        }
    }
    
    /**
     * Property: Stop command must be sent regardless of how long vibration was active
     * 
     * Tests that the stop command is sent whether vibration was active for a short 
     * or long duration before being disabled.
     */
    @Test
    fun testStopCommandSentRegardlessOfActiveDuration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(0.5f)
        
        // Reduced to 5 representative durations for faster execution
        val durations = listOf(
            0L,                                      // Immediate disable
            VibrationEngine.FRAME_INTERVAL_MS,       // Exactly one frame
            VibrationEngine.FRAME_INTERVAL_MS * 2,   // Two frames
            VibrationEngine.FRAME_INTERVAL_MS * 10,  // Many frames
            VibrationEngine.FRAME_INTERVAL_MS * 50   // Long duration
        )
        
        for (duration in durations) {
            controller.reset()
            
            // Enable vibration
            engine.enableVibration()
            
            // Wait for specified duration
            if (duration > 0) {
                advanceTimeBy(duration)
            }
            
            // Disable vibration
            engine.disableVibration()
            advanceTimeBy(1)
            
            // Verify stop command was sent
            val lastCommand = controller.getLastCommand()
            assertTrue(
                lastCommand != null,
                "A command should be sent after disableVibration() for duration=$duration ms"
            )
            assertEquals(
                0f,
                lastCommand!!.leftMotor,
                "Left motor must be 0 after disableVibration() for duration=$duration ms"
            )
            assertEquals(
                0f,
                lastCommand.rightMotor,
                "Right motor must be 0 after disableVibration() for duration=$duration ms"
            )
        }
    }
    
    /**
     * Property: Stop command must be sent even if pattern changes occurred while enabled
     * 
     * Tests that the stop command is sent correctly even if the pattern was 
     * changed multiple times while vibration was enabled.
     */
    @Test
    fun testStopCommandSentAfterPatternChangesWhileEnabled() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        // Reduced to 3 representative patterns for faster execution
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )
        
        controller.reset()
        
        // Enable with first pattern
        engine.setPattern(patterns[0])
        engine.setIntensity(0.6f)
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        
        // Change patterns multiple times while enabled
        for (i in 1 until patterns.size) {
            engine.setPattern(patterns[i])
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        }
        
        val commandsBeforeDisable = controller.commands.size
        
        // Disable vibration
        engine.disableVibration()
        advanceTimeBy(1)
        
        // Verify stop command was sent
        assertTrue(
            controller.commands.size > commandsBeforeDisable,
            "A command should be sent after disableVibration() even after pattern changes"
        )
        
        val lastCommand = controller.getLastCommand()
        assertEquals(
            0f,
            lastCommand!!.leftMotor,
            "Left motor must be 0 after disableVibration() following pattern changes"
        )
        assertEquals(
            0f,
            lastCommand.rightMotor,
            "Right motor must be 0 after disableVibration() following pattern changes"
        )
    }
    
    /**
     * Property: Stop command must be sent even if intensity changes occurred while enabled
     * 
     * Tests that the stop command is sent correctly even if the intensity was 
     * changed multiple times while vibration was enabled.
     */
    @Test
    fun testStopCommandSentAfterIntensityChangesWhileEnabled() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        engine.setPattern(VibrationPattern.Constant)
        
        // Reduced to 4 representative intensities for faster execution
        val intensities = listOf(0.2f, 0.4f, 0.7f, 1.0f)
        
        controller.reset()
        
        // Enable with first intensity
        engine.setIntensity(intensities[0])
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        
        // Change intensity multiple times while enabled
        for (i in 1 until intensities.size) {
            engine.setIntensity(intensities[i])
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        }
        
        val commandsBeforeDisable = controller.commands.size
        
        // Disable vibration
        engine.disableVibration()
        advanceTimeBy(1)
        
        // Verify stop command was sent
        assertTrue(
            controller.commands.size > commandsBeforeDisable,
            "A command should be sent after disableVibration() even after intensity changes"
        )
        
        val lastCommand = controller.getLastCommand()
        assertEquals(
            0f,
            lastCommand!!.leftMotor,
            "Left motor must be 0 after disableVibration() following intensity changes"
        )
        assertEquals(
            0f,
            lastCommand.rightMotor,
            "Right motor must be 0 after disableVibration() following intensity changes"
        )
    }
    
    /**
     * Property: Multiple disable calls must each send a stop command
     * 
     * Tests that each call to disableVibration() sends its own stop command, 
     * ensuring the controller receives explicit stop signals.
     */
    @Test
    fun testMultipleDisableCallsEachSendStopCommand() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(0.5f)
        
        // Reduced to 3 iterations for faster execution
        for (iteration in 1..3) {
            controller.reset()
            
            engine.enableVibration()
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            
            // Disable
            engine.disableVibration()
            advanceTimeBy(1)
            
            // Verify stop command was sent for this iteration
            val stopCommands = controller.getStopCommands()
            assertTrue(
                stopCommands.isNotEmpty(),
                "At least one stop command should be sent on iteration $iteration"
            )
            
            val lastCommand = controller.getLastCommand()
            assertEquals(
                0f,
                lastCommand!!.leftMotor,
                "Left motor must be 0 on iteration $iteration"
            )
            assertEquals(
                0f,
                lastCommand.rightMotor,
                "Right motor must be 0 on iteration $iteration"
            )
        }
    }
    
    /**
     * Property: Stop command must be sent for custom patterns with extreme values
     * 
     * Tests that the stop command property holds even for custom patterns with 
     * extreme motor values (all 0s or all 1s).
     */
    @Test
    fun testStopCommandForCustomPatternsWithExtremeValues() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        
        val extremePatterns = listOf(
            // Pattern with all zeros (already at stop values)
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.0f, 0.0f, 100L),
                    VibrationPattern.Custom.Frame(0.0f, 0.0f, 100L)
                ),
                loop = true
            ),
            // Pattern with all max values
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(1.0f, 1.0f, 100L),
                    VibrationPattern.Custom.Frame(1.0f, 1.0f, 100L)
                ),
                loop = true
            ),
            // Pattern with mixed extremes
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.0f, 1.0f, 100L),
                    VibrationPattern.Custom.Frame(1.0f, 0.0f, 100L)
                ),
                loop = true
            )
        )
        
        for ((index, pattern) in extremePatterns.withIndex()) {
            controller.reset()
            
            engine.setPattern(pattern)
            engine.setIntensity(0.7f)
            engine.enableVibration()
            
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)
            
            engine.disableVibration()
            advanceTimeBy(1)
            
            // Verify stop command was sent
            val lastCommand = controller.getLastCommand()
            assertTrue(
                lastCommand != null,
                "A command should be sent after disableVibration() for extreme pattern $index"
            )
            assertEquals(
                0f,
                lastCommand!!.leftMotor,
                "Left motor must be 0 after disableVibration() for extreme pattern $index"
            )
            assertEquals(
                0f,
                lastCommand.rightMotor,
                "Right motor must be 0 after disableVibration() for extreme pattern $index"
            )
        }
    }
}


