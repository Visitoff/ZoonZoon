# Implementation Plan: Gamepad Vibration Controller

## Overview

This implementation plan breaks down the Gamepad Vibration Controller feature into discrete coding tasks. The feature is a Kotlin Multiplatform application with Compose Multiplatform UI, supporting PlayStation and Xbox controller vibration control. The implementation follows a layered architecture: UI Layer → ViewModel → VibrationEngine → PlatformGamepadController (expect/actual pattern for Android/iOS).

## Tasks

- [x] 1. Set up project structure and core interfaces
  - Create directory structure for common, Android, and iOS source sets
  - Define core data models: `ConnectionState`, `VibrationState`, `VibrationPattern` sealed classes
  - Define `PlatformGamepadController` expect interface in commonMain
  - Set up dependency injection structure (if using Koin or similar)
  - _Requirements: 9.1, 9.2, 9.3_

- [x] 2. Implement vibration pattern algorithms
  - [x] 2.1 Create PatternManager and pattern interfaces
    - Define `VibrationPattern` sealed interface with `calculateFrame(time: Long, intensity: Float): Pair<Float, Float>` method
    - Implement `PatternManager` class to manage pattern instances
    - _Requirements: 3.2, 3.3, 3.4, 3.5_
  
  - [x] 2.2 Implement ConstantPattern
    - Create `ConstantPattern` class that returns constant motor values
    - _Requirements: 3.2_
  
  - [x] 2.3 Implement PulsePattern
    - Create `PulsePattern` class with configurable pulse frequency
    - Use sine wave or square wave for on/off pulsing
    - _Requirements: 3.3_
  
  - [x] 2.4 Implement WavePattern
    - Create `WavePattern` class with smooth sine wave modulation
    - _Requirements: 3.4_
  
  - [x] 2.5 Implement CustomPattern support
    - Create `CustomPattern` class that accepts user-defined frame sequences
    - _Requirements: 3.5_
  
  - [x] 2.6 Write property test for pattern frame calculation
    - **Property 11: Pattern Frame Calculation and Command Sending**
    - **Validates: Requirements 5.1, 5.2**
    - Test that all pattern types generate valid motor values (0.0 to 1.0 range)
  
  - [x] 2.7 Write unit tests for pattern algorithms
    - Test each pattern type with various intensity values
    - Test edge cases (intensity 0.0, 1.0, mid-range)
    - _Requirements: 3.2, 3.3, 3.4, 3.5_

- [x] 3. Implement VibrationEngine core logic
  - [x] 3.1 Create VibrationEngine class
    - Implement state management for current pattern, intensity, enabled status
    - Create methods: `enableVibration()`, `disableVibration()`, `setPattern()`, `setIntensity()`
    - Implement frame calculation loop using coroutines
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.1, 4.1, 4.3, 5.1, 5.2, 5.4_
  
  - [x] 3.2 Implement pattern execution loop
    - Create coroutine-based loop that calculates frames at regular intervals (e.g., 16ms for ~60fps)
    - Apply intensity scaling to calculated motor values
    - Send vibration commands to PlatformGamepadController
    - _Requirements: 5.1, 5.2, 5.4, 5.5_
  
  - [x] 3.3 Implement intensity management
    - Store current intensity value (0.0 to 1.0)
    - Apply intensity scaling to all motor values before sending commands
    - Ensure intensity persists across pattern changes
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [x] 3.4 Write property test for vibration enable/disable
    - **Property 2: Vibration Enable/Disable Commands**
    - **Validates: Requirements 2.1, 2.2**
    - Test that enable/disable calls trigger correct VibrationEngine methods
  
  - [x] 3.5 Write property test for vibration stop command
    - **Property 3: Vibration Stop Command**
    - **Validates: Requirement 2.3**
    - Test that disabling vibration sends motor values of 0
  
  - [x] 3.6 Write property test for pattern execution on enable
    - **Property 4: Pattern Execution on Enable**
    - **Validates: Requirement 2.4**
    - Test that enabling vibration executes the currently selected pattern
  
  - [x] 3.7 Write property test for intensity application
    - **Property 9: Runtime Intensity Application**
    - **Validates: Requirement 4.3**
    - Test that intensity changes apply to subsequent vibration commands
  
  - [x] 3.8 Write property test for intensity persistence
    - **Property 10: Intensity Persistence Across Pattern Changes**
    - **Validates: Requirement 4.4**
    - Test that intensity remains unchanged after pattern changes
  
  - [x] 3.9 Write property test for continuous pattern execution
    - **Property 12: Continuous Pattern Execution**
    - **Validates: Requirement 5.4**
    - Test that patterns execute continuously until explicitly disabled

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement Android platform-specific gamepad controller
  - [x] 5.1 Create AndroidGamepadController actual implementation
    - Implement `PlatformGamepadController` interface for Android
    - Set up Bluetooth and USB HID connection management
    - Implement controller discovery using Android APIs
    - _Requirements: 1.1, 1.2, 6.1, 6.2, 1.8_
  
  - [x] 5.2 Implement DualShock 4 HID protocol
    - Format vibration commands according to DualShock 4 HID output report specification
    - Handle left motor (strong rumble) and right motor (weak rumble) mapping
    - _Requirements: 1.5, 6.4_
  
  - [x] 5.3 Implement DualSense HID protocol
    - Format vibration commands according to DualSense HID output report specification
    - Handle haptic feedback motor mapping
    - _Requirements: 1.6, 6.4_
  
  - [x] 5.4 Implement Xbox controller HID protocol
    - Format vibration commands according to Xbox controller HID output report specification
    - Handle left and right motor mapping
    - _Requirements: 1.7, 6.4_
  
  - [x] 5.5 Implement connection state management
    - Emit connection/disconnection events to VibrationEngine
    - Handle connection loss during vibration
    - _Requirements: 1.2, 1.3, 1.4, 10.2_
  
  - [x] 5.6 Implement error handling and logging
    - Translate platform-specific errors to common error representation
    - Log command failures and continue execution
    - _Requirements: 10.3, 10.5_
  
  - [x] 5.7 Write property test for HID protocol formatting
    - **Property 13: HID Protocol Formatting**
    - **Validates: Requirement 6.4**
    - Test that commands are formatted correctly for each controller type
  
  - [x] 5.8 Write property test for connection loss recovery
    - **Property 18: Connection Loss Recovery**
    - **Validates: Requirement 10.2**
    - Test that connection loss stops vibration and updates state
  
  - [x] 5.9 Write unit tests for AndroidGamepadController
    - Test controller discovery and connection
    - Test HID command formatting for each controller type
    - Test error handling scenarios
    - _Requirements: 1.8, 6.2, 6.4_

- [ ] 6. Implement iOS platform-specific gamepad controller
  - [x] 6.1 Create iOSGamepadController actual implementation
    - Implement `PlatformGamepadController` interface for iOS
    - Set up GameController Framework integration
    - Implement MFi controller discovery
    - _Requirements: 1.1, 1.2, 6.1, 6.3, 1.9_
  
  - [ ] 6.2 Implement MFi controller vibration commands
    - Use GameController Framework's haptic engine APIs
    - Map vibration patterns to iOS haptic feedback
    - _Requirements: 6.3, 6.4_
  
  - [ ] 6.3 Implement connection state management
    - Emit connection/disconnection events to VibrationEngine
    - Handle MFi controller connection lifecycle
    - _Requirements: 1.2, 1.3, 1.4_
  
  - [ ] 6.4 Implement MFi-only restriction handling
    - Display error message for non-MFi controllers
    - _Requirements: 10.4_
  
  - [ ] 6.5 Implement error handling and logging
    - Translate iOS-specific errors to common error representation
    - Log command failures and continue execution
    - _Requirements: 10.3, 10.5_
  
  - [ ] 6.6 Write property test for protocol abstraction
    - **Property 14: Protocol Abstraction**
    - **Validates: Requirement 6.5**
    - Test that VibrationEngine uses same interface regardless of platform
  
  - [ ] 6.7 Write unit tests for iOSGamepadController
    - Test MFi controller discovery and connection
    - Test haptic command mapping
    - Test error handling scenarios
    - _Requirements: 1.9, 6.3, 6.4_

- [x] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Implement ViewModel and state management
  - [x] 8.1 Create ConnectionState and VibrationState data classes
    - Define `ConnectionState` sealed class: Disconnected, Scanning, Connected(controllerType)
    - Define `VibrationState` data class: enabled, activePattern, intensity
    - _Requirements: 8.1, 8.2, 1.3, 1.4, 2.5_
  
  - [x] 8.2 Create GamepadViewModel
    - Implement state holders using StateFlow for ConnectionState and VibrationState
    - Create methods: `setVibrationEnabled()`, `setPattern()`, `setIntensity()`
    - Wire ViewModel to VibrationEngine
    - _Requirements: 2.1, 2.2, 3.1, 4.1, 8.1, 8.2, 8.3_
  
  - [x] 8.3 Implement state update handlers
    - Listen to VibrationEngine state changes
    - Update StateFlow objects when engine reports changes
    - _Requirements: 8.3, 8.4, 8.5_
  
  - [x] 8.4 Write property test for controller connection state updates
    - **Property 1: Controller Connection State Updates**
    - **Validates: Requirements 1.2, 1.3, 1.4**
    - Test that connection events update ConnectionState and UI displays status
  
  - [x] 8.5 Write property test for pattern selection command
    - **Property 5: Pattern Selection Command**
    - **Validates: Requirement 3.1**
    - Test that pattern selection calls SetPattern on VibrationEngine
  
  - [x] 8.6 Write property test for pattern selection state update
    - **Property 7: Pattern Selection State Update**
    - **Validates: Requirement 3.6**
    - Test that pattern selection updates VibrationState
  
  - [x] 8.7 Write property test for intensity update command
    - **Property 8: Intensity Update Command**
    - **Validates: Requirement 4.1**
    - Test that intensity adjustment updates VibrationEngine
  
  - [x] 8.8 Write property test for state consistency
    - **Property 17: State Consistency Across Layers**
    - **Validates: Requirements 8.3, 8.4, 8.5**
    - Test that state remains consistent across ViewModel, VibrationEngine, and UI
  
  - [x] 8.9 Write unit tests for GamepadViewModel
    - Test state initialization
    - Test all user action methods
    - Test state update propagation
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 9. Implement pink pastel theme and Material3 setup
  - [x] 9.1 Create pink pastel color scheme
    - Define Material3 ColorScheme with pink pastel colors
    - Set primary, secondary, tertiary colors in pink/pastel range
    - Define light and dark theme variants
    - _Requirements: 7.3_
  
  - [x] 9.2 Create theme composable
    - Implement `GamepadVibratorTheme` composable
    - Apply Material3 theme with custom color scheme
    - _Requirements: 7.1, 7.2, 7.3_
  
  - [x] 9.3 Write unit tests for theme colors
    - Test that color values match pink pastel specifications
    - _Requirements: 7.3_

- [x] 10. Implement UI components
  - [x] 10.1 Create ConnectionStatusCard composable
    - Display current ConnectionState (disconnected, scanning, connected)
    - Show controller type when connected
    - _Requirements: 7.4_
  
  - [x] 10.2 Create VibrationControlCard composable
    - Implement power button for enable/disable vibration
    - Display current VibrationState (enabled/disabled)
    - _Requirements: 7.5, 7.6_
  
  - [x] 10.3 Create PatternSelectionCard composable
    - Display pattern selection buttons (Constant, Pulse, Wave, Custom)
    - Highlight currently active pattern
    - _Requirements: 7.7_
  
  - [x] 10.4 Create IntensitySlider composable
    - Implement slider for intensity adjustment (0.0 to 1.0)
    - Display current intensity value
    - _Requirements: 7.8_
  
  - [x] 10.5 Create main screen composable
    - Compose all UI cards into main screen layout
    - Wire UI actions to ViewModel methods
    - Observe ViewModel state and update UI reactively
    - _Requirements: 7.1, 7.2, 7.9_
  
  - [x] 10.6 Write property test for UI state display
    - **Property 15: UI State Display**
    - **Validates: Requirements 7.4, 7.5**
    - Test that UI displays correct status for all state values
  
  - [x] 10.7 Write property test for reactive UI updates
    - **Property 16: Reactive UI Updates**
    - **Validates: Requirement 7.9**
    - Test that UI updates when ViewModel state changes
  
  - [x] 10.8 Write UI tests for composables
    - Test ConnectionStatusCard displays correct states
    - Test VibrationControlCard button interactions
    - Test PatternSelectionCard selection behavior
    - Test IntensitySlider value changes
    - _Requirements: 7.4, 7.5, 7.6, 7.7, 7.8, 7.9_

- [x] 11. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 12. Implement error handling UI
  - [x] 12.1 Create error display composable
    - Implement error message display component
    - Show controller discovery errors
    - Show MFi-only restriction message on iOS
    - _Requirements: 10.1, 10.4_
  
  - [x] 12.2 Wire error handling to ViewModel
    - Add error state to ViewModel
    - Update UI to display errors from VibrationEngine and PlatformGamepadController
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [x] 12.3 Write property test for command failure resilience
    - **Property 19: Command Failure Resilience**
    - **Validates: Requirement 10.3**
    - Test that command failures are logged and execution continues
  
  - [x] 12.4 Write property test for platform error translation
    - **Property 20: Platform Error Translation**
    - **Validates: Requirement 10.5**
    - Test that platform errors are translated to common representation
  
  - [x] 12.5 Write unit tests for error handling
    - Test error display for various error types
    - Test error state propagation from engine to UI
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 13. Integration and wiring
  - [x] 13.1 Wire VibrationEngine to PlatformGamepadController
    - Initialize VibrationEngine with platform-specific controller instance
    - Set up connection event listeners
    - _Requirements: 1.1, 1.2, 9.2_
  
  - [x] 13.2 Wire ViewModel to VibrationEngine
    - Initialize ViewModel with VibrationEngine instance
    - Set up state update listeners
    - _Requirements: 8.3, 8.4, 8.5, 9.1_
  
  - [x] 13.3 Wire UI to ViewModel
    - Pass ViewModel to main screen composable
    - Connect all UI actions to ViewModel methods
    - Set up state observation
    - _Requirements: 7.9, 9.1_
  
  - [x] 13.4 Initialize app entry point
    - Set up dependency injection (if used)
    - Initialize platform-specific controller in MainActivity (Android) and iOS app delegate
    - Launch main screen with theme
    - _Requirements: 1.1, 7.1, 7.2, 9.1, 9.2, 9.3_
  
  - [x] 13.5 Write integration tests
    - Test end-to-end flow: controller connection → pattern selection → vibration execution
    - Test state propagation across all layers
    - Test error handling across layers
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 4.1, 5.1, 5.2, 5.4, 7.9, 8.3, 8.4, 8.5_

- [x] 14. Implement custom pattern support
  - [x] 14.1 Create custom pattern editor UI
    - Implement UI for defining custom vibration sequences
    - Allow users to specify frame-by-frame motor values
    - _Requirements: 3.5_
  
  - [x] 14.2 Wire custom pattern editor to PatternManager
    - Save custom patterns to PatternManager
    - Load and execute custom patterns
    - _Requirements: 3.5_
  
  - [x] 14.3 Write property test for custom pattern support
    - **Property 6: Custom Pattern Support**
    - **Validates: Requirement 3.5**
    - Test that PatternManager supports and executes custom patterns
  
  - [x] 14.4 Write unit tests for custom pattern editor
    - Test pattern creation and validation
    - Test pattern saving and loading
    - _Requirements: 3.5_

- [x] 15. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at reasonable breaks
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The implementation uses Kotlin Multiplatform with Compose Multiplatform UI
- Platform-specific code uses expect/actual pattern for Android and iOS
- Android supports full Bluetooth/USB HID access; iOS supports MFi-certified controllers only
