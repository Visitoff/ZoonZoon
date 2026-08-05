#import "GameControllerHaptics.h"
#import <CoreHaptics/CoreHaptics.h>
#import <GameController/GameController.h>
#import <math.h>

/*
 * Restored from the working Swift ZoonZoon rumble path (commit 04fb695 + origin/main Steady):
 *
 * - Keep separate engines for LeftHandle / RightHandle / Default (DualShock has 2 motors).
 * - Each frame fire a short continuous burst with intensity BAKED into the event.
 * - Do NOT cancel the previous player — overlapping bursts stack and feel ~25%+ stronger.
 * - Sharpness 0.5 matches Steady.ahap from the old Swift app.
 */

static const NSTimeInterval kBurstDurationSec = 0.10;

@implementation GameControllerHaptics {
    NSMutableDictionary<GCHapticsLocality, CHHapticEngine *> *_engines;
    __weak GCController *_controller;
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

    // DualShock: prepare both motors + default fallback (same as old Swift bridge).
    BOOL leftOk = [self ensureEngineForLocality:GCHapticsLocalityLeftHandle];
    BOOL rightOk = [self ensureEngineForLocality:GCHapticsLocalityRightHandle];
    BOOL defaultOk = [self ensureEngineForLocality:GCHapticsLocalityDefault];
    BOOL handlesOk = [self ensureEngineForLocality:GCHapticsLocalityHandles];

    BOOL ok = leftOk || rightOk || defaultOk || handlesOk;
    NSLog(@"[Haptics] Prepared for '%@' left=%d right=%d default=%d handles=%d",
          controller.vendorName, leftOk, rightOk, defaultOk, handlesOk);
    return ok;
}

/// Fire a short continuous burst. Previous players are intentionally NOT cancelled —
/// overlapping bursts are what made the old Swift ZoonZoon feel stronger on DualShock.
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

    // Keep engine alive if the system stopped it.
    NSError *startError = nil;
    [engine startAndReturnError:&startError];

    float clampedIntensity = fmaxf(0.0f, fminf(1.0f, intensity));
    float clampedSharpness = fmaxf(0.0f, fminf(1.0f, sharpness));
    CHHapticEventParameter *intensityParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticIntensity
                                                      value:clampedIntensity];
    CHHapticEventParameter *sharpnessParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticSharpness
                                                      value:clampedSharpness];

    CHHapticEvent *event =
        [[CHHapticEvent alloc] initWithEventType:CHHapticEventTypeHapticContinuous
                                      parameters:@[intensityParam, sharpnessParam]
                                    relativeTime:0
                                        duration:kBurstDurationSec];

    NSError *patternError = nil;
    CHHapticPattern *pattern =
        [[CHHapticPattern alloc] initWithEvents:@[event] parameters:@[] error:&patternError];
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

    // Player is intentionally leaked until the 100ms burst ends — Core Haptics
    // retains it while playing. No cancel = overlap stacking.
    return YES;
}

- (BOOL)updateRumbleWithLeftIntensity:(float)left
                       rightIntensity:(float)right
                            sharpness:(float)sharpness {
    if (left <= 0.01f && right <= 0.01f) {
        return YES;
    }

    BOOL leftOk = NO;
    BOOL rightOk = NO;

    // DualShock strong path: drive BOTH handle motors every frame (old Swift bridge).
    if (left > 0.01f) {
        leftOk = [self playRumbleWithIntensity:left sharpness:sharpness locality:GCHapticsLocalityLeftHandle];
    }
    if (right > 0.01f) {
        rightOk = [self playRumbleWithIntensity:right sharpness:sharpness locality:GCHapticsLocalityRightHandle];
    }

    if (leftOk || rightOk) {
        return YES;
    }

    // Fallback chain if handle localities are unavailable on this pad.
    float combined = fmaxf(left, right);
    if ([self playRumbleWithIntensity:combined sharpness:sharpness locality:GCHapticsLocalityHandles]) {
        return YES;
    }
    return [self playRumbleWithIntensity:combined sharpness:sharpness locality:GCHapticsLocalityDefault];
}

- (void)stopAll {
    for (CHHapticEngine *engine in _engines.allValues) {
        [engine stopWithCompletionHandler:nil];
    }
    [_engines removeAllObjects];
    _controller = nil;
}

@end
