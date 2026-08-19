#import "LiquidGlassView.h"
#import <objc/message.h>
#import <objc/runtime.h>

@implementation LiquidGlassView

+ (BOOL)isAvailable {
    return NSClassFromString(@"UIGlassEffect") != nil;
}

+ (nullable id)makeEffectWithTintR:(CGFloat)r
                             tintG:(CGFloat)g
                             tintB:(CGFloat)b
                             tintA:(CGFloat)a
                       interactive:(BOOL)interactive {
    Class glassClass = NSClassFromString(@"UIGlassEffect");
    if (glassClass == nil) {
        return nil;
    }
    id effect = [[glassClass alloc] init];
    UIColor *tint = [UIColor colorWithRed:r green:g blue:b alpha:a];
    if ([effect respondsToSelector:@selector(setTintColor:)]) {
        ((void (*)(id, SEL, id))objc_msgSend)(effect, @selector(setTintColor:), tint);
    }
    if ([effect respondsToSelector:@selector(setInteractive:)]) {
        ((void (*)(id, SEL, BOOL))objc_msgSend)(effect, @selector(setInteractive:), interactive);
    } else if ([effect respondsToSelector:@selector(setIsInteractive:)]) {
        ((void (*)(id, SEL, BOOL))objc_msgSend)(effect, @selector(setIsInteractive:), interactive);
    }
    return effect;
}

+ (void)styleView:(UIView *)view cornerRadius:(CGFloat)radius {
    view.layer.cornerRadius = radius;
    view.layer.masksToBounds = YES;
    view.clipsToBounds = YES;
    view.userInteractionEnabled = NO;
    view.backgroundColor = UIColor.clearColor;
}

+ (UIView *)viewWithCornerRadius:(CGFloat)radius
                           tintR:(CGFloat)r
                           tintG:(CGFloat)g
                           tintB:(CGFloat)b
                           tintA:(CGFloat)a
                     interactive:(BOOL)interactive {
    id effect = [self makeEffectWithTintR:r tintG:g tintB:b tintA:a interactive:interactive];
    if (effect == nil) {
        UIView *fallback = [[UIView alloc] initWithFrame:CGRectZero];
        fallback.backgroundColor = [UIColor colorWithRed:r green:g blue:b alpha:a];
        [self styleView:fallback cornerRadius:radius];
        return fallback;
    }
    UIVisualEffectView *view = [[UIVisualEffectView alloc] initWithEffect:(UIVisualEffect *)effect];
    [self styleView:view cornerRadius:radius];
    return view;
}

+ (void)updateView:(UIView *)view
     cornerRadius:(CGFloat)radius
            tintR:(CGFloat)r
            tintG:(CGFloat)g
            tintB:(CGFloat)b
            tintA:(CGFloat)a
      interactive:(BOOL)interactive {
    [self styleView:view cornerRadius:radius];
    if (![view isKindOfClass:[UIVisualEffectView class]]) {
        view.backgroundColor = [UIColor colorWithRed:r green:g blue:b alpha:a];
        return;
    }
    id effect = [self makeEffectWithTintR:r tintG:g tintB:b tintA:a interactive:interactive];
    if (effect != nil) {
        ((UIVisualEffectView *)view).effect = (UIVisualEffect *)effect;
    }
}

@end
