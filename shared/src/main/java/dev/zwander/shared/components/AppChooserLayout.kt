package dev.zwander.shared.components

import android.content.pm.PackageManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import dev.zwander.shared.LaunchIntentCreator
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.LaunchStrategyRootGroup
import dev.zwander.shared.R
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.launchUrl
import dev.zwander.shared.util.rememberMutablePreferenceState

enum class AppChooserView {
    Default,
    Minimal,
}

@Composable
fun AppChooserLayout(
    onStrategySelected: ((LaunchStrategy) -> Unit)? = null,
    showNotInstalled: Boolean = true,
    appChooserView: AppChooserView = AppChooserView.Default,
    modifier: Modifier = Modifier,
) {
    val appModel = LocalAppModel.current
    val prefs = appModel.prefs
    val launchStrategies = appModel.launchStrategyUtils.rememberSortedLaunchStrategies().filter {
        if (appChooserView == AppChooserView.Minimal) {
            it.key != "ASK_EVERY_TIME"
        } else {
            true
        }
    }

    var selectedStrategy by prefs.selectedApp.rememberMutablePreferenceState()
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val items = launchStrategies.filter {
            with(it) {
                if (showNotInstalled) true else context.isInstalled()
            }
        }.sortedWith(
            compareByDescending<LaunchStrategy> {
                with(it) { context.isInstalled() }
            }.thenBy {
                with(it) { context.label }
            }
        )

        LazyVerticalStaggeredGrid(
            modifier = Modifier,
            contentPadding = PaddingValues(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 3.dp,
            columns = AdaptiveMod(minSize = 300.dp, itemCount = launchStrategies.size),
        ) {
            itemsIndexed(
                items, { index, it -> it.labelRes },
            ) { index, strategy ->
                GroupCard(
                    launchStrategy = strategy,
                    isSelected = selectedStrategy == strategy,
                    onStrategySelected = onStrategySelected ?: { selectedStrategy = it },
                    index = index,
                    total = items.size,
                    appChooserView = appChooserView,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (appChooserView == AppChooserView.Default) Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                stringResource(R.string.choose_app_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupCard(
    launchStrategy: LaunchStrategy,
    isSelected: Boolean,
    onStrategySelected: (LaunchStrategy) -> Unit,
    index: Int,
    total: Int,
    appChooserView: AppChooserView,
    modifier: Modifier = Modifier,
) {
    val large = MaterialTheme.shapes.large.topStart
    val small = MaterialTheme.shapes.small.topStart
    val isInstalled = launchStrategy.rememberIsInstalled()
    val context = LocalContext.current

    val clip = if (total <= 1) RoundedCornerShape(large) else RoundedCornerShape(
        topStart = if (index == 0) large else small,
        topEnd = if (index == 0) large else small,
        bottomStart = if (index == total - 1) large else small,
        bottomEnd = if (index == total - 1) large else small
    )
    Card(
        modifier = modifier
            .clip(clip)
            .clickable(
                onClick = {
                    if (isInstalled) {
                        onStrategySelected(launchStrategy)
                    } else if (launchStrategy.sourceUrl != null) {
                        context.launchUrl(launchStrategy.sourceUrl!!)
                    }
                }
            ),
        shape = clip
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().run {
                    return@run if (appChooserView == AppChooserView.Minimal) {
                        padding(vertical = 6.dp, horizontal = 2.dp)
                    } else this
                }
            ) {
                if (appChooserView == AppChooserView.Default) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onStrategySelected(launchStrategy) },
                        enabled = isInstalled,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }


                if (appChooserView == AppChooserView.Minimal) {
                    val packageManager = context.packageManager
                    val icon =
                        if (launchStrategy.intentCreator is LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator) {
                            try {
                                packageManager.getApplicationIcon(
                                    launchStrategy.intentCreator.pkg
                                )
                            } catch (e: PackageManager.NameNotFoundException) {
                                null
                            }
                        } else {
                            null
                        }

                    if (icon != null) {
                        Image(
                            painter = BitmapPainter(icon.toBitmap().asImageBitmap()),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(vertical = 2.dp)
                ) {
                    GroupTitle(strategy = launchStrategy)

                    if (!isInstalled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_file_download_24),
                                contentDescription = stringResource(id = R.string.download),
                                modifier = Modifier.size(20.dp),
                            )

                            Text("Not installed")
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // pushes the icon to the far right

                if (!isInstalled) {
                    Icon(
                        painter = painterResource(id = R.drawable.open_in_new),
                        contentDescription = stringResource(id = R.string.download),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupTitle(
    strategy: LaunchStrategy,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Text(
        text = with(strategy) { context.label },
        style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupRow(
    strategyGroup: LaunchStrategyRootGroup,
    selectedStrategy: LaunchStrategy,
    onStrategySelected: (LaunchStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val sortedStrategies = remember(strategyGroup.children) {
        strategyGroup.children.sortedBy { with(it) { context.label } }
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
        maxItemsInEachRow = 2,
    ) {
        sortedStrategies.forEach { child ->
            SingleCard(
                strategy = child,
                selectedStrategy = selectedStrategy,
                onStrategySelected = onStrategySelected,
                modifier = Modifier.weight(1f),
                enabled = child.rememberIsInstalled(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleCard(
    strategy: LaunchStrategy,
    selectedStrategy: LaunchStrategy,
    onStrategySelected: (LaunchStrategy) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val context = LocalContext.current

    val targetBackgroundColor = if (selectedStrategy == strategy) {
        if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceDim
        }
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val color by animateColorAsState(
        targetValue = targetBackgroundColor,
        label = "SingleCardColor-${strategy.key}",
    )

    val textColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.contentColorFor(targetBackgroundColor),
        label = "SingleCardText-${strategy.key}"
    )

    val enabledContentColor = LocalContentColor.current

    ElevatedCard(
        onClick = { onStrategySelected(strategy) },
        colors = CardDefaults.elevatedCardColors(
            containerColor = color,
            disabledContainerColor = color,
        ),
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = with(strategy) { context.label },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = textColor,
            )

            if (!enabled) {
                strategy.sourceUrl?.let { sourceUrl ->
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides 24.dp,
                    ) {
                        IconButton(
                            onClick = { context.launchUrl(sourceUrl) },
                            enabled = true,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = enabledContentColor,
                            ),
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_file_download_24),
                                contentDescription = stringResource(id = R.string.download),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
