#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

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
