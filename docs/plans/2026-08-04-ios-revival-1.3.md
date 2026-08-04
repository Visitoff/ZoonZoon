# ZOONZOON iOS Revival 1.3 Implementation Plan

**Goal:** Re-release ZOONZOON with the existing controller vibration behavior, a modern single-screen design, clear pairing instructions, visible connection state, and guaranteed vibration cleanup.

**Architecture:** Keep the existing native UIKit application and Bundle ID. Replace the legacy pattern-grid storyboard UI with a programmatic single-screen controller, and simplify `HapticsManager` to one continuous `CHHapticAdvancedPatternPlayer` using the controller-specific infinite haptic duration. Keep all existing App Icon files unchanged.

**Tech Stack:** Swift 5, UIKit, GameController, Core Haptics, Xcode 26

---

### Task 1: Establish the release branch and baseline

**Files:**
- Create: `docs/plans/2026-08-04-ios-revival-1.3.md`

**Step 1:** Create `feature/ios-revival-1.3` from `origin/main` without modifying `main` or `kotlin`.

**Step 2:** Run the simulator build before implementation.

Run:

```bash
xcodebuild -project HapticControllers.xcodeproj -scheme HapticControllers -sdk iphonesimulator -configuration Debug build
```

Expected: `** BUILD SUCCEEDED **`.

### Task 2: Make vibration lifecycle deterministic

**Files:**
- Modify: `HapticControllers/Core/Haptics/HapticsManager.swift`

**Step 1:** Replace scheduled overlapping `playPattern` calls with one `CHHapticAdvancedPatternPlayer` containing a continuous `GCHapticDurationInfinite` event.

**Step 2:** Make Stop idempotent and immediate by cancelling the player and stopping the engine.

**Step 3:** Discover a controller already connected at launch and observe connect/disconnect notifications.

**Step 4:** Stop and clear the engine on disconnect, engine failure, app background, and shutdown.

**Step 5:** Replace crash paths with safe unsupported/error handling.

### Task 3: Replace the legacy UI with the approved single-screen flow

**Files:**
- Modify: `HapticControllers/Features/Main/MainViewController.swift`
- Modify: `HapticControllers/Resources/Base.lproj/Main.storyboard`

**Step 1:** Reduce the storyboard to an empty `MainViewController` scene and build the screen in UIKit.

**Step 2:** Add the ZOONZOON title, connection status card, large Start/Stop button, and a concise foreground-only notice shown only while vibration is active.

**Step 3:** When Start is tapped without a controller, present pairing instructions instead of changing playback state.

**Step 4:** Add a pairing sheet with PS, Xbox, and Bluetooth steps plus a Close action.

**Step 5:** Reflect connected, disconnected, playing, and stopped states with text, color, and animation.

**Step 6:** Observe app lifecycle notifications and stop vibration before the app becomes inactive or terminates.

**Step 7:** Prevent automatic screen locking only while vibration is active, and always restore the system idle timer on Stop, failure, disconnect, background, or shutdown.

### Task 4: Preserve release identity and icon

**Files:**
- Verify only: `HapticControllers/Resources/Assets.xcassets/AppIcon.appiconset/*`
- Verify: `HapticControllers/App/Info.plist`

**Step 1:** Confirm the App Icon asset diff is empty.

**Step 2:** Keep `com.ZoonZoon`, display name `ZOONZOON`, portrait phone layout, and existing controller declarations.

### Task 5: Verify the release candidate

**Files:**
- Verify: `HapticControllers/Core/Haptics/HapticsManager.swift`
- Verify: `HapticControllers/Features/Main/MainViewController.swift`
- Verify: `HapticControllers/Resources/Base.lproj/Main.storyboard`

**Step 1:** Run SwiftFormat lint and SwiftLint.

**Step 2:** Build for the iOS simulator with a clean derived-data directory.

**Step 3:** Launch in a simulator and verify the disconnected state and pairing sheet visually.

**Step 4:** On physical hardware, verify Start, Stop, background cleanup, disconnect, and reconnect with supported controllers before TestFlight.

**Step 5:** Confirm no App Icon files changed and no pattern, intensity, diagnostics, phone vibration, or extra tabs were added.
