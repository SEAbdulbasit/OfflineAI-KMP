package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.speech.rememberSpeechRecognizerManager
import org.abma.offlinelai_kmp.platform.toClipEntry
import org.abma.offlinelai_kmp.ui.components.ChatBubble
import org.abma.offlinelai_kmp.ui.components.EmptyStateType
import org.abma.offlinelai_kmp.ui.components.EmptyStateView
import org.abma.offlinelai_kmp.ui.components.LoadingIndicator
import org.abma.offlinelai_kmp.ui.components.MessageInput
import org.abma.offlinelai_kmp.ui.components.RelayAppearanceToggleButton
import org.abma.offlinelai_kmp.ui.components.RelayCommandDeckSheet
import org.abma.offlinelai_kmp.ui.components.RelayPersonaBar
import org.abma.offlinelai_kmp.ui.components.SentientTelemetryHud
import org.abma.offlinelai_kmp.ui.theme.GemmaTheme
import org.abma.offlinelai_kmp.ui.theme.RelayAtmosphere
import org.abma.offlinelai_kmp.ui.theme.RelayBadge
import org.abma.offlinelai_kmp.ui.theme.RelayHealthDot
import org.abma.offlinelai_kmp.ui.theme.RelayMark
import org.abma.offlinelai_kmp.ui.theme.RelayMonoCaps
import org.abma.offlinelai_kmp.ui.theme.RelayRadius
import org.abma.offlinelai_kmp.ui.theme.extendedColors
import org.abma.offlinelai_kmp.ui.viewmodel.ChatAction
import org.abma.offlinelai_kmp.ui.viewmodel.ChatUiState
import org.abma.offlinelai_kmp.ui.viewmodel.ChatViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDemos: () -> Unit = {},
    viewModel: ChatViewModel = viewModel { ChatViewModel() },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipBoardSnackBarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.extendedColors

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onAction(ChatAction.DismissError)
        }
    }

    val copyStringToClipboard: (String, String) -> Unit = { text, notice ->
        scope.launch {
            clipboard.setClipEntry(text.toClipEntry())
            clipBoardSnackBarHostState.showSnackbar(notice)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ChatScreenContent(
            uiState = uiState,
            listState = listState,
            snackbarHostState = snackbarHostState,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToDemos = onNavigateToDemos,
            onClearChat = { viewModel.onAction(ChatAction.ClearChat) },
            onInputChange = { viewModel.onAction(ChatAction.UpdateInput(it)) },
            onSend = { viewModel.onAction(ChatAction.SendMessage) },
            onStopGeneration = { viewModel.onAction(ChatAction.StopGeneration) },
            onCopyMessage = { msg -> copyStringToClipboard(msg.content, "Message copied") },
            onCopyRawText = { code -> copyStringToClipboard(code, "Code copied") },
            onRegenerateMessage = { msg -> viewModel.onAction(ChatAction.RegenerateMessage(msg.id)) },
            onAction = viewModel::onAction
        )

        // Compact Relay obsidian/porcelain pill snackbar for clipboard confirmations
        SnackbarHost(
            clipBoardSnackBarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 86.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(RelayRadius.pill),
                containerColor = colors.surfaceElevated,
                contentColor = colors.textPrimary
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
    onClearChat: () -> Unit = {},
    onInputChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onStopGeneration: () -> Unit = {},
    onCopyMessage: (ChatMessage) -> Unit = {},
    onRegenerateMessage: (ChatMessage) -> Unit = {},
    modifier: Modifier = Modifier,
    onNavigateToDemos: () -> Unit = {},
    onCopyRawText: ((String) -> Unit)? = null,
    onAction: ((ChatAction) -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.extendedColors
    val scope = rememberCoroutineScope()
    val speechRecognizer = rememberSpeechRecognizerManager()
    val isRecording by speechRecognizer.isRecording.collectAsState()
    val recognizedText by speechRecognizer.recognizedText.collectAsState()

    val dispatch: (ChatAction) -> Unit = { action ->
        if (onAction != null) {
            onAction(action)
        } else {
            when (action) {
                is ChatAction.ClearChat -> onClearChat()
                is ChatAction.UpdateInput -> onInputChange(action.text)
                is ChatAction.SendMessage -> onSend()
                is ChatAction.StopGeneration -> onStopGeneration()
                else -> {}
            }
        }
    }

    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            onInputChange(recognizedText)
        }
    }

    val displayedMessages = uiState.filteredMessages
    val showScrollToBottom by remember {
        derivedStateOf { listState.canScrollForward && displayedMessages.size > 2 }
    }

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
                isSearchActive = uiState.isSearchActive,
                isTelemetryExpanded = uiState.isTelemetryExpanded,
                onToggleSearch = { dispatch(ChatAction.ToggleSearch) },
                onToggleTelemetry = { dispatch(ChatAction.ToggleTelemetryHud) },
                onExportChat = { dispatch(ChatAction.ExportChat(true)) },
                onClearChat = onClearChat,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(RelayRadius.sm),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        containerColor = colors.canvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        RelayAtmosphere(
            focused = uiState.modelState == ModelState.GENERATING,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Sentient OS Live Neural Telemetry Strip & Expandable Inspector
                SentientTelemetryHud(
                    latestMetrics = uiState.lastMetrics,
                    performanceStats = uiState.performanceStats,
                    liveTokensPerSecond = uiState.liveTokensPerSecond,
                    liveTokenCount = uiState.liveTokenCount,
                    contextUsagePercent = uiState.contextUsagePercent,
                    estimatedContextTokens = uiState.estimatedContextTokens,
                    maxContextTokens = uiState.maxContextTokens,
                    deviceModel = uiState.deviceModel.ifBlank { "On-Device Engine" },
                    osVersion = uiState.osVersion,
                    platformName = uiState.platformName.ifBlank { "LiteRT" },
                    pinnedMemoryCount = uiState.pinnedMessages.size,
                    isGenerating = uiState.modelState == ModelState.GENERATING,
                    isThrottled = uiState.isThermalThrottled,
                    isExpanded = uiState.isTelemetryExpanded,
                    onToggleExpand = { dispatch(ChatAction.ToggleTelemetryHud) },
                    onResetMetrics = { dispatch(ChatAction.ResetMetrics) },
                    onClearPinnedMemories = { dispatch(ChatAction.ClearPinnedMessages) }
                )

                // Relay Persona / Agent Mode Selector Bar + Offline Workbench Pill
                RelayPersonaBar(
                    activePersona = uiState.activePersona,
                    onSelectPersona = { dispatch(ChatAction.SelectPersona(it)) },
                    customSystemPrompt = uiState.customSystemPrompt,
                    onUpdateCustomPrompt = { dispatch(ChatAction.UpdateCustomSystemPrompt(it)) },
                    onNavigateToDemos = if (uiState.modelState != ModelState.GENERATING && uiState.modelState != ModelState.LOADING) {
                        onNavigateToDemos
                    } else null
                )

                // Collapsible In-Conversation Search Bar
                AnimatedVisibility(
                    visible = uiState.isSearchActive,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ConversationSearchBar(
                        query = uiState.searchQuery,
                        matchCount = displayedMessages.size,
                        totalCount = uiState.messages.size,
                        onQueryChange = { dispatch(ChatAction.SetSearchQuery(it)) },
                        onClose = { dispatch(ChatAction.ToggleSearch) }
                    )
                }

                // Dismissible Status Notification Banner
                AnimatedVisibility(
                    visible = !uiState.statusBannerMessage.isNullOrBlank(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        color = colors.surfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.accentEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = uiState.statusBannerMessage.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Conversation Transcript Stream or Empty / Loading State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        uiState.modelState == ModelState.LOADING -> {
                            LoadingIndicator(
                                message = "Loading Gemma model…",
                                progress = uiState.loadingProgress,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        uiState.modelState == ModelState.NOT_LOADED -> {
                            EmptyStateView(
                                type = EmptyStateType.NO_MODEL,
                                title = "No Model Loaded",
                                message = "Open Runtime & Models settings to select and load an on-device Gemma model.",
                                actionLabel = "Open Runtime & Models",
                                onAction = onNavigateToSettings,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        uiState.modelState == ModelState.ERROR -> {
                            EmptyStateView(
                                type = EmptyStateType.ERROR,
                                title = "Runtime Error",
                                message = uiState.errorMessage ?: "Failed to load model. Please check your model file and try again.",
                                actionLabel = "Open Runtime & Models",
                                onAction = onNavigateToSettings,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        uiState.messages.isEmpty() -> {
                            EmptyStateView(
                                type = EmptyStateType.NO_MESSAGES,
                                title = "What are we working on?",
                                message = "Ask a question, synthesize code, or trigger on-device hardware tools. Conversations stay 100% private on your device.",
                                onSelectStarterPrompt = { prompt -> onInputChange(prompt) },
                                onOpenWorkbench = onNavigateToDemos,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        displayedMessages.isEmpty() && uiState.searchQuery.isNotBlank() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No messages matching “${uiState.searchQuery}”",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Session Journal Header Pill
                                item(key = "session_header") {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(RelayRadius.pill),
                                            color = colors.badgeFill,
                                            border = BorderStroke(1.dp, colors.borderSubtle)
                                        ) {
                                            RelayMonoCaps(
                                                text = "LOCAL JOURNAL · ${uiState.activePersona.displayName}",
                                                color = colors.textMuted,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                items(
                                    items = displayedMessages,
                                    key = { it.id }
                                ) { message ->
                                    ChatBubble(
                                        message = message,
                                        onCopy = { onCopyMessage(message) },
                                        onCopyText = { code ->
                                            if (onCopyRawText != null) {
                                                onCopyRawText(code)
                                            } else {
                                                onCopyMessage(message.copy(content = code))
                                            }
                                        },
                                        onRegenerate = { onRegenerateMessage(message) },
                                        onTogglePin = { dispatch(ChatAction.TogglePinMessage(message.id)) },
                                        onEditUserMessage = { newContent ->
                                            dispatch(ChatAction.EditAndResendMessage(message.id, newContent))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Relay "Latest message ↓" floating jump-to-bottom capsule pill
                    if (showScrollToBottom) {
                        Surface(
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(displayedMessages.size)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(RelayRadius.pill),
                            color = colors.surfaceElevated,
                            border = BorderStroke(1.dp, colors.borderDefault)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Jump to latest message",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Latest message",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }

                // Canonical Relay Composer Dock with Quick Action Chips & Command Deck Trigger
                val composerReady = uiState.modelState == ModelState.READY && !uiState.isToolCallInProgress
                MessageInput(
                    value = uiState.currentInput,
                    onValueChange = onInputChange,
                    onSend = onSend,
                    enabled = uiState.modelState == ModelState.READY || uiState.modelState == ModelState.GENERATING,
                    isGenerating = uiState.modelState == ModelState.GENERATING,
                    isRecording = isRecording,
                    onToggleRecording = {
                        if (isRecording) {
                            speechRecognizer.stopListening()
                        } else {
                            speechRecognizer.startListening()
                        }
                    },
                    onStopGeneration = onStopGeneration,
                    onOpenCommandDeck = { dispatch(ChatAction.ToggleCommandDeck) },
                    quickActions = if (composerReady) {
                        listOf(
                            "Flashlight on" to "Turn on the flashlight",
                            "Flashlight off" to "Turn off the flashlight",
                            "Device time" to "What is the current device time?"
                        )
                    } else emptyList(),
                    onQuickActionClick = { prompt -> onInputChange(prompt) },
                    placeholder = when (uiState.modelState) {
                        ModelState.NOT_LOADED -> "Load a local model to start…"
                        ModelState.LOADING -> "Initializing neural weights…"
                        ModelState.ERROR -> "Model runtime error…"
                        ModelState.GENERATING -> "Synthesizing response…"
                        ModelState.READY -> "Message LocalIntelligence or type / for tools…"
                    }
                )
            }

            // Sentient OS Command & Tool Deck Modal Bottom Sheet
            if (uiState.isCommandDeckOpen) {
                RelayCommandDeckSheet(
                    onDismiss = { dispatch(ChatAction.ToggleCommandDeck) },
                    onExecuteQuickTool = { toolName, argsJson ->
                        dispatch(ChatAction.ExecuteQuickTool(toolName, argsJson))
                    },
                    onFillPrompt = { prompt -> onInputChange(prompt) },
                    onSendPrompt = { prompt -> dispatch(ChatAction.SendPrompt(prompt)) },
                    onExportChat = { shareViaSystem -> dispatch(ChatAction.ExportChat(shareViaSystem)) },
                    onClearChat = onClearChat,
                    onNavigateToDemos = onNavigateToDemos,
                    onNavigateToSettings = onNavigateToSettings,
                    hasMessages = uiState.messages.isNotEmpty()
                )
            }
        }
    }
}

@Composable
private fun ConversationSearchBar(
    query: String,
    matchCount: Int,
    totalCount: Int,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.cardBG)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Find in conversation transcript…",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Text(
                            text = "$matchCount/$totalCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = colors.accentCyan,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary),
                shape = RoundedCornerShape(RelayRadius.sm),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = colors.borderFocus,
                    unfocusedBorderColor = colors.borderSubtle,
                    cursorColor = colors.accentBlue
                )
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close search",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    modelState: ModelState,
    currentModelPath: String?,
    hasMessages: Boolean,
    isSearchActive: Boolean,
    isTelemetryExpanded: Boolean,
    onToggleSearch: () -> Unit,
    onToggleTelemetry: () -> Unit,
    onExportChat: () -> Unit,
    onClearChat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors

    val modelDisplayName = currentModelPath
        ?.substringAfterLast("/")
        ?.substringBeforeLast(".")
        ?: "No model"

    val (statusColor, statusLabel) = when (modelState) {
        ModelState.READY -> colors.accentEmerald to modelDisplayName
        ModelState.GENERATING -> colors.accentBlue to "Generating…"
        ModelState.LOADING -> colors.accentAmber to "Loading…"
        ModelState.ERROR -> colors.accentCoral to "Error"
        ModelState.NOT_LOADED -> colors.textDim to "No model"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.glassBackdrop,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        RelayMark(
                            size = 24.dp,
                            active = modelState == ModelState.GENERATING
                        )

                        Column {
                            Text(
                                text = "LocalIntelligence",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Interactive Model Status Capsule (tapping opens Runtime & Models Settings)
                        Surface(
                            onClick = onNavigateToSettings,
                            shape = RoundedCornerShape(RelayRadius.pill),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.borderDefault)
                        ) {
                            Row(
                                modifier = Modifier
                                    .height(30.dp)
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RelayHealthDot(
                                    color = statusColor,
                                    pulsing = modelState == ModelState.GENERATING || modelState == ModelState.LOADING
                                )
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (hasMessages) {
                            // Search Toggle
                            HeaderActionIconButton(
                                icon = Icons.Default.Search,
                                contentDescription = "Search conversation",
                                active = isSearchActive,
                                onClick = onToggleSearch
                            )

                            // Export Chat
                            HeaderActionIconButton(
                                icon = Icons.Default.IosShare,
                                contentDescription = "Export conversation",
                                onClick = onExportChat
                            )

                            // Clear Chat
                            HeaderActionIconButton(
                                icon = Icons.Default.DeleteSweep,
                                contentDescription = "Clear conversation",
                                tint = colors.accentCoral,
                                onClick = onClearChat
                            )
                        }

                        // Telemetry Inspector Toggle
                        HeaderActionIconButton(
                            icon = Icons.Default.Speed,
                            contentDescription = "Toggle telemetry HUD",
                            active = isTelemetryExpanded,
                            onClick = onToggleTelemetry
                        )

                        // Night / Light Appearance Switcher
                        RelayAppearanceToggleButton()

                        // Settings Button
                        HeaderActionIconButton(
                            icon = Icons.Default.Settings,
                            contentDescription = "Settings",
                            onClick = onNavigateToSettings
                        )
                    }
                }
            )

            HorizontalDivider(
                color = colors.borderSubtle,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun HeaderActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    active: Boolean = false,
    tint: Color? = null
) {
    val colors = MaterialTheme.extendedColors
    Surface(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
        shape = RoundedCornerShape(RelayRadius.xs),
        color = if (active) colors.activeFill else colors.subtleFill,
        border = BorderStroke(1.dp, if (active) colors.borderDefault else colors.borderSubtle)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint ?: if (active) colors.accentCyan else colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PreviewChatScreenContent(
    uiState: ChatUiState,
    isDarkTheme: Boolean = true
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    GemmaTheme(darkTheme = isDarkTheme) {
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
            loadingProgress = 0.64f,
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
                ChatMessage.user("Turn on the flashlight"),
                ChatMessage(
                    content = "Device accepted flashlight ON request.",
                    isFromUser = false,
                    toolCallName = "toggle_torch",
                    toolArgumentsJson = """{"enable":true}""",
                    toolResultText = "Device accepted flashlight ON request."
                )
            )
        ),
        isDarkTheme = false
    )
}

@Preview
@Composable
fun ChatScreenPreviewWithMessagesDark() {
    PreviewChatScreenContent(
        uiState = ChatUiState(
            modelState = ModelState.READY,
            messages = listOf(
                ChatMessage.user("Show a Kotlin StateFlow snippet"),
                ChatMessage.ai("Here is a concise StateFlow pattern:\n```kotlin\nprivate val _state = MutableStateFlow(UiState())\nval state: StateFlow<UiState> = _state.asStateFlow()\n```")
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
                ChatMessage.user("Explain LiteRT on-device inference"),
                ChatMessage.ai("", isStreaming = true)
            )
        )
    )
}
