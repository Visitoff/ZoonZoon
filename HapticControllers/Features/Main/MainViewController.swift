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

    required init?(coder: NSCoder) {
        manager = HapticsManager()
        super.init(coder: coder)
    }

    deinit {
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
        connectionButton.setControllerState(
            connected: isConnected,
            controllerName: manager.connectedController?.productCategory
        )
        playerCard.update(playbackState: manager.playbackState)
        UIApplication.shared.isIdleTimerDisabled = manager.playbackState == .playing
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

    func setControllerState(connected: Bool, controllerName: String?) {
        imageView.image = controllerGlyphImage(connected: connected)
        imageView.tintColor = nil
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

    private func controllerGlyphImage(connected: Bool) -> UIImage {
        let size = CGSize(width: 56, height: 38)
        let renderer = UIGraphicsImageRenderer(size: size)
        return renderer.image { context in
            let path = UIBezierPath()
            path.move(to: CGPoint(x: 11, y: 6))
            path.addCurve(to: CGPoint(x: 21, y: 3),
                          controlPoint1: CGPoint(x: 13, y: 3),
                          controlPoint2: CGPoint(x: 17, y: 3))
            path.addLine(to: CGPoint(x: 35, y: 3))
            path.addCurve(to: CGPoint(x: 45, y: 6),
                          controlPoint1: CGPoint(x: 39, y: 3),
                          controlPoint2: CGPoint(x: 43, y: 3))
            path.addCurve(to: CGPoint(x: 54, y: 29),
                          controlPoint1: CGPoint(x: 51, y: 11),
                          controlPoint2: CGPoint(x: 56, y: 23))
            path.addCurve(to: CGPoint(x: 46, y: 34),
                          controlPoint1: CGPoint(x: 52, y: 34),
                          controlPoint2: CGPoint(x: 49, y: 35))
            path.addLine(to: CGPoint(x: 37, y: 25))
            path.addCurve(to: CGPoint(x: 19, y: 25),
                          controlPoint1: CGPoint(x: 32, y: 27),
                          controlPoint2: CGPoint(x: 24, y: 27))
            path.addLine(to: CGPoint(x: 10, y: 34))
            path.addCurve(to: CGPoint(x: 2, y: 29),
                          controlPoint1: CGPoint(x: 7, y: 35),
                          controlPoint2: CGPoint(x: 4, y: 34))
            path.addCurve(to: CGPoint(x: 11, y: 6),
                          controlPoint1: CGPoint(x: 0, y: 23),
                          controlPoint2: CGPoint(x: 5, y: 11))
            path.close()

            UIColor.white.withAlphaComponent(connected ? 1 : 0.58).setFill()
            path.fill()

            let detailsColor = UIColor.black.withAlphaComponent(0.80)
            detailsColor.setFill()
            UIBezierPath(ovalIn: CGRect(x: 17, y: 20, width: 7, height: 7)).fill()
            UIBezierPath(ovalIn: CGRect(x: 32, y: 20, width: 7, height: 7)).fill()

            let dPad = UIBezierPath()
            dPad.append(UIBezierPath(roundedRect: CGRect(x: 11, y: 10, width: 11, height: 4), cornerRadius: 1.5))
            dPad.append(UIBezierPath(roundedRect: CGRect(x: 14.5, y: 6.5, width: 4, height: 11), cornerRadius: 1.5))
            dPad.fill()

            for point in [CGPoint(x: 43, y: 8), CGPoint(x: 48, y: 12), CGPoint(x: 38, y: 12), CGPoint(x: 43, y: 16)] {
                UIBezierPath(ovalIn: CGRect(x: point.x - 2, y: point.y - 2, width: 4, height: 4)).fill()
            }

            context.cgContext.setStrokeColor(detailsColor.cgColor)
            context.cgContext.setLineWidth(2)
            context.cgContext.move(to: CGPoint(x: 25, y: 10))
            context.cgContext.addLine(to: CGPoint(x: 31, y: 10))
            context.cgContext.strokePath()
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
    }

    private func setup() {
        clipsToBounds = true
        backgroundColor = .black

        waveformView.isUserInteractionEnabled = false
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

        let combinedPath = CGMutablePath()
        let rowCount = 20
        let topInset: CGFloat = 10
        let bottomInset: CGFloat = 38
        let spacing = (rect.height - topInset - bottomInset) / CGFloat(rowCount - 1)
        let segments = 28

        for row in 0..<rowCount {
            let centerY = topInset + CGFloat(row) * spacing
            let amplitude = 3.0 + CGFloat((row * 7) % 12)
            let thickness = 5.0 + CGFloat((row * 5) % 6)
            let frequency = 1.05 + CGFloat(row % 4) * 0.18
            let phase = CGFloat(row) * 0.72
            var upper: [CGPoint] = []
            var lower: [CGPoint] = []

            for index in 0...segments {
                let progress = CGFloat(index) / CGFloat(segments)
                let x = progress * rect.width
                let angle = Double(progress * .pi * 2 * frequency + phase)
                let secondary = Double(progress * .pi * 4 + phase * 0.37)
                let wave = CGFloat(sin(angle)) * amplitude + CGFloat(sin(secondary)) * amplitude * 0.34
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

    private let trackView = UIView()
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
        trackView.frame = CGRect(x: 0, y: 0, width: bounds.width, height: trackHeight)
        trackView.layer.cornerRadius = trackHeight / 2
        barsView.frame = trackView.bounds.insetBy(dx: 4, dy: 4)

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
        value += 0.05
        sendActions(for: .valueChanged)
    }

    override func accessibilityDecrement() {
        value -= 0.05
        sendActions(for: .valueChanged)
    }

    private func setup() {
        isAccessibilityElement = true
        accessibilityTraits = .adjustable
        accessibilityLabel = "Intensity"

        trackView.backgroundColor = MainViewController.Palette.darkSurface
        trackView.clipsToBounds = true
        trackView.layer.borderWidth = 1
        trackView.layer.borderColor = UIColor.white.withAlphaComponent(0.05).cgColor
        trackView.isUserInteractionEnabled = false

        barsView.isUserInteractionEnabled = false
        thumb.isUserInteractionEnabled = false

        label.text = "Intensity"
        label.textAlignment = .center
        label.textColor = UIColor.white.withAlphaComponent(0.22)
        label.font = .systemFont(ofSize: 14, weight: .regular)

        addSubview(trackView)
        trackView.addSubview(barsView)
        addSubview(thumb)
        addSubview(label)
        value = 0.55
    }

    private func updateValue(for x: CGFloat) {
        let thumbSize: CGFloat = 50
        let travel = max(1, bounds.width - thumbSize)
        value = Float((x - thumbSize / 2) / travel)
        let feedbackStep = Int((value * 20).rounded())
        if feedbackStep != lastFeedbackStep {
            lastFeedbackStep = feedbackStep
            feedbackGenerator.selectionChanged()
            feedbackGenerator.prepare()
        }
        sendActions(for: .valueChanged)
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
