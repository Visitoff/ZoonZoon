import Foundation
import GameController
import CoreHaptics

/// ObjC-compatible bridge for game controller haptics.
/// Called from Kotlin/Native via @ObjCName interop.
@objc public class GameControllerHaptics: NSObject {

    private var engineMap: [String: CHHapticEngine] = [:]

    /// Create and start a haptic engine for the given controller and locality.
    /// Returns true on success.
    @objc public func prepareEngine(for controller: GCController, locality: String) -> Bool {
        guard let engine = controller.haptics?.createEngine(withLocality: GCHapticsLocality(rawValue: locality)) else {
            print("[Haptics] Controller '\(controller.vendorName ?? "unknown")' does not support haptics for locality: \(locality)")
            return false
        }

        engine.stoppedHandler = { reason in
            print("[Haptics] Engine stopped: \(reason)")
        }
        engine.resetHandler = {
            print("[Haptics] Engine reset — restarting")
            try? engine.start()
        }

        do {
            try engine.start()
            engineMap[locality] = engine
            return true
        } catch {
            print("[Haptics] Failed to start engine: \(error)")
            return false
        }
    }

    /// Play a continuous rumble with the given intensity (0.0–1.0) for 100ms.
    @objc public func playRumble(intensity: Float, locality: String) -> Bool {
        guard let engine = engineMap[locality] else { return false }

        let intensityParam = CHHapticEventParameter(parameterID: .hapticIntensity, value: intensity)
        let sharpnessParam = CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.1)

        let event = CHHapticEvent(
            eventType: .hapticContinuous,
            parameters: [intensityParam, sharpnessParam],
            relativeTime: 0,
            duration: 0.1
        )

        do {
            let pattern = try CHHapticPattern(events: [event], parameters: [])
            let player = try engine.makePlayer(with: pattern)
            try player.start(atTime: 0)
            return true
        } catch {
            print("[Haptics] Failed to play rumble: \(error)")
            return false
        }
    }

    /// Stop all haptic engines.
    @objc public func stopAll() {
        engineMap.values.forEach { $0.stop(completionHandler: nil) }
        engineMap.removeAll()
    }
}
