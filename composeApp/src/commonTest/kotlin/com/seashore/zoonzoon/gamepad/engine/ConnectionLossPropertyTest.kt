package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Property-based tests for connection loss recovery.
 * 
 * **Validates: Requirement 10.2**
 * 
 * Property 18: Connection Loss Recovery
 * For any active vibration state, when a controller connection is lost, the System 
 * SHALL update the ConnectionState and stop vibration attempts.
 * 
 * This test validates that the VibrationEngine correctly handles connection loss
 * by stopping vibration execution when the controller disconnects.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionLossPropertyTest {
    
    /**
     * Property: Connection loss during active vibration must stop vibration
     * 
     * Tests that when a controller disconnects while vibration is active,
     * the VibrationEngine stops vibration and updates its state.
     */
    @Test
    fun testConnectionLossStopsActiveVibration() = runTest {
        val controller = FakePlatformGamepadController()
        controller.setConnectionState(ConnectionState.Connected("Test Controller"))
        
        val engine = VibrationEngine(controller, this)
        
        // Enable vibration
        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(0.8f)
        engine.enableVibration()
        
        // Let it run for a few frames
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3 + 1)
        
        assertTrue(engine.vibrationState.value.enabled, "Vibration should be enabled")
        val commandsBeforeDisconnect = controller.commands.size
        assertTrue(commandsBeforeDisconnect > 0, "Should have sent commands before disconnect")
        
        // Simulate connection loss
        controller.setConnectionState(ConnectionState.Disconnected)
        
        // Advance time to allow the observer to react
        advanceTimeBy(100)
        
        // Vibration should now be disabled
        assertFalse(engine.vibrationState.value.enabled, "Vibration should be disabled after connection loss")
        
        // No more commands should be sent after connection loss
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)
        
        // Only stop command (0, 0) should have been sent after disconnect
        val commandsAfterDisconnect = controller.commands.drop(commandsBeforeDisconnect)
        val nonStopCommands = commandsAfterDisconnect.filter { it.leftMotor != 0f || it.rightMotor != 0f }
        
        assertTrue(
            nonStopCommands.isEmpty(),
            "No pattern commands should be sent after connection loss, but got: $nonStopCommands"
        )
    }
    
    /**
     * Property: Connection loss while disabled should not affect state
     * 
     * Tests that connection loss while vibration is already disabled
     * doesn't cause any issues.
     */
    @Test
    fun testConnectionLossWhileDisabledIsHandledGracefully() = runTest {
        val controller = FakePlatformGamepadController()
        controller.setConnectionState(ConnectionState.Connected("Test Controller"))
        
        val engine = VibrationEngine(controller, this)
        
        // Vibration is disabled by default
        assertFalse(engine.vibrationState.value.enabled, "Vibration should start disabled")
        
        // Simulate connection loss
        controller.setConnectionState(ConnectionState.Disconnected)
        
        // Advance time
        advanceTimeBy(100)
        
        // State should remain disabled
        assertFalse(engine.vibrationState.value.enabled, "Vibration should remain disabled")
        
        // No commands should have been sent
        assertTrue(controller.commands.isEmpty(), "No commands should be sent when vibration is disabled")
    }
    
    /**
     * Property: Reconnection after connection loss allows vibration to resume
     * 
     * Tests that after a connection is lost and then re-established,
     * vibration can be enabled again.
     */
    @Test
    fun testReconnectionAllowsVibrationToResume() = runTest {
        val controller = FakePlatformGamepadController()
        controller.setConnectionState(ConnectionState.Connected("Test Controller"))
        
        val engine = VibrationEngine(controller, this)
        
        // Enable vibration
        engine.setPattern(VibrationPattern.Constant)
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2 + 1)
        
        assertTrue(engine.vibrationState.value.enabled, "Vibration should be enabled")
        
        // Simulate connection loss
        controller.setConnectionState(ConnectionState.Disconnected)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS + 1)
        
        assertFalse(engine.vibrationState.value.enabled, "Vibration should be disabled after connection loss")
        
        controller.commands.clear()
        
        // Simulate reconnection
        controller.setConnectionState(ConnectionState.Connected("Test Controller"))
        advanceTimeBy(100)
        
        // Enable vibration again
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3 + 1)
        
        // Vibration should work again
        assertTrue(engine.vibrationState.value.enabled, "Vibration should be enabled after reconnection")
        assertTrue(controller.commands.isNotEmpty(), "Commands should be sent after reconnection")
        
        // Clean up - disable vibration before test ends
        engine.disableVibration()
        advanceTimeBy(1)
    }
}
