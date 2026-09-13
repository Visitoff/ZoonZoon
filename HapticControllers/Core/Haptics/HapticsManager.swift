import CoreHaptics
import Foundation
import GameController
import os

enum HapticsPlaybackState: Equatable {
    case idle
    case starting
    case playing
    case stopping
}

enum HapticsManagerError: LocalizedError {
    case noController
    case hapticsUnsupported
    case playbackFailed(Error)

    var errorDescription: String? {
        switch self {
        case .noController:
            return "Connect a compatible game controller first."
        case .hapticsUnsupported:
            return "This controller does not provide vibration through iOS."
        case .playbackFailed:
            return "Vibration could not be started. Reconnect the controller and try again."
        }
    }
}

protocol HapticsManagerDelegate: AnyObject {
    func didConnect(controller: GCController)
    func didDisconnectController()
    func didUpdatePlaybackState(_ state: HapticsPlaybackState)
}

final class HapticsManager {
    private enum TouchHapticChannelKey: Hashable {
        case left
        case right
        case combined
    }

    private final class TouchHapticChannel {
        let id: UUID
        let engine: CHHapticEngine
        let player: CHHapticPatternPlayer
        var intensity: Float

        init(id: UUID, engine: CHHapticEngine, player: CHHapticPatternPlayer, intensity: Float) {
            self.id = id
            self.engine = engine
            self.player = player
            self.intensity = intensity
        }
    }

    weak var delegate: HapticsManagerDelegate?

    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.ZoonZoon",
                                category: "Haptics")

    private(set) var connectedController: GCController?
    private(set) var playbackState: HapticsPlaybackState = .idle

    private var isMonitoring = false
    private var engine: CHHapticEngine?
    private var player: CHHapticPatternPlayer?
    private var engineIdentifier: UUID?
    private var leftTouchChannel: TouchHapticChannel?
    private var rightTouchChannel: TouchHapticChannel?
    private var combinedTouchChannel: TouchHapticChannel?
    private var touchPositionY: Float?
    private let touchInvalidationLock = NSLock()
    private var invalidatedTouchChannelIDs: [TouchHapticChannelKey: UUID] = [:]
    private(set) var intensity: Float = 0.55

    var isControllerConnected: Bool {
        connectedController != nil
    }

    var supportsHaptics: Bool {
        connectedController?.haptics != nil
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    func startMonitoring() {
        guard !isMonitoring else {
            refreshConnectedController()
            return
        }

        let notificationCenter = NotificationCenter.default
        notificationCenter.addObserver(self,
                                       selector: #selector(controllerDidConnect),
                                       name: .GCControllerDidConnect,
                                       object: nil)
        notificationCenter.addObserver(self,
                                       selector: #selector(controllerDidDisconnect),
                                       name: .GCControllerDidDisconnect,
                                       object: nil)
        isMonitoring = true
        refreshConnectedController()
    }

    func stopMonitoring() {
        stopHaptics()
        guard isMonitoring else { return }

        NotificationCenter.default.removeObserver(self,
                                                  name: .GCControllerDidConnect,
                                                  object: nil)
        NotificationCenter.default.removeObserver(self,
                                                  name: .GCControllerDidDisconnect,
                                                  object: nil)
        isMonitoring = false
    }

    func refreshConnectedController() {
        let detectedController = GCController.controllers().first
        updateConnectedController(detectedController)
    }

    func setIntensity(_ value: Float) {
        intensity = min(max(value, 0), 1)

        if let player {
            let parameter = intensityParameter(value: intensity)
            do {
                try player.sendParameters([parameter], atTime: CHHapticTimeImmediate)
            } catch {
                logger.error("Unable to update haptic intensity: \(error.localizedDescription, privacy: .public)")
            }
        }

        if let touchPositionY {
            applyTouchHapticIntensity(normalizedY: touchPositionY)
        }
    }

    func updateTouchHaptics(normalizedY: Float) {
        guard let controllerHaptics = connectedController?.haptics else { return }
        let requestedPositionY = min(max(normalizedY, 0), 1)
        touchPositionY = requestedPositionY

        do {
            try prepareTouchHapticChannels(using: controllerHaptics)
            touchPositionY = requestedPositionY
            applyTouchHapticIntensity(normalizedY: requestedPositionY)
        } catch {
            logger.error("Unable to start touch haptics: \(error.localizedDescription, privacy: .public)")
            stopTouchHaptics()
        }
    }

    func stopTouchHaptics() {
        touchPositionY = nil
        do {
            try updateTouchHapticChannel(leftTouchChannel, intensity: 0)
            try updateTouchHapticChannel(rightTouchChannel, intensity: 0)
            try updateTouchHapticChannel(combinedTouchChannel, intensity: 0)
        } catch {
            logger.error("Unable to mute touch haptics: \(error.localizedDescription, privacy: .public)")
            destroyTouchHapticChannels()
        }
    }

    @discardableResult
    func startDefaultHaptics() -> Result<Void, HapticsManagerError> {
        guard playbackState != .playing else {
            return .success(())
        }
        guard let controller = connectedController else {
            return .failure(.noController)
        }
        guard let controllerHaptics = controller.haptics else {
            return .failure(.hapticsUnsupported)
        }

        let supportedLocalities = controllerHaptics.supportedLocalities
            .map(\.rawValue)
            .sorted()
            .joined(separator: ", ")
        logger.info("Starting controller haptics for \(controller.productCategory, privacy: .public); localities: \(supportedLocalities, privacy: .public)")

        stopHaptics()
        updatePlaybackState(.starting)

        guard let newEngine = controllerHaptics.createEngine(withLocality: .default) else {
            updatePlaybackState(.idle)
            logger.error("Unable to create a haptic engine for the default locality")
            return .failure(.hapticsUnsupported)
        }

        let identifier = UUID()
        engineIdentifier = identifier
        configure(newEngine, identifier: identifier)

        var stage = "creating the pattern"
        do {
            let hapticPattern = try makeDefaultPattern()

            stage = "starting the engine"
            try newEngine.start()

            stage = "creating the player"
            let newPlayer = try newEngine.makePlayer(with: hapticPattern)
            engine = newEngine
            player = newPlayer

            stage = "starting the player"
            try newPlayer.start(atTime: CHHapticTimeImmediate)
            setIntensity(intensity)

            updatePlaybackState(.playing)
            return .success(())
        } catch {
            let nsError = error as NSError
            logger.error("Controller haptics failed while \(stage, privacy: .public): domain=\(nsError.domain, privacy: .public) code=\(nsError.code) description=\(nsError.localizedDescription, privacy: .public)")
            newEngine.stop(completionHandler: nil)
            clearPlaybackObjects()
            updatePlaybackState(.idle)
            return .failure(.playbackFailed(error))
        }
    }

    func stopHaptics() {
        destroyTouchHapticChannels()
        guard player != nil || engine != nil || playbackState != .idle else { return }

        updatePlaybackState(.stopping)

        let activePlayer = player
        let activeEngine = engine
        clearPlaybackObjects()

        do {
            try activePlayer?.cancel()
        } catch {
            print("Unable to stop the haptic player immediately: \(error)")
        }
        activeEngine?.stop(completionHandler: nil)
        updatePlaybackState(.idle)
    }

    @objc private func controllerDidConnect(notification: Notification) {
        guard let controller = notification.object as? GCController else { return }
        performOnMain { [weak self] in
            guard let self, self.connectedController == nil else { return }
            self.updateConnectedController(controller)
        }
    }

    @objc private func controllerDidDisconnect(notification: Notification) {
        guard let disconnectedController = notification.object as? GCController else { return }
        performOnMain { [weak self] in
            guard let self, disconnectedController === self.connectedController else { return }
            let replacementController = GCController.controllers().first {
                $0 !== disconnectedController
            }
            self.updateConnectedController(replacementController)
        }
    }

    private func updateConnectedController(_ controller: GCController?) {
        guard controller !== connectedController else { return }

        stopHaptics()
        connectedController = controller

        if let controller {
            delegateOnMain { delegate in
                delegate.didConnect(controller: controller)
            }
        } else {
            delegateOnMain { delegate in
                delegate.didDisconnectController()
            }
        }
    }

    private func configure(_ engine: CHHapticEngine, identifier: UUID) {
        engine.isAutoShutdownEnabled = false
        engine.stoppedHandler = { [weak self] reason in
            print("The haptic engine stopped because \(reason.message)")
            DispatchQueue.main.async {
                self?.handleEngineStop(identifier: identifier)
            }
        }
        engine.resetHandler = { [weak self] in
            DispatchQueue.main.async {
                self?.handleEngineStop(identifier: identifier)
            }
        }
    }

    private func makeDefaultPattern() throws -> CHHapticPattern {
        let intensity = CHHapticEventParameter(parameterID: .hapticIntensity, value: 1.0)
        let sharpness = CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.25)
        let continuousEvent = CHHapticEvent(
            eventType: .hapticContinuous,
            parameters: [intensity, sharpness],
            relativeTime: 0,
            duration: TimeInterval(GCHapticDurationInfinite)
        )
        return try CHHapticPattern(events: [continuousEvent], parameters: [])
    }

    private func prepareTouchHapticChannels(using controllerHaptics: GCDeviceHaptics) throws {
        discardInvalidatedTouchChannels()
        if combinedTouchChannel != nil || (leftTouchChannel != nil && rightTouchChannel != nil) {
            return
        }
        destroyTouchHapticChannels()

        let localities = controllerHaptics.supportedLocalities
        if localities.contains(.leftHandle), localities.contains(.rightHandle) {
            do {
                leftTouchChannel = try makeTouchHapticChannel(
                    using: controllerHaptics,
                    locality: .leftHandle,
                    key: .left
                )
                rightTouchChannel = try makeTouchHapticChannel(
                    using: controllerHaptics,
                    locality: .rightHandle,
                    key: .right
                )
                return
            } catch {
                destroyTouchHapticChannels()
                logger.info("Separate handle haptics unavailable; using the default locality")
            }
        }

        combinedTouchChannel = try makeTouchHapticChannel(
            using: controllerHaptics,
            locality: .default,
            key: .combined
        )
    }

    private func makeTouchHapticChannel(
        using controllerHaptics: GCDeviceHaptics,
        locality: GCHapticsLocality,
        key: TouchHapticChannelKey
    ) throws -> TouchHapticChannel {
        guard let touchEngine = controllerHaptics.createEngine(withLocality: locality) else {
            throw HapticsManagerError.hapticsUnsupported
        }

        let channelID = UUID()
        touchEngine.isAutoShutdownEnabled = false
        touchEngine.playsHapticsOnly = true
        touchEngine.stoppedHandler = { [weak self] _ in
            self?.markTouchChannelInvalidated(key: key, channelID: channelID)
        }
        touchEngine.resetHandler = { [weak self] in
            self?.markTouchChannelInvalidated(key: key, channelID: channelID)
        }
        do {
            let hapticPattern = try makeTouchPattern()
            try touchEngine.start()
            let touchPlayer = try touchEngine.makePlayer(with: hapticPattern)
            try touchPlayer.start(atTime: CHHapticTimeImmediate)
            return TouchHapticChannel(
                id: channelID,
                engine: touchEngine,
                player: touchPlayer,
                intensity: 0
            )
        } catch {
            touchEngine.stop(completionHandler: nil)
            throw error
        }
    }

    private func makeTouchPattern() throws -> CHHapticPattern {
        let eventIntensity = CHHapticEventParameter(parameterID: .hapticIntensity, value: 1)
        let eventSharpness = CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.42)
        let continuousEvent = CHHapticEvent(
            eventType: .hapticContinuous,
            parameters: [eventIntensity, eventSharpness],
            relativeTime: 0,
            duration: TimeInterval(GCHapticDurationInfinite)
        )
        let mutedIntensity = CHHapticDynamicParameter(
            parameterID: .hapticIntensityControl,
            value: 0,
            relativeTime: 0
        )
        return try CHHapticPattern(events: [continuousEvent], parameters: [mutedIntensity])
    }

    private func applyTouchHapticIntensity(normalizedY: Float) {
        let y = min(max(normalizedY, 0), 1)
        let maximumOutput = intensity * 0.68
        let leftIntensity = Float(cos(Double(y) * .pi / 2)) * maximumOutput
        let rightIntensity = Float(sin(Double(y) * .pi / 2)) * maximumOutput

        do {
            if let leftTouchChannel, let rightTouchChannel {
                try updateTouchHapticChannel(leftTouchChannel, intensity: leftIntensity)
                try updateTouchHapticChannel(rightTouchChannel, intensity: rightIntensity)
            } else if let combinedTouchChannel {
                try updateTouchHapticChannel(combinedTouchChannel, intensity: maximumOutput)
            }
        } catch {
            logger.error("Unable to update touch haptics: \(error.localizedDescription, privacy: .public)")
            destroyTouchHapticChannels()
        }
    }

    private func updateTouchHapticChannel(_ channel: TouchHapticChannel?, intensity: Float) throws {
        guard let channel else { return }
        let rawIntensity = min(max(intensity, 0), 1)
        let clampedIntensity: Float = rawIntensity <= 0.01 ? 0 : rawIntensity
        if clampedIntensity == 0, channel.intensity == 0 {
            return
        }
        if clampedIntensity > 0, abs(channel.intensity - clampedIntensity) < 0.02 {
            return
        }
        try channel.player.sendParameters(
            [intensityParameter(value: clampedIntensity)],
            atTime: CHHapticTimeImmediate
        )
        channel.intensity = clampedIntensity
    }

    private func discardInvalidatedTouchChannels() {
        if let channel = leftTouchChannel,
           consumeTouchChannelInvalidation(key: .left, channelID: channel.id) {
            stopTouchHapticChannel(channel)
            leftTouchChannel = nil
        }
        if let channel = rightTouchChannel,
           consumeTouchChannelInvalidation(key: .right, channelID: channel.id) {
            stopTouchHapticChannel(channel)
            rightTouchChannel = nil
        }
        if let channel = combinedTouchChannel,
           consumeTouchChannelInvalidation(key: .combined, channelID: channel.id) {
            stopTouchHapticChannel(channel)
            combinedTouchChannel = nil
        }
    }

    private func destroyTouchHapticChannels() {
        touchPositionY = nil
        let channels: [(TouchHapticChannelKey, TouchHapticChannel)] = [
            leftTouchChannel.map { (.left, $0) },
            rightTouchChannel.map { (.right, $0) },
            combinedTouchChannel.map { (.combined, $0) }
        ].compactMap { $0 }
        leftTouchChannel = nil
        rightTouchChannel = nil
        combinedTouchChannel = nil

        touchInvalidationLock.lock()
        for (key, channel) in channels where invalidatedTouchChannelIDs[key] == channel.id {
            invalidatedTouchChannelIDs.removeValue(forKey: key)
        }
        touchInvalidationLock.unlock()

        for (_, channel) in channels {
            stopTouchHapticChannel(channel)
        }
    }

    private func stopTouchHapticChannel(_ channel: TouchHapticChannel) {
        try? channel.player.stop(atTime: CHHapticTimeImmediate)
        channel.engine.stop(completionHandler: nil)
    }

    private func markTouchChannelInvalidated(key: TouchHapticChannelKey, channelID: UUID) {
        touchInvalidationLock.lock()
        invalidatedTouchChannelIDs[key] = channelID
        touchInvalidationLock.unlock()

        performOnMain { [weak self] in
            guard let self, let touchPositionY = self.touchPositionY else { return }
            self.updateTouchHaptics(normalizedY: touchPositionY)
        }
    }

    private func consumeTouchChannelInvalidation(
        key: TouchHapticChannelKey,
        channelID: UUID
    ) -> Bool {
        touchInvalidationLock.lock()
        defer { touchInvalidationLock.unlock() }
        guard let invalidatedID = invalidatedTouchChannelIDs.removeValue(forKey: key) else {
            return false
        }
        return invalidatedID == channelID
    }

    private func intensityParameter(value: Float) -> CHHapticDynamicParameter {
        CHHapticDynamicParameter(
            parameterID: .hapticIntensityControl,
            value: min(max(value, 0), 1),
            relativeTime: 0
        )
    }

    private func handleEngineStop(identifier: UUID) {
        guard engineIdentifier == identifier else { return }
        clearPlaybackObjects()
        updatePlaybackState(.idle)
    }

    private func clearPlaybackObjects() {
        player = nil
        engine = nil
        engineIdentifier = nil
    }

    private func updatePlaybackState(_ state: HapticsPlaybackState) {
        guard playbackState != state else { return }
        playbackState = state
        delegateOnMain { delegate in
            delegate.didUpdatePlaybackState(state)
        }
    }

    private func delegateOnMain(_ action: @escaping (HapticsManagerDelegate) -> Void) {
        performOnMain { [weak self] in
            guard let delegate = self?.delegate else { return }
            action(delegate)
        }
    }

    private func performOnMain(_ action: @escaping () -> Void) {
        if Thread.isMainThread {
            action()
        } else {
            DispatchQueue.main.async(execute: action)
        }
    }
}

private extension CHHapticEngine.StoppedReason {
    var message: String {
        switch self {
        case .audioSessionInterrupt:
            return "the audio session was interrupted"
        case .applicationSuspended:
            return "the application was suspended"
        case .idleTimeout:
            return "an idle timeout occurred"
        case .systemError:
            return "a system error occurred"
        case .notifyWhenFinished:
            return "playback finished"
        case .engineDestroyed:
            return "the engine was destroyed"
        case .gameControllerDisconnect:
            return "the game controller disconnected"
        @unknown default:
            return "an unknown error occurred"
        }
    }
}
