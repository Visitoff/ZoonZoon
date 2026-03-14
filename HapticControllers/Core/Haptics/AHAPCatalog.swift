import Foundation
import GameController

enum AHAPCatalog {
    struct Timing {
        let duration: TimeInterval
        let repeatDelay: TimeInterval
    }

    struct Pattern {
        let resourceName: String
        let locality: GCHapticsLocality
        let timing: Timing?
    }

    static let hitDefault = Pattern(resourceName: "AHAP/Hit",
                                    locality: .default,
                                    timing: nil)
    static let hitAll = Pattern(resourceName: "AHAP/Hit",
                                locality: .all,
                                timing: nil)
    static let hitLeftHandle = Pattern(resourceName: "AHAP/Hit",
                                       locality: .leftHandle,
                                       timing: nil)
    static let hitRightHandle = Pattern(resourceName: "AHAP/Hit",
                                        locality: .rightHandle,
                                        timing: nil)
    static let triple = Pattern(resourceName: "AHAP/Triple",
                                locality: .default,
                                timing: nil)
    static let rumble = Pattern(resourceName: "AHAP/Rumble",
                                locality: .default,
                                timing: Timing(duration: 1.48, repeatDelay: 1.2284))
    static let flutterMain = Pattern(resourceName: "AHAP/FlutterMain",
                                     locality: .default,
                                     timing: Timing(duration: 3.0, repeatDelay: 2.5))
    static let recharge = Pattern(resourceName: "AHAP/Recharge",
                                  locality: .default,
                                  timing: nil)
    static let heartbeats = Pattern(resourceName: "AHAP/Heartbeats",
                                    locality: .default,
                                    timing: nil)

    /// Change this one line to switch the main endless pattern.
    static let defaultLoopPattern = rumble

    static let mainButtonPatterns = [
        hitDefault,
        hitAll,
        hitLeftHandle,
        hitRightHandle,
        triple,
        defaultLoopPattern,
        recharge,
        heartbeats,
    ]

    static func timing(for resourceName: String) -> Timing? {
        switch resourceName {
        case rumble.resourceName:
            return rumble.timing
        case flutterMain.resourceName:
            return flutterMain.timing
        default:
            return nil
        }
    }
}
