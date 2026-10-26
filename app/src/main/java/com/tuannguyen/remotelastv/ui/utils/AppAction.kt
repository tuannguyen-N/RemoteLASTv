package com.tuannguyen.remotelastv.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

private const val APP_NAME = "RemoteLASTv"
private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.tuannguyen.remotelastv"

fun shareApp(context: Context) {
    val shareText = """
        Check out $APP_NAME!
        Control your TV directly from your phone.

        Download it here:
        $PLAY_STORE_URL
    """.trimIndent()

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Try $APP_NAME")
        putExtra(Intent.EXTRA_TITLE, "Share $APP_NAME")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    val chooser = Intent.createChooser(sendIntent, "Share RemoteLASTv")
    context.startActivity(chooser)
}

fun sendFeedback(context: Context, title: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:example@gmail.com")
        putExtra(Intent.EXTRA_SUBJECT, title)
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