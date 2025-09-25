package dev.zwander.shared

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import dev.zwander.shared.components.MainContent
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.RedirectorTheme
import dev.zwander.shared.util.locals.LocalLinkSheet
import dev.zwander.shared.util.locals.rememberLinkSheet
import dev.zwander.shared.util.openLinkInBrowser
import dev.zwander.shared.util.prefs
import fe.linksheet.interconnect.LinkSheetConnector
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.fullPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.URLConnection

open class BaseActivity : ComponentActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val linkSheet by rememberLinkSheet()

            CompositionLocalProvider(
                LocalAppModel provides appModel,
                LocalLinkSheet provides linkSheet,
            ) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isSystemInDarkTheme()
                    isAppearanceLightNavigationBars = isAppearanceLightStatusBars
                }

                RedirectorTheme {
                    Content()
                }
            }
        }
    }

    open fun getUrl(): String? {
        return if (intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            intent?.dataString
                ?.replace("web+bluesky+http://", "http://")
                ?.replace("web+bluesky+https://", "https://")
        }
    }

    suspend fun handleLink(url: String? = getUrl(), selectedStrategy: LaunchStrategy? = null) {
        Log.d(packageName, "Handling URL: $url")

        val realReferrer = intent?.let {
            LinkSheetConnector.getLinkSheetReferrer(intent)
        } ?: referrer

        val lastHandledLinkIsTheSame = prefs.lastHandledLink.currentValue(this) == url

        val skipUrl = url?.let {
            prefs.blocklistedDomains.currentValue(this).contains(it.toUri().host)
        } == true

        when {
            url.isNullOrBlank() || isSpecialUrl(url) || skipUrl -> launchInBrowser()
            prefs.openMediaInBrowser.currentValue(this) && isUrlMedia(url) -> launchInBrowser()
            else -> {
                val selectedStrategy = selectedStrategy ?: prefs.selectedApp.currentValue(this)
                Log.d(packageName, "Selected strategy: ${selectedStrategy.key}")

                selectedStrategy.run {
                    val intents = createIntents(url)
                    Log.d(
                        packageName,
                        "Created ${intents.size} intents: ${intents.map { "${it.action} -> ${it.data}" }}"
                    )

                    if (intents.any { it.`package` == realReferrer?.host } && lastHandledLinkIsTheSame) {
                        launchInBrowser()
                        return@run
                    }

                    intents.forEachIndexed { index, intent ->
                        try {
                            Log.d(
                                packageName,
                                "Attempting to launch intent: ${intent.action} -> ${intent.data}"
                            )
                            startActivity(intent)

                            if (!sequentialLaunch) {
                                // Found a working launcher, short circuit out of process.
                                return@run
                            }
                        } catch (e: Exception) {
                            Log.e(
                                packageName,
                                "Error launching intent: ${intent.action} -> ${intent.data}",
                                e
                            )

                            if (!sequentialLaunch) {
                                launchInBrowser()
                                return@run
                            }
                        }

                        if (index < intents.lastIndex) {
                            withContext(Dispatchers.IO) {
                                delay(500)
                            }
                        }
                    }

                    if (!sequentialLaunch) {
                        // Didn't find any working launchers, open browser.
                        launchInBrowser()
                    }
                }
            }
        }

        prefs.lastHandledLink.set(url)

        finish()
    }

    private fun launchInBrowser() {
        openLinkInBrowser(intent?.data)
    }

    private suspend fun isUrlMedia(url: String): Boolean {
        val parsedUrl = try {
            Url(url)
        } catch (_: Exception) {
            null
        }

        parsedUrl?.let {
            val guessedType = URLConnection.guessContentTypeFromName(parsedUrl.fullPath) ?: ""

            if (guessedType.startsWith("video") ||
                guessedType.startsWith("image") ||
                guessedType.startsWith("audio")
            ) {
                return true
            }
        }

        return try {
            val response = HttpClient().get(url)
            val returnedType = response.contentType()?.contentType

            returnedType == "video" ||
                    returnedType == "image" ||
                    returnedType == "audio"
        } catch (_: Exception) {
            false
        }
    }

    private fun isSpecialUrl(url: String): Boolean {
        val parsedUrl = url.toUri()
        val path = parsedUrl.path

        val specialStarters = arrayOf(
            "oauth",
            "auth",
            "miauth",
            "api",
            "api-doc",
        )

        return specialStarters.any { path?.startsWith("/$it") == true }
    }

    @Composable
    open fun Content() {
        MainContent()
    }
}