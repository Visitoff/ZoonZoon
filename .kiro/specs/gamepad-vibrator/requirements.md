# Requirements Document: Gamepad Vibration Controller

## Introduction

The Gamepad Vibration Controller is a Kotlin Multiplatform mobile application that enables users to control vibration and haptic feedback on PlayStation (DualShock 4, DualSense) and Xbox controllers. The system provides multiple vibration patterns, intensity controls, and a pink pastel-themed user interface. The application supports Android (full Bluetooth/USB HID access) and iOS (MFi-certified controllers only) platforms.

## Glossary

- **System**: The Gamepad Vibration Controller application
- **VibrationEngine**: The core component responsible for orchestrating vibration patterns and execution
- **PlatformGamepadController**: Platform-specific component that handles low-level gamepad communication
- **ViewModel**: The state management layer that coordinates business logic
- **UI_Layer**: The Compose Multiplatform user interface layer
- **PatternManager**: Component responsible for vibration pattern algorithms and generation
- **HID_Protocol**: Human Interface Device protocol used for gamepad communication
- **Controller**: A PlayStation or Xbox gamepad device
- **Motor**: The physical vibration motor in a gamepad (left or right)
- **Intensity**: The strength of vibration, represented as a value between 0.0 and 1.0
- **VibrationPattern**: A specific algorithm for generating vibration sequences (constant, pulse, wave, custom)
- **ConnectionState**: The current status of controller connectivity (disconnected, scanning, connected)
- **VibrationState**: The current status of vibration execution (enabled, disabled, active pattern)

## Requirements

### Requirement 1: Controller Discovery and Connection

**User Story:** As a user, I want the app to automatically discover and connect to my PlayStation or Xbox controller, so that I can control its vibration without manual configuration.

#### Acceptance Criteria

1. WHEN the app launches, THE System SHALL initialize the VibrationEngine and start controller discovery
2. WHEN a compatible controller is detected, THE PlatformGamepadController SHALL establish a connection and notify the VibrationEngine
3. WHEN a controller connection is established, THE System SHALL update the ConnectionState and display connected status in the UI_Layer
4. WHEN a controller disconnects, THE System SHALL update the ConnectionState and display disconnected status in the UI_Layer
5. THE System SHALL support PlayStation DualShock 4 controllers
6. THE System SHALL support PlayStation DualSense controllers
7. THE System SHALL support Xbox controllers
8. WHERE the platform is Android, THE PlatformGamepadController SHALL support Bluetooth and USB HID connections
9. WHERE the platform is iOS, THE PlatformGamepadController SHALL support MFi-certified controllers only

### Requirement 2: Vibration Control

**User Story:** As a user, I want to enable and disable vibration with a power button, so that I can quickly start and stop controller vibration.

#### Acceptance Criteria

1. WHEN the user enables vibration, THE ViewModel SHALL call EnableVibration on the VibrationEngine
2. WHEN the user disables vibration, THE ViewModel SHALL call DisableVibration on the VibrationEngine
3. WHEN vibration is disabled, THE VibrationEngine SHALL send a stop command with motor values of 0 to the PlatformGamepadController
4. WHEN vibration is enabled, THE System SHALL execute the currently selected VibrationPattern
5. THE System SHALL maintain VibrationState reflecting whether vibration is enabled or disabled

### Requirement 3: Vibration Pattern Selection

**User Story:** As a user, I want to select from multiple vibration patterns, so that I can experience different vibration effects on my controller.

#### Acceptance Criteria

1. WHEN the user selects a vibration pattern, THE ViewModel SHALL call SetPattern on the VibrationEngine with the selected pattern and current intensity
2. THE PatternManager SHALL support a constant vibration pattern
3. THE PatternManager SHALL support a pulse vibration pattern
4. THE PatternManager SHALL support a wave vibration pattern
5. THE PatternManager SHALL support custom vibration patterns
6. WHEN a pattern is selected, THE System SHALL update the VibrationState to reflect the active pattern

### Requirement 4: Intensity Control

**User Story:** As a user, I want to adjust the vibration intensity, so that I can control how strong the vibration feels.

#### Acceptance Criteria

1. WHEN the user adjusts intensity, THE ViewModel SHALL update the VibrationEngine with the new intensity value
2. THE System SHALL accept intensity values between 0.0 and 1.0 inclusive
3. WHEN intensity is changed during active vibration, THE VibrationEngine SHALL apply the new intensity to subsequent vibration commands
4. THE System SHALL persist the intensity setting across pattern changes

### Requirement 5: Vibration Pattern Execution

**User Story:** As a system operator, I want the vibration engine to execute patterns smoothly and reliably, so that users experience consistent vibration feedback.

#### Acceptance Criteria

1. WHEN a vibration pattern is active, THE VibrationEngine SHALL calculate the next frame values for left and right motors
2. WHEN frame values are calculated, THE VibrationEngine SHALL send vibration commands to the PlatformGamepadController
3. WHEN the PlatformGamepadController receives a vibration command, THE PlatformGamepadController SHALL send an HID output report to the Controller
4. THE VibrationEngine SHALL execute pattern frames continuously until vibration is disabled
5. THE System SHALL maintain smooth pattern execution without perceptible gaps or stuttering

### Requirement 6: Platform-Specific Communication

**User Story:** As a developer, I want platform-specific gamepad communication implementations, so that the app works correctly on both Android and iOS with their different capabilities.

#### Acceptance Criteria

1. THE PlatformGamepadController SHALL use the expect/actual pattern for platform-specific implementations
2. WHERE the platform is Android, THE AndroidGamepadController SHALL communicate via Bluetooth or USB HID protocols
3. WHERE the platform is iOS, THE iOSGamepadController SHALL communicate via the iOS GameController Framework
4. WHEN sending vibration commands, THE PlatformGamepadController SHALL format commands according to the connected controller's HID protocol specification
5. THE PlatformGamepadController SHALL handle controller-specific protocol differences transparently to the VibrationEngine

### Requirement 7: User Interface

**User Story:** As a user, I want a visually appealing and intuitive interface with a pink pastel theme, so that I can easily control vibration settings.

#### Acceptance Criteria

1. THE UI_Layer SHALL be built using Compose Multiplatform
2. THE UI_Layer SHALL use Material3 components
3. THE UI_Layer SHALL implement a pink pastel color theme
4. THE UI_Layer SHALL display the current ConnectionState (disconnected, scanning, connected)
5. THE UI_Layer SHALL display the current VibrationState (enabled/disabled, active pattern)
6. THE UI_Layer SHALL provide a power button control for enabling/disabling vibration
7. THE UI_Layer SHALL provide pattern selection controls
8. THE UI_Layer SHALL provide intensity adjustment controls
9. WHEN the ViewModel updates state, THE UI_Layer SHALL reactively update to reflect the new state

### Requirement 8: State Management

**User Story:** As a developer, I want reactive state management, so that the UI automatically reflects changes in controller connection and vibration status.

#### Acceptance Criteria

1. THE ViewModel SHALL manage ConnectionState as observable state
2. THE ViewModel SHALL manage VibrationState as observable state
3. WHEN the VibrationEngine reports a state change, THE ViewModel SHALL update the corresponding state object
4. WHEN state objects are updated, THE UI_Layer SHALL observe and react to the changes
5. THE System SHALL maintain state consistency between the ViewModel, VibrationEngine, and UI_Layer

### Requirement 9: Architecture Separation

**User Story:** As a developer, I want clear separation between UI, business logic, and platform-specific code, so that the system is maintainable and extensible.

#### Acceptance Criteria

1. THE System SHALL separate the UI_Layer from business logic in the ViewModel
2. THE System SHALL separate platform-agnostic vibration logic in the VibrationEngine from platform-specific communication in the PlatformGamepadController
3. THE System SHALL use the expect/actual pattern for platform-specific implementations
4. WHEN adding new controller types, THE System SHALL allow extension without modifying existing pattern or UI code
5. WHEN adding new vibration patterns, THE System SHALL allow extension through the PatternManager without modifying controller communication code

### Requirement 10: Error Handling

**User Story:** As a user, I want the app to handle errors gracefully, so that I understand what went wrong and can take corrective action.

#### Acceptance Criteria

1. WHEN controller discovery fails, THE System SHALL display an appropriate error message in the UI_Layer
2. WHEN a controller connection is lost during vibration, THE System SHALL update the ConnectionState and stop vibration attempts
3. WHEN a vibration command fails to send, THE System SHALL log the error and continue attempting subsequent commands
4. WHEN an unsupported controller is detected on iOS, THE System SHALL display a message indicating MFi-certified controllers are required
5. IF a platform-specific error occurs, THEN THE PlatformGamepadController SHALL translate it to a common error representation for the VibrationEngine
