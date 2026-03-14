/*
 See LICENSE folder for this sample’s licensing information.

 Abstract:
 Swift extensions on UIView.
 */

import UIKit

/// Extend Interface Builder to expose view border parameters.
@IBDesignable extension UIView {
    @IBInspectable var borderColor: UIColor? {
        get {
            guard let color = layer.borderColor else {
                return nil
            }
            return UIColor(cgColor: color)
        }
        set { layer.borderColor = newValue?.cgColor }
    }

    @IBInspectable var borderWidth: CGFloat {
        get { layer.borderWidth }
        set { layer.borderWidth = newValue }
    }

    @IBInspectable var cornerRadius: CGFloat {
        get { layer.cornerRadius }
        set {
            layer.cornerRadius = newValue
            clipsToBounds = newValue > 0
        }
    }
}
