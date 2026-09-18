#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

BOOL ZZNativeLiquidGlassAvailable(void);

UIView *ZZCreateNativeLiquidGlassView(
    CGFloat cornerRadius,
    CGFloat red,
    CGFloat green,
    CGFloat blue,
    CGFloat alpha,
    BOOL interactive
);

void ZZUpdateNativeLiquidGlassView(
    UIView *view,
    CGFloat cornerRadius,
    CGFloat red,
    CGFloat green,
    CGFloat blue,
    CGFloat alpha,
    BOOL interactive
);

NS_ASSUME_NONNULL_END
