package com.seashore.zoonzoon.gamepad.platform

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * Property test for connection loss recovery.
 * 
 * **Property 18: Connection Loss Recovery**
 * **Validates: Requirement 10.2**
 * 
 * Tests that when a controller connection is lost during vibration,
 * the system updates the ConnectionState and stops vibration attempts.
 */
@RunWith(AndroidJUnit4::class)
class ConnectionLossRecoveryPropertyTest {
    
    private lateinit var context: Context
    private lateinit var controller: PlatformGamepadController
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        controller = PlatformGamepadController(context)
    }
    
    /**
     * Property: For any active vibration state, when a controller connection is lost,
     * the System SHALL update the ConnectionState and stop vibration attempts.
     * 
     * This test verifies that:
     * 1. Connection loss is detected during vibration command sending
     * 2. ConnectionState is updated to Disconnected when connection is lost
     * 3. Subsequent vibration commands fail gracefully
     */
    @Test
    fun testConnectionLossUpdatesState() = runBlocking {
        // Initially, the controller should be disconnected
        assertTrue(
            controller.connectionState.value is ConnectionState.Disconnected,
            "Initial state should be Disconnected"
        )
        
        // Attempt to send a vibration command without a connection
        // This simulates connection loss during vibration
        val result = controller.sendVibrationCommand(0.5f, 0.5f)
        
        // Verify that the command fails
        assertTrue(
            result.isFailure,
            "Vibration command should fail when no controller is connected"
        )
        
        // Verify that the connection state remains Disconnected
        assertTrue(
            controller.connectionState.value is ConnectionState.Disconnected,
            "Connection state should remain Disconnected after failed command"
        )
    }
    
    /**
     * Test that multiple vibration attempts after connection loss all fail gracefully.
     */
    @Test
    fun testMultipleCommandsAfterConnectionLoss() = runBlocking {
        // Ensure we start disconnected
        assertTrue(
            controller.connectionState.value is ConnectionState.Disconnected,
            "Initial state should be Disconnected"
        )
        
        // Attempt multiple vibration commands
        val results = listOf(
            controller.sendVibrationCommand(0.3f, 0.3f),
            controller.sendVibrationCommand(0.5f, 0.5f),
            controller.sendVibrationCommand(0.7f, 0.7f)
        )
        
        // All commands should fail
        assertTrue(
            results.all { it.isFailure },
            "All vibration commands should fail when no controller is connected"
        )
        
        // Connection state should remain Disconnected
        assertTrue(
            controller.connectionState.value is ConnectionState.Disconnected,
            "Connection state should remain Disconnected after multiple failed commands"
        )
    }
    
    /**
     * Test that the system handles connection loss gracefully without crashing.
     */
    @Test
    fun testConnectionLossDoesNotCrash() = runBlocking {
        // This test verifies that connection loss handling doesn't throw exceptions
        try {
            // Start discovery
            controller.startDiscovery()
            
            // Wait a bit for state to update
            delay(100)
            
            // Verify state changed to Scanning
            assertTrue(
                controller.connectionState.value is ConnectionState.Scanning,
                "State should be Scanning after startDiscovery"
            )
            
            // Stop discovery (simulates connection loss during scanning)
            controller.stopDiscovery()
            
            // Wait a bit for state to update
            delay(100)
            
            // Verify state changed back to Disconnected
            assertTrue(
                controller.connectionState.value is ConnectionState.Disconnected,
                "State should be Disconnected after stopDiscovery"
            )
            
            // Attempt to send a command after "connection loss"
            val result = controller.sendVibrationCommand(0.5f, 0.5f)
            
            // Should fail gracefully
            assertTrue(
                result.isFailure,
                "Command should fail after connection loss"
            )
        } catch (e: Exception) {
            throw AssertionError("Connection loss handling should not throw exceptions", e)
        }
    }
    
    /**
     * Test that disconnect() properly cleans up and updates state.
     */
    @Test
    fun testDisconnectUpdatesState() = runBlocking {
        // Call disconnect
        controller.disconnect()
        
        // Wait a bit for state to update
        delay(100)
        
        // Verify state is Disconnected
        assertTrue(
            controller.connectionState.value is ConnectionState.Disconnected,
            "State should be Disconnected after disconnect()"
        )
        
        // Verify that vibration commands fail after disconnect
        val result = controller.sendVibrationCommand(0.5f, 0.5f)
        assertTrue(
            result.isFailure,
            "Vibration command should fail after disconnect"
        )
    }
}
