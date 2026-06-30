package com.seashore.zoonzoon.platform

import android.content.Intent
import android.net.Uri
import com.seashore.zoonzoon.appContext

actual fun openStoreReviewPage() {
    val context = appContext
    val packageName = context.packageName
    val marketUri = Uri.parse("market://details?id=$packageName")
    val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    val intent = Intent(Intent.ACTION_VIEW, marketUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
