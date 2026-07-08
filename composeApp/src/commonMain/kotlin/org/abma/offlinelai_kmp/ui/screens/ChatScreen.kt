package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.platform.toClipEntry
import org.abma.offlinelai_kmp.ui.components.EmptyStateType
import org.abma.offlinelai_kmp.ui.components.EmptyStateView
import org.abma.offlinelai_kmp.ui.components.LoadingIndicator
import org.abma.offlinelai_kmp.ui.theme.*
import org.abma.offlinelai_kmp.ui.viewmodel.ChatAction
import org.abma.offlinelai_kmp.ui.viewmodel.ChatUiState
import org.abma.offlinelai_kmp.ui.viewmodel.ChatViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = viewModel { ChatViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipBoardSnackBarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onAction(ChatAction.DismissError)
        }
    }

    val onCopyMessage = { chatMessage: ChatMessage ->
        scope.launch {
            clipboard.setClipEntry(chatMessage.content.toClipEntry())
            clipBoardSnackBarHostState.showSnackbar("Message copied")
        }
        Unit
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ChatScreenContent(
            uiState = uiState,
            listState = listState,
            snackbarHostState = snackbarHostState,
            onNavigateToSettings = onNavigateToSettings,
            onClearChat = { viewModel.onAction(ChatAction.ClearChat) },
            onInputChange = { viewModel.onAction(ChatAction.UpdateInput(it)) },
            onSend = { viewModel.onAction(ChatAction.SendMessage) },
            onStopGeneration = { viewModel.onAction(ChatAction.StopGeneration) },
            onCopyMessage = onCopyMessage,
            onRegenerateMessage = { /* TODO: Implement regenerate */ }
        )

        // Dedicated snackbar for clipboard copy confirmation
        SnackbarHost(
            clipBoardSnackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(12.dp),
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    onNavigateToSettings: () -> Unit,
    onClearChat: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStopGeneration: () -> Unit,
    onCopyMessage: (ChatMessage) -> Unit,
    onRegenerateMessage: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        topBar = {
            ChatTopBar(
                modelState = uiState.modelState,
                currentModelPath = uiState.currentModelPath,
                hasMessages = uiState.messages.isNotEmpty(),
                onClearChat = onClearChat,
                onNavigateToSettings = onNavigateToSettings
            )
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
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                    .background(MaterialTheme.extendedColors.chatBackground)
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
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Today header
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainer
                                    ) {
                                        Text(
                                            text = "Today",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            items(
                                items = uiState.messages,
                                key = { it.id }
                            ) { message ->
                                MessageBubble(
                                    message = message,
                                    onCopy = { onCopyMessage(message) },
                                    onRegenerate = { onRegenerateMessage(message) }
                                )
                            }
                        }
                    }
                }
            }

            // Message input
            ChatInputBar(
                value = uiState.currentInput,
                onValueChange = onInputChange,
                onSend = onSend,
                enabled = uiState.modelState == ModelState.READY || uiState.modelState == ModelState.GENERATING,
                isGenerating = uiState.modelState == ModelState.GENERATING,
                onStopGeneration = onStopGeneration,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    modelState: ModelState,
    currentModelPath: String?,
    hasMessages: Boolean,
    onClearChat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current

    // Derive model display name from path
    val modelDisplayName = currentModelPath
        ?.substringAfterLast("/")
        ?.substringBeforeLast(".")
        ?: "No model"

    // Derive status color and label from model state
    val (statusColor, statusLabel) = when (modelState) {
        ModelState.READY -> Color(0xFF22C55E) to modelDisplayName
        ModelState.GENERATING -> Color(0xFF3B82F6) to "Generating..."
        ModelState.LOADING -> Color(0xFFFACC15) to "Loading..."
        ModelState.ERROR -> Color(0xFFEF4444) to "Error"
        ModelState.NOT_LOADED -> Color(0xFF525252) to "No model"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )

                        Text(
                            text = "LocalIntelligence",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Model Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1A1A1A),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )

                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFA1A1A1),
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp
            )
        }
    }
}


@Composable
private fun MessageBubble(
    message: ChatMessage,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.isFromUser
    val extendedColors = MaterialTheme.extendedColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Message bubble
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when {
                    message.isError -> MaterialTheme.colorScheme.errorContainer
                    isUser -> Color(0xFF2563EB)
                    else -> Color(0xFF1A1A1A)
                },
                border = if (!isUser && !message.isError) {
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.1f)
                    )
                } else null
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
                ) {
                    if (message.isStreaming && message.content.isEmpty()) {
                        TypingDots()
                    } else if (message.content.isNotEmpty()) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp
                            ),
                            color = when {
                                message.isError -> MaterialTheme.colorScheme.onErrorContainer
                                isUser -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            // Timestamp and Actions
            Row(
                modifier = Modifier.padding(top = 6.dp, start = if (isUser) 0.dp else 4.dp, end = if (isUser) 4.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isUser && message.content.isNotEmpty() && !message.isStreaming && !message.isError) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        tint = Color(0xFF525252),
                        modifier = Modifier.size(18.dp).pointerInput(Unit) {
                            detectTapGestures(onTap = { onCopy() })
                        }
                    )
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Like",
                        tint = Color(0xFF525252),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                }

                val timestampText = remember(message.timestamp) {
                    val instant = Instant.fromEpochMilliseconds(message.timestamp)
                    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    val hour = localDateTime.hour.toString().padStart(2, '0')
                    val minute = localDateTime.minute.toString().padStart(2, '0')
                    "$hour:$minute"
                }

                Text(
                    text = timestampText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF525252),
                    fontSize = 11.sp
                )
            }
        }
    }
}


@Composable
private fun TypingDots(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 450,
                        delayMillis = index * 180,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            )
        }
    }
}


@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    isGenerating: Boolean,
    onStopGeneration: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = MaterialTheme.extendedColors
    val isDark = LocalIsDarkTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFF0A0A0A),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* TODO: Implement more actions */ },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color(0xFFA1A1A1),
                        modifier = Modifier.size(24.dp)
                    )
                }

                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF525252)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color.White
                    ),
                    enabled = enabled && !isGenerating,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White
                    ),
                    maxLines = 4
                )

                Box(modifier = Modifier.padding(4.dp)) {
                    if (isGenerating) {
                        FilledIconButton(
                            onClick = onStopGeneration,
                            shape = RoundedCornerShape(14.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        FilledIconButton(
                            onClick = onSend,
                            enabled = enabled && value.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF3B82F6),
                                disabledContainerColor = Color(0xFF262626)
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Send",
                                tint = if (enabled && value.isNotBlank()) Color.White else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewChatScreenContent(
    uiState: ChatUiState,
    isDarkTheme: Boolean = false
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    org.abma.offlinelai_kmp.ui.theme.GemmaTheme(darkTheme = isDarkTheme) {
        ChatScreenContent(
            uiState = uiState,
            listState = listState,
            snackbarHostState = snackbarHostState,
            onNavigateToSettings = {},
            onClearChat = {},
            onInputChange = {},
            onSend = {},
            onStopGeneration = {},
            onCopyMessage = {},
            onRegenerateMessage = {}
        )
    }
}

@Preview
@Composable
fun ChatScreenPreviewNoModel() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.NOT_LOADED,
            messages = emptyList()
        )
    )
}

@Preview
@Composable
fun ChatScreenPreviewEmpty() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.READY,
            messages = emptyList()
        )
    )
}

@Preview
@Composable
fun ChatScreenPreviewLoading() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.LOADING,
            loadingProgress = 0.6f,
            messages = emptyList()
        )
    )
}

@Preview
@Composable
fun ChatScreenPreviewWithMessages() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.READY,
            messages = listOf(
                ChatMessage.ai("Hi! 👋\n\nIt's nice to hear from you. What would you like to talk about today? 😊"),
                ChatMessage.user("hi"),
                ChatMessage.ai("Hi! 👋\n\nIt's nice to hear from you. What would you like to talk about today? 😊")
            )
        )
    )
}

@Preview
@Composable
fun ChatScreenPreviewWithMessagesDark() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.READY,
            messages = listOf(
                ChatMessage.ai("Hi! 👋\n\nIt's nice to hear from you. What would you like to talk about today? 😊"),
                ChatMessage.user("hi"),
                ChatMessage.ai("Hi! 👋\n\nIt's nice to hear from you. What would you like to talk about today? 😊")
            )
        ),
        isDarkTheme = true
    )
}

@Preview
@Composable
fun ChatScreenPreviewGenerating() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.GENERATING,
            messages = listOf(
                ChatMessage.user("Tell me a joke"),
                ChatMessage.ai("", isStreaming = true)
            )
        )
    )
}

@Preview
@Composable
fun ChatScreenPreviewError() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.ERROR,
            errorMessage = "Failed to load model. Please check if the model file exists.",
            messages = emptyList()
        )
    )
}

@Preview
@Composable
fun ChatScreenPreviewTyping() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.GENERATING,
            messages = listOf(
                ChatMessage.ai("Hi! How can I help you today?"),
                ChatMessage.user("What is Kotlin Multiplatform?")
            )
        )
    )
}

@Preview
@Composable
fun ChatScreenPreviewWithInput() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.READY,
            currentInput = "Hello, how are you?",
            messages = listOf(
                ChatMessage.ai("Hi! 👋 I'm Gemma, your on-device AI assistant. How can I help you today?")
            )
        )
    )
}

@Preview
@Composable
fun ChatScreenPreviewLongConversation() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.READY,
            messages = listOf(
                ChatMessage.ai("Hello! I'm Gemma, your on-device AI assistant. I can help you with various tasks like answering questions, writing, coding, and more. What would you like to talk about?"),
                ChatMessage.user("Can you explain what Kotlin Multiplatform is?"),
                ChatMessage.ai("Kotlin Multiplatform (KMP) is a technology that allows you to share code between different platforms while still having access to native APIs.\n\n**Key benefits:**\n• Write shared business logic once\n• Native UI for each platform\n• Access platform-specific features\n• Reduced development time\n\nIt's great for mobile apps (iOS/Android), desktop, and web applications!"),
                ChatMessage.user("That sounds interesting! How does it compare to Flutter?"),
                ChatMessage.ai("Great question! Here's a comparison:\n\n**Kotlin Multiplatform:**\n• Shares business logic, native UI\n• Kotlin language\n• Gradual adoption possible\n• Native performance\n\n**Flutter:**\n• Shares everything including UI\n• Dart language\n• Full rewrite needed\n• Near-native performance\n\nKMP is better when you want native look and feel, while Flutter is great for consistent UI across platforms.")
            )
        )
    )
}
