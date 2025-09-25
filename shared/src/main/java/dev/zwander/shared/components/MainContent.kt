package dev.zwander.shared.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.LinkVerificationModel
import dev.zwander.shared.util.LinkVerifyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    val appModel = LocalAppModel.current
    val context = LocalContext.current

    val verificationStatus by LinkVerifyUtils.rememberLinkVerificationAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = appModel.appName,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            ) {
                AnimatedVisibility(visible = !verificationStatus.verified) {
                    LinkVerifyLayout(
                        refresh = LinkVerificationModel::refresh,
                        missingDomains = verificationStatus.missingDomains,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    AppChooserLayout(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                FooterLayout(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
