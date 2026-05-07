#import <Foundation/Foundation.h>
#import <GameController/GameController.h>

NS_ASSUME_NONNULL_BEGIN

/// ObjC-compatible bridge for game controller haptics.
/// Wraps GCController.haptics + CoreHaptics so Kotlin/Native can call it.
@interface GameControllerHaptics : NSObject

/// Create and start haptic engines for the given controller.
/// Call this when a controller connects.
- (BOOL)prepareEngineForController:(GCController *)controller locality:(NSString *)locality;

/// Play a continuous rumble at the given intensity (0.0–1.0) for ~100ms.
- (BOOL)playRumbleWithIntensity:(float)intensity locality:(NSString *)locality;

/// Stop all haptic engines.
- (void)stopAll;

@end

NS_ASSUME_NONNULL_END
