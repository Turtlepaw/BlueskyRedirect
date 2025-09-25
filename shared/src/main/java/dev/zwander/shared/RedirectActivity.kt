package dev.zwander.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext

class RedirectActivity : BaseActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val density = LocalDensity.current

        LaunchedEffect(null) {
            withContext(Dispatchers.IO) {
                handleLink()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            ModalBottomSheet(
                onDismissRequest = {
                    finish()
                },
                sheetState = remember {
                    SheetState(
                        skipPartiallyExpanded = true,
                        density = density,
                        initialValue = SheetValue.Expanded,
                        confirmValueChange = { false },
                    )
                },
                dragHandle = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = stringResource(id = R.string.opening_link),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.size(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 128.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}