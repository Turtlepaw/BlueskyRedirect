package io.github.turtlepaw.blueskyredirect.util

import dev.zwander.shared.util.BaseLaunchStrategyUtils
import io.github.turtlepaw.blueskyredirect.BuildConfig

object LaunchStrategyUtils : BaseLaunchStrategyUtils(
    applicationId = BuildConfig.APPLICATION_ID,
    baseGroupClass = BlueskyClientLaunchStrategy::class,
)
