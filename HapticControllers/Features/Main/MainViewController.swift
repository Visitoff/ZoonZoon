import GameController
import UIKit

final class MainViewController: UIViewController {
    fileprivate enum Palette {
        static let backgroundTop = UIColor(red: 0.16, green: 0.09, blue: 0.29, alpha: 1.00)
        static let backgroundBottom = UIColor(red: 0.035, green: 0.025, blue: 0.10, alpha: 1.00)
        static let surface = UIColor(red: 0.10, green: 0.07, blue: 0.23, alpha: 1.00)
        static let accent = UIColor(red: 0.79, green: 0.29, blue: 1.00, alpha: 1.00)
        static let accentBlue = UIColor(red: 0.43, green: 0.36, blue: 1.00, alpha: 1.00)
        static let connected = UIColor(red: 0.27, green: 0.91, blue: 0.64, alpha: 1.00)
        static let primaryText = UIColor.white
        static let secondaryText = UIColor.white.withAlphaComponent(0.58)
        static let border = UIColor.white.withAlphaComponent(0.20)
    }

    private let manager: HapticsManager
    private let backgroundGradient = CAGradientLayer()
    private let topGlowGradient = CAGradientLayer()
    private let bottomGlowGradient = CAGradientLayer()
    private let contentView = UIView()
    private let connectionStatusView = ConnectionStatusView()
    private let hapticControl = HapticControlView()
    private let helperLabel = UILabel()
    private let instructionsCard = InstructionsCardView()

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
        view.backgroundColor = Palette.backgroundBottom

        backgroundGradient.colors = [
            Palette.backgroundTop.cgColor,
            UIColor(red: 0.07, green: 0.045, blue: 0.17, alpha: 1.00).cgColor,
            Palette.backgroundBottom.cgColor,
        ]
        backgroundGradient.locations = [0.0, 0.48, 1.0]
        backgroundGradient.startPoint = CGPoint(x: 0.18, y: 0.0)
        backgroundGradient.endPoint = CGPoint(x: 0.82, y: 1.0)
        view.layer.insertSublayer(backgroundGradient, at: 0)

        topGlowGradient.type = .radial
        topGlowGradient.colors = [
            Palette.accent.withAlphaComponent(0.22).cgColor,
            UIColor.clear.cgColor,
        ]
        topGlowGradient.locations = [0.0, 1.0]
        topGlowGradient.startPoint = CGPoint(x: 0.86, y: 0.09)
        topGlowGradient.endPoint = CGPoint(x: 0.20, y: 0.65)
        view.layer.insertSublayer(topGlowGradient, above: backgroundGradient)

        bottomGlowGradient.type = .radial
        bottomGlowGradient.colors = [
            Palette.accentBlue.withAlphaComponent(0.18).cgColor,
            UIColor.clear.cgColor,
        ]
        bottomGlowGradient.locations = [0.0, 1.0]
        bottomGlowGradient.startPoint = CGPoint(x: 0.82, y: 0.96)
        bottomGlowGradient.endPoint = CGPoint(x: 0.18, y: 0.43)
        view.layer.insertSublayer(bottomGlowGradient, above: topGlowGradient)
    }

    private func buildInterface() {
        contentView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(contentView)

        let brandLabel = UILabel()
        brandLabel.text = "ZOONZOON"
        brandLabel.font = .systemFont(ofSize: 35, weight: .bold)
        brandLabel.textColor = Palette.primaryText
        brandLabel.adjustsFontSizeToFitWidth = true
        brandLabel.minimumScaleFactor = 0.55
        brandLabel.numberOfLines = 1
        brandLabel.lineBreakMode = .byClipping
        brandLabel.translatesAutoresizingMaskIntoConstraints = false

        connectionStatusView.translatesAutoresizingMaskIntoConstraints = false
        connectionStatusView.accessibilityIdentifier = "connectionStatusCard"
        connectionStatusView.addTarget(self,
                                       action: #selector(connectionStatusTapped),
                                       for: .touchUpInside)

        hapticControl.translatesAutoresizingMaskIntoConstraints = false
        hapticControl.accessibilityIdentifier = "hapticControlButton"
        hapticControl.addTarget(self,
                                action: #selector(hapticControlTapped),
                                for: .touchUpInside)

        helperLabel.font = .systemFont(ofSize: 14, weight: .medium)
        helperLabel.textColor = Palette.secondaryText
        helperLabel.textAlignment = .center
        helperLabel.numberOfLines = 0
        helperLabel.isHidden = true
        helperLabel.accessibilityIdentifier = "activeVibrationNotice"
        helperLabel.translatesAutoresizingMaskIntoConstraints = false

        instructionsCard.translatesAutoresizingMaskIntoConstraints = false
        instructionsCard.accessibilityIdentifier = "pairingInstructionsButton"
        instructionsCard.addTarget(self,
                                   action: #selector(showPairingInstructions),
                                   for: .touchUpInside)

        contentView.addSubview(connectionStatusView)
        contentView.addSubview(brandLabel)
        contentView.addSubview(hapticControl)
        contentView.addSubview(helperLabel)
        contentView.addSubview(instructionsCard)

        let safeArea = view.safeAreaLayoutGuide
        let hapticVerticalPosition = NSLayoutConstraint(
            item: hapticControl,
            attribute: .centerY,
            relatedBy: .equal,
            toItem: contentView,
            attribute: .bottom,
            multiplier: 0.25,
            constant: 140
        )

        NSLayoutConstraint.activate([
            contentView.topAnchor.constraint(equalTo: safeArea.topAnchor),
            contentView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            contentView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            contentView.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            connectionStatusView.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 14),
            connectionStatusView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20),
            connectionStatusView.widthAnchor.constraint(equalToConstant: 144),
            connectionStatusView.heightAnchor.constraint(equalToConstant: 44),

            brandLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 22),
            brandLabel.trailingAnchor.constraint(lessThanOrEqualTo: connectionStatusView.leadingAnchor, constant: -12),
            brandLabel.centerYAnchor.constraint(equalTo: connectionStatusView.centerYAnchor),

            hapticControl.centerXAnchor.constraint(equalTo: contentView.centerXAnchor),
            hapticVerticalPosition,
            hapticControl.widthAnchor.constraint(equalToConstant: 258),
            hapticControl.widthAnchor.constraint(lessThanOrEqualTo: contentView.widthAnchor, constant: -56),
            hapticControl.heightAnchor.constraint(equalTo: hapticControl.widthAnchor),

            helperLabel.topAnchor.constraint(equalTo: hapticControl.bottomAnchor, constant: 24),
            helperLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 34),
            helperLabel.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -34),

            instructionsCard.topAnchor.constraint(greaterThanOrEqualTo: helperLabel.bottomAnchor, constant: 28),
            instructionsCard.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20),
            instructionsCard.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20),
            instructionsCard.heightAnchor.constraint(equalToConstant: 76),
            instructionsCard.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -22),
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

    @objc private func hapticControlTapped() {
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

    @objc private func connectionStatusTapped() {
        guard !manager.isControllerConnected else { return }
        showPairingInstructions()
    }

    @objc private func showPairingInstructions() {
        guard presentedViewController == nil else { return }

        let instructionsViewController = PairingInstructionsViewController()
        instructionsViewController.onClose = { [weak self, weak instructionsViewController] in
            self?.stopControllerDiscovery()
            instructionsViewController?.dismiss(animated: true)
        }

        if let sheet = instructionsViewController.sheetPresentationController {
            if #available(iOS 16.0, *) {
                let pairingDetentIdentifier = UISheetPresentationController.Detent.Identifier("pairing")
                let pairingDetent = UISheetPresentationController.Detent.custom(
                    identifier: pairingDetentIdentifier
                ) { context in
                    min(context.maximumDetentValue, 540)
                }
                sheet.detents = [pairingDetent]
                sheet.selectedDetentIdentifier = pairingDetentIdentifier
            } else {
                sheet.detents = [.large()]
                sheet.selectedDetentIdentifier = .large
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
        let controllerName = manager.connectedController?.productCategory
        let isPlaying = manager.playbackState == .playing

        connectionStatusView.update(isConnected: isConnected, controllerName: controllerName)
        hapticControl.update(playbackState: manager.playbackState)
        helperLabel.text = isPlaying
            ? "Keep the app open. Vibration stops when locked or minimized."
            : nil
        helperLabel.isHidden = !isPlaying
        UIApplication.shared.isIdleTimerDisabled = isPlaying
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

private final class ConnectionStatusView: UIControl {
    private let blurView = UIVisualEffectView(effect: UIBlurEffect(style: .systemUltraThinMaterialDark))
    private let statusDot = UIView()
    private let titleLabel = UILabel()

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
        layer.cornerRadius = bounds.height / 2
        blurView.frame = bounds
        blurView.layer.cornerRadius = bounds.height / 2
        statusDot.layer.cornerRadius = 5
    }

    func update(isConnected: Bool, controllerName: String?) {
        statusDot.backgroundColor = isConnected ? MainViewController.Palette.connected : MainViewController.Palette.accent
        titleLabel.text = isConnected ? "Connected" : "Connect"
        titleLabel.textColor = isConnected ? MainViewController.Palette.connected : MainViewController.Palette.accent

        isAccessibilityElement = true
        accessibilityTraits = isConnected ? .staticText : .button
        accessibilityLabel = isConnected ? "Controller connected" : "Connect controller"
        accessibilityValue = isConnected ? controllerName : nil
        accessibilityHint = isConnected ? nil : "Opens controller connection instructions"
    }

    private func setup() {
        clipsToBounds = true
        backgroundColor = UIColor.white.withAlphaComponent(0.035)
        layer.borderWidth = 1
        layer.borderColor = MainViewController.Palette.border.cgColor

        blurView.alpha = 0.30
        blurView.isUserInteractionEnabled = false
        addSubview(blurView)

        statusDot.translatesAutoresizingMaskIntoConstraints = false

        titleLabel.font = .systemFont(ofSize: 16, weight: .semibold)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false

        addSubview(statusDot)
        addSubview(titleLabel)

        NSLayoutConstraint.activate([
            statusDot.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20),
            statusDot.centerYAnchor.constraint(equalTo: centerYAnchor),
            statusDot.widthAnchor.constraint(equalToConstant: 10),
            statusDot.heightAnchor.constraint(equalTo: statusDot.widthAnchor),

            titleLabel.leadingAnchor.constraint(equalTo: statusDot.trailingAnchor, constant: 11),
            titleLabel.centerYAnchor.constraint(equalTo: centerYAnchor),
            titleLabel.trailingAnchor.constraint(lessThanOrEqualTo: trailingAnchor, constant: -16),
        ])

        addTarget(self, action: #selector(pressed), for: .touchDown)
        addTarget(self, action: #selector(released), for: [.touchUpInside, .touchCancel, .touchDragExit])
        update(isConnected: false, controllerName: nil)
    }

    @objc private func pressed() {
        UIView.animate(withDuration: 0.12) {
            self.alpha = 0.72
            self.transform = CGAffineTransform(scaleX: 0.97, y: 0.97)
        }
    }

    @objc private func released() {
        UIView.animate(withDuration: 0.18) {
            self.alpha = 1
            self.transform = .identity
        }
    }
}

private final class HapticControlView: UIControl {
    private let pulseLayer = CAShapeLayer()
    private let surfaceGradient = CAGradientLayer()
    private let highlightGradient = CAGradientLayer()
    private let borderGradient = CAGradientLayer()
    private let borderMask = CAShapeLayer()
    private let innerRingLayer = CAShapeLayer()
    private let titleLabel = UILabel()

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
        let circlePath = UIBezierPath(ovalIn: bounds.insetBy(dx: 2, dy: 2)).cgPath
        let innerPath = UIBezierPath(ovalIn: bounds.insetBy(dx: 8, dy: 8)).cgPath

        layer.cornerRadius = bounds.width / 2
        layer.shadowPath = UIBezierPath(ovalIn: bounds).cgPath

        surfaceGradient.frame = bounds
        surfaceGradient.cornerRadius = bounds.width / 2

        highlightGradient.frame = bounds
        highlightGradient.cornerRadius = bounds.width / 2

        borderGradient.frame = bounds
        borderMask.frame = bounds
        borderMask.path = circlePath

        innerRingLayer.frame = bounds
        innerRingLayer.path = innerPath

        pulseLayer.frame = bounds
        pulseLayer.path = circlePath
    }

    func update(playbackState: HapticsPlaybackState) {
        let isActive = playbackState == .playing || playbackState == .starting

        CATransaction.begin()
        CATransaction.setDisableActions(true)
        if isActive {
            surfaceGradient.colors = [
                MainViewController.Palette.accent.withAlphaComponent(0.58).cgColor,
                MainViewController.Palette.accentBlue.withAlphaComponent(0.42).cgColor,
                MainViewController.Palette.surface.cgColor,
            ]
            layer.shadowColor = MainViewController.Palette.accent.cgColor
            layer.shadowOpacity = 0.55
        } else {
            surfaceGradient.colors = [
                UIColor.white.withAlphaComponent(0.38).cgColor,
                UIColor(red: 0.18, green: 0.12, blue: 0.31, alpha: 0.88).cgColor,
                MainViewController.Palette.surface.withAlphaComponent(0.96).cgColor,
            ]
            layer.shadowColor = MainViewController.Palette.accentBlue.cgColor
            layer.shadowOpacity = 0.20
        }
        CATransaction.commit()

        switch playbackState {
        case .idle:
            setTitle("VIBRATE", color: MainViewController.Palette.accent)
            stopPulseAnimation()
            accessibilityLabel = "Start vibration"
            accessibilityValue = "Off"
        case .starting:
            setTitle("STARTING", color: .white)
            startPulseAnimation()
            accessibilityLabel = "Stop vibration"
            accessibilityValue = "Starting"
        case .playing:
            setTitle("STOP", color: .white)
            startPulseAnimation()
            accessibilityLabel = "Stop vibration"
            accessibilityValue = "On"
        case .stopping:
            setTitle("STOPPING", color: UIColor.white.withAlphaComponent(0.74))
            stopPulseAnimation()
            accessibilityLabel = "Vibration stopping"
            accessibilityValue = "Stopping"
        }
    }

    private func setup() {
        isAccessibilityElement = true
        accessibilityTraits = .button
        layer.masksToBounds = false
        layer.shadowRadius = 34
        layer.shadowOffset = .zero

        pulseLayer.fillColor = UIColor.clear.cgColor
        pulseLayer.strokeColor = MainViewController.Palette.accent.withAlphaComponent(0.62).cgColor
        pulseLayer.lineWidth = 2
        pulseLayer.opacity = 0
        layer.addSublayer(pulseLayer)

        surfaceGradient.locations = [0.0, 0.42, 1.0]
        surfaceGradient.startPoint = CGPoint(x: 0.16, y: 0.10)
        surfaceGradient.endPoint = CGPoint(x: 0.82, y: 0.90)
        layer.addSublayer(surfaceGradient)

        highlightGradient.type = .radial
        highlightGradient.colors = [
            UIColor.white.withAlphaComponent(0.34).cgColor,
            UIColor.white.withAlphaComponent(0.05).cgColor,
            UIColor.clear.cgColor,
        ]
        highlightGradient.locations = [0.0, 0.35, 1.0]
        highlightGradient.startPoint = CGPoint(x: 0.30, y: 0.28)
        highlightGradient.endPoint = CGPoint(x: 0.84, y: 0.84)
        layer.addSublayer(highlightGradient)

        borderGradient.colors = [
            MainViewController.Palette.accent.cgColor,
            MainViewController.Palette.accentBlue.cgColor,
            MainViewController.Palette.accent.cgColor,
        ]
        borderGradient.locations = [0.0, 0.55, 1.0]
        borderGradient.startPoint = CGPoint(x: 0.1, y: 0.0)
        borderGradient.endPoint = CGPoint(x: 0.9, y: 1.0)
        borderMask.fillColor = UIColor.clear.cgColor
        borderMask.strokeColor = UIColor.white.cgColor
        borderMask.lineWidth = 3
        borderGradient.mask = borderMask
        layer.addSublayer(borderGradient)

        innerRingLayer.fillColor = UIColor.clear.cgColor
        innerRingLayer.strokeColor = UIColor.white.withAlphaComponent(0.06).cgColor
        innerRingLayer.lineWidth = 1
        layer.addSublayer(innerRingLayer)

        titleLabel.textAlignment = .center
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        addSubview(titleLabel)

        NSLayoutConstraint.activate([
            titleLabel.centerXAnchor.constraint(equalTo: centerXAnchor),
            titleLabel.centerYAnchor.constraint(equalTo: centerYAnchor),
            titleLabel.leadingAnchor.constraint(greaterThanOrEqualTo: leadingAnchor, constant: 28),
            titleLabel.trailingAnchor.constraint(lessThanOrEqualTo: trailingAnchor, constant: -28),
        ])

        addTarget(self, action: #selector(pressed), for: .touchDown)
        addTarget(self, action: #selector(released), for: [.touchUpInside, .touchCancel, .touchDragExit])
        update(playbackState: .idle)
    }

    private func setTitle(_ title: String, color: UIColor) {
        titleLabel.attributedText = NSAttributedString(
            string: title,
            attributes: [
                .font: UIFont.systemFont(ofSize: 22, weight: .bold),
                .foregroundColor: color,
                .kern: 3.2,
            ]
        )
    }

    private func startPulseAnimation() {
        guard pulseLayer.animation(forKey: "pulse") == nil else { return }

        let scale = CABasicAnimation(keyPath: "transform.scale")
        scale.fromValue = 1.0
        scale.toValue = 1.10

        let opacity = CABasicAnimation(keyPath: "opacity")
        opacity.fromValue = 0.48
        opacity.toValue = 0.0

        let group = CAAnimationGroup()
        group.animations = [scale, opacity]
        group.duration = 1.45
        group.repeatCount = .infinity
        group.timingFunction = CAMediaTimingFunction(name: .easeOut)
        pulseLayer.add(group, forKey: "pulse")
    }

    private func stopPulseAnimation() {
        pulseLayer.removeAnimation(forKey: "pulse")
        pulseLayer.opacity = 0
    }

    @objc private func pressed() {
        UIView.animate(withDuration: 0.12) {
            self.transform = CGAffineTransform(scaleX: 0.97, y: 0.97)
        }
    }

    @objc private func released() {
        UIView.animate(withDuration: 0.22,
                       delay: 0,
                       usingSpringWithDamping: 0.70,
                       initialSpringVelocity: 0.5) {
            self.transform = .identity
        }
    }
}

private final class InstructionsCardView: UIControl {
    private let blurView = UIVisualEffectView(effect: UIBlurEffect(style: .systemUltraThinMaterialDark))
    private let iconContainer = UIView()
    private let iconView = UIImageView(image: UIImage(systemName: "gamecontroller.fill"))

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
        layer.cornerRadius = 24
        blurView.frame = bounds
        blurView.layer.cornerRadius = 24
        iconContainer.layer.cornerRadius = 18
    }

    private func setup() {
        clipsToBounds = true
        backgroundColor = UIColor.white.withAlphaComponent(0.035)
        layer.borderWidth = 1
        layer.borderColor = MainViewController.Palette.border.cgColor

        blurView.alpha = 0.28
        blurView.isUserInteractionEnabled = false
        addSubview(blurView)

        iconContainer.backgroundColor = MainViewController.Palette.accent.withAlphaComponent(0.14)
        iconContainer.isUserInteractionEnabled = false
        iconContainer.translatesAutoresizingMaskIntoConstraints = false

        iconView.tintColor = MainViewController.Palette.accent
        iconView.contentMode = .scaleAspectFit
        iconView.translatesAutoresizingMaskIntoConstraints = false

        let titleLabel = UILabel()
        titleLabel.text = "How to connect"
        titleLabel.font = .systemFont(ofSize: 16, weight: .semibold)
        titleLabel.textColor = MainViewController.Palette.primaryText

        let detailLabel = UILabel()
        detailLabel.text = "PlayStation, Xbox or MFi controller"
        detailLabel.font = .systemFont(ofSize: 12, weight: .medium)
        detailLabel.textColor = MainViewController.Palette.secondaryText

        let labels = UIStackView(arrangedSubviews: [titleLabel, detailLabel])
        labels.axis = .vertical
        labels.spacing = 3
        labels.isUserInteractionEnabled = false
        labels.translatesAutoresizingMaskIntoConstraints = false

        let chevron = UIImageView(image: UIImage(systemName: "chevron.right"))
        chevron.tintColor = MainViewController.Palette.accent
        chevron.contentMode = .scaleAspectFit
        chevron.isUserInteractionEnabled = false
        chevron.translatesAutoresizingMaskIntoConstraints = false

        addSubview(iconContainer)
        iconContainer.addSubview(iconView)
        addSubview(labels)
        addSubview(chevron)

        NSLayoutConstraint.activate([
            iconContainer.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 16),
            iconContainer.centerYAnchor.constraint(equalTo: centerYAnchor),
            iconContainer.widthAnchor.constraint(equalToConstant: 44),
            iconContainer.heightAnchor.constraint(equalTo: iconContainer.widthAnchor),

            iconView.centerXAnchor.constraint(equalTo: iconContainer.centerXAnchor),
            iconView.centerYAnchor.constraint(equalTo: iconContainer.centerYAnchor),
            iconView.widthAnchor.constraint(equalToConstant: 23),
            iconView.heightAnchor.constraint(equalTo: iconView.widthAnchor),

            labels.leadingAnchor.constraint(equalTo: iconContainer.trailingAnchor, constant: 14),
            labels.centerYAnchor.constraint(equalTo: centerYAnchor),
            labels.trailingAnchor.constraint(lessThanOrEqualTo: chevron.leadingAnchor, constant: -12),

            chevron.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -18),
            chevron.centerYAnchor.constraint(equalTo: centerYAnchor),
            chevron.widthAnchor.constraint(equalToConstant: 16),
            chevron.heightAnchor.constraint(equalTo: chevron.widthAnchor),
        ])

        isAccessibilityElement = true
        accessibilityTraits = .button
        accessibilityLabel = "How to connect a controller"
        accessibilityHint = "Opens controller connection instructions"

        addTarget(self, action: #selector(pressed), for: .touchDown)
        addTarget(self, action: #selector(released), for: [.touchUpInside, .touchCancel, .touchDragExit])
    }

    @objc private func pressed() {
        UIView.animate(withDuration: 0.12) {
            self.alpha = 0.72
            self.transform = CGAffineTransform(scaleX: 0.985, y: 0.985)
        }
    }

    @objc private func released() {
        UIView.animate(withDuration: 0.18) {
            self.alpha = 1
            self.transform = .identity
        }
    }
}

private final class PairingInstructionsViewController: UIViewController {
    private let backgroundGradient = CAGradientLayer()
    var onClose: (() -> Void)?

    override func viewDidLoad() {
        super.viewDidLoad()
        overrideUserInterfaceStyle = .dark
        view.backgroundColor = MainViewController.Palette.backgroundBottom
        configureBackground()
        buildInterface()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        backgroundGradient.frame = view.bounds
    }

    private func configureBackground() {
        backgroundGradient.colors = [
            MainViewController.Palette.backgroundTop.cgColor,
            MainViewController.Palette.backgroundBottom.cgColor,
        ]
        backgroundGradient.startPoint = CGPoint(x: 0.2, y: 0.0)
        backgroundGradient.endPoint = CGPoint(x: 0.8, y: 1.0)
        view.layer.insertSublayer(backgroundGradient, at: 0)
    }

    private func buildInterface() {
        let titleLabel = UILabel()
        titleLabel.text = "Connect a controller"
        titleLabel.font = .systemFont(ofSize: 28, weight: .bold)
        titleLabel.textColor = MainViewController.Palette.primaryText
        titleLabel.numberOfLines = 0

        let subtitleLabel = UILabel()
        subtitleLabel.text = "Pair it in iPhone Settings, then return to ZOONZOON."
        subtitleLabel.font = .systemFont(ofSize: 15, weight: .medium)
        subtitleLabel.textColor = MainViewController.Palette.secondaryText
        subtitleLabel.numberOfLines = 0

        let firstStep = makeStep(
            number: "1",
            title: "Turn on pairing mode",
            detail: "PlayStation: hold PS + Share/Create. Xbox: hold the Pair button."
        )
        let secondStep = makeStep(
            number: "2",
            title: "Open Bluetooth settings",
            detail: "On your iPhone, go to Settings → Bluetooth and select the controller."
        )
        let thirdStep = makeStep(
            number: "3",
            title: "Return to ZOONZOON",
            detail: "The connection status updates automatically when pairing is complete."
        )

        let compatibilityLabel = UILabel()
        compatibilityLabel.text = "Compatible with PlayStation, Xbox and MFi controllers supported by iOS."
        compatibilityLabel.font = .systemFont(ofSize: 12, weight: .medium)
        compatibilityLabel.textColor = MainViewController.Palette.secondaryText
        compatibilityLabel.numberOfLines = 0
        compatibilityLabel.textAlignment = .center

        var closeConfiguration = UIButton.Configuration.filled()
        closeConfiguration.title = "Got it"
        closeConfiguration.baseBackgroundColor = MainViewController.Palette.accent
        closeConfiguration.baseForegroundColor = .white
        closeConfiguration.cornerStyle = .large
        closeConfiguration.contentInsets = NSDirectionalEdgeInsets(
            top: 14,
            leading: 18,
            bottom: 14,
            trailing: 18
        )
        let closeButton = UIButton(configuration: closeConfiguration)
        closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)

        let headerStack = UIStackView(arrangedSubviews: [titleLabel, subtitleLabel])
        headerStack.axis = .vertical
        headerStack.alignment = .fill
        headerStack.spacing = 8
        headerStack.translatesAutoresizingMaskIntoConstraints = false

        let stepsStack = UIStackView(arrangedSubviews: [firstStep, secondStep, thirdStep])
        stepsStack.axis = .vertical
        stepsStack.alignment = .fill
        stepsStack.spacing = 12
        stepsStack.translatesAutoresizingMaskIntoConstraints = false

        let footerStack = UIStackView(arrangedSubviews: [compatibilityLabel, closeButton])
        footerStack.axis = .vertical
        footerStack.alignment = .fill
        footerStack.spacing = 14
        footerStack.translatesAutoresizingMaskIntoConstraints = false

        let stepsArea = UIView()
        stepsArea.translatesAutoresizingMaskIntoConstraints = false

        view.addSubview(headerStack)
        view.addSubview(stepsArea)
        stepsArea.addSubview(stepsStack)
        view.addSubview(footerStack)

        NSLayoutConstraint.activate([
            headerStack.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 22),
            headerStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            headerStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24),

            stepsArea.topAnchor.constraint(equalTo: headerStack.bottomAnchor, constant: 14),
            stepsArea.leadingAnchor.constraint(equalTo: headerStack.leadingAnchor),
            stepsArea.trailingAnchor.constraint(equalTo: headerStack.trailingAnchor),
            stepsArea.bottomAnchor.constraint(equalTo: footerStack.topAnchor, constant: -14),

            stepsStack.leadingAnchor.constraint(equalTo: stepsArea.leadingAnchor),
            stepsStack.trailingAnchor.constraint(equalTo: stepsArea.trailingAnchor),
            stepsStack.centerYAnchor.constraint(equalTo: stepsArea.centerYAnchor),
            stepsStack.topAnchor.constraint(greaterThanOrEqualTo: stepsArea.topAnchor),
            stepsStack.bottomAnchor.constraint(lessThanOrEqualTo: stepsArea.bottomAnchor),

            footerStack.leadingAnchor.constraint(equalTo: headerStack.leadingAnchor),
            footerStack.trailingAnchor.constraint(equalTo: headerStack.trailingAnchor),
            footerStack.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -12),
            closeButton.heightAnchor.constraint(equalToConstant: 52),
        ])
    }

    private func makeStep(number: String, title: String, detail: String) -> UIView {
        let badge = UILabel()
        badge.text = number
        badge.font = .systemFont(ofSize: 14, weight: .bold)
        badge.textColor = .white
        badge.textAlignment = .center
        badge.backgroundColor = MainViewController.Palette.accent
        badge.layer.cornerRadius = 15
        badge.clipsToBounds = true
        badge.translatesAutoresizingMaskIntoConstraints = false

        let titleLabel = UILabel()
        titleLabel.text = title
        titleLabel.font = .systemFont(ofSize: 15, weight: .bold)
        titleLabel.textColor = MainViewController.Palette.primaryText

        let detailLabel = UILabel()
        detailLabel.text = detail
        detailLabel.font = .systemFont(ofSize: 13, weight: .medium)
        detailLabel.textColor = MainViewController.Palette.secondaryText
        detailLabel.numberOfLines = 0

        let labels = UIStackView(arrangedSubviews: [titleLabel, detailLabel])
        labels.axis = .vertical
        labels.spacing = 3

        let row = UIStackView(arrangedSubviews: [badge, labels])
        row.axis = .horizontal
        row.alignment = .center
        row.spacing = 12
        row.translatesAutoresizingMaskIntoConstraints = false

        let card = UIView()
        card.backgroundColor = UIColor.white.withAlphaComponent(0.045)
        card.layer.cornerRadius = 18
        card.layer.borderWidth = 1
        card.layer.borderColor = MainViewController.Palette.border.withAlphaComponent(0.55).cgColor
        card.addSubview(row)

        NSLayoutConstraint.activate([
            badge.widthAnchor.constraint(equalToConstant: 30),
            badge.heightAnchor.constraint(equalTo: badge.widthAnchor),
            row.topAnchor.constraint(equalTo: card.topAnchor, constant: 12),
            row.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 14),
            row.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -14),
            row.bottomAnchor.constraint(equalTo: card.bottomAnchor, constant: -12),
        ])
        return card
    }

    @objc private func close() {
        onClose?()
    }
}
