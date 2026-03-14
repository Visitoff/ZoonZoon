# Project Structure Cleanup Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Reorganize the iOS project into a cleaner, modern UIKit structure without changing runtime behavior.

**Architecture:** Keep the app on the same UIKit/storyboard flow, but move code and resources into clearer domains: `App`, `Core`, `Features`, `Shared`, and `Resources`. Split `AHAPCatalog` out of `HapticsManager`, keep storyboard wiring intact through `project.pbxproj`, and clean repository hygiene by removing tracked editor junk.

**Tech Stack:** Swift, UIKit, CoreHaptics, GameController, Xcode project file editing

---

### Task 1: Save the current cleanup scope in-repo

**Files:**
- Create: `/Users/q/projects/swift/zz/docs/plans/2026-03-14-structure-cleanup.md`

**Step 1: Write the implementation plan**

Save the cleanup scope, target layout, and verification strategy in this file.

**Step 2: Keep the refactor behavior-neutral**

Limit the refactor to file moves, naming cleanup, Xcode wiring updates, and tracked-junk cleanup.

### Task 2: Reorganize source files into stable domains

**Files:**
- Create: `/Users/q/projects/swift/zz/HapticControllers/Core/Haptics/AHAPCatalog.swift`
- Move: `/Users/q/projects/swift/zz/HapticControllers/HapticsManager.swift`
- Move: `/Users/q/projects/swift/zz/HapticControllers/User Interface/MainViewController.swift`
- Move: `/Users/q/projects/swift/zz/HapticControllers/Extensions/UIViewExtensions.swift`
- Move: `/Users/q/projects/swift/zz/HapticControllers/Info.plist`

**Step 1: Move runtime code into domain folders**

Use this target layout:
- `/Users/q/projects/swift/zz/HapticControllers/App`
- `/Users/q/projects/swift/zz/HapticControllers/Core/Haptics`
- `/Users/q/projects/swift/zz/HapticControllers/Features/Main`
- `/Users/q/projects/swift/zz/HapticControllers/Shared/Extensions`

**Step 2: Split `AHAPCatalog` out of `HapticsManager`**

Create a dedicated source file so `HapticsManager.swift` stays focused on engine lifecycle/state and `AHAPCatalog.swift` owns pattern metadata.

### Task 3: Reorganize resources without changing app behavior

**Files:**
- Move: `/Users/q/projects/swift/zz/HapticControllers/AHAP`
- Move: `/Users/q/projects/swift/zz/HapticControllers/User Interface/Assets.xcassets`
- Move: `/Users/q/projects/swift/zz/HapticControllers/User Interface/Base.lproj/Main.storyboard`
- Move: `/Users/q/projects/swift/zz/HapticControllers/User Interface/Base.lproj/LaunchScreen.storyboard`
- Move/Rename: `/Users/q/projects/swift/zz/images/Group 3nigg.png`

**Step 1: Create a `Resources` layout**

Use this target layout:
- `/Users/q/projects/swift/zz/HapticControllers/Resources/AHAP`
- `/Users/q/projects/swift/zz/HapticControllers/Resources/Assets.xcassets`
- `/Users/q/projects/swift/zz/HapticControllers/Resources/Base.lproj`
- `/Users/q/projects/swift/zz/HapticControllers/Resources/Images`

**Step 2: Clean up unsafe resource naming**

Rename the main button image to a neutral descriptive name and update project/storyboard references.

### Task 4: Update Xcode project structure

**Files:**
- Modify: `/Users/q/projects/swift/zz/HapticControllers.xcodeproj/project.pbxproj`

**Step 1: Align PBX groups with the new disk layout**

Reflect `App`, `Core/Haptics`, `Features/Main`, `Shared/Extensions`, and `Resources` in project groups.

**Step 2: Update source/resource file references**

Keep the same target membership and storyboard names while pointing every reference to the new paths.

**Step 3: Update build settings**

Point `INFOPLIST_FILE` to the new `App/Info.plist` location.

### Task 5: Clean repository hygiene

**Files:**
- Modify: `/Users/q/projects/swift/zz/.gitignore`
- Remove from git index: `/Users/q/projects/swift/zz/.idea/*`

**Step 1: Ignore editor junk**

Add `.idea/` to `.gitignore`.

**Step 2: Stop tracking local IDE files**

Remove currently tracked `.idea` files from git without deleting local copies.

### Task 6: Verify the refactor

**Files:**
- Verify: `/Users/q/projects/swift/zz/HapticControllers.xcodeproj/project.pbxproj`
- Verify: `/Users/q/projects/swift/zz/HapticControllers/Core/Haptics/HapticsManager.swift`
- Verify: `/Users/q/projects/swift/zz/HapticControllers/Core/Haptics/AHAPCatalog.swift`
- Verify: `/Users/q/projects/swift/zz/HapticControllers/Features/Main/MainViewController.swift`

**Step 1: Run a fresh typecheck**

Run:

```bash
SWIFT_MODULE_CACHE_PATH=/Users/q/projects/swift/zz/.swift-cache CLANG_MODULE_CACHE_PATH=/Users/q/projects/swift/zz/.clang-cache xcrun --sdk iphoneos swiftc -typecheck -target arm64-apple-ios15.0 -sdk /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS26.2.sdk -parse-as-library /Users/q/projects/swift/zz/HapticControllers/App/AppDelegate.swift /Users/q/projects/swift/zz/HapticControllers/App/SceneDelegate.swift /Users/q/projects/swift/zz/HapticControllers/Shared/Extensions/UIViewExtensions.swift /Users/q/projects/swift/zz/HapticControllers/Core/Haptics/AHAPCatalog.swift /Users/q/projects/swift/zz/HapticControllers/Core/Haptics/HapticsManager.swift /Users/q/projects/swift/zz/HapticControllers/Features/Main/MainViewController.swift
```

Expected: exit code `0`

**Step 2: Verify git status**

Run:

```bash
git status --short
```

Expected: only intentional structural cleanup changes
