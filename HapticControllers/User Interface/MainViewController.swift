

import UIKit
import CoreHaptics
import GameController

class MainViewController: UIViewController{
    
    let manager: HapticsManager
    
    let maxColIndex = 1
    let maxRowIndex = 3
    
    var selectedRow = 0
    var selectedCol = 0
    
    var highlightedButton: UIButton?
    var discoveryStatusText: String?
    var isDiscoveringController = false
    
    private let discoveryOverlayView = UIView()
    private let discoveryDialogView = UIView()
    private let discoverySpinner = UIActivityIndicatorView(style: .large)
    private let discoveryTitleLabel = UILabel()
    private let discoveryMessageLabel = UILabel()
    private let discoveryOpenSettingsButton = UIButton(type: .system)
    private let discoveryCancelButton = UIButton(type: .system)
    
    let buttonColor = UIColor(red: 0.937, green: 0.937, blue: 0.937, alpha: 1.0)
    let selectedButtonColor = UIColor(red: 0.77, green: 0.77, blue: 0.85, alpha: 1.0)

    let ahapFiles = [
        "AHAP/Hit",
        "AHAP/Hit",
        "AHAP/Hit",
        "AHAP/Hit",
        "AHAP/Triple",
        "AHAP/Rumble",
        "AHAP/Recharge",
        "AHAP/Heartbeats"
    ]
    
    let ahapLocalities = [
        GCHapticsLocality.default,
        GCHapticsLocality.all,
        GCHapticsLocality.leftHandle,
        GCHapticsLocality.rightHandle,
        GCHapticsLocality.default,
        GCHapticsLocality.default,
        GCHapticsLocality.default,
        GCHapticsLocality.default
    ]

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
        
        gamePad.dpad.valueChangedHandler = { input, x, y in
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
        
      
        gamePad.buttonA.pressedChangedHandler = { input, value, isPressed in
            if isPressed {
                let highlightedToggleCellColor = UIColor(red: 0.65, green: 0.65, blue: 0.75, alpha: 1.0)
                self.highlightedButton?.backgroundColor = highlightedToggleCellColor
                if let index = self.highlightedButton?.tag {
                    self.manager.playHapticsFile(named: self.ahapFiles[index], locality: self.ahapLocalities[index])
                }
            } else {
                self.highlightedButton?.backgroundColor = self.selectedButtonColor
            }
        }
    }
    
    func updateControllerLabel() {
        if self.controller != nil {
            discoveryStatusText = nil
            self.controllerLabel.text = self.controller?.productCategory
            self.controllerLabel.textColor = .black
            setControllerActionButton(title: "Connected", isEnabled: false)
        } else if let discoveryStatusText {
            self.controllerLabel.text = discoveryStatusText
            self.controllerLabel.textColor = .lightGray
            setControllerActionButton(title: "Connect Controller", isEnabled: true)
        } else {
            self.controllerLabel.text = "Tap Connect Controller to connect DualSense"
            self.controllerLabel.textColor = .lightGray
            setControllerActionButton(title: "Connect Controller", isEnabled: true)
        }
    }
    
    func highlightButton(atRow row: Int, column: Int) {
       
        highlightedButton?.backgroundColor = buttonColor
        
        highlightedButton?.backgroundColor = selectedButtonColor
    }

    @IBAction func StopButton(_ sender: Any)  {
        guard controller == nil else { return }
        presentPairingInstructions()
    }

    @IBOutlet weak var controllerActionButton: UIButton!
    @IBOutlet weak var controllerLabel: UILabel!
    
  
    @IBAction func buttonBackgroundRegular(_ sender: UIButton) {
        sender.backgroundColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 0.8470588235)
    }
    
  
    @IBAction func buttonBackgroundHighlight(_ sender: UIButton) {
        sender.backgroundColor = #colorLiteral(red: 0, green: 0, blue: 0, alpha: 0)
    }
    
    // Respond to presses from each button, created in Interface Builder.
    @IBAction func playAHAP(sender: UIButton) {
        guard controller != nil else {
            presentPairingInstructions()
            return
        }

        let index = sender.tag
        manager.playHapticsFile(named: self.ahapFiles[index], locality: self.ahapLocalities[index])
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
        
        discoverySpinner.translatesAutoresizingMaskIntoConstraints = false
        discoverySpinner.stopAnimating()
        discoverySpinner.isHidden = true
        
        discoveryTitleLabel.translatesAutoresizingMaskIntoConstraints = false
        discoveryTitleLabel.font = .systemFont(ofSize: 20, weight: .semibold)
        discoveryTitleLabel.text = "Connect DualSense"
        discoveryTitleLabel.textAlignment = .center
        discoveryTitleLabel.numberOfLines = 0
        
        discoveryMessageLabel.translatesAutoresizingMaskIntoConstraints = false
        discoveryMessageLabel.font = .systemFont(ofSize: 15, weight: .regular)
        discoveryMessageLabel.text = "1. Put the controller into pairing mode.\n2. Tap Open Settings.\n3. Open Bluetooth and select \"DualSense Wireless Controller\".\n4. Return to the app."
        discoveryMessageLabel.textAlignment = .center
        discoveryMessageLabel.numberOfLines = 0
        discoveryMessageLabel.textColor = .secondaryLabel
        
        discoveryOpenSettingsButton.translatesAutoresizingMaskIntoConstraints = false
        discoveryOpenSettingsButton.setTitle("Open Settings", for: .normal)
        discoveryOpenSettingsButton.addTarget(self, action: #selector(openSettingsForPairing), for: .touchUpInside)
        
        discoveryCancelButton.translatesAutoresizingMaskIntoConstraints = false
        discoveryCancelButton.setTitle("Cancel", for: .normal)
        discoveryCancelButton.addTarget(self, action: #selector(cancelControllerDiscovery), for: .touchUpInside)
        
        let stackView = UIStackView(arrangedSubviews: [
            discoverySpinner,
            discoveryTitleLabel,
            discoveryMessageLabel,
            discoveryOpenSettingsButton,
            discoveryCancelButton
        ])
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.axis = .vertical
        stackView.alignment = .center
        stackView.spacing = 16
        
        view.addSubview(discoveryOverlayView)
        discoveryOverlayView.addSubview(discoveryDialogView)
        discoveryDialogView.addSubview(stackView)
        
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
            
            stackView.leadingAnchor.constraint(equalTo: discoveryDialogView.leadingAnchor, constant: 24),
            stackView.trailingAnchor.constraint(equalTo: discoveryDialogView.trailingAnchor, constant: -24),
            stackView.topAnchor.constraint(equalTo: discoveryDialogView.topAnchor, constant: 24),
            stackView.bottomAnchor.constraint(equalTo: discoveryDialogView.bottomAnchor, constant: -24),
            
            discoverySpinner.widthAnchor.constraint(equalToConstant: 36),
            discoverySpinner.heightAnchor.constraint(equalToConstant: 36)
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
            discoveryStatusText = "Still not connected. Pair DualSense in Settings > Bluetooth and try again."
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
        discoveryStatusText = "Open Settings, pair DualSense, then return here."
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
        self.controller = nil
        discoveryStatusText = "Controller disconnected"
        logDebug("didDisconnect controllers=\(GCController.controllers().count)")
        updateControllerLabel()
    }
}
