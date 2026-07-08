#import "GameControllerHaptics.h"
#import <CoreHaptics/CoreHaptics.h>
#import <GameController/GameController.h>

static const NSTimeInterval kContinuousPatternDuration = 30.0;

@interface GCHapticMotorChannel : NSObject
- (BOOL)prepareWithController:(GCController *)controller locality:(GCHapticsLocality)locality;
- (BOOL)setIntensity:(float)intensity;
- (void)stop;
@end

@implementation GCHapticMotorChannel {
    CHHapticEngine *_engine;
    id<CHHapticAdvancedPatternPlayer> _player;
}

- (BOOL)prepareWithController:(GCController *)controller locality:(GCHapticsLocality)locality {
    [self stop];

    if (!controller.haptics) {
        return NO;
    }

    CHHapticEngine *engine = [controller.haptics createEngineWithLocality:locality];
    if (!engine) {
        NSLog(@"[Haptics] createEngine failed for locality %@", locality);
        return NO;
    }

    engine.stoppedHandler = ^(CHHapticEngineStoppedReason reason) {
        NSLog(@"[Haptics] Engine stopped: %ld", (long)reason);
    };

    __weak CHHapticEngine *weakEngine = engine;
    engine.resetHandler = ^{
        NSError *restartError = nil;
        [weakEngine startAndReturnError:&restartError];
        if (restartError) {
            NSLog(@"[Haptics] Engine restart error: %@", restartError);
        }
    };

    NSError *startError = nil;
    if (![engine startAndReturnError:&startError]) {
        NSLog(@"[Haptics] Engine start error: %@", startError);
        return NO;
    }

    CHHapticEventParameter *intensityParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticIntensity
                                                      value:0.0f];
    CHHapticEventParameter *sharpnessParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticSharpness
                                                      value:0.5f];

    CHHapticEvent *event =
        [[CHHapticEvent alloc] initWithEventType:CHHapticEventTypeHapticContinuous
                                      parameters:@[intensityParam, sharpnessParam]
                                    relativeTime:0
                                        duration:kContinuousPatternDuration];

    NSError *patternError = nil;
    CHHapticPattern *pattern =
        [[CHHapticPattern alloc] initWithEvents:@[event] parameters:@[] error:&patternError];
    if (patternError || !pattern) {
        NSLog(@"[Haptics] Pattern error: %@", patternError);
        [engine stopWithCompletionHandler:nil];
        return NO;
    }

    NSError *playerError = nil;
    id<CHHapticAdvancedPatternPlayer> player =
        [engine createAdvancedPlayerWithPattern:pattern error:&playerError];
    if (playerError || !player) {
        NSLog(@"[Haptics] Advanced player error: %@", playerError);
        [engine stopWithCompletionHandler:nil];
        return NO;
    }

    player.loopEnabled = YES;

    NSError *playError = nil;
    if (![player startAtTime:0 error:&playError]) {
        NSLog(@"[Haptics] Player start error: %@", playError);
        [engine stopWithCompletionHandler:nil];
        return NO;
    }

    _engine = engine;
    _player = player;
    return YES;
}

- (BOOL)setIntensity:(float)intensity {
    if (!_player) {
        return NO;
    }

    float clamped = fmaxf(0.0f, fminf(1.0f, intensity));
    CHHapticDynamicParameter *param =
        [[CHHapticDynamicParameter alloc] initWithParameterID:CHHapticDynamicParameterIDHapticIntensityControl
                                                         value:clamped
                                                  relativeTime:0];

    NSError *error = nil;
    if (![_player sendParameters:@[param] atTime:0 error:&error]) {
        NSLog(@"[Haptics] sendParameters error: %@", error);
        [self stop];
        return NO;
    }
    return YES;
}

- (void)stop {
    if (_player) {
        [_player cancelAndReturnError:nil];
        _player = nil;
    }
    if (_engine) {
        [_engine stopWithCompletionHandler:nil];
        _engine = nil;
    }
}

@end

@implementation GameControllerHaptics {
    GCHapticMotorChannel *_leftChannel;
    GCHapticMotorChannel *_rightChannel;
    GCHapticMotorChannel *_combinedChannel;
    BOOL _useSplitMotors;
}

+ (void)startWirelessDiscovery {
    [GCController startWirelessControllerDiscoveryWithCompletionHandler:nil];
}

+ (void)stopWirelessDiscovery {
    [GCController stopWirelessControllerDiscovery];
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _leftChannel = [[GCHapticMotorChannel alloc] init];
        _rightChannel = [[GCHapticMotorChannel alloc] init];
        _combinedChannel = [[GCHapticMotorChannel alloc] init];
        _useSplitMotors = NO;
    }
    return self;
}

- (void)logSupportedLocalitiesForController:(GCController *)controller {
    NSArray<GCHapticsLocality> *localities = controller.haptics.supportedLocalities;
    if (localities.count == 0) {
        NSLog(@"[Haptics] Controller '%@' reports no supported localities", controller.vendorName);
        return;
    }
    NSLog(@"[Haptics] Controller '%@' supported localities: %@", controller.vendorName, localities);
}

- (BOOL)prepareForController:(GCController *)controller {
    [self stopAll];

    if (!controller.haptics) {
        NSLog(@"[Haptics] Controller '%@' has no haptics support", controller.vendorName ?: @"unknown");
        return NO;
    }

    [self logSupportedLocalitiesForController:controller];

    BOOL leftOk = [_leftChannel prepareWithController:controller locality:GCHapticsLocalityLeftHandle];
    BOOL rightOk = [_rightChannel prepareWithController:controller locality:GCHapticsLocalityRightHandle];

    if (leftOk && rightOk) {
        _useSplitMotors = YES;
        NSLog(@"[Haptics] Prepared split left/right motors for '%@'", controller.vendorName);
        return YES;
    }

    [_leftChannel stop];
    [_rightChannel stop];
    _useSplitMotors = NO;

    if ([_combinedChannel prepareWithController:controller locality:GCHapticsLocalityHandles]) {
        NSLog(@"[Haptics] Prepared combined handles motor for '%@'", controller.vendorName);
        return YES;
    }

    if ([_combinedChannel prepareWithController:controller locality:GCHapticsLocalityDefault]) {
        NSLog(@"[Haptics] Prepared default motor for '%@'", controller.vendorName);
        return YES;
    }

    if ([_combinedChannel prepareWithController:controller locality:GCHapticsLocalityAll]) {
        NSLog(@"[Haptics] Prepared all-motors channel for '%@'", controller.vendorName);
        return YES;
    }

    NSLog(@"[Haptics] Failed to prepare any haptic channel for '%@'", controller.vendorName);
    return NO;
}

- (BOOL)updateRumbleWithLeftIntensity:(float)left rightIntensity:(float)right {
    if (_useSplitMotors) {
        BOOL leftOk = [_leftChannel setIntensity:left];
        BOOL rightOk = [_rightChannel setIntensity:right];
        return leftOk || rightOk;
    }

    float combined = fmaxf(left, right);
    return [_combinedChannel setIntensity:combined];
}

- (void)stopAll {
    [_leftChannel stop];
    [_rightChannel stop];
    [_combinedChannel stop];
    _useSplitMotors = NO;
}

@end
