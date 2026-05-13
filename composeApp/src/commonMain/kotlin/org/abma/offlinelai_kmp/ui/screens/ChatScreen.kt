package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.ui.components.*
import org.abma.offlinelai_kmp.ui.viewmodel.ChatAction
import org.abma.offlinelai_kmp.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = viewModel { ChatViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemma AI") },
                actions = {
                    if (uiState.messages.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onAction(ChatAction.ClearChat) }) {
                            Icon(Icons.Default.Delete, "Clear")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().imePadding()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (uiState.modelState) {
                    ModelState.LOADING -> LoadingIndicator("Loading Gemma...", uiState.loadingProgress)
                    ModelState.NOT_LOADED -> EmptyStateView(
                        title = "No Model Loaded",
                        message = "Go to settings to load a Gemma model",
                        actionLabel = "Settings",
                        onAction = onNavigateToSettings
                    )
                    ModelState.ERROR -> EmptyStateView(
                        title = "Error",
                        message = uiState.errorMessage ?: "Unknown error",
                        actionLabel = "Retry",
                        onAction = onNavigateToSettings
                    )
                    else -> ChatMessageList(uiState, listState)
                }
            }

            MessageInput(
                value = uiState.currentInput,
                onValueChange = { viewModel.onAction(ChatAction.UpdateInput(it)) },
                onSend = { viewModel.onAction(ChatAction.SendMessage) },
                enabled = uiState.modelState == ModelState.READY,
                isGenerating = uiState.modelState == ModelState.GENERATING
            )
        }
    }
}

@Composable
private fun ChatMessageList(uiState: org.abma.offlinelai_kmp.ui.viewmodel.ChatUiState, listState: androidx.compose.foundation.lazy.LazyListState) {
    if (uiState.messages.isEmpty()) {
        EmptyStateView(title = "Gemma is ready", message = "Start a conversation!")
    } else {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            items(uiState.messages) { message ->
                ChatBubble(message)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
