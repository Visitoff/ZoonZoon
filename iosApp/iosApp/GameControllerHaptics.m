#import "GameControllerHaptics.h"
#import <CoreHaptics/CoreHaptics.h>
#import <GameController/GameController.h>

@implementation GameControllerHaptics {
    NSMutableDictionary<NSString *, CHHapticEngine *> *_engineMap;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _engineMap = [NSMutableDictionary dictionary];
    }
    return self;
}

- (BOOL)prepareEngineForController:(GCController *)controller locality:(NSString *)locality {
    GCHapticsLocality *loc = [[GCHapticsLocality alloc] initWithRawValue:locality];
    CHHapticEngine *engine = [controller.haptics createEngineWithLocality:loc];
    if (!engine) {
        NSLog(@"[Haptics] Controller '%@' does not support haptics for locality: %@",
              controller.vendorName, locality);
        return NO;
    }

    engine.stoppedHandler = ^(CHHapticEngineStoppedReason reason) {
        NSLog(@"[Haptics] Engine stopped: %ld", (long)reason);
    };

    __weak CHHapticEngine *weakEngine = engine;
    engine.resetHandler = ^{
        NSLog(@"[Haptics] Engine reset — restarting");
        NSError *error = nil;
        [weakEngine startAndReturnError:&error];
        if (error) NSLog(@"[Haptics] Restart error: %@", error);
    };

    NSError *startError = nil;
    [engine startAndReturnError:&startError];
    if (startError) {
        NSLog(@"[Haptics] Failed to start engine: %@", startError);
        return NO;
    }

    _engineMap[locality] = engine;
    return YES;
}

- (BOOL)playRumbleWithIntensity:(float)intensity locality:(NSString *)locality {
    CHHapticEngine *engine = _engineMap[locality];
    if (!engine) return NO;

    CHHapticEventParameter *intensityParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticIntensity
                                                      value:intensity];
    CHHapticEventParameter *sharpnessParam =
        [[CHHapticEventParameter alloc] initWithParameterID:CHHapticEventParameterIDHapticSharpness
                                                      value:0.1f];

    CHHapticEvent *event =
        [[CHHapticEvent alloc] initWithEventType:CHHapticEventTypeHapticContinuous
                                      parameters:@[intensityParam, sharpnessParam]
                                    relativeTime:0
                                        duration:0.1];

    NSError *error = nil;
    CHHapticPattern *pattern =
        [[CHHapticPattern alloc] initWithEvents:@[event] parameters:@[] error:&error];
    if (error || !pattern) {
        NSLog(@"[Haptics] Pattern error: %@", error);
        return NO;
    }

    id<CHHapticPatternPlayer> player = [engine createPlayerWithPattern:pattern error:&error];
    if (error || !player) {
        NSLog(@"[Haptics] Player error: %@", error);
        return NO;
    }

    [player startAtTime:0 error:&error];
    if (error) {
        NSLog(@"[Haptics] Start error: %@", error);
        return NO;
    }

    return YES;
}

- (void)stopAll {
    for (CHHapticEngine *engine in _engineMap.allValues) {
        [engine stopWithCompletionHandler:nil];
    }
    [_engineMap removeAllObjects];
}

@end
