package dev.zwander.mastodonredirect.util

import dev.zwander.shared.BaseFetchActivity

class FetchInstancesActivity : BaseFetchActivity() {
    override val softwareNames: Array<String> = arrayOf(
        "bluesky-pds"
    )
}
