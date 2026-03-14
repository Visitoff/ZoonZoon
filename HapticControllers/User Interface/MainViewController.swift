

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
        
        // Initialize controller label
        updateControllerLabel()
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
            self.controllerLabel.text = self.controller?.productCategory
            self.controllerLabel.textColor = .black
        } else {
            self.controllerLabel.text = "Disconnected"
            self.controllerLabel.textColor = .lightGray
        }
    }
    
    func highlightButton(atRow row: Int, column: Int) {
       
        highlightedButton?.backgroundColor = buttonColor
        
        highlightedButton?.backgroundColor = selectedButtonColor
    }

    @IBAction func StopButton(_ sender: Any)  {
       // let hapticManager = HapticsManager()
      //  hapticManager.i = 1000000000000
        exit(0)
    }
    @IBOutlet weak var controllerLabel: UILabel!
    
  
    @IBAction func buttonBackgroundRegular(_ sender: UIButton) {
        sender.backgroundColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 0.8470588235)
    }
    
  
    @IBAction func buttonBackgroundHighlight(_ sender: UIButton) {
        sender.backgroundColor = #colorLiteral(red: 0, green: 0, blue: 0, alpha: 0)
    }
    
    // Respond to presses from each button, created in Interface Builder.
    @IBAction func playAHAP(sender: UIButton) {
        let index = sender.tag
        manager.playHapticsFile(named: self.ahapFiles[index], locality: self.ahapLocalities[index])
    }
}

extension MainViewController: HapticsManagerDelegate {
    func didConnect(controller: GCController) {
        self.controller = controller
        updateControllerLabel()
    }
    
    func didDisconnectController() {
        self.controller = nil
        updateControllerLabel()
    }
}
