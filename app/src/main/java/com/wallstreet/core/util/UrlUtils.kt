package com.wallstreet.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Opens [url] in the user's browser.
 *
 * A device with no browser is rare but real (kiosk builds, stripped ROMs), and
 * an unhandled ActivityNotFoundException there would crash the app, so the
 * failure is reported instead of thrown.
 */
fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No app available to open this link", Toast.LENGTH_SHORT).show()
    }
}
