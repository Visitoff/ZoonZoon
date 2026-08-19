#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// Native iOS 26 Liquid Glass (`UIGlassEffect`). Resolved at runtime so the
/// project still compiles against the iOS 18 SDK. Returns NO on iOS 18.
@interface LiquidGlassView : NSObject

+ (BOOL)isAvailable;

+ (UIView *)viewWithCornerRadius:(CGFloat)radius
                           tintR:(CGFloat)r
                           tintG:(CGFloat)g
                           tintB:(CGFloat)b
                           tintA:(CGFloat)a
                     interactive:(BOOL)interactive;

+ (void)updateView:(UIView *)view
     cornerRadius:(CGFloat)radius
            tintR:(CGFloat)r
            tintG:(CGFloat)g
            tintB:(CGFloat)b
            tintA:(CGFloat)a
      interactive:(BOOL)interactive;

@end

NS_ASSUME_NONNULL_END
