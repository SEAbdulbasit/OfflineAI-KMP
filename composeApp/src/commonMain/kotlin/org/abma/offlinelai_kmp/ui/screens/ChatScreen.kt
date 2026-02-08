package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.abma.offlinelai_kmp.domain.model.Attachment
import org.abma.offlinelai_kmp.domain.model.AttachmentType
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.picker.AttachmentPickerType
import org.abma.offlinelai_kmp.picker.rememberAttachmentPicker
import org.abma.offlinelai_kmp.ui.components.EmptyStateType
import org.abma.offlinelai_kmp.ui.components.EmptyStateView
import org.abma.offlinelai_kmp.ui.components.LoadingIndicator
import org.abma.offlinelai_kmp.ui.theme.GradientIndigo
import org.abma.offlinelai_kmp.ui.theme.GradientPurple
import org.abma.offlinelai_kmp.ui.theme.LocalThemeToggle
import org.abma.offlinelai_kmp.ui.theme.SlateGray
import org.abma.offlinelai_kmp.ui.theme.extendedColors
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

    // Attachment picker
    val launchAttachmentPicker = rememberAttachmentPicker(
        type = AttachmentPickerType.IMAGES_AND_PDFS
    ) { result ->
        viewModel.setAttachmentLoading(false)
        if (result != null) {
            val attachment = Attachment.fromPath(result.path, result.mimeType)
            viewModel.addAttachment(attachment)
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }

    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.dismissError()
        }
    }

    ChatScreenContent(
        uiState = uiState,
        listState = listState,
        snackbarHostState = snackbarHostState,
        onNavigateToSettings = onNavigateToSettings,
        onClearChat = { viewModel.clearChat() },
        onInputChange = { viewModel.updateInput(it) },
        onSend = { viewModel.sendMessage() },
        onStopGeneration = { /* TODO: Implement stop generation */ },
        onCopyMessage = { /* TODO: Implement copy */ },
        onRegenerateMessage = { /* TODO: Implement regenerate */ },
        onAttachmentClick = {
            viewModel.setAttachmentLoading(true)
            launchAttachmentPicker()
        },
        onRemoveAttachment = { viewModel.removeAttachment(it) }
    )
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
    onAttachmentClick: () -> Unit = {},
    onRemoveAttachment: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ChatTopBar(
                modelState = uiState.modelState,
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
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Today header
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "TODAY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp
                                    )
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
                onAttachmentClick = onAttachmentClick,
                pendingAttachments = uiState.pendingAttachments,
                onRemoveAttachment = onRemoveAttachment,
                isAttachmentLoading = uiState.isAttachmentLoading,
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
    hasMessages: Boolean,
    onClearChat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = MaterialTheme.extendedColors
    val toggleTheme = LocalThemeToggle.current

    Surface(
        modifier = modifier,
        color = extendedColors.headerBackground.copy(alpha = 0.8f),
        tonalElevation = 0.dp
    ) {
        Column {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Gradient avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(GradientIndigo, GradientPurple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Gemma 2b",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusIndicator(modelState = modelState)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = toggleTheme) {
                        Icon(
                            imageVector = Icons.Default.Contrast,
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = hasMessages,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = onClearChat) {
                            Icon(
                                imageVector = Icons.Default.Delete,
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

            // Border divider
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = extendedColors.headerBorder
            ) {}
        }
    }
}

@Composable
private fun StatusIndicator(
    modelState: ModelState,
    modifier: Modifier = Modifier
) {
    val extendedColors = MaterialTheme.extendedColors
    val (statusText, statusColor, showPulse) = when (modelState) {
        ModelState.NOT_LOADED -> Triple("Not loaded", SlateGray, false)
        ModelState.LOADING -> Triple("Loading...", extendedColors.statusLoading, true)
        ModelState.READY -> Triple("Ready • On-device", extendedColors.statusReady, true)
        ModelState.ERROR -> Triple("Error", MaterialTheme.colorScheme.error, false)
        ModelState.GENERATING -> Triple("Generating...", extendedColors.statusLoading, true)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Animated pulse dot
        if (showPulse) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_alpha"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
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
            .padding(horizontal = 16.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                // AI Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(extendedColors.avatarAi),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.widthIn(max = 300.dp),
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {
                // Sender label for AI
                if (!isUser) {
                    Text(
                        text = "Gemma",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                // Message bubble
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    color = when {
                        message.isError -> MaterialTheme.colorScheme.errorContainer
                        isUser -> extendedColors.bubbleUser
                        else -> extendedColors.bubbleAi
                    },
                    shadowElevation = if (isUser) 4.dp else 1.dp,
                    border = if (!isUser && !message.isError) {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            extendedColors.bubbleAiBorder
                        )
                    } else null
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = if (isUser) 16.dp else 14.dp,
                            vertical = if (isUser) 10.dp else 12.dp
                        )
                    ) {
                        // Display attachments if any
                        if (message.attachments.isNotEmpty()) {
                            MessageAttachments(
                                attachments = message.attachments,
                                isUser = isUser
                            )
                            if (message.content.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        if (message.isStreaming && message.content.isEmpty()) {
                            TypingDots()
                        } else if (message.content.isNotEmpty()) {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 22.sp
                                ),
                                fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                                color = when {
                                    message.isError -> MaterialTheme.colorScheme.onErrorContainer
                                    isUser -> Color.White
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(Modifier.width(12.dp))
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(extendedColors.bubbleUser),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(28.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TypingIndicatorBubble(
    modifier: Modifier = Modifier
) {
    val extendedColors = MaterialTheme.extendedColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // AI Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(extendedColors.avatarAi),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(12.dp))

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 4.dp,
                bottomEnd = 16.dp
            ),
            color = extendedColors.bubbleAi,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                extendedColors.bubbleAiBorder
            )
        ) {
            TypingDots(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
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
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400,
                        delayMillis = index * 150,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun AttachmentsPreview(
    attachments: List<Attachment>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        attachments.forEach { attachment ->
            AttachmentChip(
                attachment = attachment,
                onRemove = { onRemove(attachment.id) }
            )
        }
    }
}

@Composable
private fun AttachmentChip(
    attachment: Attachment,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon based on attachment type
            Icon(
                imageVector = when (attachment.type) {
                    AttachmentType.IMAGE -> Icons.Default.Image
                    AttachmentType.PDF -> Icons.Default.PictureAsPdf
                    AttachmentType.DOCUMENT -> Icons.Default.Description
                },
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // File name
            Text(
                text = attachment.fileName.take(20) + if (attachment.fileName.length > 20) "..." else "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove attachment",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MessageAttachments(
    attachments: List<Attachment>,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        attachments.forEach { attachment ->
            MessageAttachmentItem(
                attachment = attachment,
                isUser = isUser
            )
        }
    }
}

@Composable
private fun MessageAttachmentItem(
    attachment: Attachment,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isUser) {
                    Color.White.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = when (attachment.type) {
                AttachmentType.IMAGE -> Icons.Default.Image
                AttachmentType.PDF -> Icons.Default.PictureAsPdf
                AttachmentType.DOCUMENT -> Icons.Default.Description
            },
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
        )

        Text(
            text = attachment.fileName.take(25) + if (attachment.fileName.length > 25) "..." else "",
            style = MaterialTheme.typography.labelSmall,
            color = if (isUser) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
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
    onAttachmentClick: () -> Unit,
    pendingAttachments: List<Attachment>,
    onRemoveAttachment: (String) -> Unit,
    isAttachmentLoading: Boolean,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = MaterialTheme.extendedColors

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column {
            // Pending attachments preview
            AnimatedVisibility(
                visible = pendingAttachments.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                AttachmentsPreview(
                    attachments = pendingAttachments,
                    onRemove = onRemoveAttachment,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Divider
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = extendedColors.headerBorder.copy(alpha = 0.5f)
            ) {}

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Input field
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = extendedColors.inputBackground
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = enabled && !isGenerating,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (value.isNotBlank() && enabled && !isGenerating) {
                                        onSend()
                                    }
                                }
                            ),
                            singleLine = false,
                            maxLines = 4
                        )

                        // Attach button
                        IconButton(
                            onClick = onAttachmentClick,
                            enabled = enabled && !isGenerating && !isAttachmentLoading,
                            modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                        ) {
                            if (isAttachmentLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Attach file",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Send/Stop button
                AnimatedVisibility(
                    visible = isGenerating,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    FilledIconButton(
                        onClick = onStopGeneration,
                        modifier = Modifier.size(46.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop generation",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !isGenerating,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    FilledIconButton(
                        onClick = onSend,
                        enabled = enabled && value.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(
                                elevation = if (enabled && value.isNotBlank()) 8.dp else 0.dp,
                                shape = CircleShape,
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = if (enabled && value.isNotBlank()) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            },
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(-45f)
                        )
                    }
                }
            }

            // Home indicator bar (iOS style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(128.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
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

