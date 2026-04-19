# Gamepad Vibration Controller

This package contains the implementation of the Gamepad Vibration Controller feature.

## Package Structure

```
gamepad/
├── model/                          # Core data models (common)
│   ├── ConnectionState.kt         # Controller connection state
│   ├── VibrationPattern.kt        # Vibration pattern definitions
│   └── VibrationState.kt          # Vibration execution state
│
├── platform/                       # Platform-specific implementations
│   ├── PlatformGamepadController.kt           # expect interface (common)
│   ├── PlatformGamepadController.android.kt   # Android actual implementation
│   └── PlatformGamepadController.ios.kt       # iOS actual implementation
│
├── engine/                         # Business logic (to be implemented in Task 3)
│   ├── VibrationEngine.kt         # Core vibration orchestration
│   └── PatternManager.kt          # Pattern management
│
├── viewmodel/                      # State management (to be implemented in Task 8)
│   └── GamepadViewModel.kt        # ViewModel for UI state
│
└── ui/                            # UI components (to be implemented in Task 9-10)
    ├── theme/                     # Pink pastel theme
    ├── components/                # Reusable UI components
    └── screens/                   # Main screens
```

## Core Data Models

### ConnectionState
Sealed class representing controller connection status:
- `Disconnected`: No controller connected
- `Scanning`: Actively searching for controllers
- `Connected(controllerType)`: Controller connected with type information

### VibrationPattern
Sealed class defining vibration patterns:
- `Constant`: Continuous vibration at constant intensity
- `Pulse(frequencyHz)`: On/off pulsing at specified frequency
- `Wave(frequencyHz)`: Smooth sine wave modulation
- `Custom(frames, loop)`: User-defined frame sequences

Each pattern implements `calculateFrame(time, intensity)` to generate motor values.

### VibrationState
Data class tracking vibration execution state:
- `enabled`: Whether vibration is active
- `activePattern`: Currently selected pattern
- `intensity`: Intensity level [0.0, 1.0]

## Platform-Specific Controller

### PlatformGamepadController (expect/actual)
Interface for platform-specific gamepad communication:
- `connectionState`: Observable StateFlow of connection status
- `startDiscovery()`: Begin controller scanning
- `stopDiscovery()`: Stop scanning
- `sendVibrationCommand(left, right)`: Send vibration to controller
- `disconnect()`: Clean up and disconnect

**Android Implementation**: Uses Bluetooth/USB HID protocols
**iOS Implementation**: Uses GameController Framework (MFi only)

## Implementation Status

✅ Task 1: Project structure and core interfaces (COMPLETE)
- Directory structure created
- Core data models implemented
- PlatformGamepadController expect/actual defined
- Stub implementations for Android and iOS

⏳ Task 2: Vibration pattern algorithms (PENDING)
⏳ Task 3: VibrationEngine core logic (PENDING)
⏳ Task 5: Android platform implementation (PENDING)
⏳ Task 6: iOS platform implementation (PENDING)
⏳ Task 8: ViewModel and state management (PENDING)
⏳ Task 9-10: UI implementation (PENDING)

## Requirements Validated

This implementation addresses:
- **Requirement 9.1**: Separation of UI from business logic
- **Requirement 9.2**: Platform-agnostic vibration logic separated from platform-specific communication
- **Requirement 9.3**: expect/actual pattern for platform-specific implementations
