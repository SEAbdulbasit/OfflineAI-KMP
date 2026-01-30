package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.ui.components.ChatBubble
import org.abma.offlinelai_kmp.ui.components.EmptyStateType
import org.abma.offlinelai_kmp.ui.components.EmptyStateView
import org.abma.offlinelai_kmp.ui.components.LoadingIndicator
import org.abma.offlinelai_kmp.ui.components.MessageInput
import org.abma.offlinelai_kmp.ui.theme.GradientEnd
import org.abma.offlinelai_kmp.ui.theme.GradientStart
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
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gemma Icon with gradient
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(GradientStart, GradientEnd)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Gemma AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                StatusIndicator(modelState = uiState.modelState)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        AnimatedVisibility(
                            visible = uiState.messages.isNotEmpty(),
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            IconButton(onClick = { viewModel.clearChat() }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Clear chat",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        EmptyStateView(
                            type = EmptyStateType.NO_MODEL,
                            title = "No Model Loaded",
                            message = "Go to Settings to select and load a Gemma model to start chatting",
                            actionLabel = "Open Settings",
                            onAction = onNavigateToSettings,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.modelState == ModelState.ERROR -> {
                        EmptyStateView(
                            type = EmptyStateType.ERROR,
                            title = "Something went wrong",
                            message = uiState.errorMessage ?: "Failed to load model. Please try again.",
                            actionLabel = "Try Again",
                            onAction = onNavigateToSettings,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.messages.isEmpty() -> {
                        EmptyStateView(
                            type = EmptyStateType.NO_MESSAGES,
                            title = "Start a Conversation",
                            message = "Ask Gemma anything! Your conversations stay private on your device.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 12.dp)
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

            // Divider
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ) {}

            // Message input
            MessageInput(
                value = uiState.currentInput,
                onValueChange = { viewModel.updateInput(it) },
                onSend = { viewModel.sendMessage() },
                enabled = uiState.modelState == ModelState.READY || uiState.modelState == ModelState.GENERATING,
                isGenerating = uiState.modelState == ModelState.GENERATING,
                onStopGeneration = { /* TODO: Implement stop generation */ },
                placeholder = when (uiState.modelState) {
                    ModelState.NOT_LOADED -> "Load a model to start..."
                    ModelState.LOADING -> "Loading model..."
                    ModelState.ERROR -> "Model error..."
                    ModelState.GENERATING -> "Generating..."
                    ModelState.READY -> "Message Gemma..."
                }
            )
        }
    }
}

@Composable
private fun StatusIndicator(modelState: ModelState) {
    val (statusText, statusColor) = when (modelState) {
        ModelState.NOT_LOADED -> "Not loaded" to MaterialTheme.colorScheme.onSurfaceVariant
        ModelState.LOADING -> "Loading..." to MaterialTheme.colorScheme.tertiary
        ModelState.READY -> "Ready" to MaterialTheme.colorScheme.primary
        ModelState.ERROR -> "Error" to MaterialTheme.colorScheme.error
        ModelState.GENERATING -> "Generating..." to MaterialTheme.colorScheme.tertiary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = statusColor
        )
    }
}
