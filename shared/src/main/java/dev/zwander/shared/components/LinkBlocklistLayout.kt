package dev.zwander.shared.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import dev.zwander.shared.data.LinkSelectionData
import dev.zwander.shared.util.collectAvailableDomainsSortedByStatus
import kotlinx.coroutines.launch

@Composable
fun LinkBlocklistLayout(
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
                placeholder = {
                    Text(modifier = Modifier.clearAndSetSemantics {}, text = "Search")
                },
            )
        }

    val domains by collectAvailableDomainsSortedByStatus()

    val filteredDomains by remember {
        derivedStateOf {
            if (textFieldState.text.isBlank()) domains else domains.filter {
                it.host.contains(
                    textFieldState.text,
                    true
                )
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = domains.isEmpty(),
            modifier = Modifier.matchParentSize(),
            label = "domains_crossfade",
        ) { empty ->
            if (empty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularWavyProgressIndicator()
                }
            } else {
                val listState = rememberLazyListState()

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchBar(
                        state = searchBarState,
                        inputField = inputField,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    )

                    ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            state = listState,
                        ) {
                            items(items = filteredDomains, key = { it.host }) {
                                LinkBlocklistItem(
                                    domain = it,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                        .animateItem(),
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = listState,
                    ) {
                        items(items = domains, key = { it.host }) {
                            LinkBlocklistItem(
                                domain = it,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LinkBlocklistItem(
    domain: LinkSelectionData,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val blocked by domain.getLinkBlockedStatusAsState()
    val interactionSource = remember { MutableInteractionSource() }

    ListItem(
        interactionSource = interactionSource,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        content = {
            Text(domain.host)
        },
        trailingContent = {
            Switch(!blocked, null, interactionSource = interactionSource)
        },
        onClick = {
            scope.launch {
                with(domain) {
                    context.updateLinkBlockedStatus(!blocked)
                }
            }
        },
//        checked = !blocked,
//        onCheckedChange = {
//            scope.launch {
//                with (domain) {
//                    context.updateLinkBlockedStatus(!it)
//                }
//            }
//        },
        modifier = modifier,
    )
}
