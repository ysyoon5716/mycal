package com.example.mycal.presentation.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mycal.presentation.screens.subscription.components.AddSubscriptionDialog
import com.example.mycal.presentation.screens.subscription.components.SubscriptionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sources by viewModel.sources.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar Subscriptions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.syncAll() },
                        enabled = !uiState.isSyncingAll
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync All")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subscription")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (sources.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No calendar subscriptions",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tap + to add your first subscription",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sources) { source ->
                        SubscriptionItem(
                            source = source,
                            isSyncing = source.id in uiState.syncingSourceIds,
                            onToggleSync = { enabled ->
                                viewModel.toggleSyncEnabled(source.id, enabled)
                            },
                            onSync = {
                                viewModel.syncSource(source.id)
                            },
                            onDelete = {
                                viewModel.deleteSource(source.id)
                            }
                        )
                    }
                }
            }

            if (uiState.isSyncingAll) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }

    if (uiState.showAddDialog) {
        AddSubscriptionDialog(
            state = uiState.addDialogState,
            onUrlChange = viewModel::updateUrl,
            onNameChange = viewModel::updateName,
            onColorChange = viewModel::updateColor,
            onConfirm = viewModel::addSource,
            onDismiss = viewModel::hideAddDialog,
            isLoading = uiState.isLoading
        )
    }
}