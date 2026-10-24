package dev.zwander.shared

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.zwander.shared.components.AppChooserLayout
import dev.zwander.shared.components.AppChooserView
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.openLinkInBrowser
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LinkSheet : BaseActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val anim = remember { Animatable(Color.Transparent) }
        val targetColor = BottomSheetDefaults.ScrimColor

        val appModel = LocalAppModel.current
        val intentUrl = intent.getStringExtra("url")
        val url = intentUrl?.let { appModel.resolveUrl(it) }

        LaunchedEffect(Unit) {
            delay(200) // wait 1 second
            anim.animateTo(targetColor, animationSpec = tween(1000)) // fade over 1 second
        }

        ModalBottomSheet(
            onDismissRequest = {
                finish()
            },
            scrimColor = anim.value
        ) {
            val type = getUrlType()
            val locale = LocalLocale.current.platformLocale
            if (url == null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(R.drawable.error_24px),
                            contentDescription = stringResource(R.string.error),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(
                                R.string.failed_to_resolve
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                MaterialTheme.shapes.medium
                            )
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.shapes.medium
                            )
                            .padding(16.dp)
                    ) {
                        Text(intentUrl ?: stringResource(R.string.no_url_provided))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { finish() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                stringResource(android.R.string.cancel)
                            )
                        }
                        Button(
                            onClick = { openLinkInBrowser(intent?.data) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.open_in_browser)
                                )
                                Icon(
                                    painterResource(R.drawable.open_in_new),
                                    contentDescription = stringResource(R.string.open_in_browser)
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = if (type != null) {
                        stringResource(
                            R.string.open_with_type,
                            stringResource(type).lowercase(locale)
                        )
                    } else {
                        stringResource(R.string.open_with)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, bottom = 2.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
                AppChooserLayout(
                    onStrategySelected = {
                        coroutineScope.launch {
                            handleLink(url, it)
                        }
                    },
                    showNotInstalled = false,
                    appChooserView = AppChooserView.Minimal
                )
            }
        }
    }

    override fun getUrl(): String? {
        return intent.getStringExtra("url") ?: return null
    }

    fun getUrlType(): Int? {
        val url = getUrl()
        val uri = try {
            java.net.URI(url)
        } catch (e: Exception) {
            return null
        }

        val segments = uri.path.split("/").filter { it.isNotBlank() }
        if (segments.isEmpty()) return null

        // Map segment names to resource IDs
        val typeMap = mapOf(
            "starter-pack" to R.string.type_starter_pack,
            "lists" to R.string.type_list,
            "post" to R.string.type_post,
            "feed" to R.string.type_feed,
            "profile" to R.string.type_profile,
        )

        return when (segments[0].lowercase()) {
            "profile" -> {
                // Check second or third segment for specific types
                val key = segments.getOrNull(2) ?: segments.getOrNull(0)
                typeMap[key?.lowercase()] ?: R.string.type_profile
            }

            else -> typeMap[segments[0].lowercase()]
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}