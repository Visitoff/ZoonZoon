#import "GameControllerHaptics.h"
#import <CoreHaptics/CoreHaptics.h>
#import <GameController/GameController.h>

/// One motor channel: infinite continuous event at base intensity 1.0,
/// modulated by CHHapticDynamicParameterIDHapticIntensityControl (multiply).
/// Matches Apple's LongRunningHaptics sample from game-porting-toolkit.
@interface GCHapticMotorChannel : NSObject
- (BOOL)prepareWithController:(GCController *)controller locality:(GCHapticsLocality)locality;
- (BOOL)setIntensity:(float)intensity;
- (void)stop;
@end

@implementation GCHapticMotorChannel {
    CHHapticEngine *_engine;
    id<CHHapticPatternPlayer> _player;
    BOOL _engineStopped;
}

- (BOOL)prepareWithController:(GCController *)controller locality:(GCHapticsLocality)locality {
    [self stop];

    if (!controller.haptics) {
        return NO;
    }

    NSArray<GCHapticsLocality> *supported = controller.haptics.supportedLocalities;
    if (supported.count > 0 && ![supported containsObject:locality]) {
        NSLog(@"[Haptics] Locality %@ not in supported %@", locality, supported);
        return NO;
    }

    CHHapticEngine *engine = [controller.haptics createEngineWithLocality:locality];
    if (!engine) {
        NSLog(@"[Haptics] createEngine failed for locality %@", locality);
        return NO;
    }

    if ([engine respondsToSelector:@selector(setPlaysHapticsOnly:)]) {
        engine.playsHapticsOnly = YES;
    }
    if ([engine respondsToSelector:@selector(setIsAutoShutdownEnabled:)]) {
        engine.isAutoShutdownEnabled = NO;
    }

    __weak GCHapticMotorChannel *weakSelf = self;
    engine.stoppedHandler = ^(CHHapticEngineStoppedReason reason) {
        NSLog(@"[Haptics] Engine stopped: %ld", (long)reason);
        GCHapticMotorChannel *strongSelf = weakSelf;
        if (strongSelf) {
            strongSelf->_engineStopped = YES;
        }
    };

    engine.resetHandler = ^{
        NSLog(@"[Haptics] Engine reset — will restart on next intensity update");
        GCHapticMotorChannel *strongSelf = weakSelf;
        if (strongSelf) {
            strongSelf->_engineStopped = YES;
        }
    };

    _engine = engine;
    _engineStopped = YES;

    if (![self startEngineIfNeeded]) {
        _engine = nil;
        return NO;
    }

    // Base intensity MUST be 1.0: dynamic IntensityControl multiplies against it.
    // Starting at 0.0 makes every update permanently silent.
    CHHapticEventParameter *intensityParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticIntensity
                                                      value:1.0f];
    CHHapticEventParameter *sharpnessParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticSharpness
                                                      value:0.5f];

    CHHapticEvent *event =
        [[CHHapticEvent alloc] initWithEventType:CHHapticEventTypeHapticContinuous
                                      parameters:@[intensityParam, sharpnessParam]
                                    relativeTime:0
                                        duration:GCHapticDurationInfinite];

    NSError *patternError = nil;
    CHHapticPattern *pattern =
        [[CHHapticPattern alloc] initWithEvents:@[event] parameters:@[] error:&patternError];
    if (patternError || !pattern) {
        NSLog(@"[Haptics] Pattern error: %@", patternError);
        [self stop];
        return NO;
    }

    // Prefer regular PatternPlayer — AdvancedPlayer fails on some DualShock/DualSense stacks.
    NSError *playerError = nil;
    id<CHHapticPatternPlayer> player = [engine createPlayerWithPattern:pattern error:&playerError];
    if (playerError || !player) {
        NSLog(@"[Haptics] Pattern player error: %@ — trying advanced player", playerError);
        playerError = nil;
        player = [engine createAdvancedPlayerWithPattern:pattern error:&playerError];
        if (playerError || !player) {
            NSLog(@"[Haptics] Advanced player also failed: %@", playerError);
            [self stop];
            return NO;
        }
        if ([player respondsToSelector:@selector(setLoopEnabled:)]) {
            [(id<CHHapticAdvancedPatternPlayer>)player setLoopEnabled:YES];
        }
    }

    NSError *playError = nil;
    if (![player startAtTime:CHHapticTimeImmediate error:&playError]) {
        NSLog(@"[Haptics] Player start error: %@", playError);
        [self stop];
        return NO;
    }

    _player = player;

    // Mute until the first real intensity update (Apple sample pattern).
    [self setIntensity:0.0f];
    NSLog(@"[Haptics] Channel ready for locality %@", locality);
    return YES;
}

- (BOOL)startEngineIfNeeded {
    if (!_engine) {
        return NO;
    }
    if (!_engineStopped) {
        return YES;
    }

    NSError *startError = nil;
    if (![_engine startAndReturnError:&startError]) {
        NSLog(@"[Haptics] Engine start error: %@", startError);
        return NO;
    }
    _engineStopped = NO;
    return YES;
}

- (BOOL)setIntensity:(float)intensity {
    if (!_player || !_engine) {
        return NO;
    }

    if (![self startEngineIfNeeded]) {
        return NO;
    }

    float clamped = fmaxf(0.0f, fminf(1.0f, intensity));
    CHHapticDynamicParameter *param =
        [[CHHapticDynamicParameter alloc] initWithParameterID:CHHapticDynamicParameterIDHapticIntensityControl
                                                         value:clamped
                                                  relativeTime:0];

    NSError *error = nil;
    if (![_player sendParameters:@[param] atTime:CHHapticTimeImmediate error:&error]) {
        NSLog(@"[Haptics] sendParameters error: %@", error);
        _engineStopped = YES;
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
    _engineStopped = YES;
}

@end

@implementation GameControllerHaptics {
    GCHapticMotorChannel *_leftChannel;
    GCHapticMotorChannel *_rightChannel;
    GCHapticMotorChannel *_combinedChannel;
    BOOL _useSplitMotors;
    __weak GCController *_preparedController;
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

- (BOOL)prepareChannel:(GCHapticMotorChannel *)channel
        withController:(GCController *)controller
             localities:(NSArray<GCHapticsLocality> *)localities {
    for (GCHapticsLocality locality in localities) {
        if ([channel prepareWithController:controller locality:locality]) {
            return YES;
        }
    }
    return NO;
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
        _preparedController = controller;
        NSLog(@"[Haptics] Prepared split left/right motors for '%@'", controller.vendorName);
        return YES;
    }

    [_leftChannel stop];
    [_rightChannel stop];
    _useSplitMotors = NO;

    NSArray<GCHapticsLocality> *fallbackLocalities = @[
        GCHapticsLocalityHandles,
        GCHapticsLocalityDefault,
        GCHapticsLocalityAll
    ];

    if ([self prepareChannel:_combinedChannel withController:controller localities:fallbackLocalities]) {
        _preparedController = controller;
        NSLog(@"[Haptics] Prepared combined motor for '%@'", controller.vendorName);
        return YES;
    }

    // Last resort: try every locality the controller reports.
    NSArray<GCHapticsLocality> *supported = controller.haptics.supportedLocalities;
    if ([self prepareChannel:_combinedChannel withController:controller localities:supported]) {
        _preparedController = controller;
        NSLog(@"[Haptics] Prepared via supported locality for '%@'", controller.vendorName);
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
    _preparedController = nil;
}

@end
