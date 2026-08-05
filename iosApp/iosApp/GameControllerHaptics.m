#import "GameControllerHaptics.h"
#import <CoreHaptics/CoreHaptics.h>
#import <GameController/GameController.h>
#import <math.h>

/*
 * DualShock rumble:
 * - Left+Right engines, overlapping bursts (no cancel) for strength.
 * - Sharpness is mapped to burst duration + transient punch because DualShock
 *   rumble motors barely respond to HapticSharpness alone.
 */

static const float kSharpnessFlushEpsilon = 0.04f;

@implementation GameControllerHaptics {
    NSMutableDictionary<GCHapticsLocality, CHHapticEngine *> *_engines;
    __weak GCController *_controller;
    float _activeSharpness;
    BOOL _hasActiveSharpness;

    // Phone (device) continuous rumble — separate from gamepad engines.
    CHHapticEngine *_phoneEngine;
    id<CHHapticAdvancedPatternPlayer> _phonePlayer;
    BOOL _phoneEngineStopped;
    float _phoneLastIntensity;
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
        _engines = [NSMutableDictionary dictionary];
        _activeSharpness = 0.5f;
        _hasActiveSharpness = NO;
        _phoneEngineStopped = YES;
        _phoneLastIntensity = -1.0f;
    }
    return self;
}

- (CHHapticEngine *)createEngineForController:(GCController *)controller
                                    locality:(GCHapticsLocality)locality {
    if (!controller.haptics) {
        return nil;
    }

    NSArray<GCHapticsLocality> *supported = controller.haptics.supportedLocalities;
    if (supported.count > 0 && ![supported containsObject:locality]) {
        return nil;
    }

    CHHapticEngine *engine = [controller.haptics createEngineWithLocality:locality];
    if (!engine) {
        return nil;
    }

    if ([engine respondsToSelector:@selector(setPlaysHapticsOnly:)]) {
        engine.playsHapticsOnly = YES;
    }
    if ([engine respondsToSelector:@selector(setAutoShutdownEnabled:)]) {
        engine.autoShutdownEnabled = NO;
    }

    GCHapticsLocality localityKey = [locality copy];
    __weak typeof(self) weakSelf = self;
    engine.stoppedHandler = ^(CHHapticEngineStoppedReason reason) {
        NSLog(@"[Haptics] Engine stopped (%@): %ld", localityKey, (long)reason);
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (!strongSelf) return;
        if (reason == CHHapticEngineStoppedReasonGameControllerDisconnect ||
            reason == CHHapticEngineStoppedReasonSystemError ||
            reason == CHHapticEngineStoppedReasonEngineDestroyed) {
            [strongSelf->_engines removeObjectForKey:localityKey];
        }
    };
    engine.resetHandler = ^{
        NSLog(@"[Haptics] Engine reset (%@) — restarting", localityKey);
        NSError *error = nil;
        [engine startAndReturnError:&error];
        if (error) {
            NSLog(@"[Haptics] Restart failed: %@", error);
        }
    };

    NSError *startError = nil;
    if (![engine startAndReturnError:&startError]) {
        NSLog(@"[Haptics] Failed to start engine %@: %@", locality, startError);
        return nil;
    }

    return engine;
}

- (BOOL)ensureEngineForLocality:(GCHapticsLocality)locality {
    if (_engines[locality]) {
        return YES;
    }
    GCController *controller = _controller;
    if (!controller) {
        return NO;
    }
    CHHapticEngine *engine = [self createEngineForController:controller locality:locality];
    if (!engine) {
        return NO;
    }
    _engines[locality] = engine;
    NSLog(@"[Haptics] Engine ready for %@", locality);
    return YES;
}

- (BOOL)prepareForController:(GCController *)controller {
    [self stopAll];
    _controller = controller;

    if (!controller.haptics) {
        NSLog(@"[Haptics] Controller '%@' has no haptics support", controller.vendorName ?: @"unknown");
        return NO;
    }

    NSLog(@"[Haptics] Controller '%@' supported localities: %@",
          controller.vendorName,
          controller.haptics.supportedLocalities);

    BOOL leftOk = [self ensureEngineForLocality:GCHapticsLocalityLeftHandle];
    BOOL rightOk = [self ensureEngineForLocality:GCHapticsLocalityRightHandle];
    BOOL defaultOk = [self ensureEngineForLocality:GCHapticsLocalityDefault];
    BOOL handlesOk = [self ensureEngineForLocality:GCHapticsLocalityHandles];

    BOOL ok = leftOk || rightOk || defaultOk || handlesOk;
    NSLog(@"[Haptics] Prepared for '%@' left=%d right=%d default=%d handles=%d",
          controller.vendorName, leftOk, rightOk, defaultOk, handlesOk);
    return ok;
}

/// Clear in-flight overlapping players so a new sharpness takes effect immediately.
- (void)flushOverlappingPlayers {
    GCController *controller = _controller;
    if (!controller) {
        return;
    }
    for (CHHapticEngine *engine in _engines.allValues) {
        [engine stopWithCompletionHandler:nil];
    }
    [_engines removeAllObjects];
    _controller = controller;
    [self ensureEngineForLocality:GCHapticsLocalityLeftHandle];
    [self ensureEngineForLocality:GCHapticsLocalityRightHandle];
    [self ensureEngineForLocality:GCHapticsLocalityDefault];
    [self ensureEngineForLocality:GCHapticsLocalityHandles];
}

- (BOOL)playRumbleWithIntensity:(float)intensity
                      sharpness:(float)sharpness
                       locality:(GCHapticsLocality)locality {
    if (intensity < 0.01f) {
        return YES;
    }
    if (![self ensureEngineForLocality:locality]) {
        return NO;
    }

    CHHapticEngine *engine = _engines[locality];
    if (!engine) {
        return NO;
    }

    NSError *startError = nil;
    [engine startAndReturnError:&startError];

    float clampedIntensity = fmaxf(0.0f, fminf(1.0f, intensity));
    float clampedSharpness = fmaxf(0.0f, fminf(1.0f, sharpness));

    // DualShock barely reacts to HapticSharpness alone — also map it to duration:
    // sharpness 0 → long heavy rumble (0.16s), sharpness 1 → short tick (0.035s).
    NSTimeInterval burstDuration = 0.16 - (0.125 * (double)clampedSharpness);

    CHHapticEventParameter *intensityParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticIntensity
                                                      value:clampedIntensity];
    CHHapticEventParameter *sharpnessParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticSharpness
                                                      value:clampedSharpness];

    NSMutableArray<CHHapticEvent *> *events = [NSMutableArray array];

    // High sharpness: lead with a transient "click" so DualShock feels the change.
    if (clampedSharpness >= 0.25f) {
        float transientIntensity = fminf(1.0f, clampedIntensity * (0.55f + 0.45f * clampedSharpness));
        CHHapticEventParameter *tIntensity =
            [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticIntensity
                                                          value:transientIntensity];
        CHHapticEventParameter *tSharpness =
            [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticSharpness
                                                          value:fmaxf(0.6f, clampedSharpness)];
        [events addObject:[[CHHapticEvent alloc] initWithEventType:CHHapticEventTypeHapticTransient
                                                        parameters:@[tIntensity, tSharpness]
                                                      relativeTime:0]];
    }

    [events addObject:[[CHHapticEvent alloc] initWithEventType:CHHapticEventTypeHapticContinuous
                                                    parameters:@[intensityParam, sharpnessParam]
                                                  relativeTime:0
                                                      duration:burstDuration]];

    NSError *patternError = nil;
    CHHapticPattern *pattern =
        [[CHHapticPattern alloc] initWithEvents:events parameters:@[] error:&patternError];
    if (!pattern) {
        NSLog(@"[Haptics] Pattern error: %@", patternError);
        return NO;
    }

    NSError *playerError = nil;
    id<CHHapticPatternPlayer> player = [engine createPlayerWithPattern:pattern error:&playerError];
    if (!player) {
        NSLog(@"[Haptics] Player error: %@", playerError);
        return NO;
    }

    NSError *playError = nil;
    if (![player startAtTime:CHHapticTimeImmediate error:&playError]) {
        NSLog(@"[Haptics] Play error: %@", playError);
        return NO;
    }

    return YES;
}

- (BOOL)updateRumbleWithLeftIntensity:(float)left
                       rightIntensity:(float)right
                            sharpness:(float)sharpness {
    if (left <= 0.01f && right <= 0.01f) {
        return YES;
    }

    float clampedSharpness = fmaxf(0.0f, fminf(1.0f, sharpness));
    if (!_hasActiveSharpness || fabsf(clampedSharpness - _activeSharpness) >= kSharpnessFlushEpsilon) {
        // Drop old overlapping bursts that still carry the previous sharpness.
        [self flushOverlappingPlayers];
        _activeSharpness = clampedSharpness;
        _hasActiveSharpness = YES;
        NSLog(@"[Haptics] Sharpness applied: %.2f (duration=%.0fms)",
              clampedSharpness,
              (0.16 - 0.125 * clampedSharpness) * 1000.0);
    }

    BOOL leftOk = NO;
    BOOL rightOk = NO;

    if (left > 0.01f) {
        leftOk = [self playRumbleWithIntensity:left sharpness:clampedSharpness locality:GCHapticsLocalityLeftHandle];
    }
    if (right > 0.01f) {
        rightOk = [self playRumbleWithIntensity:right sharpness:clampedSharpness locality:GCHapticsLocalityRightHandle];
    }

    if (leftOk || rightOk) {
        return YES;
    }

    float combined = fmaxf(left, right);
    if ([self playRumbleWithIntensity:combined sharpness:clampedSharpness locality:GCHapticsLocalityHandles]) {
        return YES;
    }
    return [self playRumbleWithIntensity:combined sharpness:clampedSharpness locality:GCHapticsLocalityDefault];
}

- (void)stopAll {
    for (CHHapticEngine *engine in _engines.allValues) {
        [engine stopWithCompletionHandler:nil];
    }
    [_engines removeAllObjects];
    _controller = nil;
    _hasActiveSharpness = NO;
}

#pragma mark - Phone (device Taptic) continuous rumble

- (BOOL)preparePhoneEngine {
    if (_phoneEngine && _phonePlayer && !_phoneEngineStopped) {
        return YES;
    }

    [self stopPhone];

    if (![CHHapticEngine capabilitiesForHardware].supportsHaptics) {
        NSLog(@"[PhoneHaptics] Hardware does not support Core Haptics");
        return NO;
    }

    NSError *engineError = nil;
    CHHapticEngine *engine = [[CHHapticEngine alloc] initAndReturnError:&engineError];
    if (!engine) {
        NSLog(@"[PhoneHaptics] Engine create error: %@", engineError);
        return NO;
    }

    engine.playsHapticsOnly = YES;
    engine.autoShutdownEnabled = NO;

    __weak typeof(self) weakSelf = self;
    engine.stoppedHandler = ^(CHHapticEngineStoppedReason reason) {
        NSLog(@"[PhoneHaptics] Engine stopped: %ld", (long)reason);
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (strongSelf) {
            strongSelf->_phoneEngineStopped = YES;
        }
    };
    engine.resetHandler = ^{
        NSLog(@"[PhoneHaptics] Engine reset — restarting");
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (!strongSelf) return;
        NSError *restartError = nil;
        [strongSelf->_phoneEngine startAndReturnError:&restartError];
        if (!restartError) {
            strongSelf->_phoneEngineStopped = NO;
        }
    };

    NSError *startError = nil;
    if (![engine startAndReturnError:&startError]) {
        NSLog(@"[PhoneHaptics] Engine start error: %@", startError);
        return NO;
    }

    // Base intensity 1.0 — dynamic IntensityControl multiplies against it.
    CHHapticEventParameter *intensityParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticIntensity
                                                      value:1.0f];
    CHHapticEventParameter *sharpnessParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticSharpness
                                                      value:0.4f];
    CHHapticEvent *event =
        [[CHHapticEvent alloc] initWithEventType:CHHapticEventTypeHapticContinuous
                                      parameters:@[intensityParam, sharpnessParam]
                                    relativeTime:0
                                        duration:30.0];

    NSError *patternError = nil;
    CHHapticPattern *pattern =
        [[CHHapticPattern alloc] initWithEvents:@[event] parameters:@[] error:&patternError];
    if (!pattern) {
        NSLog(@"[PhoneHaptics] Pattern error: %@", patternError);
        return NO;
    }

    NSError *playerError = nil;
    id<CHHapticAdvancedPatternPlayer> player =
        [engine createAdvancedPlayerWithPattern:pattern error:&playerError];
    if (!player) {
        // Fall back to regular player with finite looping duration.
        id<CHHapticPatternPlayer> basic =
            [engine createPlayerWithPattern:pattern error:&playerError];
        if (!basic) {
            NSLog(@"[PhoneHaptics] Player error: %@", playerError);
            return NO;
        }
        NSError *playError = nil;
        if (![basic startAtTime:CHHapticTimeImmediate error:&playError]) {
            NSLog(@"[PhoneHaptics] Play error: %@", playError);
            return NO;
        }
        // Can't dynamically modulate basic as well — store as advanced if possible.
        // Re-try advanced is already failed; keep basic via cast won't work for sendParameters well.
        // Create finite pattern with advanced path failed - try duration 30 loop via advanced only.
        NSLog(@"[PhoneHaptics] Advanced player unavailable");
        return NO;
    }

    player.loopEnabled = YES;

    NSError *playError = nil;
    if (![player startAtTime:CHHapticTimeImmediate error:&playError]) {
        NSLog(@"[PhoneHaptics] Play error: %@", playError);
        return NO;
    }

    // Start muted.
    CHHapticDynamicParameter *mute =
        [[CHHapticDynamicParameter alloc] initWithParameterID:CHHapticDynamicParameterIDHapticIntensityControl
                                                         value:0.0f
                                                  relativeTime:0];
    [player sendParameters:@[mute] atTime:CHHapticTimeImmediate error:nil];

    _phoneEngine = engine;
    _phonePlayer = player;
    _phoneEngineStopped = NO;
    _phoneLastIntensity = 0.0f;
    NSLog(@"[PhoneHaptics] Continuous phone engine ready");
    return YES;
}

- (BOOL)startPhoneEngineIfNeeded {
    if (!_phoneEngine) {
        return [self preparePhoneEngine];
    }
    if (!_phoneEngineStopped) {
        return YES;
    }
    NSError *startError = nil;
    if (![_phoneEngine startAndReturnError:&startError]) {
        NSLog(@"[PhoneHaptics] Restart error: %@", startError);
        return [self preparePhoneEngine];
    }
    _phoneEngineStopped = NO;
    return YES;
}

- (BOOL)updatePhoneIntensity:(float)intensity {
    float clamped = fmaxf(0.0f, fminf(1.0f, intensity));
    if (clamped < 0.01f) {
        if (_phonePlayer && fabsf(_phoneLastIntensity) > 0.01f) {
            CHHapticDynamicParameter *mute =
                [[CHHapticDynamicParameter alloc] initWithParameterID:CHHapticDynamicParameterIDHapticIntensityControl
                                                                 value:0.0f
                                                          relativeTime:0];
            [_phonePlayer sendParameters:@[mute] atTime:CHHapticTimeImmediate error:nil];
            _phoneLastIntensity = 0.0f;
        }
        return YES;
    }

    if (![self startPhoneEngineIfNeeded] || !_phonePlayer) {
        return NO;
    }

    if (fabsf(clamped - _phoneLastIntensity) < 0.02f) {
        return YES;
    }

    CHHapticDynamicParameter *param =
        [[CHHapticDynamicParameter alloc] initWithParameterID:CHHapticDynamicParameterIDHapticIntensityControl
                                                         value:clamped
                                                  relativeTime:0];
    NSError *error = nil;
    if (![_phonePlayer sendParameters:@[param] atTime:CHHapticTimeImmediate error:&error]) {
        NSLog(@"[PhoneHaptics] sendParameters error: %@", error);
        _phoneEngineStopped = YES;
        return NO;
    }
    _phoneLastIntensity = clamped;
    return YES;
}

- (void)stopPhone {
    if (_phonePlayer) {
        [_phonePlayer cancelAndReturnError:nil];
        _phonePlayer = nil;
    }
    if (_phoneEngine) {
        [_phoneEngine stopWithCompletionHandler:nil];
        _phoneEngine = nil;
    }
    _phoneEngineStopped = YES;
    _phoneLastIntensity = -1.0f;
}

@end
