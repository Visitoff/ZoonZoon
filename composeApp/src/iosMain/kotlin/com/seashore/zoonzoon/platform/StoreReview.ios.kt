package com.seashore.zoonzoon.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openStoreReviewPage() {
    val url = NSURL.URLWithString("https://apps.apple.com/app/id1595186051?action=write-review")
        ?: return
    UIApplication.sharedApplication.openURL(url)
}
