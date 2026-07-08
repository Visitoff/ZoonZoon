#import <Foundation/Foundation.h>
#import <GameController/GameController.h>

NS_ASSUME_NONNULL_BEGIN

@interface GameControllerHaptics : NSObject

+ (void)startWirelessDiscovery;
+ (void)stopWirelessDiscovery;

- (BOOL)prepareForController:(GCController *)controller;
- (BOOL)updateRumbleWithLeftIntensity:(float)left rightIntensity:(float)right;
- (void)stopAll;

@end

NS_ASSUME_NONNULL_END
