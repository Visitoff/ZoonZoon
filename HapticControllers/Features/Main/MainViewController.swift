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

        if let sheet = instructionsViewController.sheetPresentationController {
            sheet.detents = [.medium(), .large()]
            sheet.selectedDetentIdentifier = .medium
            sheet.prefersGrabberVisible = true
            sheet.preferredCornerRadius = 30
            sheet.prefersScrollingExpandsWhenScrolledToEdge = false
        }

        present(instructionsViewController, animated: true)
    }

    @objc private func applicationWillLeaveForeground() {
        UIApplication.shared.isIdleTimerDisabled = false
        leftTriggerValue = 0
        rightTriggerValue = 0
        manager.stopHaptics()
    }

    @objc private func applicationDidBecomeActive() {
        manager.refreshConnectedController()
        updateInterface()
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

private enum PairingControllerKind: CaseIterable {
    case playStation5
    case playStation4
    case xbox
    case other

    var optionTitle: String {
        switch self {
        case .playStation5: "PS5"
        case .playStation4: "PS4"
        case .xbox: "Xbox"
        case .other: "Other"
        }
    }

    var detailTitle: String {
        switch self {
        case .playStation5: "DualSense controller"
        case .playStation4: "DUALSHOCK 4 controller"
        case .xbox: "Xbox Wireless Controller"
        case .other: "Bluetooth controller"
        }
    }

    var assetName: String {
        switch self {
        case .playStation5: "controller-dualsense"
        case .playStation4: "controller-dualshock4"
        case .xbox: "controller-xbox"
        case .other: "controller-generic"
        }
    }

    var pairingButtons: String {
        switch self {
        case .playStation5: "PS + Create"
        case .playStation4: "PS + SHARE"
        case .xbox: "Xbox + Pair"
        case .other: "Pairing mode"
        }
    }

    var firstInstruction: String {
        switch self {
        case .playStation5:
            "Hold the PS and Create buttons until the light bar flashes blue."
        case .playStation4:
            "Hold the PS and SHARE buttons until the light bar starts flashing."
        case .xbox:
            "Press the Xbox button, then hold Pair until the Xbox button flashes quickly."
        case .other:
            "Put the controller into Bluetooth pairing mode. Check its manual for the correct button."
        }
    }

    var secondInstruction: String {
        switch self {
        case .playStation5:
            "Open Settings → Bluetooth and select “DualSense Wireless Controller”."
        case .playStation4:
            "Open Settings → Bluetooth and select “DUALSHOCK 4 Wireless Controller”."
        case .xbox:
            "Open Settings → Bluetooth and select “Xbox Wireless Controller”."
        case .other:
            "Open Settings → Bluetooth and select the controller when it appears."
        }
    }

    var hotspotPositions: [CGPoint] {
        switch self {
        case .playStation5:
            [CGPoint(x: 0.276, y: 0.174), CGPoint(x: 0.50, y: 0.522)]
        case .playStation4:
            [CGPoint(x: 0.306, y: 0.163), CGPoint(x: 0.50, y: 0.553)]
        case .xbox:
            [CGPoint(x: 0.57, y: 0.085)]
        case .other:
            []
        }
    }
}

private final class PairingInstructionsViewController: UIViewController {
    private let backgroundGradient = CAGradientLayer()
    private let contentStack = UIStackView()
    private let detailView = ControllerPairingDetailView()
    private let selectionFeedback = UISelectionFeedbackGenerator()
    private var optionControls: [ControllerOptionControl] = []
    private var selectedKind: PairingControllerKind?

    override func viewDidLoad() {
        super.viewDidLoad()
        overrideUserInterfaceStyle = .dark
        view.backgroundColor = UIColor(red: 0.035, green: 0.025, blue: 0.045, alpha: 1)
        configureBackground()
        buildInterface()
        selectionFeedback.prepare()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        backgroundGradient.frame = view.bounds
    }

    private func configureBackground() {
        backgroundGradient.colors = [
            UIColor(red: 0.10, green: 0.035, blue: 0.085, alpha: 1).cgColor,
            UIColor(red: 0.035, green: 0.025, blue: 0.045, alpha: 1).cgColor,
            UIColor.black.cgColor,
        ]
        backgroundGradient.locations = [0, 0.48, 1]
        backgroundGradient.startPoint = CGPoint(x: 0.15, y: 0)
        backgroundGradient.endPoint = CGPoint(x: 0.85, y: 1)
        view.layer.insertSublayer(backgroundGradient, at: 0)
    }

    private func buildInterface() {
        let titleLabel = UILabel()
        titleLabel.text = "Connect a controller"
        titleLabel.font = .systemFont(ofSize: 28, weight: .bold)
        titleLabel.textColor = .white
        titleLabel.adjustsFontForContentSizeCategory = true

        let subtitleLabel = UILabel()
        subtitleLabel.text = "Choose your controller to feel the full power of endless vibration."
        subtitleLabel.font = .systemFont(ofSize: 15, weight: .medium)
        subtitleLabel.textColor = MainViewController.Palette.secondaryText
        subtitleLabel.numberOfLines = 0
        subtitleLabel.adjustsFontForContentSizeCategory = true

        let titleStack = UIStackView(arrangedSubviews: [titleLabel, subtitleLabel])
        titleStack.axis = .vertical
        titleStack.spacing = 6

        optionControls = PairingControllerKind.allCases.map { kind in
            let control = ControllerOptionControl(kind: kind)
            control.addTarget(self, action: #selector(optionSelected(_:)), for: .touchUpInside)
            return control
        }
        let options = UIStackView(arrangedSubviews: optionControls)
        options.axis = .horizontal
        options.alignment = .fill
        options.distribution = .fillEqually
        options.spacing = 8

        detailView.isHidden = true

        contentStack.axis = .vertical
        contentStack.spacing = 20
        contentStack.addArrangedSubview(titleStack)
        contentStack.addArrangedSubview(options)
        contentStack.addArrangedSubview(detailView)
        contentStack.setCustomSpacing(24, after: options)
        contentStack.translatesAutoresizingMaskIntoConstraints = false

        let scrollView = UIScrollView()
        scrollView.alwaysBounceVertical = true
        scrollView.showsVerticalScrollIndicator = false
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scrollView)
        scrollView.addSubview(contentStack)

        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 10),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            contentStack.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor, constant: 12),
            contentStack.leadingAnchor.constraint(equalTo: scrollView.frameLayoutGuide.leadingAnchor, constant: 20),
            contentStack.trailingAnchor.constraint(equalTo: scrollView.frameLayoutGuide.trailingAnchor, constant: -20),
            contentStack.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor, constant: -28),

            options.heightAnchor.constraint(equalToConstant: 94),
        ])
    }

    @objc private func optionSelected(_ sender: ControllerOptionControl) {
        guard selectedKind != sender.kind else { return }
        selectedKind = sender.kind
        optionControls.forEach { $0.isSelected = $0 === sender }
        detailView.configure(for: sender.kind)
        selectionFeedback.selectionChanged()
        selectionFeedback.prepare()

        if detailView.isHidden {
            detailView.alpha = 0
            detailView.transform = CGAffineTransform(translationX: 0, y: 16)
            detailView.isHidden = false
            UIView.animate(withDuration: 0.28,
                           delay: 0,
                           usingSpringWithDamping: 0.84,
                           initialSpringVelocity: 0.25) {
                self.detailView.alpha = 1
                self.detailView.transform = .identity
                self.view.layoutIfNeeded()
            }
        }

        sheetPresentationController?.animateChanges {
            self.sheetPresentationController?.selectedDetentIdentifier = .large
        }
        UIAccessibility.post(notification: .layoutChanged, argument: detailView.accessibilityFocusView)
    }
}

private final class ControllerOptionControl: UIControl {
    let kind: PairingControllerKind

    private let effectView = UIVisualEffectView()
    private let imageView = UIImageView()
    private let titleLabel = UILabel()

    override var isSelected: Bool {
        didSet { updateAppearance() }
    }

    override var isHighlighted: Bool {
        didSet {
            UIView.animate(withDuration: 0.12) {
                self.transform = self.isHighlighted
                    ? CGAffineTransform(scaleX: 0.96, y: 0.96)
                    : .identity
            }
        }
    }

    init(kind: PairingControllerKind) {
        self.kind = kind
        super.init(frame: .zero)
        setup()
    }

    required init?(coder: NSCoder) {
        kind = .other
        super.init(coder: coder)
        setup()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        layer.cornerRadius = 20
        effectView.layer.cornerRadius = 20
    }

    private func setup() {
        isAccessibilityElement = true
        accessibilityLabel = kind.detailTitle
        accessibilityHint = "Shows pairing instructions"
        clipsToBounds = false
        layer.borderWidth = 1

        effectView.isUserInteractionEnabled = false
        effectView.clipsToBounds = true
        effectView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(effectView)

        imageView.image = UIImage(named: kind.assetName)
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(imageView)

        titleLabel.text = kind.optionTitle
        titleLabel.textAlignment = .center
        titleLabel.font = .systemFont(ofSize: 12, weight: .semibold)
        titleLabel.textColor = .white
        titleLabel.adjustsFontSizeToFitWidth = true
        titleLabel.minimumScaleFactor = 0.78
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        addSubview(titleLabel)

        NSLayoutConstraint.activate([
            effectView.topAnchor.constraint(equalTo: topAnchor),
            effectView.leadingAnchor.constraint(equalTo: leadingAnchor),
            effectView.trailingAnchor.constraint(equalTo: trailingAnchor),
            effectView.bottomAnchor.constraint(equalTo: bottomAnchor),

            imageView.topAnchor.constraint(equalTo: topAnchor, constant: 13),
            imageView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 8),
            imageView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -8),
            imageView.heightAnchor.constraint(equalToConstant: 45),

            titleLabel.topAnchor.constraint(equalTo: imageView.bottomAnchor, constant: 7),
            titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 4),
            titleLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -4),
            titleLabel.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor, constant: -8),
        ])
        updateAppearance()
    }

    private func updateAppearance() {
        let tint = isSelected
            ? MainViewController.Palette.pink.withAlphaComponent(0.24)
            : UIColor.white.withAlphaComponent(0.04)
        if #available(iOS 26.0, *) {
            let effect = UIGlassEffect(style: .regular)
            effect.isInteractive = true
            effect.tintColor = tint
            effectView.effect = effect
            effectView.contentView.backgroundColor = .clear
        } else {
            effectView.effect = UIBlurEffect(style: .systemUltraThinMaterialDark)
            effectView.contentView.backgroundColor = tint
        }
        layer.borderColor = (isSelected
            ? MainViewController.Palette.pink.withAlphaComponent(0.90)
            : MainViewController.Palette.border).cgColor
        layer.shadowColor = MainViewController.Palette.pink.cgColor
        layer.shadowOpacity = isSelected ? 0.22 : 0
        layer.shadowRadius = 14
        layer.shadowOffset = .zero
        accessibilityTraits = isSelected ? [.button, .selected] : .button
    }
}

private final class ControllerPairingDetailView: UIView {
    private let effectView = UIVisualEffectView()
    private let titleLabel = UILabel()
    private let illustrationView = ControllerPairingIllustrationView()
    private let pairingButtonsLabel = UILabel()
    private let firstStepLabel = UILabel()
    private let secondStepLabel = UILabel()

    var accessibilityFocusView: UIView { titleLabel }

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
        layer.cornerRadius = 28
        effectView.layer.cornerRadius = 28
    }

    func configure(for kind: PairingControllerKind) {
        titleLabel.text = "Put your \(kind.detailTitle) in pairing mode"
        pairingButtonsLabel.text = kind.pairingButtons
        firstStepLabel.text = kind.firstInstruction
        secondStepLabel.text = kind.secondInstruction
        illustrationView.configure(imageName: kind.assetName, hotspots: kind.hotspotPositions)
        accessibilityLabel = [titleLabel.text, kind.pairingButtons,
                              kind.firstInstruction, kind.secondInstruction]
            .compactMap { $0 }
            .joined(separator: ". ")
    }

    private func setup() {
        isAccessibilityElement = false
        clipsToBounds = true
        layer.borderWidth = 1
        layer.borderColor = MainViewController.Palette.border.cgColor

        if #available(iOS 26.0, *) {
            let effect = UIGlassEffect(style: .regular)
            effect.tintColor = MainViewController.Palette.violet.withAlphaComponent(0.12)
            effectView.effect = effect
        } else {
            effectView.effect = UIBlurEffect(style: .systemUltraThinMaterialDark)
            effectView.contentView.backgroundColor = UIColor.white.withAlphaComponent(0.035)
        }
        effectView.clipsToBounds = true
        effectView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(effectView)

        titleLabel.font = .systemFont(ofSize: 20, weight: .bold)
        titleLabel.textColor = .white
        titleLabel.textAlignment = .center
        titleLabel.numberOfLines = 0
        titleLabel.adjustsFontForContentSizeCategory = true

        illustrationView.translatesAutoresizingMaskIntoConstraints = false

        pairingButtonsLabel.font = .monospacedSystemFont(ofSize: 15, weight: .bold)
        pairingButtonsLabel.textColor = .white
        pairingButtonsLabel.textAlignment = .center
        pairingButtonsLabel.backgroundColor = MainViewController.Palette.pink.withAlphaComponent(0.20)
        pairingButtonsLabel.layer.borderWidth = 1
        pairingButtonsLabel.layer.borderColor = MainViewController.Palette.pink.withAlphaComponent(0.72).cgColor
        pairingButtonsLabel.layer.cornerRadius = 17
        pairingButtonsLabel.clipsToBounds = true
        pairingButtonsLabel.translatesAutoresizingMaskIntoConstraints = false

        let buttonsContainer = UIView()
        buttonsContainer.addSubview(pairingButtonsLabel)

        configureInstructionLabel(firstStepLabel)
        configureInstructionLabel(secondStepLabel)
        let steps = UIStackView(arrangedSubviews: [
            makeStep(number: "1", label: firstStepLabel),
            makeStep(number: "2", label: secondStepLabel),
        ])
        steps.axis = .vertical
        steps.spacing = 14

        let statusDot = UIView()
        statusDot.backgroundColor = MainViewController.Palette.connected
        statusDot.layer.cornerRadius = 4
        statusDot.translatesAutoresizingMaskIntoConstraints = false

        let statusLabel = UILabel()
        statusLabel.text = "Connection is detected automatically when you return."
        statusLabel.font = .systemFont(ofSize: 13, weight: .medium)
        statusLabel.textColor = MainViewController.Palette.secondaryText
        statusLabel.numberOfLines = 0

        let status = UIStackView(arrangedSubviews: [statusDot, statusLabel])
        status.axis = .horizontal
        status.alignment = .center
        status.spacing = 10

        let stack = UIStackView(arrangedSubviews: [
            titleLabel,
            illustrationView,
            buttonsContainer,
            steps,
            status,
        ])
        stack.axis = .vertical
        stack.spacing = 16
        stack.setCustomSpacing(8, after: illustrationView)
        stack.setCustomSpacing(22, after: buttonsContainer)
        stack.translatesAutoresizingMaskIntoConstraints = false
        effectView.contentView.addSubview(stack)

        NSLayoutConstraint.activate([
            effectView.topAnchor.constraint(equalTo: topAnchor),
            effectView.leadingAnchor.constraint(equalTo: leadingAnchor),
            effectView.trailingAnchor.constraint(equalTo: trailingAnchor),
            effectView.bottomAnchor.constraint(equalTo: bottomAnchor),

            stack.topAnchor.constraint(equalTo: effectView.contentView.topAnchor, constant: 20),
            stack.leadingAnchor.constraint(equalTo: effectView.contentView.leadingAnchor, constant: 18),
            stack.trailingAnchor.constraint(equalTo: effectView.contentView.trailingAnchor, constant: -18),
            stack.bottomAnchor.constraint(equalTo: effectView.contentView.bottomAnchor, constant: -20),

            illustrationView.heightAnchor.constraint(equalToConstant: 142),
            buttonsContainer.heightAnchor.constraint(equalToConstant: 34),
            pairingButtonsLabel.centerXAnchor.constraint(equalTo: buttonsContainer.centerXAnchor),
            pairingButtonsLabel.topAnchor.constraint(equalTo: buttonsContainer.topAnchor),
            pairingButtonsLabel.bottomAnchor.constraint(equalTo: buttonsContainer.bottomAnchor),
            pairingButtonsLabel.widthAnchor.constraint(greaterThanOrEqualToConstant: 132),

            statusDot.widthAnchor.constraint(equalToConstant: 8),
            statusDot.heightAnchor.constraint(equalTo: statusDot.widthAnchor),
        ])
    }

    private func configureInstructionLabel(_ label: UILabel) {
        label.font = .systemFont(ofSize: 15, weight: .medium)
        label.textColor = .white
        label.numberOfLines = 0
        label.adjustsFontForContentSizeCategory = true
    }

    private func makeStep(number: String, label: UILabel) -> UIView {
        let badge = UILabel()
        badge.text = number
        badge.textAlignment = .center
        badge.font = .systemFont(ofSize: 13, weight: .bold)
        badge.textColor = .white
        badge.backgroundColor = MainViewController.Palette.pink
        badge.layer.cornerRadius = 14
        badge.clipsToBounds = true
        badge.translatesAutoresizingMaskIntoConstraints = false

        let row = UIStackView(arrangedSubviews: [badge, label])
        row.axis = .horizontal
        row.alignment = .center
        row.spacing = 12

        NSLayoutConstraint.activate([
            badge.widthAnchor.constraint(equalToConstant: 28),
            badge.heightAnchor.constraint(equalTo: badge.widthAnchor),
        ])
        return row
    }
}

private final class ControllerPairingIllustrationView: UIView {
    private let imageView = UIImageView()
    private var hotspotViews: [PairingHotspotView] = []
    private var hotspotPositions: [CGPoint] = []

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
        guard let image = imageView.image, image.size.width > 0, image.size.height > 0 else { return }

        let availableBounds = bounds.insetBy(dx: 22, dy: 5)
        let scale = min(availableBounds.width / image.size.width,
                        availableBounds.height / image.size.height)
        let imageSize = CGSize(width: image.size.width * scale, height: image.size.height * scale)
        let imageFrame = CGRect(
            x: bounds.midX - imageSize.width / 2,
            y: bounds.midY - imageSize.height / 2,
            width: imageSize.width,
            height: imageSize.height
        )
        imageView.frame = imageFrame

        for (index, hotspot) in hotspotViews.enumerated() where index < hotspotPositions.count {
            let position = hotspotPositions[index]
            hotspot.bounds = CGRect(x: 0, y: 0, width: 34, height: 34)
            hotspot.center = CGPoint(
                x: imageFrame.minX + imageFrame.width * position.x,
                y: imageFrame.minY + imageFrame.height * position.y
            )
        }
    }

    func configure(imageName: String, hotspots: [CGPoint]) {
        imageView.image = UIImage(named: imageName)
        hotspotViews.forEach { $0.removeFromSuperview() }
        hotspotPositions = hotspots
        hotspotViews = hotspots.map { _ in
            let hotspot = PairingHotspotView()
            addSubview(hotspot)
            return hotspot
        }

        UIView.performWithoutAnimation {
            setNeedsLayout()
            layoutIfNeeded()
        }

        hotspotViews.forEach {
            $0.alpha = 0
            $0.transform = CGAffineTransform(scaleX: 0.78, y: 0.78)
        }
        UIView.animate(withDuration: 0.22,
                       delay: 0,
                       options: [.curveEaseOut, .beginFromCurrentState, .allowUserInteraction]) {
            self.hotspotViews.forEach {
                $0.alpha = 1
                $0.transform = .identity
            }
        }
    }

    private func setup() {
        isAccessibilityElement = false
        imageView.contentMode = .scaleAspectFit
        imageView.layer.shadowColor = MainViewController.Palette.pink.cgColor
        imageView.layer.shadowOpacity = 0.25
        imageView.layer.shadowRadius = 18
        imageView.layer.shadowOffset = .zero
        addSubview(imageView)
    }
}

private final class PairingHotspotView: UIView {
    private let ring = UIView()
    private let dot = UIView()

    override init(frame: CGRect) {
        super.init(frame: frame)
        isUserInteractionEnabled = false
        ring.layer.borderWidth = 2
        ring.layer.borderColor = MainViewController.Palette.coral.cgColor
        ring.backgroundColor = MainViewController.Palette.pink.withAlphaComponent(0.14)
        addSubview(ring)

        dot.backgroundColor = MainViewController.Palette.pink
        dot.layer.shadowColor = MainViewController.Palette.pink.cgColor
        dot.layer.shadowOpacity = 0.9
        dot.layer.shadowRadius = 8
        dot.layer.shadowOffset = .zero
        addSubview(dot)
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        ring.frame = bounds.insetBy(dx: 2, dy: 2)
        ring.layer.cornerRadius = ring.bounds.width / 2
        dot.bounds = CGRect(x: 0, y: 0, width: 10, height: 10)
        dot.center = CGPoint(x: bounds.midX, y: bounds.midY)
        dot.layer.cornerRadius = 5
    }

    override func didMoveToWindow() {
        super.didMoveToWindow()
        ring.layer.removeAllAnimations()
        guard window != nil else { return }

        let scale = CABasicAnimation(keyPath: "transform.scale")
        scale.fromValue = 0.72
        scale.toValue = 1.18

        let opacity = CABasicAnimation(keyPath: "opacity")
        opacity.fromValue = 1
        opacity.toValue = 0.28

        let animation = CAAnimationGroup()
        animation.animations = [scale, opacity]
        animation.duration = 1.05
        animation.timingFunction = CAMediaTimingFunction(name: .easeOut)
        animation.autoreverses = true
        animation.repeatCount = .infinity
        ring.layer.add(animation, forKey: "pairingPulse")
    }
}
