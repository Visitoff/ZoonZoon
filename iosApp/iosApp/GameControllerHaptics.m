#import "GameControllerHaptics.h"
#import <CoreHaptics/CoreHaptics.h>
#import <GameController/GameController.h>
#import <math.h>

/*
 * Strong rumble for GCController (Apple LongRunningHaptics + direct intensity):
 *
 * - ONE engine on Handles (not split L/R — two engines feel weak on DualSense).
 * - Continuous event uses GCHapticDurationInfinite with intensity BAKED into the
 *   event parameter (not 1.0 × dynamic multiply). Dynamic multiply on PatternPlayer
 *   often stays soft even at "max".
 * - Player is recreated only when intensity changes meaningfully.
 */

static const float kIntensityEpsilon = 0.03f;

@interface GCHapticMotorChannel : NSObject
- (BOOL)prepareWithController:(GCController *)controller locality:(GCHapticsLocality)locality;
- (BOOL)setIntensity:(float)intensity;
- (void)stop;
@end

@implementation GCHapticMotorChannel {
    CHHapticEngine *_engine;
    id<CHHapticPatternPlayer> _player;
    BOOL _engineStopped;
    float _lastIntensity;
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
    if ([engine respondsToSelector:@selector(setAutoShutdownEnabled:)]) {
        engine.autoShutdownEnabled = NO;
    }

    __weak GCHapticMotorChannel *weakSelf = self;
    engine.stoppedHandler = ^(CHHapticEngineStoppedReason reason) {
        NSLog(@"[Haptics] Engine stopped: %ld", (long)reason);
        GCHapticMotorChannel *strongSelf = weakSelf;
        if (strongSelf) {
            strongSelf->_engineStopped = YES;
            strongSelf->_player = nil;
            strongSelf->_lastIntensity = -1.0f;
        }
    };
    engine.resetHandler = ^{
        NSLog(@"[Haptics] Engine reset");
        GCHapticMotorChannel *strongSelf = weakSelf;
        if (strongSelf) {
            strongSelf->_engineStopped = YES;
            strongSelf->_player = nil;
            strongSelf->_lastIntensity = -1.0f;
        }
    };

    _engine = engine;
    _engineStopped = YES;
    _lastIntensity = -1.0f;

    if (![self startEngineIfNeeded]) {
        _engine = nil;
        return NO;
    }

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

- (void)cancelPlayer {
    if (_player) {
        [_player cancelAndReturnError:nil];
        _player = nil;
    }
}

- (BOOL)startContinuousAtIntensity:(float)intensity {
    if (![self startEngineIfNeeded]) {
        return NO;
    }

    [self cancelPlayer];

    if (intensity < 0.01f) {
        _lastIntensity = 0.0f;
        return YES;
    }

    // Intensity is the EVENT value (full scale). Sharpness 0 = heaviest rumble motors.
    CHHapticEventParameter *intensityParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticIntensity
                                                      value:intensity];
    CHHapticEventParameter *sharpnessParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticSharpness
                                                      value:0.0f];

    CHHapticEvent *continuous =
        [[CHHapticEvent alloc] initWithEventType:CHHapticEventTypeHapticContinuous
                                      parameters:@[intensityParam, sharpnessParam]
                                    relativeTime:0
                                        duration:GCHapticDurationInfinite];

    NSMutableArray<CHHapticEvent *> *events = [NSMutableArray arrayWithObject:continuous];

    // Initial transient gives DualSense a stronger "kick" when engaging.
    if (intensity >= 0.4f) {
        CHHapticEventParameter *tIntensity =
            [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticIntensity
                                                          value:intensity];
        CHHapticEventParameter *tSharpness =
            [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticSharpness
                                                          value:0.15f];
        [events insertObject:[[CHHapticEvent alloc] initWithEventType:CHHapticEventTypeHapticTransient
                                                           parameters:@[tIntensity, tSharpness]
                                                         relativeTime:0]
                     atIndex:0];
    }

    NSError *patternError = nil;
    CHHapticPattern *pattern =
        [[CHHapticPattern alloc] initWithEvents:events parameters:@[] error:&patternError];
    if (!pattern) {
        NSLog(@"[Haptics] Pattern error: %@", patternError);
        return NO;
    }

    NSError *playerError = nil;
    id<CHHapticPatternPlayer> player = [_engine createPlayerWithPattern:pattern error:&playerError];
    if (!player) {
        // Advanced player as fallback (some firmwares prefer it for infinite events).
        playerError = nil;
        player = [_engine createAdvancedPlayerWithPattern:pattern error:&playerError];
        if (!player) {
            NSLog(@"[Haptics] Player error: %@", playerError);
            return NO;
        }
        if ([player respondsToSelector:@selector(setLoopEnabled:)]) {
            [(id<CHHapticAdvancedPatternPlayer>)player setLoopEnabled:YES];
        }
    }

    NSError *playError = nil;
    if (![player startAtTime:CHHapticTimeImmediate error:&playError]) {
        NSLog(@"[Haptics] Player start error: %@", playError);
        return NO;
    }

    _player = player;
    _lastIntensity = intensity;
    return YES;
}

- (BOOL)setIntensity:(float)intensity {
    if (!_engine) {
        return NO;
    }

    float clamped = fmaxf(0.0f, fminf(1.0f, intensity));

    if (clamped < 0.01f) {
        [self cancelPlayer];
        _lastIntensity = 0.0f;
        return YES;
    }

    // Keep existing infinite player if intensity barely changed — avoids choppy restarts.
    if (_player && fabsf(clamped - _lastIntensity) < kIntensityEpsilon) {
        return YES;
    }

    return [self startContinuousAtIntensity:clamped];
}

- (void)stop {
    [self cancelPlayer];
    if (_engine) {
        [_engine stopWithCompletionHandler:nil];
        _engine = nil;
    }
    _engineStopped = YES;
    _lastIntensity = -1.0f;
}

@end

@implementation GameControllerHaptics {
    GCHapticMotorChannel *_channel;
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
        _channel = [[GCHapticMotorChannel alloc] init];
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

    // Single engine, Handles first — matches Apple sample, strongest rumble path.
    NSArray<GCHapticsLocality> *preferred = @[
        GCHapticsLocalityHandles,
        GCHapticsLocalityDefault,
        GCHapticsLocalityAll,
        GCHapticsLocalityLeftHandle,
        GCHapticsLocalityRightHandle
    ];

    for (GCHapticsLocality locality in preferred) {
        if ([_channel prepareWithController:controller locality:locality]) {
            _preparedController = controller;
            NSLog(@"[Haptics] Prepared single engine '%@' locality %@", controller.vendorName, locality);
            return YES;
        }
    }

    for (GCHapticsLocality locality in controller.haptics.supportedLocalities) {
        if ([_channel prepareWithController:controller locality:locality]) {
            _preparedController = controller;
            NSLog(@"[Haptics] Prepared via supported locality %@ for '%@'", locality, controller.vendorName);
            return YES;
        }
    }

    NSLog(@"[Haptics] Failed to prepare any haptic channel for '%@'", controller.vendorName);
    return NO;
}

- (BOOL)updateRumbleWithLeftIntensity:(float)left rightIntensity:(float)right {
    float combined = fmaxf(left, right);
    return [_channel setIntensity:combined];
}

- (void)stopAll {
    [_channel stop];
    _preparedController = nil;
}

@end
