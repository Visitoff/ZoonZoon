package com.seashore.zoonzoon.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Opens the App Store write-review page.
 *
 * iOS 18+ forces the deprecated single-arg [UIApplication.openURL] to return NO
 * ("BUG IN CLIENT OF UIKIT … migrate to openURL:options:completionHandler:").
 */
actual fun openStoreReviewPage() {
    val https = NSURL.URLWithString(
        "https://apps.apple.com/app/id1595186051?action=write-review"
    ) ?: return
    val itms = NSURL.URLWithString(
        "itms-apps://apps.apple.com/app/id1595186051?action=write-review"
    )

    dispatch_async(dispatch_get_main_queue()) {
        val app = UIApplication.sharedApplication
        // Prefer App Store scheme; fall back to https.
        val target = itms ?: https
        app.openURL(target, options = emptyMap<Any?, Any>(), completionHandler = { ok ->
            if (ok != true && target != https) {
                app.openURL(https, options = emptyMap<Any?, Any>(), completionHandler = null)
            }
        })
    }
}
