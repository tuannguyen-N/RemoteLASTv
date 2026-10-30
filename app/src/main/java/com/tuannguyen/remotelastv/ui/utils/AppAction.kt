package com.tuannguyen.remotelastv.ui.utils

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.net.toUri
import com.tuannguyen.remotelastv.R

private const val APP_NAME = "RemoteLASTv"
private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.tuannguyen.remotelastv"

fun shareApp(context: Context) {
    val shareText = context.getString(R.string.share_message, APP_NAME, PLAY_STORE_URL)

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject, APP_NAME))
        putExtra(Intent.EXTRA_TITLE, context.getString(R.string.share_chooser_title))
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    val chooser = Intent.createChooser(
        sendIntent,
        context.getString(R.string.share_chooser_title),
    )
    context.startActivity(chooser)
}

fun sendFeedback(context: Context, @StringRes titleResId: Int) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:tuannguyencode@gmail.com".toUri()
        putExtra(Intent.EXTRA_SUBJECT, context.getString(titleResId))
    }

    runCatching {
        context.startActivity(intent)
    }
}

fun openPrivacyPolicy(context: Context) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        "https://sites.google.com/view/remotelatvprivacypolicy".toUri()
    )
    context.startActivity(intent)
}