#import <Foundation/Foundation.h>
#import <GameController/GameController.h>

NS_ASSUME_NONNULL_BEGIN

/// ObjC bridge for game controller haptics (GCController.haptics + CoreHaptics).
/// Single Handles engine; continuous rumble with intensity baked into the event
/// (not 1.0 × dynamic multiply, which stays soft on many controllers).
@interface GameControllerHaptics : NSObject

/// Start scanning for nearby wireless controllers.
+ (void)startWirelessDiscovery;

/// Stop wireless controller discovery.
+ (void)stopWirelessDiscovery;

/// Prepare haptic engines for the connected controller. Call on connect / become-current.
- (BOOL)prepareForController:(GCController *)controller;

/// Update left/right motor intensities (0.0–1.0). Safe to call every frame.
- (BOOL)updateRumbleWithLeftIntensity:(float)left rightIntensity:(float)right;

/// Stop all haptic engines. Call on disconnect.
- (void)stopAll;

@end

NS_ASSUME_NONNULL_END
