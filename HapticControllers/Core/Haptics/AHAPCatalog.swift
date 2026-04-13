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
    
    // New vibrator patterns
    static let steady = Pattern(resourceName: "AHAP/Steady",
                               locality: .default,
                               timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let pulse = Pattern(resourceName: "AHAP/Pulse",
                              locality: .default,
                              timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let wave = Pattern(resourceName: "AHAP/Wave",
                             locality: .default,
                             timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let escalation = Pattern(resourceName: "AHAP/Escalation",
                                   locality: .default,
                                   timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let roller = Pattern(resourceName: "AHAP/Roller",
                               locality: .default,
                               timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let staircase = Pattern(resourceName: "AHAP/Staircase",
                                  locality: .default,
                                  timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let zigzag = Pattern(resourceName: "AHAP/Zigzag",
                               locality: .default,
                               timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let butterfly = Pattern(resourceName: "AHAP/Butterfly",
                                  locality: .default,
                                  timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let earthquake = Pattern(resourceName: "AHAP/Earthquake",
                                   locality: .default,
                                   timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let jackhammer = Pattern(resourceName: "AHAP/Jackhammer",
                                   locality: .default,
                                   timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let tornado = Pattern(resourceName: "AHAP/Tornado",
                                locality: .default,
                                timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let volcano = Pattern(resourceName: "AHAP/Volcano",
                                locality: .default,
                                timing: Timing(duration: 2.0, repeatDelay: 1.66))
    static let random = Pattern(resourceName: "AHAP/Random",
                               locality: .default,
                               timing: Timing(duration: 2.0, repeatDelay: 1.66))

    /// Change this one line to switch the main endless pattern.
    static let defaultLoopPattern = steady

    static let mainButtonPatterns = [
        steady,
        pulse,
        wave,
        escalation,
        roller,
        staircase,
        zigzag,
        butterfly,
        earthquake,
        jackhammer,
        tornado,
        volcano,
        random,
    ]

    static func timing(for resourceName: String) -> Timing? {
        switch resourceName {
        case rumble.resourceName:
            return rumble.timing
        case flutterMain.resourceName:
            return flutterMain.timing
        case steady.resourceName:
            return steady.timing
        case pulse.resourceName:
            return pulse.timing
        case wave.resourceName:
            return wave.timing
        case escalation.resourceName:
            return escalation.timing
        case roller.resourceName:
            return roller.timing
        case staircase.resourceName:
            return staircase.timing
        case zigzag.resourceName:
            return zigzag.timing
        case butterfly.resourceName:
            return butterfly.timing
        case earthquake.resourceName:
            return earthquake.timing
        case jackhammer.resourceName:
            return jackhammer.timing
        case tornado.resourceName:
            return tornado.timing
        case volcano.resourceName:
            return volcano.timing
        case random.resourceName:
            return random.timing
        default:
            return nil
        }
    }
}
