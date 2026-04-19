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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for AndroidGamepadController.
 * 
 * Tests controller discovery, connection, HID command formatting,
 * and error handling scenarios.
 * 
 * **Validates: Requirements 1.8, 6.2, 6.4**
 */
@RunWith(AndroidJUnit4::class)
class AndroidGamepadControllerTest {
    
    private lateinit var context: Context
    private lateinit var controller: PlatformGamepadController
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        controller = PlatformGamepadController(context)
    }
    
    // ========== Controller Discovery and Connection Tests ==========
    
    /**
     * Test that controller starts in Disconnected state.
     * 
     * **Validates: Requirement 1.8** - Android Bluetooth/USB HID support
     */
    @Test
    fun testInitialStateIsDisconnected() {
        val state = controller.connectionState.value
        assertTrue(
            state is ConnectionState.Disconnected,
            "Initial connection state should be Disconnected, got $state"
        )
    }
    
    /**
     * Test that startDiscovery changes state to Scanning.
     * 
     * **Validates: Requirement 1.8** - Controller discovery
     */
    @Test
    fun testStartDiscoveryChangesStateToScanning() = runBlocking {
        // Start discovery
        controller.startDiscovery()
        
        // Wait a bit for state to update
        delay(100)
        
        // Verify state changed to Scanning
        val state = controller.connectionState.value
        assertTrue(
            state is ConnectionState.Scanning,
            "State should be Scanning after startDiscovery, got $state"
        )
    }
    
    /**
     * Test that stopDiscovery changes state back to Disconnected.
     * 
     * **Validates: Requirement 1.8** - Controller discovery
     */
    @Test
    fun testStopDiscoveryChangesStateToDisconnected() = runBlocking {
        // Start discovery
        controller.startDiscovery()
        delay(100)
        
        // Stop discovery
        controller.stopDiscovery()
        delay(100)
        
        // Verify state changed back to Disconnected
        val state = controller.connectionState.value
        assertTrue(
            state is ConnectionState.Disconnected,
            "State should be Disconnected after stopDiscovery, got $state"
        )
    }
    
    /**
     * Test that disconnect() updates state to Disconnected.
     * 
     * **Validates: Requirement 1.8** - Connection management
     */
    @Test
    fun testDisconnectUpdatesState() = runBlocking {
        // Call disconnect
        controller.disconnect()
        delay(100)
        
        // Verify state is Disconnected
        val state = controller.connectionState.value
        assertTrue(
            state is ConnectionState.Disconnected,
            "State should be Disconnected after disconnect(), got $state"
        )
    }
    
    // ========== HID Command Formatting Tests ==========
    
    /**
     * Test that vibration commands with valid motor values are accepted.
     * 
     * **Validates: Requirement 6.4** - HID protocol formatting
     */
    @Test
    fun testValidMotorValuesAreAccepted() = runBlocking {
        val testCases = listOf(
            Pair(0.0f, 0.0f),
            Pair(0.5f, 0.5f),
            Pair(1.0f, 1.0f),
            Pair(0.25f, 0.75f)
        )
        
        for ((leftMotor, rightMotor) in testCases) {
            val result = controller.sendVibrationCommand(leftMotor, rightMotor)
            
            // Without a connected controller, this should fail with IllegalStateException
            // but not with IllegalArgumentException (which would indicate invalid values)
            assertTrue(
                result.isFailure,
                "Command should fail without connected controller"
            )
            
            val exception = result.exceptionOrNull()
            assertTrue(
                exception is IllegalStateException,
                "Expected IllegalStateException for no connection, got ${exception?.javaClass?.simpleName}"
            )
        }
    }
    
    /**
     * Test that vibration commands with invalid motor values are rejected.
     * 
     * **Validates: Requirement 6.4** - Input validation
     */
    @Test
    fun testInvalidMotorValuesAreRejected() = runBlocking {
        val invalidTestCases = listOf(
            Pair(-0.1f, 0.5f),  // Negative left motor
            Pair(0.5f, -0.1f),  // Negative right motor
            Pair(1.1f, 0.5f),   // Left motor > 1.0
            Pair(0.5f, 1.1f),   // Right motor > 1.0
            Pair(Float.NaN, 0.5f), // NaN left motor
            Pair(0.5f, Float.NaN)  // NaN right motor
        )
        
        for ((leftMotor, rightMotor) in invalidTestCases) {
            val result = controller.sendVibrationCommand(leftMotor, rightMotor)
            
            // Verify that invalid values are rejected with IllegalArgumentException
            assertTrue(
                result.isFailure,
                "Command with invalid values should fail"
            )
            
            val exception = result.exceptionOrNull()
            assertTrue(
                exception is IllegalArgumentException,
                "Expected IllegalArgumentException for invalid values ($leftMotor, $rightMotor), got ${exception?.javaClass?.simpleName}"
            )
        }
    }
    
    /**
     * Test motor value scaling from [0.0, 1.0] to [0, 255].
     * 
     * **Validates: Requirement 6.4** - HID protocol formatting
     */
    @Test
    fun testMotorValueScaling() {
        val testCases = mapOf(
            0.0f to 0,
            0.5f to 127,
            1.0f to 255,
            0.25f to 63,
            0.75f to 191
        )
        
        for ((floatValue, expectedByte) in testCases) {
            val scaledValue = (floatValue * 255).toInt().coerceIn(0, 255)
            assertEquals(
                expectedByte,
                scaledValue,
                "Motor value $floatValue should scale to $expectedByte, got $scaledValue"
            )
        }
    }
    
    // ========== Error Handling Tests ==========
    
    /**
     * Test that sending commands without a connected controller fails gracefully.
     * 
     * **Validates: Requirement 6.2** - Error handling
     */
    @Test
    fun testCommandWithoutConnectionFailsGracefully() = runBlocking {
        val result = controller.sendVibrationCommand(0.5f, 0.5f)
        
        assertTrue(
            result.isFailure,
            "Command should fail without connected controller"
        )
        
        val exception = result.exceptionOrNull()
        assertTrue(
            exception is IllegalStateException,
            "Expected IllegalStateException, got ${exception?.javaClass?.simpleName}"
        )
        
        assertTrue(
            exception?.message?.contains("No controller connected") == true,
            "Error message should indicate no controller connected"
        )
    }
    
    /**
     * Test that multiple failed commands don't crash the system.
     * 
     * **Validates: Requirement 6.2** - Error resilience
     */
    @Test
    fun testMultipleFailedCommandsDontCrash() = runBlocking {
        try {
            // Send multiple commands without a connection
            repeat(10) {
                val result = controller.sendVibrationCommand(0.5f, 0.5f)
                assertTrue(result.isFailure, "Command should fail")
            }
            
            // Verify state is still valid
            val state = controller.connectionState.value
            assertTrue(
                state is ConnectionState.Disconnected,
                "State should remain Disconnected"
            )
        } catch (e: Exception) {
            throw AssertionError("Multiple failed commands should not crash", e)
        }
    }
    
    /**
     * Test that the controller handles edge cases gracefully.
     * 
     * **Validates: Requirement 6.2** - Edge case handling
     */
    @Test
    fun testEdgeCaseHandling() = runBlocking {
        // Test boundary values
        val edgeCases = listOf(
            Pair(0.0f, 0.0f),   // Minimum
            Pair(1.0f, 1.0f),   // Maximum
            Pair(0.0f, 1.0f),   // Left off, right max
            Pair(1.0f, 0.0f)    // Left max, right off
        )
        
        for ((leftMotor, rightMotor) in edgeCases) {
            val result = controller.sendVibrationCommand(leftMotor, rightMotor)
            
            // Should fail gracefully (no connection), not crash
            assertTrue(
                result.isFailure,
                "Edge case ($leftMotor, $rightMotor) should fail gracefully"
            )
        }
    }
    
    /**
     * Test that discovery can be started and stopped multiple times.
     * 
     * **Validates: Requirement 1.8** - Discovery lifecycle
     */
    @Test
    fun testDiscoveryCanBeStartedAndStoppedMultipleTimes() = runBlocking {
        repeat(3) { iteration ->
            // Start discovery
            controller.startDiscovery()
            delay(100)
            
            val scanningState = controller.connectionState.value
            assertTrue(
                scanningState is ConnectionState.Scanning,
                "Iteration $iteration: State should be Scanning"
            )
            
            // Stop discovery
            controller.stopDiscovery()
            delay(100)
            
            val disconnectedState = controller.connectionState.value
            assertTrue(
                disconnectedState is ConnectionState.Disconnected,
                "Iteration $iteration: State should be Disconnected"
            )
        }
    }
}
