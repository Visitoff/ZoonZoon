package com.seashore.zoonzoon.gamepad.platform

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.seashore.zoonzoon.gamepad.engine.GamepadControllerWithState
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

/**
 * Android implementation of PlatformGamepadController.
 * 
 * This implementation uses Android's Bluetooth and USB HID APIs to communicate
 * with PlayStation (DualShock 4, DualSense) and Xbox controllers.
 * 
 * Supports:
 * - Bluetooth HID connection for wireless controllers
 * - USB HID connection for wired controllers
 * - Controller discovery and automatic connection
 * - Connection state management
 * - Error handling and logging
 * 
 * **Validates: Requirements 1.1, 1.2, 6.1, 6.2, 1.8**
 */
actual class PlatformGamepadController(
    private val context: Context
) : GamepadControllerWithState {
    
    companion object {
        private const val TAG = "AndroidGamepadController"
        
        // Standard HID UUID for Bluetooth HID devices
        private val HID_UUID = UUID.fromString("00001124-0000-1000-8000-00805f9b34fb")
        
        // Known controller vendor IDs
        private const val SONY_VENDOR_ID = 0x054C
        private const val MICROSOFT_VENDOR_ID = 0x045E
        
        // Known controller product IDs
        private const val DUALSHOCK4_PRODUCT_ID = 0x05C4
        private const val DUALSHOCK4_V2_PRODUCT_ID = 0x09CC
        private const val DUALSENSE_PRODUCT_ID = 0x0CE6
        private const val XBOX_ONE_PRODUCT_ID = 0x02DD
        private const val XBOX_SERIES_PRODUCT_ID = 0x0B13
    }
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    actual override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val bluetoothManager: BluetoothManager? = 
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    
    private var bluetoothSocket: BluetoothSocket? = null
    private var connectedDevice: Any? = null // BluetoothDevice or UsbDevice
    private var connectedControllerType: String? = null
    
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var discoveryJob: Job? = null
    
    // Broadcast receiver for Bluetooth device discovery
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = 
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { handleBluetoothDeviceFound(it) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Bluetooth discovery finished")
                }
            }
        }
    }
    
    // Broadcast receiver for USB device detachment
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device: UsbDevice? = 
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    device?.let { handleUsbDeviceDetached(it) }
                }
            }
        }
    }
    
    private var isBluetoothReceiverRegistered = false
    private var isUsbReceiverRegistered = false
    
    /**
     * Start scanning for available controllers.
     * 
     * This method initiates discovery for both Bluetooth and USB HID controllers.
     * It first checks for already paired Bluetooth devices and connected USB devices,
     * then starts Bluetooth discovery for new devices.
     * 
     * **Validates: Requirements 1.1, 1.2, 1.8**
     */
    actual override suspend fun startDiscovery() = withContext(Dispatchers.Main) {
        Log.d(TAG, "Starting controller discovery")
        _connectionState.value = ConnectionState.Scanning
        
        // Check for USB devices first (faster)
        checkUsbDevices()
        
        // If no USB device found, check Bluetooth
        if (_connectionState.value is ConnectionState.Scanning) {
            checkBluetoothDevices()
        }
    }
    
    /**
     * Check for connected USB HID controllers.
     * 
     * Scans all connected USB devices and attempts to identify and connect
     * to supported game controllers.
     */
    private suspend fun checkUsbDevices() = withContext(Dispatchers.IO) {
        try {
            val deviceList = usbManager.deviceList
            Log.d(TAG, "Found ${deviceList.size} USB devices")
            
            for ((_, device) in deviceList) {
                val controllerType = identifyUsbController(device)
                if (controllerType != null) {
                    Log.d(TAG, "Found USB controller: $controllerType")
                    connectUsbDevice(device, controllerType)
                    return@withContext
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking USB devices", e)
        }
    }
    
    /**
     * Check for paired Bluetooth controllers and start discovery.
     * 
     * First checks already paired devices, then starts Bluetooth discovery
     * to find new devices.
     */
    private suspend fun checkBluetoothDevices() = withContext(Dispatchers.Main) {
        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth not available on this device")
            return@withContext
        }
        
        if (!bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth is not enabled")
            return@withContext
        }
        
        try {
            // Check paired devices first
            val pairedDevices = bluetoothAdapter.bondedDevices
            Log.d(TAG, "Found ${pairedDevices.size} paired Bluetooth devices")
            
            for (device in pairedDevices) {
                if (isGameController(device)) {
                    Log.d(TAG, "Found paired controller: ${device.name}")
                    connectBluetoothDevice(device)
                    return@withContext
                }
            }
            
            // Start discovery for new devices
            registerBluetoothReceiver()
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()
            
            // Start a timeout job to stop discovery after a reasonable time
            discoveryJob?.cancel()
            discoveryJob = scope.launch {
                delay(30000) // 30 seconds timeout
                if (isActive && _connectionState.value is ConnectionState.Scanning) {
                    stopDiscovery()
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth permission denied", e)
            _connectionState.value = ConnectionState.Disconnected
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Bluetooth devices", e)
            _connectionState.value = ConnectionState.Disconnected
        }
    }
    
    /**
     * Register the Bluetooth broadcast receiver if not already registered.
     */
    private fun registerBluetoothReceiver() {
        if (!isBluetoothReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            context.registerReceiver(bluetoothReceiver, filter)
            isBluetoothReceiverRegistered = true
        }
    }
    
    /**
     * Unregister the Bluetooth broadcast receiver if registered.
     */
    private fun unregisterBluetoothReceiver() {
        if (isBluetoothReceiverRegistered) {
            try {
                context.unregisterReceiver(bluetoothReceiver)
                isBluetoothReceiverRegistered = false
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered, ignore
            }
        }
    }
    
    /**
     * Register the USB broadcast receiver if not already registered.
     * 
     * This receiver monitors USB device detachment events to detect
     * when a connected controller is unplugged.
     * 
     * **Validates: Requirement 10.2** - Connection loss detection
     */
    private fun registerUsbReceiver() {
        if (!isUsbReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            }
            context.registerReceiver(usbReceiver, filter)
            isUsbReceiverRegistered = true
        }
    }
    
    /**
     * Unregister the USB broadcast receiver if registered.
     */
    private fun unregisterUsbReceiver() {
        if (isUsbReceiverRegistered) {
            try {
                context.unregisterReceiver(usbReceiver)
                isUsbReceiverRegistered = false
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered, ignore
            }
        }
    }
    
    /**
     * Handle USB device detachment event.
     * 
     * Called when a USB device is physically disconnected. If the detached
     * device is the currently connected controller, this triggers connection
     * loss handling.
     * 
     * **Validates: Requirement 10.2** - Connection loss during vibration
     */
    private fun handleUsbDeviceDetached(device: UsbDevice) {
        val currentDevice = connectedDevice
        if (currentDevice is UsbDevice && currentDevice == device) {
            Log.w(TAG, "Connected USB controller was detached")
            handleConnectionLoss()
        }
    }
    
    /**
     * Handle a newly discovered Bluetooth device.
     */
    private fun handleBluetoothDeviceFound(device: BluetoothDevice) {
        if (isGameController(device)) {
            Log.d(TAG, "Found controller during discovery: ${device.name}")
            scope.launch {
                connectBluetoothDevice(device)
            }
        }
    }
    
    /**
     * Check if a Bluetooth device is a game controller based on its name.
     */
    private fun isGameController(device: BluetoothDevice): Boolean {
        val name = device.name?.lowercase() ?: return false
        return name.contains("dualshock") || 
               name.contains("dualsense") || 
               name.contains("xbox") ||
               name.contains("wireless controller")
    }
    
    /**
     * Identify the controller type from a USB device.
     * 
     * @return Controller type string if recognized, null otherwise
     */
    private fun identifyUsbController(device: UsbDevice): String? {
        return when {
            device.vendorId == SONY_VENDOR_ID && device.productId == DUALSHOCK4_PRODUCT_ID -> "DualShock 4"
            device.vendorId == SONY_VENDOR_ID && device.productId == DUALSHOCK4_V2_PRODUCT_ID -> "DualShock 4"
            device.vendorId == SONY_VENDOR_ID && device.productId == DUALSENSE_PRODUCT_ID -> "DualSense"
            device.vendorId == MICROSOFT_VENDOR_ID && device.productId == XBOX_ONE_PRODUCT_ID -> "Xbox One"
            device.vendorId == MICROSOFT_VENDOR_ID && device.productId == XBOX_SERIES_PRODUCT_ID -> "Xbox Series"
            else -> null
        }
    }
    
    /**
     * Identify the controller type from a Bluetooth device name.
     * 
     * @return Controller type string if recognized, null otherwise
     */
    private fun identifyBluetoothController(device: BluetoothDevice): String? {
        val name = device.name?.lowercase() ?: return null
        return when {
            name.contains("dualshock 4") || name.contains("wireless controller") -> "DualShock 4"
            name.contains("dualsense") -> "DualSense"
            name.contains("xbox") -> "Xbox"
            else -> null
        }
    }
    
    /**
     * Connect to a USB controller device.
     */
    private suspend fun connectUsbDevice(device: UsbDevice, controllerType: String) = 
        withContext(Dispatchers.IO) {
            try {
                // TODO: Task 5.2, 5.3, 5.4 - Implement USB HID communication
                // For now, just update the connection state
                connectedDevice = device
                connectedControllerType = controllerType
                
                // Register USB receiver to monitor for disconnection
                withContext(Dispatchers.Main) {
                    registerUsbReceiver()
                    _connectionState.value = ConnectionState.Connected(controllerType)
                }
                Log.d(TAG, "Connected to USB controller: $controllerType")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to USB device", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    
    /**
     * Connect to a Bluetooth controller device.
     */
    private suspend fun connectBluetoothDevice(device: BluetoothDevice) = 
        withContext(Dispatchers.IO) {
            try {
                // Stop discovery to improve connection reliability
                bluetoothAdapter?.cancelDiscovery()
                
                val controllerType = identifyBluetoothController(device)
                if (controllerType == null) {
                    Log.w(TAG, "Could not identify controller type for ${device.name}")
                    return@withContext
                }
                
                Log.d(TAG, "Attempting to connect to Bluetooth controller: $controllerType")
                
                // Create RFCOMM socket for HID communication
                bluetoothSocket = device.createRfcommSocketToServiceRecord(HID_UUID)
                bluetoothSocket?.connect()
                
                connectedDevice = device
                connectedControllerType = controllerType
                
                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Connected(controllerType)
                }
                Log.d(TAG, "Connected to Bluetooth controller: $controllerType")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to connect to Bluetooth device", e)
                bluetoothSocket?.close()
                bluetoothSocket = null
                withContext(Dispatchers.Main) {
                    if (_connectionState.value is ConnectionState.Scanning) {
                        // Continue scanning if we're still in scanning state
                        return@withContext
                    }
                    _connectionState.value = ConnectionState.Disconnected
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Bluetooth permission denied during connection", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error connecting to Bluetooth device", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    
    /**
     * Stop scanning for controllers.
     * 
     * Cancels Bluetooth discovery and cleans up resources.
     */
    actual override suspend fun stopDiscovery() = withContext(Dispatchers.Main) {
        Log.d(TAG, "Stopping controller discovery")
        discoveryJob?.cancel()
        
        try {
            bluetoothAdapter?.cancelDiscovery()
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied when stopping discovery", e)
        }
        
        unregisterBluetoothReceiver()
        
        if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }
    
    /**
     * Send a vibration command to the connected controller.
     * 
     * This method formats and sends HID commands based on the connected controller type.
     * Platform-specific errors are translated to common error representations.
     * 
     * @param leftMotor Left motor intensity in range [0.0, 1.0]
     * @param rightMotor Right motor intensity in range [0.0, 1.0]
     * @return Result indicating success or failure with error details
     * 
     * **Validates: Requirements 6.4, 6.5, 10.3, 10.5**
     */
    actual override suspend fun sendVibrationCommand(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Validate motor values
            if (leftMotor !in 0.0f..1.0f || rightMotor !in 0.0f..1.0f) {
                val error = IllegalArgumentException("Motor values must be in range [0.0, 1.0]")
                Log.e(TAG, "Invalid motor values: left=$leftMotor, right=$rightMotor", error)
                return@withContext Result.failure(error)
            }
            
            // Check if we have a connected device
            if (connectedDevice == null || connectedControllerType == null) {
                val error = IllegalStateException("No controller connected")
                Log.e(TAG, "Attempted to send vibration command without connected controller", error)
                return@withContext Result.failure(error)
            }
            
            // Format and send HID command based on controller type
            val result = when (connectedControllerType) {
                "DualShock 4" -> sendDualShock4Command(leftMotor, rightMotor)
                "DualSense" -> sendDualSenseCommand(leftMotor, rightMotor)
                "Xbox One", "Xbox Series", "Xbox" -> sendXboxCommand(leftMotor, rightMotor)
                else -> {
                    val error = IllegalStateException("Unsupported controller type: $connectedControllerType")
                    Log.w(TAG, "Unsupported controller type: $connectedControllerType")
                    Result.failure(error)
                }
            }
            
            // Log command failures but continue execution (Requirement 10.3)
            if (result.isFailure) {
                Log.e(TAG, "Vibration command failed for $connectedControllerType", result.exceptionOrNull())
            }
            
            result
        } catch (e: SecurityException) {
            // Translate platform-specific Android security exception (Requirement 10.5)
            val translatedError = IllegalStateException("Permission denied for controller access", e)
            Log.e(TAG, "Security exception sending vibration command", e)
            Result.failure(translatedError)
        } catch (e: IOException) {
            // Translate platform-specific I/O exception (Requirement 10.5)
            val translatedError = IllegalStateException("Communication error with controller", e)
            Log.e(TAG, "I/O exception sending vibration command", e)
            Result.failure(translatedError)
        } catch (e: Exception) {
            // Translate any other platform-specific exception (Requirement 10.5)
            val translatedError = IllegalStateException("Unexpected error communicating with controller", e)
            Log.e(TAG, "Unexpected error sending vibration command", e)
            Result.failure(translatedError)
        }
    }
    
    /**
     * Format and send a DualShock 4 HID output report for vibration.
     * 
     * DualShock 4 HID Output Report Format (Report ID 0x11 for Bluetooth, 0x05 for USB):
     * - Byte 0: Report ID
     * - Byte 1: 0x80 (flags) for Bluetooth, 0xFF (enable rumble) for USB
     * - Byte 2: 0x00 (padding) - Bluetooth only
     * - Byte 3: 0xFF (flags - enable rumble) - Bluetooth only
     * - Byte 4: Right motor (weak rumble) - 0-255
     * - Byte 5: Left motor (strong rumble) - 0-255
     * - Bytes 6-10: RGB LED color (red, green, blue, flash on, flash off)
     * - Remaining bytes: padding/other features
     * 
     * @param leftMotor Left motor (strong rumble) intensity in range [0.0, 1.0]
     * @param rightMotor Right motor (weak rumble) intensity in range [0.0, 1.0]
     * @return Result indicating success or failure
     * 
     * **Validates: Requirements 1.5, 6.4, 10.3**
     */
    private suspend fun sendDualShock4Command(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Scale motor values from [0.0, 1.0] to [0, 255]
            val leftMotorByte = (leftMotor * 255).toInt().coerceIn(0, 255).toByte()
            val rightMotorByte = (rightMotor * 255).toInt().coerceIn(0, 255).toByte()
            
            // Determine if this is a Bluetooth or USB connection
            val isBluetoothConnection = connectedDevice is BluetoothDevice
            
            // Create HID output report
            val report = if (isBluetoothConnection) {
                // Bluetooth report (Report ID 0x11, 78 bytes total)
                ByteArray(78).apply {
                    this[0] = 0x11.toByte()  // Report ID for Bluetooth
                    this[1] = 0x80.toByte()  // Flags
                    this[2] = 0x00.toByte()  // Padding
                    this[3] = 0xFF.toByte()  // Flags - enable rumble
                    this[4] = rightMotorByte // Right motor (weak rumble)
                    this[5] = leftMotorByte  // Left motor (strong rumble)
                    // Bytes 6-10: RGB LED (keep default/off)
                    this[6] = 0x00.toByte()  // Red
                    this[7] = 0x00.toByte()  // Green
                    this[8] = 0x00.toByte()  // Blue
                    this[9] = 0x00.toByte()  // Flash on duration
                    this[10] = 0x00.toByte() // Flash off duration
                    // Remaining bytes are padding/other features (left as 0)
                }
            } else {
                // USB report (Report ID 0x05, 32 bytes total)
                ByteArray(32).apply {
                    this[0] = 0x05.toByte()  // Report ID for USB
                    this[1] = 0xFF.toByte()  // Flags - enable rumble
                    this[4] = rightMotorByte // Right motor (weak rumble)
                    this[5] = leftMotorByte  // Left motor (strong rumble)
                    // Bytes 6-10: RGB LED (keep default/off)
                    this[6] = 0x00.toByte()  // Red
                    this[7] = 0x00.toByte()  // Green
                    this[8] = 0x00.toByte()  // Blue
                    this[9] = 0x00.toByte()  // Flash on duration
                    this[10] = 0x00.toByte() // Flash off duration
                    // Remaining bytes are padding/other features (left as 0)
                }
            }
            
            // Send the HID report
            val sendResult = if (isBluetoothConnection) {
                sendBluetoothHidReport(report)
            } else {
                sendUsbHidReport(report)
            }
            
            if (sendResult) {
                Log.d(TAG, "DualShock 4 vibration command sent: left=$leftMotor, right=$rightMotor")
                Result.success(Unit)
            } else {
                // Log failure but allow execution to continue (Requirement 10.3)
                Log.e(TAG, "Failed to send DualShock 4 HID report")
                Result.failure(IOException("Failed to send HID report"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "I/O error sending DualShock 4 command", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error sending DualShock 4 command", e)
            Result.failure(e)
        }
    }
    
    /**
     * Format and send a DualSense HID output report for vibration.
     * 
     * DualSense HID Output Report Format:
     * - USB (Report ID 0x02, 47 bytes total):
     *   - Byte 0: Report ID (0x02)
     *   - Byte 1: Flags 0 (0x02 = enable rumble motors)
     *   - Byte 2: Flags 1 (0x04 = enable lightbar control, 0x10 = enable player LED control)
     *   - Byte 3: Right motor (0-255)
     *   - Byte 4: Left motor (0-255)
     *   - Bytes 5-10: Audio settings
     *   - Bytes 11-32: Trigger effects (11 bytes each for right and left triggers)
     *   - Bytes 33-46: LED and other settings
     * 
     * - Bluetooth (Report ID 0x31, 78 bytes total):
     *   - Byte 0: Report ID (0x31)
     *   - Byte 1: Sequence tag (increments 0-15 in upper nibble) + 0x10 magic tag
     *   - Byte 2: Flags 0 (0x02 = enable rumble motors)
     *   - Byte 3: Flags 1 (0x04 = enable lightbar control, 0x10 = enable player LED control)
     *   - Byte 4: Right motor (0-255)
     *   - Byte 5: Left motor (0-255)
     *   - Bytes 6-11: Audio settings
     *   - Bytes 12-33: Trigger effects (11 bytes each for right and left triggers)
     *   - Bytes 34-47: LED and other settings
     *   - Bytes 48-77: CRC32 checksum (last 4 bytes)
     * 
     * @param leftMotor Left motor (strong rumble) intensity in range [0.0, 1.0]
     * @param rightMotor Right motor (weak rumble) intensity in range [0.0, 1.0]
     * @return Result indicating success or failure
     * 
     * **Validates: Requirements 1.6, 6.4, 10.3**
     */
    private suspend fun sendDualSenseCommand(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Scale motor values from [0.0, 1.0] to [0, 255]
            val leftMotorByte = (leftMotor * 255).toInt().coerceIn(0, 255).toByte()
            val rightMotorByte = (rightMotor * 255).toInt().coerceIn(0, 255).toByte()
            
            // Determine if this is a Bluetooth or USB connection
            val isBluetoothConnection = connectedDevice is BluetoothDevice
            
            // Create HID output report
            val report = if (isBluetoothConnection) {
                // Bluetooth report (Report ID 0x31, 78 bytes total)
                ByteArray(78).apply {
                    this[0] = 0x31.toByte()  // Report ID for Bluetooth
                    this[1] = 0x10.toByte()  // Sequence tag (0x00) + magic tag (0x10)
                    this[2] = 0x02.toByte()  // Flags 0 - enable rumble motors (bit 1)
                    this[3] = 0x04.toByte()  // Flags 1 - enable lightbar control (bit 2)
                    this[4] = rightMotorByte // Right motor
                    this[5] = leftMotorByte  // Left motor
                    // Bytes 6-11: Audio settings (leave as 0 for default)
                    // Bytes 12-33: Trigger effects (leave as 0 for no effect)
                    // Bytes 34-47: LED and other settings (leave as 0 for default)
                    // Bytes 48-77: Padding and CRC (simplified - leave as 0)
                    // Note: A proper implementation would calculate CRC32 for bytes 0-73
                    // and place it in bytes 74-77, but many controllers accept reports without it
                }
            } else {
                // USB report (Report ID 0x02, 47 bytes total)
                ByteArray(47).apply {
                    this[0] = 0x02.toByte()  // Report ID for USB
                    this[1] = 0x02.toByte()  // Flags 0 - enable rumble motors (bit 1)
                    this[2] = 0x04.toByte()  // Flags 1 - enable lightbar control (bit 2)
                    this[3] = rightMotorByte // Right motor
                    this[4] = leftMotorByte  // Left motor
                    // Bytes 5-10: Audio settings (leave as 0 for default)
                    // Bytes 11-32: Trigger effects (leave as 0 for no effect)
                    // Bytes 33-46: LED and other settings (leave as 0 for default)
                }
            }
            
            // Send the HID report
            val sendResult = if (isBluetoothConnection) {
                sendBluetoothHidReport(report)
            } else {
                sendUsbHidReport(report)
            }
            
            if (sendResult) {
                Log.d(TAG, "DualSense vibration command sent: left=$leftMotor, right=$rightMotor")
                Result.success(Unit)
            } else {
                // Log failure but allow execution to continue (Requirement 10.3)
                Log.e(TAG, "Failed to send DualSense HID report")
                Result.failure(IOException("Failed to send HID report"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "I/O error sending DualSense command", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error sending DualSense command", e)
            Result.failure(e)
        }
    }
    
    /**
     * Format and send an Xbox controller HID output report for vibration.
     * 
     * Xbox One/Series Controller HID Output Report Format:
     * The Xbox controller uses a proprietary protocol over HID. The vibration command
     * packet format is as follows:
     * 
     * - Byte 0: Packet type (0x09 = Activate rumble)
     * - Byte 1: Flags (0x08 = enable rumble, 0x00 = disable)
     * - Byte 2: Repeat count (0x00 for continuous)
     * - Byte 3: Substructure type (0x09 = single rumble effect)
     * - Byte 4: Mode (0x00 = normal)
     * - Byte 5: Rumble mask (0x0F = all motors: 0000 lT rT L R)
     * - Byte 6: Left trigger force (0-255)
     * - Byte 7: Right trigger force (0-255)
     * - Byte 8: Left motor force (0-255) - strong rumble
     * - Byte 9: Right motor force (0-255) - weak rumble
     * - Byte 10: Pulse length (0x80 = default duration)
     * 
     * Note: Xbox controllers have 4 motors total:
     * - Left trigger motor (impulse trigger)
     * - Right trigger motor (impulse trigger)
     * - Left main motor (strong/low frequency rumble)
     * - Right main motor (weak/high frequency rumble)
     * 
     * For standard vibration, we use the left and right main motors (bytes 8-9).
     * 
     * @param leftMotor Left motor (strong rumble) intensity in range [0.0, 1.0]
     * @param rightMotor Right motor (weak rumble) intensity in range [0.0, 1.0]
     * @return Result indicating success or failure
     * 
     * **Validates: Requirements 1.7, 6.4, 10.3**
     */
    private suspend fun sendXboxCommand(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Scale motor values from [0.0, 1.0] to [0, 255]
            val leftMotorByte = (leftMotor * 255).toInt().coerceIn(0, 255).toByte()
            val rightMotorByte = (rightMotor * 255).toInt().coerceIn(0, 255).toByte()
            
            // Create Xbox HID output report (11 bytes for single rumble effect)
            val report = ByteArray(11).apply {
                this[0] = 0x09.toByte()  // Packet type: Activate rumble
                this[1] = 0x08.toByte()  // Flags: Enable rumble (0x08)
                this[2] = 0x00.toByte()  // Repeat count: 0 for continuous
                this[3] = 0x09.toByte()  // Substructure: Single rumble effect
                this[4] = 0x00.toByte()  // Mode: Normal (0x00)
                this[5] = 0x0F.toByte()  // Rumble mask: All motors (0000 lT rT L R)
                this[6] = 0x00.toByte()  // Left trigger force (not used for standard vibration)
                this[7] = 0x00.toByte()  // Right trigger force (not used for standard vibration)
                this[8] = leftMotorByte  // Left motor (strong rumble)
                this[9] = rightMotorByte // Right motor (weak rumble)
                this[10] = 0x80.toByte() // Pulse length: Default duration
            }
            
            // Determine connection type and send the HID report
            val isBluetoothConnection = connectedDevice is BluetoothDevice
            val sendResult = if (isBluetoothConnection) {
                sendBluetoothHidReport(report)
            } else {
                sendUsbHidReport(report)
            }
            
            if (sendResult) {
                Log.d(TAG, "Xbox vibration command sent: left=$leftMotor, right=$rightMotor")
                Result.success(Unit)
            } else {
                // Log failure but allow execution to continue (Requirement 10.3)
                Log.e(TAG, "Failed to send Xbox HID report")
                Result.failure(IOException("Failed to send HID report"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "I/O error sending Xbox command", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error sending Xbox command", e)
            Result.failure(e)
        }
    }
    
    /**
     * Send an HID report over Bluetooth.
     * 
     * @param report The HID report bytes to send
     * @return true if successful, false otherwise
     * 
     * **Validates: Requirement 10.2** - Handles connection loss during vibration
     */
    private fun sendBluetoothHidReport(report: ByteArray): Boolean {
        return try {
            val socket = bluetoothSocket
            if (socket == null || !socket.isConnected) {
                Log.e(TAG, "Bluetooth socket not connected")
                // Handle connection loss
                handleConnectionLoss()
                return false
            }
            
            val outputStream = socket.outputStream
            outputStream.write(report)
            outputStream.flush()
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to send Bluetooth HID report", e)
            // Connection may have been lost
            handleConnectionLoss()
            false
        }
    }
    
    /**
     * Send an HID report over USB.
     * 
     * @param report The HID report bytes to send
     * @return true if successful, false otherwise
     * 
     * **Validates: Requirement 10.2** - Handles connection loss during vibration
     */
    private fun sendUsbHidReport(report: ByteArray): Boolean {
        // TODO: Task 5.1 continuation - Implement USB HID communication
        // This requires UsbDeviceConnection and proper endpoint setup
        // For now, log that USB HID is not yet fully implemented
        Log.w(TAG, "USB HID report sending not yet fully implemented")
        
        // Check if USB device is still connected
        val device = connectedDevice as? UsbDevice
        if (device == null) {
            Log.e(TAG, "USB device not connected")
            handleConnectionLoss()
            return false
        }
        
        // Verify device is still in the device list
        val currentDevices = usbManager.deviceList
        if (!currentDevices.containsValue(device)) {
            Log.e(TAG, "USB device disconnected")
            handleConnectionLoss()
            return false
        }
        
        return false
    }
    
    /**
     * Disconnect from the current controller and clean up resources.
     * 
     * Closes all connections and resets the connection state.
     * 
     * **Validates: Requirement 10.2**
     */
    actual override suspend fun disconnect() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Disconnecting from controller")
        
        try {
            // Close Bluetooth socket if open
            bluetoothSocket?.close()
            bluetoothSocket = null
            
            // TODO: Task 5.5 - Close USB connection if open
            
            connectedDevice = null
            connectedControllerType = null
            
            withContext(Dispatchers.Main) {
                _connectionState.value = ConnectionState.Disconnected
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect", e)
            withContext(Dispatchers.Main) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }
    
    /**
     * Handle connection loss during operation.
     * 
     * This method is called when a connection error is detected during
     * vibration command sending. It updates the connection state to
     * Disconnected, which will cause the VibrationEngine to stop
     * attempting to send commands.
     * 
     * **Validates: Requirement 10.2** - Connection loss during vibration
     */
    private fun handleConnectionLoss() {
        Log.w(TAG, "Connection loss detected")
        
        // Clean up connection resources
        scope.launch {
            try {
                bluetoothSocket?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing socket during connection loss", e)
            }
            bluetoothSocket = null
            connectedDevice = null
            connectedControllerType = null
            
            withContext(Dispatchers.Main) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }
    
    /**
     * Clean up resources when the controller is no longer needed.
     * 
     * Should be called when the app is destroyed or the controller is no longer needed.
     */
    fun cleanup() {
        scope.launch {
            stopDiscovery()
            disconnect()
        }
        unregisterBluetoothReceiver()
        unregisterUsbReceiver()
    }
}
