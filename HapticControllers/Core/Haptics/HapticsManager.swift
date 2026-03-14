import Foundation
import GameController
import CoreHaptics

enum HapticsPlaybackState: Equatable {
    case idle
    case starting
    case playing
    case stopping
    case stopped
}

protocol HapticsManagerDelegate: AnyObject {
    func didConnect(controller: GCController)
    func didDisconnectController()
    func didUpdatePlaybackState(_ state: HapticsPlaybackState)
}

class HapticsManager {
    private enum LoopingConstants {
        static let overlapFactor = 0.83
        static let fallbackRepeatDelay: TimeInterval = 2.5
        static let minimumRepeatDelay: TimeInterval = 0.25
        static let reasonableEventTimeUpperBound: TimeInterval = 5.0
        static let stopSettleBuffer: TimeInterval = 0.08
        static let stoppedIndicatorDuration: TimeInterval = 1.0
    }

    private struct PatternTiming {
        let duration: TimeInterval
        let repeatDelay: TimeInterval
    }

    private var isSetup = false
    private var controller: GCController?
    private var engineMap = [GCHapticsLocality: CHHapticEngine]()

    private let loopingStateQueue = DispatchQueue(label: "HapticsManager.looping-state")
    private var loopingReplayWorkItem: DispatchWorkItem?
    private var loopingPatternIdentifier: String?
    private var loopingLocality: GCHapticsLocality?
    private var activeIterationEndUptime: TimeInterval?
    private var stoppedTransitionWorkItem: DispatchWorkItem?
    private var idleTransitionWorkItem: DispatchWorkItem?

    private(set) var playbackState: HapticsPlaybackState = .idle {
        didSet {
            guard oldValue != playbackState else { return }
            if Thread.isMainThread {
                delegate?.didUpdatePlaybackState(playbackState)
            } else {
                DispatchQueue.main.async { [weak self] in
                    guard let self else { return }
                    self.delegate?.didUpdatePlaybackState(self.playbackState)
                }
            }
        }
    }

    weak var delegate: HapticsManagerDelegate? {
        didSet {
            if delegate != nil {
                startObserving()
            }
        }
    }

    private func startObserving() {
        guard !isSetup else { return }

        let nc = NotificationCenter.default

        NotificationCenter.default.addObserver(self,
                                               selector: #selector(controllerDidConnect),
                                               name: .GCControllerDidConnect,
                                               object: nil)

        nc.addObserver(self,
                       selector: #selector(controllerDidDisconnect),
                       name: .GCControllerDidDisconnect,
                       object: nil)
        isSetup = true
    }

    @objc private func controllerDidConnect(notification: Notification) {
        guard let controller = notification.object as? GCController else {
            fatalError("Invalid notification object.")
        }

        print("Connected \(controller.productCategory) game controller.")

        self.controller = controller
        if let engine = createEngine(for: controller, locality: .default) {
            engineMap[GCHapticsLocality.default] = engine
        }

        delegate?.didConnect(controller: controller)
    }

    private func createEngine(for controller: GCController, locality: GCHapticsLocality) -> CHHapticEngine? {
        guard let engine = controller.haptics?.createEngine(withLocality: locality) else {
            print("Failed to create engine.")
            return nil
        }

        engine.isAutoShutdownEnabled = false

        engine.stoppedHandler = { [weak self] reason in
            print("The engine stopped because \(reason.message)")
            guard let self else { return }
            switch reason {
            case .gameControllerDisconnect, .systemError, .engineDestroyed:
                self.clearLoopingState()
                self.engineMap.removeValue(forKey: locality)
                self.playbackState = .idle
            default:
                break
            }
        }

        engine.resetHandler = {
            print("The engine reset --> Restarting now!")
            do {
                try engine.start()
            } catch {
                print("Failed to restart the engine: \(error)")
            }
        }
        return engine
    }

    @objc private func controllerDidDisconnect(notification: Notification) {
        guard controller == notification.object as? GCController else { return }

        clearLoopingState()
        cancelPlaybackTransitionWorkItems()
        engineMap.removeAll(keepingCapacity: true)
        controller = nil
        playbackState = .idle
        delegate?.didDisconnectController()
    }

    func playHapticsFile(named filename: String, locality: GCHapticsLocality = .default) {
        guard let engine = engine(for: locality) else {
            print("Unable to play haptics: no engine available for locality \(locality)")
            return
        }

        guard let url = Bundle.main.url(forResource: filename, withExtension: "ahap") else {
            print("Unable to find haptics file named '\(filename)'.")
            return
        }

        do {
            try engine.start()
            try engine.playPattern(from: url)
        } catch {
            print("An error occured playing \(filename): \(error).")
        }
    }

    func startLoopingHapticsFile(named filename: String, locality: GCHapticsLocality = .default) {
        guard playbackState == .idle else { return }

        guard let engine = engine(for: locality) else {
            print("Unable to play haptics: no engine available for locality \(locality)")
            return
        }

        guard let url = Bundle.main.url(forResource: filename, withExtension: "ahap") else {
            print("Unable to find haptics file named '\(filename)'.")
            return
        }

        clearLoopingState()
        cancelPlaybackTransitionWorkItems()

        let identifier = playbackIdentifier(for: filename, locality: locality)
        let resolvedPatternTiming: PatternTiming
        if let timingOverride = AHAPCatalog.timing(for: filename) {
            resolvedPatternTiming = PatternTiming(duration: timingOverride.duration,
                                                  repeatDelay: timingOverride.repeatDelay)
        } else {
            resolvedPatternTiming = patternTiming(for: url)
        }

        loopingStateQueue.sync {
            loopingPatternIdentifier = identifier
            loopingLocality = locality
            activeIterationEndUptime = nil
        }

        playbackState = .starting

        do {
            try engine.start()
            playLoopIteration(filename: filename,
                              url: url,
                              engine: engine,
                              identifier: identifier,
                              locality: locality,
                              patternDuration: resolvedPatternTiming.duration,
                              repeatDelay: resolvedPatternTiming.repeatDelay)
        } catch {
            clearLoopingState()
            engineMap.removeValue(forKey: locality)
            playbackState = .idle
            print("An error occured playing \(filename): \(error).")
        }
    }

    func stopHaptics() {
        guard playbackState == .starting || playbackState == .playing else { return }

        playbackState = .stopping

        let activeLoop = loopingStateQueue.sync { () -> (DispatchWorkItem?, GCHapticsLocality?, TimeInterval) in
            let activeReplayWorkItem = loopingReplayWorkItem
            let activeLoopingLocality = loopingLocality
            let now = ProcessInfo.processInfo.systemUptime
            let remainingTailDuration = max(0, (activeIterationEndUptime ?? now) - now)
            loopingReplayWorkItem = nil
            loopingPatternIdentifier = nil
            loopingLocality = nil
            activeIterationEndUptime = nil
            return (activeReplayWorkItem, activeLoopingLocality, remainingTailDuration)
        }

        activeLoop.0?.cancel()
        scheduleStoppedStateTransition(after: activeLoop.2 + LoopingConstants.stopSettleBuffer,
                                       locality: activeLoop.1)
    }

    func isLoopingHapticsFile(named filename: String, locality: GCHapticsLocality = .default) -> Bool {
        let identifier = playbackIdentifier(for: filename, locality: locality)
        return loopingStateQueue.sync {
            (playbackState == .starting || playbackState == .playing) &&
            loopingPatternIdentifier == identifier
        }
    }

    private func playLoopIteration(filename: String,
                                   url: URL,
                                   engine: CHHapticEngine,
                                   identifier: String,
                                   locality: GCHapticsLocality,
                                   patternDuration: TimeInterval,
                                   repeatDelay: TimeInterval) {
        guard shouldKeepLooping(identifier: identifier, locality: locality) else {
            return
        }

        do {
            try engine.start()
            try engine.playPattern(from: url)
            let expectedEndUptime = ProcessInfo.processInfo.systemUptime + patternDuration
            if playbackState == .starting {
                playbackState = .playing
            }

            let replayWorkItem = DispatchWorkItem { [weak self] in
                self?.playLoopIteration(filename: filename,
                                        url: url,
                                        engine: engine,
                                        identifier: identifier,
                                        locality: locality,
                                        patternDuration: patternDuration,
                                        repeatDelay: repeatDelay)
            }

            loopingStateQueue.sync {
                guard loopingPatternIdentifier == identifier,
                      loopingLocality == locality else { return }
                activeIterationEndUptime = expectedEndUptime
                loopingReplayWorkItem = replayWorkItem
            }

            DispatchQueue.main.asyncAfter(deadline: .now() + repeatDelay, execute: replayWorkItem)
        } catch {
            clearLoopingState()
            engine.stop(completionHandler: nil)
            engineMap.removeValue(forKey: locality)
            playbackState = .idle
            print("An error occured playing \(filename): \(error).")
        }
    }

    private func clearLoopingState() {
        let activeWorkItem = loopingStateQueue.sync { () -> DispatchWorkItem? in
            let workItem = loopingReplayWorkItem
            loopingReplayWorkItem = nil
            loopingPatternIdentifier = nil
            loopingLocality = nil
            activeIterationEndUptime = nil
            return workItem
        }
        activeWorkItem?.cancel()
    }

    private func cancelPlaybackTransitionWorkItems() {
        stoppedTransitionWorkItem?.cancel()
        idleTransitionWorkItem?.cancel()
        stoppedTransitionWorkItem = nil
        idleTransitionWorkItem = nil
    }

    private func scheduleStoppedStateTransition(after delay: TimeInterval,
                                                locality: GCHapticsLocality?) {
        cancelPlaybackTransitionWorkItems()

        let stoppedWorkItem = DispatchWorkItem { [weak self] in
            guard let self else { return }
            self.stopEngine(for: locality)
            self.playbackState = .stopped
            self.scheduleIdleStateTransition()
        }
        stoppedTransitionWorkItem = stoppedWorkItem
        DispatchQueue.main.asyncAfter(deadline: .now() + max(0, delay),
                                      execute: stoppedWorkItem)
    }

    private func scheduleIdleStateTransition() {
        idleTransitionWorkItem?.cancel()

        let idleWorkItem = DispatchWorkItem { [weak self] in
            guard let self else { return }
            self.playbackState = .idle
        }
        idleTransitionWorkItem = idleWorkItem
        DispatchQueue.main.asyncAfter(deadline: .now() + LoopingConstants.stoppedIndicatorDuration,
                                      execute: idleWorkItem)
    }

    private func patternTiming(for url: URL) -> PatternTiming {
        do {
            let data = try Data(contentsOf: url)
            let object = try JSONSerialization.jsonObject(with: data)
            guard let dictionary = object as? [String: Any],
                  let patternItems = dictionary["Pattern"] as? [[String: Any]] else {
                return PatternTiming(duration: LoopingConstants.fallbackRepeatDelay,
                                     repeatDelay: LoopingConstants.fallbackRepeatDelay)
            }

            let duration = patternItems.reduce(0.0) { currentMax, item in
                guard let event = item["Event"] as? [String: Any] else {
                    return currentMax
                }

                let time = event["Time"] as? TimeInterval ?? 0
                guard time <= LoopingConstants.reasonableEventTimeUpperBound else {
                    return currentMax
                }
                let eventDuration = event["EventDuration"] as? TimeInterval ?? 0
                return max(currentMax, time + eventDuration)
            }

            guard duration > 0 else {
                return PatternTiming(duration: LoopingConstants.fallbackRepeatDelay,
                                     repeatDelay: LoopingConstants.fallbackRepeatDelay)
            }

            return PatternTiming(duration: duration,
                                 repeatDelay: max(duration * LoopingConstants.overlapFactor,
                                                  LoopingConstants.minimumRepeatDelay))
        } catch {
            print("Failed to calculate repeat delay for \(url.lastPathComponent): \(error)")
            return PatternTiming(duration: LoopingConstants.fallbackRepeatDelay,
                                 repeatDelay: LoopingConstants.fallbackRepeatDelay)
        }
    }

    private func stopEngine(for locality: GCHapticsLocality?) {
        guard let locality,
              let engine = engineMap[locality] else { return }
        engine.stop(completionHandler: nil)
        engineMap.removeValue(forKey: locality)
    }

    private func engine(for locality: GCHapticsLocality) -> CHHapticEngine? {
        guard let controller else {
            print("Unable to play haptics: no game controller connected")
            return nil
        }

        if let existingEngine = engineMap[locality] {
            return existingEngine
        }

        guard let newEngine = createEngine(for: controller, locality: locality) else {
            return nil
        }

        engineMap[locality] = newEngine
        return newEngine
    }

    private func shouldKeepLooping(identifier: String, locality: GCHapticsLocality) -> Bool {
        loopingStateQueue.sync {
            loopingPatternIdentifier == identifier &&
            loopingLocality == locality &&
            (playbackState == .starting || playbackState == .playing)
        }
    }

    private func playbackIdentifier(for filename: String, locality: GCHapticsLocality) -> String {
        "\(filename)|\(String(describing: locality))"
    }
}

extension CHHapticEngine.StoppedReason {
    var message: String {
        switch self {
        case .audioSessionInterrupt:
            return "the audio session was interrupted."
        case .applicationSuspended:
            return "the application was suspended."
        case .idleTimeout:
            return "an idle timeout occurred."
        case .systemError:
            return "a system error occurred."
        case .notifyWhenFinished:
            return "playback finished."
        case .engineDestroyed:
            return "the engine was destroyed."
        case .gameControllerDisconnect:
            return "the game controller disconnected."
        @unknown default:
            fatalError()
        }
    }
}
