# DualShock 4 HID Protocol Implementation

## Overview

This document describes the DualShock 4 HID protocol implementation for vibration control in the Gamepad Vibration Controller application.

## Implementation Details

### Location
`composeApp/src/androidMain/kotlin/com/seashore/zoonzoon/gamepad/platform/PlatformGamepadController.android.kt`

### Key Methods

#### `sendDualShock4Command(leftMotor: Float, rightMotor: Float): Result<Unit>`
Formats and sends DualShock 4 HID output reports for vibration control.

**Parameters:**
- `leftMotor`: Left motor (strong rumble) intensity in range [0.0, 1.0]
- `rightMotor`: Right motor (weak rumble) intensity in range [0.0, 1.0]

**Returns:** `Result<Unit>` indicating success or failure

### HID Output Report Format

#### Bluetooth Report (Report ID 0x11)
- **Total Size:** 78 bytes
- **Byte 0:** Report ID (0x11)
- **Byte 1:** Flags (0x80)
- **Byte 2:** Padding (0x00)
- **Byte 3:** Rumble enable flag (0xFF)
- **Byte 4:** Right motor (weak rumble) - 0-255
- **Byte 5:** Left motor (strong rumble) - 0-255
- **Bytes 6-10:** RGB LED color (red, green, blue, flash on, flash off)
- **Remaining bytes:** Padding/other features (0x00)

#### USB Report (Report ID 0x05)
- **Total Size:** 32 bytes
- **Byte 0:** Report ID (0x05)
- **Byte 1:** Rumble enable flag (0xFF)
- **Byte 4:** Right motor (weak rumble) - 0-255
- **Byte 5:** Left motor (strong rumble) - 0-255
- **Bytes 6-10:** RGB LED color (red, green, blue, flash on, flash off)
- **Remaining bytes:** Padding/other features (0x00)

### Motor Mapping

The DualShock 4 controller has two vibration motors:
- **Left Motor (Byte 5):** Strong rumble - provides heavy, low-frequency vibration
- **Right Motor (Byte 4):** Weak rumble - provides light, high-frequency vibration

### Value Scaling

Motor intensity values are scaled from the application's [0.0, 1.0] float range to the HID protocol's [0, 255] byte range:

```kotlin
val leftMotorByte = (leftMotor * 255).toInt().coerceIn(0, 255).toByte()
val rightMotorByte = (rightMotor * 255).toInt().coerceIn(0, 255).toByte()
```

**Examples:**
- 0.0 → 0 (no vibration)
- 0.5 → 127 (half intensity)
- 1.0 → 255 (full intensity)

### Connection Type Detection

The implementation automatically detects whether the controller is connected via Bluetooth or USB:

```kotlin
val isBluetoothConnection = connectedDevice is BluetoothDevice
```

This determines which HID report format to use (0x11 for Bluetooth, 0x05 for USB).

### Error Handling

The implementation includes comprehensive error handling:
- Validates motor values are in [0.0, 1.0] range
- Checks for connected device before sending commands
- Catches and logs IOException during HID report transmission
- Returns `Result<Unit>` to indicate success or failure

### LED Control

The current implementation sets the RGB LED to off (0x00 for all color channels) and disables LED flashing. This can be extended in future versions to provide visual feedback synchronized with vibration patterns.

## Validation

**Validates Requirements:**
- 1.5: Support PlayStation DualShock 4 controllers
- 6.4: Format vibration commands according to DualShock 4 HID output report specification

## Testing

### Manual Testing Steps

1. **Connect DualShock 4 Controller:**
   - Pair the controller via Bluetooth or connect via USB
   - Verify the app detects and connects to the controller

2. **Test Vibration Commands:**
   - Enable vibration in the app
   - Select different patterns (constant, pulse, wave)
   - Adjust intensity slider
   - Verify the controller vibrates with appropriate strength

3. **Test Motor Mapping:**
   - Set left motor to 1.0, right motor to 0.0
   - Verify strong rumble (left motor) activates
   - Set left motor to 0.0, right motor to 1.0
   - Verify weak rumble (right motor) activates

4. **Test Value Scaling:**
   - Set intensity to 0.0 - verify no vibration
   - Set intensity to 0.5 - verify medium vibration
   - Set intensity to 1.0 - verify full vibration

### Expected Behavior

- **Bluetooth Connection:** Controller should vibrate smoothly with no perceptible lag
- **USB Connection:** Controller should vibrate immediately with minimal latency
- **Motor Mapping:** Left motor should provide stronger, deeper vibration than right motor
- **Intensity Scaling:** Vibration strength should scale linearly with intensity value

## Known Limitations

1. **USB HID Communication:** The `sendUsbHidReport` method is not yet fully implemented. It requires proper UsbDeviceConnection and endpoint setup. Currently, USB connections will log a warning and return false.

2. **LED Control:** The implementation does not currently support RGB LED control or flashing. All LED bytes are set to 0x00 (off).

3. **Advanced Features:** The DualShock 4 supports additional features (gyroscope, touchpad, audio) that are not addressed in this vibration-only implementation.

## Future Enhancements

1. **Complete USB HID Implementation:** Implement full USB HID communication with proper endpoint setup
2. **LED Synchronization:** Add RGB LED control synchronized with vibration patterns
3. **Advanced Rumble Effects:** Implement more sophisticated rumble effects using both motors
4. **Battery Status:** Read and display controller battery level
5. **Connection Quality Monitoring:** Monitor Bluetooth connection quality and adjust accordingly

## References

- [DualShock 4 HID Protocol Documentation](https://www.psdevwiki.com/ps4/DS4-USB)
- [Android Bluetooth HID Profile](https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice)
- [Android USB Host API](https://developer.android.com/guide/topics/connectivity/usb/host)
