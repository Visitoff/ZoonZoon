import GameController
import UIKit

final class MainViewController: UIViewController {
    fileprivate enum Palette {
        static let background = UIColor.black
        static let coral = UIColor(red: 1.00, green: 0.34, blue: 0.36, alpha: 1.00)
        static let pink = UIColor(red: 0.93, green: 0.25, blue: 0.96, alpha: 1.00)
        static let violet = UIColor(red: 0.54, green: 0.18, blue: 0.78, alpha: 1.00)
        static let connected = UIColor(red: 0.35, green: 0.93, blue: 0.68, alpha: 1.00)
        static let primaryText = UIColor.white
        static let secondaryText = UIColor.white.withAlphaComponent(0.54)
        static let border = UIColor.white.withAlphaComponent(0.22)
        static let darkSurface = UIColor(red: 0.055, green: 0.052, blue: 0.060, alpha: 0.96)
    }

    private let manager: HapticsManager
    private let backgroundGradient = CAGradientLayer()
    private let topGlowGradient = CAGradientLayer()
    private let bottomGlowGradient = CAGradientLayer()
    private let connectionButton = GlassCircleControl(symbolName: "gamecontroller", diameter: 48)
    private let playerCard = PlayerCardView()
    private let intensitySlider = IntensitySliderView()

    private var isDiscoveringController = false
    private weak var triggerController: GCController?
    private var leftTriggerValue: Float = 0
    private var rightTriggerValue: Float = 0
    private var triggerDisplayLink: CADisplayLink?

    required init?(coder: NSCoder) {
        manager = HapticsManager()
        super.init(coder: coder)
    }

    deinit {
        stopTriggerControl()
        NotificationCenter.default.removeObserver(self)
        GCController.stopWirelessControllerDiscovery()
        UIApplication.shared.isIdleTimerDisabled = false
        manager.delegate = nil
        manager.stopMonitoring()
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        .lightContent
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        configureAppearance()
        buildInterface()
        observeApplicationLifecycle()

        manager.delegate = self
        manager.startMonitoring()
        intensitySlider.value = manager.intensity
        updateInterface()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        backgroundGradient.frame = view.bounds
        topGlowGradient.frame = view.bounds
        bottomGlowGradient.frame = view.bounds
    }

    private func configureAppearance() {
        overrideUserInterfaceStyle = .dark
        view.backgroundColor = Palette.background

        backgroundGradient.colors = [
            UIColor.black.cgColor,
            UIColor(red: 0.08, green: 0.025, blue: 0.065, alpha: 1).cgColor,
            UIColor(red: 0.035, green: 0.025, blue: 0.045, alpha: 1).cgColor,
            UIColor.black.cgColor,
        ]
        backgroundGradient.locations = [0.0, 0.18, 0.66, 1.0]
        backgroundGradient.startPoint = CGPoint(x: 0.18, y: 0)
        backgroundGradient.endPoint = CGPoint(x: 0.82, y: 1)
        view.layer.insertSublayer(backgroundGradient, at: 0)

        topGlowGradient.type = .radial
        topGlowGradient.colors = [
            Palette.coral.withAlphaComponent(0.20).cgColor,
            Palette.pink.withAlphaComponent(0.08).cgColor,
            UIColor.clear.cgColor,
        ]
        topGlowGradient.locations = [0.0, 0.35, 1.0]
        topGlowGradient.startPoint = CGPoint(x: 0.22, y: 0.08)
        topGlowGradient.endPoint = CGPoint(x: 0.86, y: 0.58)
        view.layer.insertSublayer(topGlowGradient, above: backgroundGradient)

        bottomGlowGradient.type = .radial
        bottomGlowGradient.colors = [
            Palette.violet.withAlphaComponent(0.18).cgColor,
            UIColor.clear.cgColor,
        ]
        bottomGlowGradient.locations = [0.0, 1.0]
        bottomGlowGradient.startPoint = CGPoint(x: 0.50, y: 0.84)
        bottomGlowGradient.endPoint = CGPoint(x: 0.05, y: 0.35)
        view.layer.insertSublayer(bottomGlowGradient, above: topGlowGradient)
    }

    private func buildInterface() {
        let topBar = UIView()
        let logo = WaveLogoView()
        let trademarkLabel = UILabel()

        topBar.translatesAutoresizingMaskIntoConstraints = false
        logo.translatesAutoresizingMaskIntoConstraints = false
        trademarkLabel.translatesAutoresizingMaskIntoConstraints = false
        connectionButton.translatesAutoresizingMaskIntoConstraints = false
        playerCard.translatesAutoresizingMaskIntoConstraints = false
        intensitySlider.translatesAutoresizingMaskIntoConstraints = false

        trademarkLabel.text = "™"
        trademarkLabel.textColor = .white
        trademarkLabel.font = .systemFont(ofSize: 9, weight: .medium)

        connectionButton.accessibilityIdentifier = "connectionButton"
        connectionButton.addTarget(self, action: #selector(connectionButtonTapped), for: .touchUpInside)
        playerCard.accessibilityIdentifier = "heartbeatPlayer"
        playerCard.onPowerTap = { [weak self] in
            self?.toggleHaptics()
        }
        playerCard.onWaveTouch = { [weak self] point in
            if let point {
                self?.manager.updateTouchHaptics(normalizedY: Float(point.y))
            } else {
                self?.manager.stopTouchHaptics()
            }
        }
        intensitySlider.accessibilityIdentifier = "intensitySlider"
        intensitySlider.addTarget(self, action: #selector(intensityChanged), for: .valueChanged)

        view.addSubview(topBar)
        topBar.addSubview(connectionButton)
        topBar.addSubview(logo)
        topBar.addSubview(trademarkLabel)
        view.addSubview(playerCard)
        view.addSubview(intensitySlider)

        let playerAspect = playerCard.heightAnchor.constraint(
            equalTo: playerCard.widthAnchor,
            multiplier: 445.0 / 345.0
        )
        playerAspect.priority = .defaultHigh

        NSLayoutConstraint.activate([
            topBar.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 2),
            topBar.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            topBar.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24),
            topBar.heightAnchor.constraint(equalToConstant: 52),

            connectionButton.trailingAnchor.constraint(equalTo: topBar.trailingAnchor),
            connectionButton.centerYAnchor.constraint(equalTo: topBar.centerYAnchor),
            connectionButton.widthAnchor.constraint(equalToConstant: 48),
            connectionButton.heightAnchor.constraint(equalTo: connectionButton.widthAnchor),

            logo.centerXAnchor.constraint(equalTo: topBar.centerXAnchor),
            logo.centerYAnchor.constraint(equalTo: topBar.centerYAnchor),
            logo.widthAnchor.constraint(equalToConstant: 50),
            logo.heightAnchor.constraint(equalTo: logo.widthAnchor),

            trademarkLabel.leadingAnchor.constraint(equalTo: logo.trailingAnchor, constant: 2),
            trademarkLabel.topAnchor.constraint(equalTo: logo.topAnchor, constant: -1),

            playerCard.topAnchor.constraint(equalTo: topBar.bottomAnchor, constant: 24),
            playerCard.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 15),
            playerCard.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -15),
            playerAspect,

            intensitySlider.topAnchor.constraint(equalTo: playerCard.bottomAnchor, constant: 18),
            intensitySlider.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 15),
            intensitySlider.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -15),
            intensitySlider.heightAnchor.constraint(equalToConstant: 83),
            intensitySlider.bottomAnchor.constraint(lessThanOrEqualTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -8),
        ])
    }

    private func observeApplicationLifecycle() {
        let notificationCenter = NotificationCenter.default
        notificationCenter.addObserver(self,
                                       selector: #selector(applicationWillLeaveForeground),
                                       name: UIApplication.willResignActiveNotification,
                                       object: nil)
        notificationCenter.addObserver(self,
                                       selector: #selector(applicationWillLeaveForeground),
                                       name: UIApplication.didEnterBackgroundNotification,
                                       object: nil)
        notificationCenter.addObserver(self,
                                       selector: #selector(applicationWillLeaveForeground),
                                       name: UIApplication.willTerminateNotification,
                                       object: nil)
        notificationCenter.addObserver(self,
                                       selector: #selector(applicationDidBecomeActive),
                                       name: UIApplication.didBecomeActiveNotification,
                                       object: nil)
    }

    @objc private func connectionButtonTapped() {
        guard !manager.isControllerConnected else { return }
        showPairingInstructions()
    }

    @objc private func intensityChanged() {
        manager.setIntensity(intensitySlider.value)
        playerCard.setIntensity(intensitySlider.value)
    }

    private func toggleHaptics() {
        guard manager.isControllerConnected else {
            showPairingInstructions()
            return
        }

        if manager.playbackState == .playing || manager.playbackState == .starting {
            manager.stopHaptics()
            return
        }

        switch manager.startDefaultHaptics() {
        case .success:
            break
        case let .failure(error):
            presentPlaybackError(error.localizedDescription)
        }
    }

    private func showPairingInstructions() {
        guard presentedViewController == nil else { return }

        let instructionsViewController = PairingInstructionsViewController()
        instructionsViewController.onClose = { [weak self, weak instructionsViewController] in
            self?.stopControllerDiscovery()
            instructionsViewController?.dismiss(animated: true)
        }

        if let sheet = instructionsViewController.sheetPresentationController {
            if #available(iOS 16.0, *) {
                let identifier = UISheetPresentationController.Detent.Identifier("pairing")
                let detent = UISheetPresentationController.Detent.custom(identifier: identifier) { context in
                    min(context.maximumDetentValue, 540)
                }
                sheet.detents = [detent]
                sheet.selectedDetentIdentifier = identifier
            } else {
                sheet.detents = [.large()]
            }
            sheet.prefersGrabberVisible = true
            sheet.preferredCornerRadius = 30
        }

        present(instructionsViewController, animated: true) { [weak self] in
            guard let self else { return }
            instructionsViewController.presentationController?.delegate = self
            self.startControllerDiscovery()
        }
    }

    @objc private func applicationWillLeaveForeground() {
        UIApplication.shared.isIdleTimerDisabled = false
        leftTriggerValue = 0
        rightTriggerValue = 0
        stopControllerDiscovery()
        manager.stopHaptics()
    }

    @objc private func applicationDidBecomeActive() {
        manager.refreshConnectedController()
        updateInterface()
    }

    private func startControllerDiscovery() {
        guard !isDiscoveringController else { return }
        isDiscoveringController = true
        GCController.startWirelessControllerDiscovery { [weak self] in
            DispatchQueue.main.async {
                self?.isDiscoveringController = false
            }
        }
    }

    private func stopControllerDiscovery() {
        guard isDiscoveringController else { return }
        GCController.stopWirelessControllerDiscovery()
        isDiscoveringController = false
    }

    private func presentPlaybackError(_ message: String) {
        guard presentedViewController == nil else { return }
        let alert = UIAlertController(title: "Unable to start vibration",
                                      message: message,
                                      preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }

    private func updateInterface() {
        let isConnected = manager.isControllerConnected
        configureTriggerControl(for: manager.connectedController)
        connectionButton.setControllerState(
            connected: isConnected,
            controller: manager.connectedController
        )
        playerCard.update(playbackState: manager.playbackState)
        playerCard.setIntensity(manager.intensity)
        UIApplication.shared.isIdleTimerDisabled = manager.playbackState == .playing
    }

    private func configureTriggerControl(for controller: GCController?) {
        if triggerController === controller {
            leftTriggerValue = controller?.extendedGamepad?.leftTrigger.value ?? 0
            rightTriggerValue = controller?.extendedGamepad?.rightTrigger.value ?? 0
            return
        }

        triggerController?.extendedGamepad?.leftTrigger.valueChangedHandler = nil
        triggerController?.extendedGamepad?.rightTrigger.valueChangedHandler = nil
        triggerController?.extendedGamepad?.buttonA.valueChangedHandler = nil
        triggerController = controller
        leftTriggerValue = 0
        rightTriggerValue = 0

        guard let controller, let gamepad = controller.extendedGamepad else {
            stopTriggerDisplayLink()
            return
        }

        controller.handlerQueue = .main
        leftTriggerValue = gamepad.leftTrigger.value
        rightTriggerValue = gamepad.rightTrigger.value

        gamepad.leftTrigger.valueChangedHandler = { [weak self] _, value, _ in
            self?.leftTriggerValue = value
        }
        gamepad.rightTrigger.valueChangedHandler = { [weak self] _, value, _ in
            self?.rightTriggerValue = value
        }
        gamepad.buttonA.valueChangedHandler = { [weak self] _, _, pressed in
            guard pressed else { return }
            self?.toggleHaptics()
        }
        startTriggerDisplayLink()
    }

    private func startTriggerDisplayLink() {
        guard triggerDisplayLink == nil else { return }
        let displayLink = CADisplayLink(target: self, selector: #selector(adjustIntensityFromTriggers(_:)))
        displayLink.add(to: .main, forMode: .common)
        triggerDisplayLink = displayLink
    }

    private func stopTriggerDisplayLink() {
        triggerDisplayLink?.invalidate()
        triggerDisplayLink = nil
    }

    private func stopTriggerControl() {
        triggerController?.extendedGamepad?.leftTrigger.valueChangedHandler = nil
        triggerController?.extendedGamepad?.rightTrigger.valueChangedHandler = nil
        triggerController?.extendedGamepad?.buttonA.valueChangedHandler = nil
        triggerController = nil
        leftTriggerValue = 0
        rightTriggerValue = 0
        stopTriggerDisplayLink()
    }

    @objc private func adjustIntensityFromTriggers(_ displayLink: CADisplayLink) {
        let signedPressure = rightTriggerValue - leftTriggerValue
        let deadZone: Float = 0.08
        guard abs(signedPressure) > deadZone else { return }

        let direction: Float = signedPressure > 0 ? 1 : -1
        let pressure = (abs(signedPressure) - deadZone) / (1 - deadZone)
        let elapsed = Float(min(max(displayLink.targetTimestamp - displayLink.timestamp, 1.0 / 120.0), 1.0 / 15.0))
        let nextValue = intensitySlider.value + direction * pressure * elapsed * 0.45

        guard intensitySlider.setValue(nextValue, emitsFeedback: true) else { return }
        manager.setIntensity(intensitySlider.value)
        playerCard.setIntensity(intensitySlider.value)
    }
}

extension MainViewController: HapticsManagerDelegate {
    func didConnect(controller: GCController) {
        stopControllerDiscovery()
        if presentedViewController is PairingInstructionsViewController {
            dismiss(animated: true)
        }
        updateInterface()
    }

    func didDisconnectController() {
        updateInterface()
    }

    func didUpdatePlaybackState(_ state: HapticsPlaybackState) {
        updateInterface()
    }
}

extension MainViewController: UIAdaptivePresentationControllerDelegate {
    func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
        stopControllerDiscovery()
    }
}

private final class GlassCircleControl: UIControl {
    private let diameter: CGFloat
    private let effectView = UIVisualEffectView()
    private let imageView = UIImageView()
    private let statusDot = UIView()
    private let feedbackGenerator = UIImpactFeedbackGenerator(style: .light)
    private var symbolName: String
    private var glassTint = UIColor.white.withAlphaComponent(0.05)
    private var iconWidthConstraint: NSLayoutConstraint?
    private var iconHeightConstraint: NSLayoutConstraint?

    init(symbolName: String, diameter: CGFloat) {
        self.symbolName = symbolName
        self.diameter = diameter
        super.init(frame: .zero)
        setup()
    }

    required init?(coder: NSCoder) {
        diameter = 48
        symbolName = "circle"
        super.init(coder: coder)
        setup()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        layer.cornerRadius = bounds.height / 2
        effectView.frame = bounds
        effectView.layer.cornerRadius = bounds.height / 2
        statusDot.layer.cornerRadius = statusDot.bounds.width / 2
    }

    func setControllerState(connected: Bool, controller: GCController?) {
        imageView.image = UIImage(named: controllerAssetName(for: controller))
        imageView.tintColor = nil
        imageView.alpha = connected ? 1 : 0.58
        iconWidthConstraint?.constant = 28
        iconHeightConstraint?.constant = 19
        statusDot.isHidden = false
        statusDot.backgroundColor = connected
            ? MainViewController.Palette.connected
            : UIColor.white.withAlphaComponent(0.24)
        applyGlass(tint: connected
            ? MainViewController.Palette.connected.withAlphaComponent(0.16)
            : UIColor.white.withAlphaComponent(0.05))
        accessibilityLabel = connected ? "Controller connected" : "Connect controller"
        accessibilityValue = connected ? "Connected" : "Disconnected"
        accessibilityHint = connected ? nil : "Opens controller connection instructions"
    }

    func setActive(_ active: Bool) {
        imageView.tintColor = .white
        applyGlass(tint: active
            ? MainViewController.Palette.pink.withAlphaComponent(0.30)
            : UIColor.white.withAlphaComponent(0.08))
        accessibilityLabel = active ? "Stop vibration" : "Start vibration"
        accessibilityValue = active ? "On" : "Off"
    }

    private func setup() {
        isAccessibilityElement = true
        accessibilityTraits = .button
        clipsToBounds = true
        layer.borderWidth = 1
        layer.borderColor = MainViewController.Palette.border.cgColor

        effectView.isUserInteractionEnabled = false
        addSubview(effectView)

        imageView.image = symbolImage(named: symbolName)
        imageView.tintColor = .white
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(imageView)

        statusDot.isHidden = true
        statusDot.layer.borderWidth = 2
        statusDot.layer.borderColor = UIColor.black.withAlphaComponent(0.72).cgColor
        statusDot.translatesAutoresizingMaskIntoConstraints = false
        addSubview(statusDot)

        let iconSize = min(19, diameter * 0.38)
        iconWidthConstraint = imageView.widthAnchor.constraint(equalToConstant: iconSize)
        iconHeightConstraint = imageView.heightAnchor.constraint(equalToConstant: iconSize)
        NSLayoutConstraint.activate([
            imageView.centerXAnchor.constraint(equalTo: centerXAnchor),
            imageView.centerYAnchor.constraint(equalTo: centerYAnchor),
            iconWidthConstraint!,
            iconHeightConstraint!,

            statusDot.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -4),
            statusDot.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -4),
            statusDot.widthAnchor.constraint(equalToConstant: 11),
            statusDot.heightAnchor.constraint(equalTo: statusDot.widthAnchor),
        ])

        applyGlass(tint: glassTint)
        addTarget(self, action: #selector(pressed), for: .touchDown)
        addTarget(self, action: #selector(released), for: [.touchUpInside, .touchCancel, .touchDragExit])
        addTarget(self, action: #selector(activated), for: .touchUpInside)
    }

    private func applyGlass(tint: UIColor) {
        glassTint = tint
        if #available(iOS 26.0, *) {
            let effect = UIGlassEffect(style: .regular)
            effect.isInteractive = true
            effect.tintColor = tint
            effectView.effect = effect
            backgroundColor = .clear
        } else {
            effectView.effect = UIBlurEffect(style: .systemUltraThinMaterialDark)
            backgroundColor = tint
        }
    }

    private func symbolImage(named name: String) -> UIImage? {
        let configuration = UIImage.SymbolConfiguration(pointSize: 16, weight: .medium)
        return UIImage(systemName: name, withConfiguration: configuration)
    }

    private func controllerAssetName(for controller: GCController?) -> String {
        guard let controller else { return "controller-generic" }

        switch controller.physicalInputProfile {
        case is GCDualSenseGamepad:
            return "controller-dualsense"
        case is GCDualShockGamepad:
            return "controller-dualshock4"
        case is GCXboxGamepad:
            return "controller-xbox"
        default:
            let identity = [controller.productCategory, controller.vendorName]
                .compactMap { $0 }
                .joined(separator: " ")
                .lowercased()

            if identity.contains("steam") {
                return "controller-steam"
            }
            if identity.contains("dualsense") || identity.contains("playstation 5") {
                return "controller-dualsense"
            }
            if identity.contains("dualshock") || identity.contains("playstation 4") {
                return "controller-dualshock4"
            }
            if identity.contains("xbox") {
                return "controller-xbox"
            }
            return "controller-generic"
        }
    }

    @objc private func pressed() {
        UIView.animate(withDuration: 0.10) {
            self.alpha = 0.76
            self.transform = CGAffineTransform(scaleX: 0.94, y: 0.94)
        }
    }

    @objc private func released() {
        UIView.animate(withDuration: 0.18,
                       delay: 0,
                       usingSpringWithDamping: 0.72,
                       initialSpringVelocity: 0.4) {
            self.alpha = 1
            self.transform = .identity
        }
    }

    @objc private func activated() {
        feedbackGenerator.impactOccurred(intensity: 0.72)
        feedbackGenerator.prepare()
    }
}

private final class WaveLogoView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        configureTransparency()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        configureTransparency()
    }

    private func configureTransparency() {
        backgroundColor = .clear
        isOpaque = false
        contentMode = .redraw
    }

    override func draw(_ rect: CGRect) {
        UIColor.white.setFill()
        let heights: [CGFloat] = [20, 34, 44, 50, 44, 34, 20]
        let width: CGFloat = 5
        let gap: CGFloat = 2
        let totalWidth = CGFloat(heights.count) * width + CGFloat(heights.count - 1) * gap
        var x = (rect.width - totalWidth) / 2

        for height in heights {
            let barRect = CGRect(x: x, y: (rect.height - height) / 2, width: width, height: height)
            UIBezierPath(roundedRect: barRect, cornerRadius: width / 2).fill()
            x += width + gap
        }
    }
}

private final class PlayerCardView: UIView {
    var onPowerTap: (() -> Void)?
    var onWaveTouch: ((CGPoint?) -> Void)?

    private let waveformView = WaveformView()
    private let shadeLayer = CAGradientLayer()
    private let heartBadge = UIView()
    private let heartImage = UIImageView(image: UIImage(systemName: "waveform.path"))
    private let titleLabel = UILabel()
    private let detailLabel = UILabel()
    private let powerButton = GlassCircleControl(symbolName: "power", diameter: 60)

    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setup()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        layer.cornerRadius = 42
        waveformView.frame = bounds
        shadeLayer.frame = CGRect(x: 0, y: max(0, bounds.height - 165), width: bounds.width, height: 165)
        heartBadge.layer.cornerRadius = heartBadge.bounds.width / 2
    }

    func update(playbackState: HapticsPlaybackState) {
        let active = playbackState == .playing || playbackState == .starting
        powerButton.setActive(active)
        powerButton.isEnabled = playbackState != .stopping
        waveformView.setActive(active)
    }

    func setIntensity(_ intensity: Float) {
        waveformView.setIntensity(intensity)
    }

    private func setup() {
        clipsToBounds = true
        backgroundColor = .black

        waveformView.onInteractionChanged = { [weak self] point in
            self?.onWaveTouch?(point)
        }
        addSubview(waveformView)

        shadeLayer.colors = [
            UIColor.clear.cgColor,
            UIColor.black.withAlphaComponent(0.40).cgColor,
            UIColor.black.withAlphaComponent(0.92).cgColor,
        ]
        shadeLayer.locations = [0.0, 0.40, 1.0]
        layer.addSublayer(shadeLayer)

        heartBadge.translatesAutoresizingMaskIntoConstraints = false
        heartBadge.backgroundColor = UIColor.black.withAlphaComponent(0.26)
        heartBadge.layer.borderWidth = 1
        heartBadge.layer.borderColor = MainViewController.Palette.coral.cgColor

        heartImage.translatesAutoresizingMaskIntoConstraints = false
        heartImage.tintColor = MainViewController.Palette.pink
        heartImage.contentMode = .scaleAspectFit

        titleLabel.text = "Steady"
        titleLabel.font = .systemFont(ofSize: 16, weight: .semibold)
        titleLabel.textColor = MainViewController.Palette.primaryText

        detailLabel.text = "Continuous vibration"
        detailLabel.font = .systemFont(ofSize: 14, weight: .regular)
        detailLabel.textColor = MainViewController.Palette.secondaryText

        let labels = UIStackView(arrangedSubviews: [titleLabel, detailLabel])
        labels.axis = .vertical
        labels.spacing = 5
        labels.translatesAutoresizingMaskIntoConstraints = false

        powerButton.translatesAutoresizingMaskIntoConstraints = false
        powerButton.addTarget(self, action: #selector(powerTapped), for: .touchUpInside)

        addSubview(heartBadge)
        heartBadge.addSubview(heartImage)
        addSubview(labels)
        addSubview(powerButton)

        NSLayoutConstraint.activate([
            heartBadge.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20),
            heartBadge.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -28),
            heartBadge.widthAnchor.constraint(equalToConstant: 60),
            heartBadge.heightAnchor.constraint(equalTo: heartBadge.widthAnchor),

            heartImage.centerXAnchor.constraint(equalTo: heartBadge.centerXAnchor),
            heartImage.centerYAnchor.constraint(equalTo: heartBadge.centerYAnchor),
            heartImage.widthAnchor.constraint(equalToConstant: 25),
            heartImage.heightAnchor.constraint(equalTo: heartImage.widthAnchor),

            labels.leadingAnchor.constraint(equalTo: heartBadge.trailingAnchor, constant: 10),
            labels.centerYAnchor.constraint(equalTo: heartBadge.centerYAnchor),
            labels.trailingAnchor.constraint(lessThanOrEqualTo: powerButton.leadingAnchor, constant: -8),

            powerButton.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -20),
            powerButton.centerYAnchor.constraint(equalTo: heartBadge.centerYAnchor),
            powerButton.widthAnchor.constraint(equalToConstant: 60),
            powerButton.heightAnchor.constraint(equalTo: powerButton.widthAnchor),
        ])

        accessibilityLabel = "Steady. Continuous vibration."
        update(playbackState: .idle)
    }

    @objc private func powerTapped() {
        onPowerTap?()
    }
}

private final class WaveformView: UIView {
    var onInteractionChanged: ((CGPoint?) -> Void)?

    private final class DisplayLinkTarget: NSObject {
        weak var owner: WaveformView?

        init(owner: WaveformView) {
            self.owner = owner
        }

        @objc func frameDidChange(_ displayLink: CADisplayLink) {
            owner?.updateFrame(displayLink)
        }
    }

    private var displayLink: CADisplayLink?
    private lazy var displayLinkTarget = DisplayLinkTarget(owner: self)
    private var lastTimestamp: CFTimeInterval = 0
    private var elapsedTime: CGFloat = 0
    private var activity: CGFloat = 0
    private var targetActivity: CGFloat = 0
    private var intensity: CGFloat = 0.55
    private var interactionPoint = CGPoint(x: 0.5, y: 0.5)
    private var interactionAmount: CGFloat = 0
    private var isInteracting = false

    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setup()
    }

    deinit {
        stopAnimating()
    }

    override func didMoveToWindow() {
        super.didMoveToWindow()
        if window == nil {
            stopAnimating()
        } else {
            startAnimating()
        }
    }

    func setActive(_ active: Bool) {
        targetActivity = active ? 1 : 0
        startAnimating()
    }

    func setIntensity(_ value: Float) {
        intensity = CGFloat(min(max(value, 0), 1))
        setNeedsDisplay()
    }

    private func setup() {
        isOpaque = false
        contentMode = .redraw
        isMultipleTouchEnabled = false
    }

    private func startAnimating() {
        guard displayLink == nil, window != nil else { return }
        let displayLink = CADisplayLink(
            target: displayLinkTarget,
            selector: #selector(DisplayLinkTarget.frameDidChange(_:))
        )
        if #available(iOS 15.0, *) {
            displayLink.preferredFrameRateRange = CAFrameRateRange(
                minimum: 20,
                maximum: 30,
                preferred: 30
            )
        }
        displayLink.add(to: .main, forMode: .common)
        self.displayLink = displayLink
    }

    private func stopAnimating() {
        displayLink?.invalidate()
        displayLink = nil
        lastTimestamp = 0
    }

    private func updateFrame(_ displayLink: CADisplayLink) {
        let frameDuration: CFTimeInterval
        if lastTimestamp == 0 {
            frameDuration = 1.0 / 30.0
        } else {
            frameDuration = min(displayLink.timestamp - lastTimestamp, 1.0 / 15.0)
        }
        lastTimestamp = displayLink.timestamp

        let delta = CGFloat(frameDuration)
        elapsedTime += delta
        let smoothing = 1 - exp(-delta * 5.5)
        activity += (targetActivity - activity) * smoothing
        let interactionTarget: CGFloat = isInteracting ? 1 : 0
        let interactionSmoothing = 1 - exp(-delta * (isInteracting ? 14 : 3.8))
        interactionAmount += (interactionTarget - interactionAmount) * interactionSmoothing
        setNeedsDisplay()
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        guard let touch = touches.first else { return }
        isInteracting = true
        updateInteraction(with: touch)
    }

    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        guard let touch = touches.first else { return }
        updateInteraction(with: touch)
    }

    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        endInteraction()
    }

    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        endInteraction()
    }

    private func updateInteraction(with touch: UITouch) {
        guard bounds.width > 0, bounds.height > 0 else { return }
        let location = touch.location(in: self)
        interactionPoint = CGPoint(
            x: min(max(location.x / bounds.width, 0), 1),
            y: min(max(location.y / bounds.height, 0), 1)
        )
        onInteractionChanged?(interactionPoint)
        startAnimating()
        setNeedsDisplay()
    }

    private func endInteraction() {
        isInteracting = false
        onInteractionChanged?(nil)
    }

    override func draw(_ rect: CGRect) {
        guard let context = UIGraphicsGetCurrentContext() else { return }

        let combinedPath = CGMutablePath()
        let rowCount = 18
        let topInset: CGFloat = 8
        let bottomInset: CGFloat = 26
        let spacing = (rect.height - topInset - bottomInset) / CGFloat(rowCount - 1)
        let segments = 72
        let responsiveAmount = 0.12 + activity * 0.88
        let speed = 0.14 + activity * (0.65 + intensity * 0.90)
        let displacement = 1.2 + responsiveAmount * (2.5 + intensity * 8.5)
        let thicknessResponse = responsiveAmount * (2.0 + intensity * 8.0)
        let twoPi = CGFloat.pi * 2
        let touchX = interactionPoint.x * rect.width
        let touchY = interactionPoint.y * rect.height
        let horizontalReach = max(1, rect.width * 0.30)
        let verticalReach = max(1, spacing * 2.8)

        for row in 0..<rowCount {
            let centerY = topInset + CGFloat(row) * spacing
            let rowPosition = CGFloat(row) / CGFloat(rowCount - 1)
            let rowPhase = rowPosition * 2.15
            let baseThickness = 5.5 + 1.4 * sin(rowPosition * .pi * 3.2)
            var upper: [CGPoint] = []
            var lower: [CGPoint] = []

            for index in 0...segments {
                let progress = CGFloat(index) / CGFloat(segments)
                let x = progress * rect.width
                let travelingPhase = progress * twoPi * 0.92 - elapsedTime * speed
                let primaryWave = sin(travelingPhase + rowPhase)
                let secondaryWave = sin(
                    progress * twoPi * 1.75 + elapsedTime * speed * 0.52 - rowPhase * 0.58
                )
                let sharedFlow = primaryWave * 0.72 + secondaryWave * 0.28
                var wave = sharedFlow * displacement

                let widthPulse = 0.5 + 0.5 * sin(
                    progress * twoPi * 1.16 - elapsedTime * speed * 0.78 + rowPhase * 0.82
                )
                let softPulse = widthPulse * widthPulse * (3 - 2 * widthPulse)
                var thickness = baseThickness + softPulse * thicknessResponse

                let horizontalDistance = abs(x - touchX)
                let verticalDistance = abs(centerY + wave - touchY)
                let horizontalFalloff = exp(-pow(horizontalDistance / horizontalReach, 2) * 2.2)
                let verticalFalloff = max(0, 1 - verticalDistance / verticalReach)
                let stringInfluence = horizontalFalloff * verticalFalloff * interactionAmount
                let fingerPull = (touchY - centerY - wave) * stringInfluence * 0.18
                let travelingRipple = sin(
                    horizontalDistance * 0.075 - elapsedTime * (8.5 + intensity * 4.5)
                ) * exp(-horizontalDistance / horizontalReach)
                wave += fingerPull + travelingRipple * stringInfluence * (3.0 + intensity * 4.0)
                thickness += stringInfluence * (1.5 + intensity * 2.5)

                upper.append(CGPoint(x: x, y: centerY + wave - thickness / 2))
                lower.append(CGPoint(x: x, y: centerY + wave + thickness / 2))
            }

            let path = CGMutablePath()
            if let first = upper.first {
                path.move(to: first)
            }
            upper.dropFirst().forEach { path.addLine(to: $0) }
            lower.reversed().forEach { path.addLine(to: $0) }
            path.closeSubpath()
            combinedPath.addPath(path)
        }

        context.saveGState()
        context.addPath(combinedPath)
        context.clip()
        let colors = [
            MainViewController.Palette.coral.cgColor,
            MainViewController.Palette.pink.cgColor,
            MainViewController.Palette.violet.cgColor,
        ] as CFArray
        if let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(),
                                     colors: colors,
                                     locations: [0, 0.62, 1]) {
            context.drawLinearGradient(
                gradient,
                start: CGPoint(x: 0, y: rect.midY),
                end: CGPoint(x: rect.width, y: rect.midY),
                options: []
            )
        }
        context.restoreGState()
    }
}

private final class IntensitySliderView: UIControl {
    var value: Float = 0.55 {
        didSet {
            value = min(max(value, 0), 1)
            barsView.fraction = CGFloat(value)
            accessibilityValue = "\(Int((value * 100).rounded())) percent"
            setNeedsLayout()
        }
    }

    private let backingView = IntensityBackingView()
    private let barsView = IntensityBarsView()
    private let thumb = GlassCircleControl(symbolName: "arrow.left.and.right", diameter: 50)
    private let label = UILabel()
    private let feedbackGenerator = UISelectionFeedbackGenerator()
    private var lastFeedbackStep = 11

    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setup()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        let trackHeight: CGFloat = 50
        backingView.frame = bounds
        barsView.frame = CGRect(x: 4, y: 4, width: bounds.width - 8, height: trackHeight - 8)

        let thumbSize: CGFloat = 50
        let travel = max(0, bounds.width - thumbSize)
        thumb.frame = CGRect(x: CGFloat(value) * travel, y: 0, width: thumbSize, height: thumbSize)
        label.frame = CGRect(x: 0, y: 57, width: bounds.width, height: 20)
    }

    override func beginTracking(_ touch: UITouch, with event: UIEvent?) -> Bool {
        feedbackGenerator.prepare()
        updateValue(for: touch.location(in: self).x)
        return true
    }

    override func continueTracking(_ touch: UITouch, with event: UIEvent?) -> Bool {
        updateValue(for: touch.location(in: self).x)
        return true
    }

    override func accessibilityIncrement() {
        if setValue(value + 0.05, emitsFeedback: true) {
            sendActions(for: .valueChanged)
        }
    }

    override func accessibilityDecrement() {
        if setValue(value - 0.05, emitsFeedback: true) {
            sendActions(for: .valueChanged)
        }
    }

    @discardableResult
    func setValue(_ newValue: Float, emitsFeedback: Bool) -> Bool {
        let clampedValue = min(max(newValue, 0), 1)
        guard abs(clampedValue - value) > 0.0001 else { return false }
        value = clampedValue
        if emitsFeedback {
            emitFeedbackIfNeeded()
        }
        return true
    }

    private func setup() {
        isAccessibilityElement = true
        accessibilityTraits = .adjustable
        accessibilityLabel = "Intensity"

        backingView.isUserInteractionEnabled = false
        barsView.isUserInteractionEnabled = false
        thumb.isUserInteractionEnabled = false

        label.text = "Intensity"
        label.textAlignment = .center
        label.textColor = UIColor.white.withAlphaComponent(0.22)
        label.font = .systemFont(ofSize: 14, weight: .regular)

        addSubview(backingView)
        addSubview(barsView)
        addSubview(thumb)
        addSubview(label)
        value = 0.55
    }

    private func updateValue(for x: CGFloat) {
        let thumbSize: CGFloat = 50
        let travel = max(1, bounds.width - thumbSize)
        _ = setValue(Float((x - thumbSize / 2) / travel), emitsFeedback: true)
        sendActions(for: .valueChanged)
    }

    private func emitFeedbackIfNeeded() {
        let feedbackStep = Int((value * 20).rounded())
        if feedbackStep != lastFeedbackStep {
            lastFeedbackStep = feedbackStep
            feedbackGenerator.selectionChanged()
            feedbackGenerator.prepare()
        }
    }
}

private final class IntensityBackingView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .clear
        isOpaque = false
        contentMode = .redraw
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        backgroundColor = .clear
        isOpaque = false
        contentMode = .redraw
    }

    override func draw(_ rect: CGRect) {
        guard rect.width > 0, rect.height > 0 else { return }

        let trackHeight = min(CGFloat(50), rect.height)
        let trackRadius = trackHeight / 2
        let tabBottom = rect.height
        let tabTopHalfWidth = min(CGFloat(68), rect.width * 0.19)
        let tabBottomHalfWidth = min(CGFloat(40), rect.width * 0.12)

        let path = UIBezierPath()
        path.move(to: CGPoint(x: trackRadius, y: 0))
        path.addLine(to: CGPoint(x: rect.width - trackRadius, y: 0))
        path.addArc(
            withCenter: CGPoint(x: rect.width - trackRadius, y: trackRadius),
            radius: trackRadius,
            startAngle: -.pi / 2,
            endAngle: .pi / 2,
            clockwise: true
        )
        path.addLine(to: CGPoint(x: rect.midX + tabTopHalfWidth, y: trackHeight))
        path.addCurve(
            to: CGPoint(x: rect.midX + tabBottomHalfWidth, y: tabBottom),
            controlPoint1: CGPoint(x: rect.midX + tabTopHalfWidth - 12, y: trackHeight),
            controlPoint2: CGPoint(x: rect.midX + tabBottomHalfWidth + 16, y: tabBottom)
        )
        path.addLine(to: CGPoint(x: rect.midX - tabBottomHalfWidth, y: tabBottom))
        path.addCurve(
            to: CGPoint(x: rect.midX - tabTopHalfWidth, y: trackHeight),
            controlPoint1: CGPoint(x: rect.midX - tabBottomHalfWidth - 16, y: tabBottom),
            controlPoint2: CGPoint(x: rect.midX - tabTopHalfWidth + 12, y: trackHeight)
        )
        path.addLine(to: CGPoint(x: trackRadius, y: trackHeight))
        path.addArc(
            withCenter: CGPoint(x: trackRadius, y: trackRadius),
            radius: trackRadius,
            startAngle: .pi / 2,
            endAngle: .pi * 3 / 2,
            clockwise: true
        )
        path.close()

        MainViewController.Palette.darkSurface.setFill()
        path.fill()
        UIColor.white.withAlphaComponent(0.05).setStroke()
        path.lineWidth = 1
        path.stroke()
    }
}

private final class IntensityBarsView: UIView {
    var fraction: CGFloat = 0.55 {
        didSet {
            setNeedsDisplay()
        }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        isOpaque = false
        contentMode = .redraw
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        isOpaque = false
        contentMode = .redraw
    }

    override func draw(_ rect: CGRect) {
        guard let context = UIGraphicsGetCurrentContext() else { return }
        let barCount = 49
        let gap: CGFloat = 2.4
        let barWidth = max(2.5, (rect.width - CGFloat(barCount - 1) * gap) / CGFloat(barCount))
        let path = CGMutablePath()

        for index in 0..<barCount {
            let edgeDistance = min(index, barCount - 1 - index)
            let scale = min(1, 0.50 + CGFloat(edgeDistance) * 0.16)
            let height = rect.height * scale
            let x = CGFloat(index) * (barWidth + gap)
            let barRect = CGRect(x: x, y: (rect.height - height) / 2, width: barWidth, height: height)
            path.addPath(UIBezierPath(roundedRect: barRect, cornerRadius: barWidth / 2).cgPath)
        }

        context.addPath(path)
        context.setFillColor(UIColor.white.withAlphaComponent(0.09).cgColor)
        context.fillPath()

        context.saveGState()
        context.clip(to: CGRect(x: 0, y: 0, width: rect.width * fraction, height: rect.height))
        context.addPath(path)
        context.clip()
        let colors = [
            MainViewController.Palette.coral.cgColor,
            MainViewController.Palette.pink.cgColor,
        ] as CFArray
        if let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(),
                                     colors: colors,
                                     locations: [0, 1]) {
            context.drawLinearGradient(
                gradient,
                start: CGPoint(x: 0, y: rect.midY),
                end: CGPoint(x: rect.width, y: rect.midY),
                options: []
            )
        }
        context.restoreGState()
    }
}

private final class PairingInstructionsViewController: UIViewController {
    var onClose: (() -> Void)?

    override func viewDidLoad() {
        super.viewDidLoad()
        overrideUserInterfaceStyle = .dark
        view.backgroundColor = UIColor(red: 0.045, green: 0.035, blue: 0.055, alpha: 1)
        buildInterface()
    }

    private func buildInterface() {
        let titleLabel = UILabel()
        titleLabel.text = "Connect a controller"
        titleLabel.font = .systemFont(ofSize: 28, weight: .bold)
        titleLabel.textColor = .white

        let subtitleLabel = UILabel()
        subtitleLabel.text = "Pair it in iPhone Settings, then return to ZOONZOON."
        subtitleLabel.font = .systemFont(ofSize: 15, weight: .medium)
        subtitleLabel.textColor = MainViewController.Palette.secondaryText
        subtitleLabel.numberOfLines = 0

        let steps = UIStackView(arrangedSubviews: [
            makeStep(number: "1", text: "Turn on pairing mode on the controller."),
            makeStep(number: "2", text: "Open Settings → Bluetooth and select it."),
            makeStep(number: "3", text: "Return here. Connection updates automatically."),
        ])
        steps.axis = .vertical
        steps.spacing = 18

        var configuration = UIButton.Configuration.filled()
        configuration.title = "Got it"
        configuration.baseBackgroundColor = MainViewController.Palette.pink
        configuration.baseForegroundColor = .white
        configuration.cornerStyle = .capsule
        let closeButton = UIButton(configuration: configuration)
        closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)

        let stack = UIStackView(arrangedSubviews: [titleLabel, subtitleLabel, steps, closeButton])
        stack.axis = .vertical
        stack.spacing = 18
        stack.setCustomSpacing(10, after: titleLabel)
        stack.setCustomSpacing(28, after: subtitleLabel)
        stack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stack)

        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 22),
            stack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            stack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24),
            stack.bottomAnchor.constraint(lessThanOrEqualTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -14),
            closeButton.heightAnchor.constraint(equalToConstant: 52),
        ])
    }

    private func makeStep(number: String, text: String) -> UIView {
        let badge = UILabel()
        badge.text = number
        badge.textAlignment = .center
        badge.font = .systemFont(ofSize: 14, weight: .bold)
        badge.textColor = .white
        badge.backgroundColor = MainViewController.Palette.pink
        badge.layer.cornerRadius = 15
        badge.clipsToBounds = true
        badge.translatesAutoresizingMaskIntoConstraints = false

        let label = UILabel()
        label.text = text
        label.font = .systemFont(ofSize: 15, weight: .medium)
        label.textColor = .white
        label.numberOfLines = 0

        let row = UIStackView(arrangedSubviews: [badge, label])
        row.axis = .horizontal
        row.alignment = .center
        row.spacing = 14

        NSLayoutConstraint.activate([
            badge.widthAnchor.constraint(equalToConstant: 30),
            badge.heightAnchor.constraint(equalTo: badge.widthAnchor),
        ])
        return row
    }

    @objc private func close() {
        onClose?()
    }
}
