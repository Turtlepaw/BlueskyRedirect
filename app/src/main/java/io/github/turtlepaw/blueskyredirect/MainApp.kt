package io.github.turtlepaw.blueskyredirect

import dev.zwander.mastodonredirect.util.FetchInstancesActivity
import dev.zwander.shared.App
import io.github.turtlepaw.blueskyredirect.util.BlueskySocialApp
import io.github.turtlepaw.blueskyredirect.util.LaunchStrategyUtils

class MainApp : App(
    launchStrategyUtils = LaunchStrategyUtils,
    fetchActivity = FetchInstancesActivity::class.java,
    defaultLaunchStrategy = BlueskySocialApp,
    appNameRes = R.string.app_name,
)
