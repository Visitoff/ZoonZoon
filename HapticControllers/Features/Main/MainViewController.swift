import CoreHaptics
import GameController
import UIKit

class MainViewController: UIViewController {
    private struct PlaybackIndicatorAppearance {
        let title: String
        let dotColor: UIColor
        let backgroundColor: UIColor
        let textColor: UIColor
    }

    let manager: HapticsManager

    var maxColIndex: Int {
        return 3
    }
    
    var maxRowIndex: Int {
        return (ahapPatterns.count - 1) / 4
    }

    var selectedRow = 0
    var selectedCol = 0

    var highlightedButton: UIButton?
    var discoveryStatusText: String?
    var isDiscoveringController = false

    private let discoveryOverlayView = UIView()
    private let discoveryDialogView = UIView()
    private let discoveryTitleLabel = UILabel()
    private let discoveryMessageLabel = UILabel()
    private let discoveryOpenSettingsButton = UIButton(type: .system)
    private let discoveryCancelButton = UIButton(type: .system)
    private let playbackStatusView = UIView()
    private let playbackStatusDotView = UIView()
    private let playbackStatusLabel = UILabel()

    let buttonColor = UIColor(red: 0.937, green: 0.937, blue: 0.937, alpha: 1.0)
    let selectedButtonColor = UIColor(red: 0.77, green: 0.77, blue: 0.85, alpha: 1.0)

    let ahapPatterns = AHAPCatalog.mainButtonPatterns

    var controller: GCController? {
        didSet {
            if let controller = controller {
                configure(controller: controller)
            }
        }
    }

    required init?(coder: NSCoder) {
        manager = HapticsManager()
        super.init(coder: coder)
        manager.delegate = self
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        setupDiscoveryOverlay()
        setupPlaybackStatusIndicator()
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(appDidBecomeActive),
                                               name: UIApplication.didBecomeActiveNotification,
                                               object: nil)
        refreshConnectedController(reason: "viewDidLoad")
    }

    func configure(controller: GCController) {
        guard let gamePad = controller.extendedGamepad else { fatalError() }

        // Set the initial button "selection".
        highlightButton(atRow: 0, column: 0)

        gamePad.dpad.valueChangedHandler = { _, x, y in
            var row = self.selectedRow
            var col = self.selectedCol
            switch (x, y) {
            case (-1.0, _):
                col -= 1
            case (1.0, _):
                col += 1
            case (_, 1.0):
                row -= 1
            case (_, -1.0):
                row += 1
            default: ()
            }
            self.selectedCol = max(0, min(col, self.maxColIndex))
            self.selectedRow = max(0, min(row, self.maxRowIndex))
            self.highlightButton(atRow: self.selectedRow, column: self.selectedCol)
        }

        gamePad.buttonA.pressedChangedHandler = { _, _, isPressed in
            if isPressed {
                let highlightedToggleCellColor = UIColor(red: 0.65, green: 0.65, blue: 0.75, alpha: 1.0)
                self.highlightedButton?.backgroundColor = highlightedToggleCellColor
                if let index = self.highlightedButton?.tag,
                   self.ahapPatterns.indices.contains(index) {
                    let pattern = self.ahapPatterns[index]
                    self.manager.playHapticsFile(named: pattern.resourceName, locality: pattern.locality)
                }
            } else {
                self.highlightedButton?.backgroundColor = self.selectedButtonColor
            }
        }
    }

    func updateControllerLabel() {
        if controller != nil {
            discoveryStatusText = nil
            controllerLabel.text = controller?.productCategory
            controllerLabel.textColor = .black
            setControllerActionButton(title: "Connected", isEnabled: false)
            updatePlaybackStatusIndicator()
        } else if let discoveryStatusText {
            controllerLabel.text = discoveryStatusText
            controllerLabel.textColor = .lightGray
            setControllerActionButton(title: "Connect Controller", isEnabled: true)
            playbackStatusView.isHidden = true
        } else {
            controllerLabel.text = "Tap Connect Controller to connect a controller"
            controllerLabel.textColor = .lightGray
            setControllerActionButton(title: "Connect Controller", isEnabled: true)
            playbackStatusView.isHidden = true
        }
        updateMainHapticButtonState()
    }

    func highlightButton(atRow row: Int, column: Int) {
        highlightedButton?.backgroundColor = buttonColor
        
        let index = row * 4 + column
        guard ahapPatterns.indices.contains(index) else { return }
        
        highlightedButton = view.viewWithTag(index) as? UIButton
        highlightedButton?.backgroundColor = selectedButtonColor
    }

    @IBAction func connectControllerButton(_ sender: Any) {
        guard controller == nil else { return }
        presentPairingInstructions()
    }

    @IBOutlet var controllerActionButton: UIButton!
    @IBOutlet var controllerLabel: UILabel!
    @IBOutlet var mainHapticButton: UIButton!

    @IBAction func buttonBackgroundRegular(_ sender: UIButton) {
        sender.backgroundColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 0.8470588235)
    }

    @IBAction func buttonBackgroundHighlight(_ sender: UIButton) {
        sender.backgroundColor = #colorLiteral(red: 0, green: 0, blue: 0, alpha: 0)
    }

    /// Respond to presses from each button, created in Interface Builder.
    @IBAction func playAHAP(sender: UIButton) {
        guard controller != nil else {
            presentPairingInstructions()
            return
        }

        let index = sender.tag
        guard ahapPatterns.indices.contains(index) else { return }
        let pattern = ahapPatterns[index]
        let filename = pattern.resourceName
        let locality = pattern.locality

        // All patterns now support looping
        switch manager.playbackState {
        case .idle:
            manager.startLoopingHapticsFile(named: filename, locality: locality)
        case .playing:
            manager.stopHaptics()
        case .starting, .stopping, .stopped:
            break
        }
        updateControllerLabel()
    }

    private func playbackIndicatorAppearance() -> PlaybackIndicatorAppearance? {
        switch manager.playbackState {
        case .idle:
            return nil
        case .starting:
            return PlaybackIndicatorAppearance(title: "Starting",
                                               dotColor: UIColor(red: 0.97, green: 0.65, blue: 0.24, alpha: 1.0),
                                               backgroundColor: UIColor(red: 1.0, green: 0.95, blue: 0.86, alpha: 1.0),
                                               textColor: UIColor(red: 0.61, green: 0.38, blue: 0.04, alpha: 1.0))
        case .playing:
            return PlaybackIndicatorAppearance(title: "On",
                                               dotColor: UIColor(red: 0.21, green: 0.71, blue: 0.45, alpha: 1.0),
                                               backgroundColor: UIColor(red: 0.88, green: 0.97, blue: 0.92, alpha: 1.0),
                                               textColor: UIColor(red: 0.10, green: 0.46, blue: 0.27, alpha: 1.0))
        case .stopping:
            return PlaybackIndicatorAppearance(title: "Stopping",
                                               dotColor: UIColor(red: 0.96, green: 0.53, blue: 0.19, alpha: 1.0),
                                               backgroundColor: UIColor(red: 1.0, green: 0.93, blue: 0.88, alpha: 1.0),
                                               textColor: UIColor(red: 0.63, green: 0.29, blue: 0.08, alpha: 1.0))
        case .stopped:
            return PlaybackIndicatorAppearance(title: "Stopped",
                                               dotColor: UIColor(red: 0.86, green: 0.20, blue: 0.19, alpha: 1.0),
                                               backgroundColor: UIColor(red: 0.99, green: 0.90, blue: 0.90, alpha: 1.0),
                                               textColor: UIColor(red: 0.62, green: 0.12, blue: 0.13, alpha: 1.0))
        }
    }

    private func setupPlaybackStatusIndicator() {
        playbackStatusView.translatesAutoresizingMaskIntoConstraints = false
        playbackStatusView.layer.cornerRadius = 13
        playbackStatusView.layer.cornerCurve = .continuous
        playbackStatusView.isHidden = true

        playbackStatusDotView.translatesAutoresizingMaskIntoConstraints = false
        playbackStatusDotView.layer.cornerRadius = 4
        playbackStatusDotView.layer.cornerCurve = .continuous

        playbackStatusLabel.translatesAutoresizingMaskIntoConstraints = false
        playbackStatusLabel.font = .systemFont(ofSize: 13, weight: .semibold)
        playbackStatusLabel.textAlignment = .left

        let playbackStatusStackView = UIStackView(arrangedSubviews: [
            playbackStatusDotView,
            playbackStatusLabel,
        ])
        playbackStatusStackView.translatesAutoresizingMaskIntoConstraints = false
        playbackStatusStackView.axis = .horizontal
        playbackStatusStackView.alignment = .center
        playbackStatusStackView.spacing = 8

        playbackStatusView.addSubview(playbackStatusStackView)
        view.addSubview(playbackStatusView)

        NSLayoutConstraint.activate([
            playbackStatusView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            playbackStatusView.topAnchor.constraint(equalTo: controllerLabel.bottomAnchor, constant: 8),

            playbackStatusStackView.leadingAnchor.constraint(equalTo: playbackStatusView.leadingAnchor, constant: 12),
            playbackStatusStackView.trailingAnchor.constraint(equalTo: playbackStatusView.trailingAnchor, constant: -12),
            playbackStatusStackView.topAnchor.constraint(equalTo: playbackStatusView.topAnchor, constant: 8),
            playbackStatusStackView.bottomAnchor.constraint(equalTo: playbackStatusView.bottomAnchor, constant: -8),

            playbackStatusDotView.widthAnchor.constraint(equalToConstant: 8),
            playbackStatusDotView.heightAnchor.constraint(equalToConstant: 8),
        ])
    }

    private func updatePlaybackStatusIndicator() {
        guard let appearance = playbackIndicatorAppearance() else {
            playbackStatusView.isHidden = true
            return
        }
        playbackStatusView.isHidden = false
        playbackStatusView.backgroundColor = appearance.backgroundColor
        playbackStatusDotView.backgroundColor = appearance.dotColor
        playbackStatusLabel.textColor = appearance.textColor
        playbackStatusLabel.text = appearance.title
    }

    private func updateMainHapticButtonState() {
        let isTransitioning = manager.playbackState == .starting ||
            manager.playbackState == .stopping ||
            manager.playbackState == .stopped
        mainHapticButton?.isEnabled = !isTransitioning
        mainHapticButton?.alpha = isTransitioning ? 0.75 : 1.0
    }

    private func setupDiscoveryOverlay() {
        discoveryOverlayView.translatesAutoresizingMaskIntoConstraints = false
        discoveryOverlayView.backgroundColor = UIColor(white: 0, alpha: 0.35)
        discoveryOverlayView.alpha = 0
        discoveryOverlayView.isHidden = true

        discoveryDialogView.translatesAutoresizingMaskIntoConstraints = false
        discoveryDialogView.backgroundColor = .systemBackground
        discoveryDialogView.layer.cornerRadius = 20
        discoveryDialogView.layer.cornerCurve = .continuous

        discoveryTitleLabel.translatesAutoresizingMaskIntoConstraints = false
        discoveryTitleLabel.font = .systemFont(ofSize: 20, weight: .semibold)
        discoveryTitleLabel.text = "Connect Controller"
        discoveryTitleLabel.textAlignment = .center
        discoveryTitleLabel.numberOfLines = 0

        discoveryMessageLabel.translatesAutoresizingMaskIntoConstraints = false
        discoveryMessageLabel.font = .systemFont(ofSize: 15, weight: .regular)
        discoveryMessageLabel.text = "1. Put the controller into pairing mode.\n\n2. Tap Open Settings.\n\n3. Open Bluetooth and connect your controller.\n\n4. Return to the app."
        discoveryMessageLabel.textAlignment = .left
        discoveryMessageLabel.numberOfLines = 0
        discoveryMessageLabel.textColor = .secondaryLabel

        discoveryOpenSettingsButton.translatesAutoresizingMaskIntoConstraints = false
        discoveryOpenSettingsButton.setTitle("Open Settings", for: .normal)
        discoveryOpenSettingsButton.titleLabel?.font = .systemFont(ofSize: 17, weight: .semibold)
        discoveryOpenSettingsButton.addTarget(self, action: #selector(openSettingsForPairing), for: .touchUpInside)

        discoveryCancelButton.translatesAutoresizingMaskIntoConstraints = false
        discoveryCancelButton.setTitle("Cancel", for: .normal)
        discoveryCancelButton.titleLabel?.font = .systemFont(ofSize: 17, weight: .regular)
        discoveryCancelButton.addTarget(self, action: #selector(cancelControllerDiscovery), for: .touchUpInside)

        let contentStackView = UIStackView(arrangedSubviews: [
            discoveryTitleLabel,
            discoveryMessageLabel,
        ])
        contentStackView.translatesAutoresizingMaskIntoConstraints = false
        contentStackView.axis = .vertical
        contentStackView.alignment = .fill
        contentStackView.spacing = 16

        let contentContainerView = UIView()
        contentContainerView.translatesAutoresizingMaskIntoConstraints = false
        contentContainerView.addSubview(contentStackView)

        let horizontalSeparator = UIView()
        horizontalSeparator.translatesAutoresizingMaskIntoConstraints = false
        horizontalSeparator.backgroundColor = .separator

        let verticalSeparator = UIView()
        verticalSeparator.translatesAutoresizingMaskIntoConstraints = false
        verticalSeparator.backgroundColor = .separator

        let actionsStackView = UIStackView(arrangedSubviews: [
            discoveryOpenSettingsButton,
            verticalSeparator,
            discoveryCancelButton,
        ])
        actionsStackView.translatesAutoresizingMaskIntoConstraints = false
        actionsStackView.axis = .horizontal
        actionsStackView.alignment = .fill
        actionsStackView.distribution = .fill
        actionsStackView.spacing = 0

        let rootStackView = UIStackView(arrangedSubviews: [
            contentContainerView,
            horizontalSeparator,
            actionsStackView,
        ])
        rootStackView.translatesAutoresizingMaskIntoConstraints = false
        rootStackView.axis = .vertical
        rootStackView.alignment = .fill
        rootStackView.spacing = 0

        view.addSubview(discoveryOverlayView)
        discoveryOverlayView.addSubview(discoveryDialogView)
        discoveryDialogView.addSubview(rootStackView)

        NSLayoutConstraint.activate([
            discoveryOverlayView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            discoveryOverlayView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            discoveryOverlayView.topAnchor.constraint(equalTo: view.topAnchor),
            discoveryOverlayView.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            discoveryDialogView.centerXAnchor.constraint(equalTo: discoveryOverlayView.centerXAnchor),
            discoveryDialogView.centerYAnchor.constraint(equalTo: discoveryOverlayView.centerYAnchor),
            discoveryDialogView.leadingAnchor.constraint(greaterThanOrEqualTo: discoveryOverlayView.leadingAnchor, constant: 24),
            discoveryDialogView.trailingAnchor.constraint(lessThanOrEqualTo: discoveryOverlayView.trailingAnchor, constant: -24),
            discoveryDialogView.widthAnchor.constraint(equalToConstant: 300),

            rootStackView.leadingAnchor.constraint(equalTo: discoveryDialogView.leadingAnchor),
            rootStackView.trailingAnchor.constraint(equalTo: discoveryDialogView.trailingAnchor),
            rootStackView.topAnchor.constraint(equalTo: discoveryDialogView.topAnchor),
            rootStackView.bottomAnchor.constraint(equalTo: discoveryDialogView.bottomAnchor),

            contentStackView.leadingAnchor.constraint(equalTo: contentContainerView.leadingAnchor, constant: 24),
            contentStackView.trailingAnchor.constraint(equalTo: contentContainerView.trailingAnchor, constant: -24),
            contentStackView.topAnchor.constraint(equalTo: contentContainerView.topAnchor, constant: 24),
            contentStackView.bottomAnchor.constraint(equalTo: contentContainerView.bottomAnchor, constant: -24),

            horizontalSeparator.heightAnchor.constraint(equalToConstant: 1),
            verticalSeparator.widthAnchor.constraint(equalToConstant: 1),
            actionsStackView.heightAnchor.constraint(equalToConstant: 52),
            discoveryCancelButton.widthAnchor.constraint(equalTo: discoveryOpenSettingsButton.widthAnchor),
        ])
    }

    private func showDiscoveryOverlay() {
        discoveryOverlayView.isHidden = false
        UIView.animate(withDuration: 0.2) {
            self.discoveryOverlayView.alpha = 1
        }
    }

    private func hideDiscoveryOverlay() {
        guard !discoveryOverlayView.isHidden else { return }

        UIView.animate(withDuration: 0.2, animations: {
            self.discoveryOverlayView.alpha = 0
        }, completion: { _ in
            self.discoveryOverlayView.isHidden = true
        })
    }

    private func setControllerActionButton(title: String, isEnabled: Bool) {
        if var configuration = controllerActionButton.configuration {
            configuration.title = title
            controllerActionButton.configuration = configuration
        } else {
            controllerActionButton.setTitle(title, for: .normal)
        }
        controllerActionButton.isEnabled = isEnabled
    }

    @objc private func cancelControllerDiscovery() {
        discoveryStatusText = "Pairing window closed"
        logDebug("pairing modal closed controllers=\(GCController.controllers().count)")
        hideDiscoveryOverlay()
        updateControllerLabel()
    }

    @objc private func openSettingsForPairing() {
        logDebug("open settings tapped")
        if let url = URL(string: "App-prefs:"),
           UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        } else if let url = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(url)
        }
    }

    @objc private func appDidBecomeActive() {
        refreshConnectedController(reason: "appDidBecomeActive")
        if controller == nil, !discoveryOverlayView.isHidden {
            discoveryStatusText = "Still not connected. Pair your controller in Settings > Bluetooth and try again."
            updateControllerLabel()
        }
    }

    private func refreshConnectedController(reason: String) {
        let controllers = GCController.controllers()
        let firstController = controllers.first
        controller = firstController
        logDebug("\(reason) controllers=\(controllers.count) first=\(firstController?.productCategory ?? "nil")")
        if firstController != nil {
            hideDiscoveryOverlay()
        }
        updateControllerLabel()
    }

    private func presentPairingInstructions() {
        discoveryStatusText = "Open Settings, pair your controller, then return here."
        logDebug("pairing instructions opened controllers=\(GCController.controllers().count)")
        showDiscoveryOverlay()
        updateControllerLabel()
    }

    private func logDebug(_ message: String) {
        print("[ControllerDiscovery] \(message)")
    }
}

extension MainViewController: HapticsManagerDelegate {
    func didConnect(controller: GCController) {
        self.controller = controller
        logDebug("didConnect product=\(controller.productCategory) controllers=\(GCController.controllers().count)")
        hideDiscoveryOverlay()
        updateControllerLabel()
    }

    func didDisconnectController() {
        controller = nil
        discoveryStatusText = "Controller disconnected"
        logDebug("didDisconnect controllers=\(GCController.controllers().count)")
        updateControllerLabel()
    }

    func didUpdatePlaybackState(_ state: HapticsPlaybackState) {
        updateControllerLabel()
    }
}
