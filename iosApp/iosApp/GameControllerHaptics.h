#import <Foundation/Foundation.h>
#import <GameController/GameController.h>

NS_ASSUME_NONNULL_BEGIN

/// ObjC bridge for game controller haptics (GCController.haptics + CoreHaptics).
/// DualShock-strong path: Left+Right engines, overlapping 100ms bursts (no cancel),
/// matching the old Swift ZoonZoon rumble recipe.
@interface GameControllerHaptics : NSObject

/// Start scanning for nearby wireless controllers.
+ (void)startWirelessDiscovery;

/// Stop wireless controller discovery.
+ (void)stopWirelessDiscovery;

/// Prepare haptic engines for the connected controller. Call on connect / become-current.
- (BOOL)prepareForController:(GCController *)controller;

/// Update left/right motor intensities and sharpness (0.0–1.0). Safe to call every frame.
- (BOOL)updateRumbleWithLeftIntensity:(float)left
                       rightIntensity:(float)right
                            sharpness:(float)sharpness;

/// Stop all haptic engines. Call on disconnect.
- (void)stopAll;

@end

NS_ASSUME_NONNULL_END
