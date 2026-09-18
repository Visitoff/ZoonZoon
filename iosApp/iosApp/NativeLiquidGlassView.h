#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// True on iOS 26+ when UIGlassEffect is present at runtime.
BOOL ZZNativeLiquidGlassAvailable(void);

/// UIVisualEffectView backed by UIGlassEffect. Touches pass through to Compose.
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
