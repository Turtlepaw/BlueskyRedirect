package dev.zwander.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zwander.shared.components.LinkBlocklistLayout

class LinkBlocklistActivity : BaseActivity() {
    @Composable
    override fun Content() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { contentPadding ->
            LinkBlocklistLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            )
        }
    }
}
