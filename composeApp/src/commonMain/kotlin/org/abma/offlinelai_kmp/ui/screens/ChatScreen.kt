package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.ui.components.ChatBubble
import org.abma.offlinelai_kmp.ui.components.LoadingIndicator
import org.abma.offlinelai_kmp.ui.components.MessageInput
import org.abma.offlinelai_kmp.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = viewModel { ChatViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Show error message
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gemma AI")
                        Text(
                            text = when (uiState.modelState) {
                                ModelState.NOT_LOADED -> "Model not loaded"
                                ModelState.LOADING -> "Loading model..."
                                ModelState.READY -> "Ready"
                                ModelState.ERROR -> "Error"
                                ModelState.GENERATING -> "Generating..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (uiState.messages.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Text("🗑️")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Text("⚙️")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Messages list or loading/empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        uiState.modelState == ModelState.LOADING -> {
                            LoadingIndicator(
                                message = "Loading Gemma model...",
                                progress = uiState.loadingProgress,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        uiState.modelState == ModelState.NOT_LOADED -> {
                            EmptyState(
                                title = "No Model Loaded",
                                message = "Go to Settings to load a Gemma model",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        uiState.modelState == ModelState.ERROR -> {
                            EmptyState(
                                title = "Error",
                                message = uiState.errorMessage ?: "Failed to load model",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        uiState.messages.isEmpty() -> {
                            EmptyState(
                                title = "Start a Conversation",
                                message = "Ask Gemma anything!",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(
                                    items = uiState.messages,
                                    key = { it.id }
                                ) { message ->
                                    ChatBubble(message = message)
                                }
                            }
                        }
                    }
                }

                // Message input
                MessageInput(
                    value = uiState.currentInput,
                    onValueChange = { viewModel.updateInput(it) },
                    onSend = { viewModel.sendMessage() },
                    enabled = uiState.modelState == ModelState.READY,
                    placeholder = when (uiState.modelState) {
                        ModelState.NOT_LOADED -> "Load a model first..."
                        ModelState.LOADING -> "Loading model..."
                        ModelState.ERROR -> "Model error..."
                        ModelState.GENERATING -> "Generating response..."
                        ModelState.READY -> "Type a message..."
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
