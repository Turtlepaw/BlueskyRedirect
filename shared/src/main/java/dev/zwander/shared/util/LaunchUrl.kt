package dev.zwander.shared.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Modified from https://github.com/zacharee/PatreonSupportersRetrieval/blob/d2e9143db29e8a0efbb5b4246bfb077231e36560/app/src/main/java/tk/zwander/patreonsupportersretrieval/util/Misc.kt#L9
 */
fun Context.launchUrl(url: String) {
    try {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(browserIntent)
    } catch (e: Exception) {}
}