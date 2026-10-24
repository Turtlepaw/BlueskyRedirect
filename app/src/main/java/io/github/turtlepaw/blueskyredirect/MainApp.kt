package io.github.turtlepaw.blueskyredirect

import androidx.core.net.toUri
import dev.zwander.mastodonredirect.util.FetchInstancesActivity
import dev.zwander.shared.App
import io.github.turtlepaw.blueskyredirect.util.BlueskySocialApp
import io.github.turtlepaw.blueskyredirect.util.LaunchStrategyUtils

class MainApp : App(
    launchStrategyUtils = LaunchStrategyUtils,
    fetchActivity = FetchInstancesActivity::class.java,
    defaultLaunchStrategy = BlueskySocialApp,
    resolveUrl = {
        val uri = it.toUri()

        if (uri.host == "aturi.to" && uri.path?.startsWith("explore") == true) {
            null
        } else {
            it
        }
    },
    appNameRes = R.string.app_name,
)
