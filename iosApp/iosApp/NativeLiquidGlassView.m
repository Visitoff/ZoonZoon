#import "NativeLiquidGlassView.h"

#import <objc/message.h>
#import <objc/runtime.h>

static void ZZConfigureGlassEffect(
    id effect,
    CGFloat red,
    CGFloat green,
    CGFloat blue,
    CGFloat alpha,
    BOOL interactive
) {
    UIColor *tint = [UIColor colorWithRed:red green:green blue:blue alpha:alpha];
    SEL tintSel = NSSelectorFromString(@"setTintColor:");
    if ([effect respondsToSelector:tintSel]) {
        ((void (*)(id, SEL, UIColor *))objc_msgSend)(effect, tintSel, tint);
    }
    SEL interactiveSel = NSSelectorFromString(@"setInteractive:");
    if ([effect respondsToSelector:interactiveSel]) {
        ((void (*)(id, SEL, BOOL))objc_msgSend)(effect, interactiveSel, interactive);
    }
}

static id ZZMakeGlassEffect(CGFloat red, CGFloat green, CGFloat blue, CGFloat alpha, BOOL interactive) {
    Class effectClass = NSClassFromString(@"UIGlassEffect");
    if (effectClass == Nil) {
        return nil;
    }
    id effect = [[effectClass alloc] init];
    if (effect == nil) {
        return nil;
    }
    ZZConfigureGlassEffect(effect, red, green, blue, alpha, interactive);
    return effect;
}

BOOL ZZNativeLiquidGlassAvailable(void) {
    NSOperatingSystemVersion v = {26, 0, 0};
    return [[NSProcessInfo processInfo] isOperatingSystemAtLeastVersion:v]
        && NSClassFromString(@"UIGlassEffect") != Nil;
}

UIView *ZZCreateNativeLiquidGlassView(
    CGFloat cornerRadius,
    CGFloat red,
    CGFloat green,
    CGFloat blue,
    CGFloat alpha,
    BOOL interactive
) {
    UIView *view = nil;
    id effect = ZZMakeGlassEffect(red, green, blue, alpha, interactive);
    if (effect != nil) {
        UIVisualEffectView *effectView =
            [[UIVisualEffectView alloc] initWithEffect:(UIVisualEffect *)effect];
        effectView.backgroundColor = UIColor.clearColor;
        view = effectView;
    } else {
        view = [[UIView alloc] initWithFrame:CGRectZero];
        view.backgroundColor = [UIColor colorWithRed:red green:green blue:blue alpha:alpha];
    }
    view.opaque = NO;
    view.userInteractionEnabled = NO;
    view.layer.cornerRadius = cornerRadius;
    view.clipsToBounds = YES;
    return view;
}

void ZZUpdateNativeLiquidGlassView(
    UIView *view,
    CGFloat cornerRadius,
    CGFloat red,
    CGFloat green,
    CGFloat blue,
    CGFloat alpha,
    BOOL interactive
) {
    view.layer.cornerRadius = cornerRadius;
    view.clipsToBounds = YES;
    if (![view isKindOfClass:[UIVisualEffectView class]]) {
        view.backgroundColor = [UIColor colorWithRed:red green:green blue:blue alpha:alpha];
        return;
    }
    id effect = ZZMakeGlassEffect(red, green, blue, alpha, interactive);
    if (effect == nil) {
        return;
    }
    ((UIVisualEffectView *)view).effect = (UIVisualEffect *)effect;
}
