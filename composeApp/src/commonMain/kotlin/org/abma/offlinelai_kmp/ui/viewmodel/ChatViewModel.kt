package org.abma.offlinelai_kmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.GenerationMetrics
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.model.PerformanceStats
import org.abma.offlinelai_kmp.domain.model.PersonaPreset
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.domain.repository.ModelRepository
import org.abma.offlinelai_kmp.domain.usecase.*
import org.abma.offlinelai_kmp.inference.GemmaInference
import org.abma.offlinelai_kmp.inference.MetricsCollector
import org.abma.offlinelai_kmp.inference.PromptBuilder
import org.abma.offlinelai_kmp.inference.ThermalMonitor
import org.abma.offlinelai_kmp.inference.TrimmingStrategy
import org.abma.offlinelai_kmp.tools.AppActionsProvider
import org.abma.offlinelai_kmp.tools.PlatformInfoProvider
import org.abma.offlinelai_kmp.tools.ToolCall
import org.abma.offlinelai_kmp.tools.ToolRegistry
import org.abma.offlinelai_kmp.tools.ToolSpec
import org.abma.offlinelai_kmp.tools.buildSystemPrompt
import org.abma.offlinelai_kmp.tools.createDefaultToolRegistry

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: UI State - Single Source of Truth (Relay & Sentient OS Enhanced)
 * ═══════════════════════════════════════════════════════════════════════════════
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val modelState: ModelState = ModelState.NOT_LOADED,
    val loadingProgress: Float = 0f,
    val currentInput: String = "",
    val errorMessage: String? = null,
    val currentModelPath: String? = null,
    val loadedModels: List<LoadedModel> = emptyList(),
    val isToolCallInProgress: Boolean = false,
    // Sentient OS & Relay Configuration + Telemetry State
    val activeConfig: ModelConfig = ModelConfig(),
    val activePersona: PersonaPreset = PersonaPreset.DEFAULT,
    val customSystemPrompt: String = "",
    val trimmingStrategy: TrimmingStrategy = TrimmingStrategy.KEEP_RECENT,
    val isTelemetryExpanded: Boolean = false,
    val isCommandDeckOpen: Boolean = false,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val lastMetrics: GenerationMetrics? = null,
    val performanceStats: PerformanceStats = PerformanceStats.fromMetrics(emptyList()),
    val liveTokensPerSecond: Double = 0.0,
    val liveTokenCount: Int = 0,
    val contextUsagePercent: Float = 0f,
    val estimatedContextTokens: Int = 0,
    val maxContextTokens: Int = PromptBuilder.MAX_CONTEXT_TOKENS,
    val isThermalThrottled: Boolean = false,
    val platformName: String = "",
    val deviceModel: String = "",
    val osVersion: String = "",
    val availableTools: List<ToolSpec> = emptyList(),
    val statusBannerMessage: String? = null
) {
    /** Active pinned messages acting as persistent context memory cards. */
    val pinnedMessages: List<ChatMessage>
        get() = messages.filter { it.isPinned }

    /** Messages filtered by the active search query (or all messages when query is blank). */
    val filteredMessages: List<ChatMessage>
        get() = if (searchQuery.isBlank()) {
            messages
        } else {
            messages.filter { msg ->
                msg.content.contains(searchQuery, ignoreCase = true) ||
                    (msg.toolCallName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: ChatViewModel - The Orchestrator
 * ═══════════════════════════════════════════════════════════════════════════════
 */
class ChatViewModel : ViewModel() {

    // ═══════════════════════════════════════════════════════════════════════════
    // STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // ═══════════════════════════════════════════════════════════════════════════
    // DEPENDENCIES
    // ═══════════════════════════════════════════════════════════════════════════

    private val gemmaInference by lazy { GemmaInference() }
    private val modelRepository by lazy { ModelRepository() }
    private val toolRegistry: ToolRegistry by lazy { createDefaultToolRegistry() }

    private var streamingMessageId: String? = null
    private var generationJob: Job? = null

    // ═══════════════════════════════════════════════════════════════════════════
    // USE CASES (Clean Architecture)
    // ═══════════════════════════════════════════════════════════════════════════

    private val loadModelUseCase by lazy { LoadModelUseCase(gemmaInference, modelRepository) }
    private val generateResponseUseCase by lazy { GenerateResponseUseCase(gemmaInference) }
    private val executeToolUseCase by lazy { ExecuteToolUseCase(toolRegistry) }
    private val getLoadedModelsUseCase by lazy { GetLoadedModelsUseCase(modelRepository) }
    private val removeModelUseCase by lazy { RemoveModelUseCase(modelRepository) }

    /** Base tool-calling system prompt */
    private val toolSystemPrompt: String by lazy {
        buildSystemPrompt(toolRegistry.specs())
    }

    val demos by lazy {
        org.abma.offlinelai_kmp.demo.DemoController(
            scope = viewModelScope,
            acquire = {
                val current = _uiState.value
                current.modelState == ModelState.READY &&
                    _uiState.compareAndSet(current, current.copy(modelState = ModelState.GENERATING))
            },
            release = { _uiState.update { it.copy(modelState = ModelState.READY) } },
            generate = { prompt -> gemmaInference.generateResponse(org.abma.offlinelai_kmp.inference.formatPrompt(prompt)) }
        )
    }

    init {
        initializeDeviceAndToolsMetadata()
        onAction(ChatAction.RefreshModels)
    }

    private fun initializeDeviceAndToolsMetadata() {
        val platformName = runCatching { PlatformInfoProvider.getPlatformName() }.getOrDefault("KMP")
        val deviceModel = runCatching { PlatformInfoProvider.getDeviceModel() }.getOrDefault("On-Device")
        val osVersion = runCatching { PlatformInfoProvider.getOsVersion() }.getOrDefault("")
        val specs = runCatching { toolRegistry.specs() }.getOrDefault(emptyList())

        _uiState.update { state ->
            state.copy(
                platformName = platformName,
                deviceModel = deviceModel,
                osVersion = osVersion,
                availableTools = specs
            )
        }
    }

    /**
     * Build the combined system prompt including active persona, pinned memory cards, and tool specs.
     */
    private fun currentCombinedSystemPrompt(): String {
        val state = _uiState.value
        return PromptBuilder.buildCombinedSystemPrompt(
            preset = state.activePersona,
            customPrompt = state.customSystemPrompt,
            toolInstructions = toolSystemPrompt,
            pinnedMessages = state.pinnedMessages
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ACTION HANDLER (MVI Pattern)
    // ═══════════════════════════════════════════════════════════════════════════

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.LoadModel -> loadModel(action.path, action.config)
            is ChatAction.RemoveModel -> removeLoadedModel(action.path)
            is ChatAction.UpdateInput -> updateInput(action.text)
            is ChatAction.SendMessage -> sendMessage()
            is ChatAction.SendPrompt -> sendDirectPrompt(action.prompt)
            is ChatAction.StopGeneration -> stopGeneration()
            is ChatAction.ClearChat -> clearChat()
            is ChatAction.DismissError -> dismissError()
            is ChatAction.RefreshModels -> refreshLoadedModels()
            is ChatAction.SelectPersona -> selectPersona(action.persona)
            is ChatAction.UpdateCustomSystemPrompt -> updateCustomSystemPrompt(action.prompt)
            is ChatAction.UpdateModelConfig -> updateModelConfig(action.config)
            is ChatAction.UpdateTrimmingStrategy -> updateTrimmingStrategy(action.strategy)
            is ChatAction.RegenerateMessage -> regenerateMessage(action.messageId)
            is ChatAction.EditAndResendMessage -> editAndResendMessage(action.messageId, action.newContent)
            is ChatAction.TogglePinMessage -> togglePinMessage(action.messageId)
            is ChatAction.ClearPinnedMessages -> clearPinnedMessages()
            is ChatAction.ToggleTelemetryHud -> toggleTelemetryHud()
            is ChatAction.ToggleCommandDeck -> toggleCommandDeck()
            is ChatAction.ToggleSearch -> toggleSearch()
            is ChatAction.SetSearchQuery -> setSearchQuery(action.query)
            is ChatAction.ExportChat -> exportChat(action.shareViaSystemSheet)
            is ChatAction.ResetMetrics -> resetMetrics()
            is ChatAction.ExecuteQuickTool -> executeQuickTool(action.toolName, action.argumentsJson)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MODEL MANAGEMENT & RUNTIME CONFIG
    // ═══════════════════════════════════════════════════════════════════════════

    private fun refreshLoadedModels() {
        getLoadedModelsUseCase().onSuccess { models ->
            val currentPath = modelRepository.getCurrentModelPath()
            val activeModelConfig = models.firstOrNull { it.path == currentPath }?.config
                ?: _uiState.value.activeConfig
            _uiState.update {
                it.copy(
                    loadedModels = models,
                    currentModelPath = currentPath,
                    activeConfig = activeModelConfig
                )
            }
        }
    }

    private fun loadModel(modelPath: String, config: ModelConfig = _uiState.value.activeConfig) {
        if (_uiState.value.modelState in listOf(ModelState.LOADING, ModelState.GENERATING)) return
        _uiState.update {
            it.copy(
                modelState = ModelState.LOADING,
                errorMessage = null,
                activeConfig = config
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            loadModelUseCase(modelPath, config)
                .onSuccess { loadedModel ->
                    _uiState.update {
                        it.copy(
                            modelState = ModelState.READY,
                            loadingProgress = 1f,
                            currentModelPath = loadedModel.path,
                            loadedModels = modelRepository.getLoadedModels(),
                            activeConfig = loadedModel.config
                        )
                    }
                    recalculateContextUsage()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            modelState = ModelState.ERROR,
                            errorMessage = exception.message ?: "Failed to load model",
                            loadingProgress = 0f
                        )
                    }
                }
        }
    }

    private fun removeLoadedModel(path: String) {
        removeModelUseCase(path)
        refreshLoadedModels()
    }

    private fun updateModelConfig(config: ModelConfig) {
        _uiState.update { it.copy(activeConfig = config) }
        val currentPath = _uiState.value.currentModelPath
        if (currentPath != null) {
            val existing = _uiState.value.loadedModels.firstOrNull { it.path == currentPath }
            if (existing != null) {
                modelRepository.saveModel(existing.copy(config = config))
                refreshLoadedModels()
            }
        }
        recalculateContextUsage()
    }

    private fun selectPersona(persona: PersonaPreset) {
        _uiState.update { it.copy(activePersona = persona) }
        recalculateContextUsage()
    }

    private fun updateCustomSystemPrompt(prompt: String) {
        _uiState.update { it.copy(customSystemPrompt = prompt) }
        recalculateContextUsage()
    }

    private fun updateTrimmingStrategy(strategy: TrimmingStrategy) {
        _uiState.update { it.copy(trimmingStrategy = strategy) }
        recalculateContextUsage()
    }

    private fun toggleTelemetryHud() {
        _uiState.update { it.copy(isTelemetryExpanded = !it.isTelemetryExpanded) }
    }

    private fun toggleCommandDeck() {
        _uiState.update { it.copy(isCommandDeckOpen = !it.isCommandDeckOpen) }
    }

    private fun toggleSearch() {
        _uiState.update {
            val nextActive = !it.isSearchActive
            it.copy(
                isSearchActive = nextActive,
                searchQuery = if (nextActive) it.searchQuery else ""
            )
        }
    }

    private fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun resetMetrics() {
        MetricsCollector.reset()
        ThermalMonitor.reset()
        _uiState.update {
            it.copy(
                lastMetrics = null,
                performanceStats = PerformanceStats.fromMetrics(emptyList()),
                liveTokensPerSecond = 0.0,
                liveTokenCount = 0,
                isThermalThrottled = false
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CHAT INPUT, REGENERATION, BRANCHING & PINNED MEMORIES
    // ═══════════════════════════════════════════════════════════════════════════

    private fun updateInput(input: String) {
        _uiState.update { it.copy(currentInput = input) }
        recalculateContextUsage()
    }

    private fun sendDirectPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { it.copy(currentInput = trimmed, isCommandDeckOpen = false) }
        sendMessage()
    }

    private fun sendMessage() {
        val input = _uiState.value.currentInput.trim()

        if (input.isEmpty()) return
        if (_uiState.value.modelState != ModelState.READY) return

        val userMessage = ChatMessage.user(input)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                currentInput = "",
                modelState = ModelState.GENERATING,
                isCommandDeckOpen = false
            )
        }

        recalculateContextUsage()
        generateResponse(input, _uiState.value.messages.dropLast(1))
    }

    /**
     * Regenerate an AI response.
     * If [messageId] points to an AI message, removes from that AI message onward and regenerates
     * from the preceding user message. If [messageId] is null, regenerates the last user turn.
     */
    private fun regenerateMessage(messageId: String?) {
        if (_uiState.value.modelState != ModelState.READY) return
        val currentMessages = _uiState.value.messages
        if (currentMessages.isEmpty()) return

        val targetIndex = if (messageId != null) {
            currentMessages.indexOfFirst { it.id == messageId }
        } else {
            currentMessages.indexOfLast { !it.isFromUser }
        }

        // Find the user message that triggered this response
        val searchBeforeIndex = if (targetIndex >= 0) targetIndex else currentMessages.lastIndex
        val userMsgIndex = (searchBeforeIndex downTo 0).firstOrNull { currentMessages[it].isFromUser }
            ?: return

        val userMessage = currentMessages[userMsgIndex]
        val historyBeforeUser = currentMessages.subList(0, userMsgIndex)

        _uiState.update { state ->
            state.copy(
                messages = historyBeforeUser + userMessage,
                modelState = ModelState.GENERATING
            )
        }

        recalculateContextUsage()
        generateResponse(userMessage.content, historyBeforeUser)
    }

    /**
     * Edit a user message in-place, branch the conversation from that point, and generate a new AI response.
     */
    private fun editAndResendMessage(messageId: String, newContent: String) {
        val trimmed = newContent.trim()
        if (trimmed.isEmpty() || _uiState.value.modelState != ModelState.READY) return

        val currentMessages = _uiState.value.messages
        val userIndex = currentMessages.indexOfFirst { it.id == messageId && it.isFromUser }
        if (userIndex < 0) return

        val historyBeforeUser = currentMessages.subList(0, userIndex)
        val updatedUserMessage = currentMessages[userIndex].copy(content = trimmed)

        _uiState.update { state ->
            state.copy(
                messages = historyBeforeUser + updatedUserMessage,
                modelState = ModelState.GENERATING
            )
        }

        recalculateContextUsage()
        generateResponse(trimmed, historyBeforeUser)
    }

    private fun togglePinMessage(messageId: String) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) msg.copy(isPinned = !msg.isPinned) else msg
                }
            )
        }
        recalculateContextUsage()
    }

    private fun clearPinnedMessages() {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.isPinned) msg.copy(isPinned = false) else msg
                }
            )
        }
        recalculateContextUsage()
    }

    private fun clearChat() {
        _uiState.update {
            it.copy(
                messages = emptyList(),
                contextUsagePercent = 0f,
                estimatedContextTokens = 0,
                searchQuery = "",
                isSearchActive = false
            )
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null, statusBannerMessage = null) }
    }

    private fun stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        MetricsCollector.cancelGeneration()
        finishStreaming()
    }

    private fun recalculateContextUsage() {
        val state = _uiState.value
        val combinedSysPrompt = currentCombinedSystemPrompt()
        val maxCtx = state.maxContextTokens.coerceAtLeast(512)
        val estimatedTokens = PromptBuilder.estimatePromptTokens(
            systemPrompt = combinedSysPrompt,
            messages = state.messages,
            currentMessage = state.currentInput
        )
        val usagePercent = (estimatedTokens.toFloat() / maxCtx.toFloat()).coerceIn(0f, 1f)
        _uiState.update {
            it.copy(
                estimatedContextTokens = estimatedTokens,
                contextUsagePercent = usagePercent
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXPORT TRANSCRIPT & DIRECT TOOL EXECUTION
    // ═══════════════════════════════════════════════════════════════════════════

    private fun exportChat(shareViaSystemSheet: Boolean) {
        val state = _uiState.value
        if (state.messages.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No messages to export yet.") }
            return
        }

        val transcript = buildString {
            appendLine("# Sentient OS · OfflineAI-KMP Transcript")
            appendLine("- **Model**: ${state.currentModelPath?.substringAfterLast("/") ?: "Local Model"}")
            appendLine("- **Persona**: ${state.activePersona.displayName} (${state.activePersona.shortTag})")
            appendLine("- **Device**: ${state.deviceModel} (${state.platformName})")
            if (state.performanceStats.totalGenerations > 0) {
                val avgTps = ((state.performanceStats.averageTokensPerSecond * 10).toInt() / 10.0)
                appendLine("- **Avg Speed**: $avgTps tok/s · **Avg TTFT**: ${state.performanceStats.averageTimeToFirstToken}ms")
            }
            appendLine()
            appendLine("---")
            appendLine()

            state.messages.forEach { msg ->
                val instant = Instant.fromEpochMilliseconds(msg.timestamp)
                val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                val timeStr = "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
                val speaker = if (msg.isFromUser) "USER" else "SENTIENT (${msg.personaLabel ?: state.activePersona.shortTag})"
                val pinBadge = if (msg.isPinned) " [PINNED]" else ""
                appendLine("### [$timeStr] $speaker$pinBadge")
                appendLine(msg.content)
                msg.metrics?.let { m ->
                    val tps = ((m.tokensPerSecond * 10).toInt() / 10.0)
                    appendLine("> *Telemetry: ${tps} tok/s · TTFT ${m.timeToFirstTokenMs}ms · ${m.responseTokens} tokens*")
                }
                appendLine()
            }
        }

        viewModelScope.launch {
            val success = if (shareViaSystemSheet) {
                AppActionsProvider.shareText(transcript, "OfflineAI-KMP Transcript")
            } else {
                AppActionsProvider.copyToClipboard(transcript)
            }
            if (!success) {
                AppActionsProvider.copyToClipboard(transcript)
            }
        }
    }

    private fun executeQuickTool(toolName: String, argumentsJson: String) {
        val argsObject: JsonObject = runCatching {
            Json.parseToJsonElement(argumentsJson).jsonObject
        }.getOrElse { JsonObject(emptyMap()) }

        val userMsg = ChatMessage.user("Execute tool: $toolName")
        val aiMsg = ChatMessage.ai(
            content = "",
            isStreaming = true
        ).copy(
            toolCallName = toolName,
            toolArgumentsJson = argumentsJson,
            personaLabel = _uiState.value.activePersona.shortTag
        )
        streamingMessageId = aiMsg.id

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMsg + aiMsg,
                isCommandDeckOpen = false
            )
        }

        handleToolCall(ToolCall(tool = toolName, arguments = argsObject), null)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RESPONSE GENERATION - THE CORE STREAMING LOGIC
    // ═══════════════════════════════════════════════════════════════════════════

    private fun generateResponse(prompt: String, history: List<ChatMessage>) {
        generationJob = viewModelScope.launch(Dispatchers.IO) {
            val activePersonaTag = _uiState.value.activePersona.shortTag
            val aiMessage = ChatMessage.ai("", isStreaming = true).copy(
                personaLabel = activePersonaTag
            )
            streamingMessageId = aiMessage.id

            _uiState.update { state ->
                state.copy(
                    messages = state.messages + aiMessage,
                    liveTokensPerSecond = 0.0,
                    liveTokenCount = 0
                )
            }

            val combinedSystemPrompt = currentCombinedSystemPrompt()
            val strategy = _uiState.value.trimmingStrategy
            val maxCtx = _uiState.value.maxContextTokens

            generateResponseUseCase(
                systemPrompt = combinedSystemPrompt,
                messages = history,
                userPrompt = prompt,
                trimmingStrategy = strategy,
                maxContextTokens = maxCtx
            ).collect { result ->
                when (result) {
                    is GenerateResponseResult.Streaming -> {
                        updateStreamingMessage(
                            content = result.partialResponse,
                            liveTps = result.liveTokensPerSecond,
                            liveTokens = result.liveTokenCount
                        )
                    }

                    is GenerateResponseResult.Complete -> {
                        if (result.toolCall != null) {
                            handleToolCall(result.toolCall, result.metrics)
                        } else {
                            finishStreaming(result.metrics)
                        }
                    }

                    is GenerateResponseResult.Error -> {
                        handleError(result.exception)
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TOOL CALLING (Advanced Feature)
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleToolCall(toolCall: ToolCall, generationMetrics: GenerationMetrics? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val argsString = toolCall.arguments.toString()
            _uiState.update { it.copy(isToolCallInProgress = true) }
            val proposal = "Model proposal\n${toolCall.tool} ${toolCall.arguments}\n\nDevice result\n"

            executeToolUseCase(
                toolCall = toolCall,
                loadedModels = _uiState.value.loadedModels,
                currentModelPath = _uiState.value.currentModelPath
            ).collect { result ->
                when (result) {
                    is ExecuteToolResult.Executing -> {
                        updateStreamingToolMessage(
                            content = proposal + "Executing…",
                            toolName = toolCall.tool,
                            toolArgs = argsString,
                            toolResult = "Executing…"
                        )
                    }
                    is ExecuteToolResult.Streaming -> {
                        updateStreamingToolMessage(
                            content = proposal + result.partialResponse,
                            toolName = toolCall.tool,
                            toolArgs = argsString,
                            toolResult = result.partialResponse
                        )
                    }
                    is ExecuteToolResult.Complete -> {
                        updateStreamingToolMessage(
                            content = proposal + result.response,
                            toolName = toolCall.tool,
                            toolArgs = argsString,
                            toolResult = result.response
                        )
                        finishToolCall(generationMetrics)
                    }
                    is ExecuteToolResult.Error -> {
                        handleToolError(toolCall.tool, result.exception)
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MESSAGE UPDATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun updateStreamingMessage(
        content: String,
        liveTps: Double = _uiState.value.liveTokensPerSecond,
        liveTokens: Int = _uiState.value.liveTokenCount
    ) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) msg.copy(content = content)
                else msg
            }
            state.copy(
                messages = updatedMessages,
                liveTokensPerSecond = liveTps,
                liveTokenCount = liveTokens
            )
        }
    }

    private fun updateStreamingToolMessage(
        content: String,
        toolName: String,
        toolArgs: String,
        toolResult: String
    ) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) {
                    msg.copy(
                        content = content,
                        toolCallName = toolName,
                        toolArgumentsJson = toolArgs,
                        toolResultText = toolResult
                    )
                } else msg
            }
            state.copy(messages = updatedMessages)
        }
    }

    private fun finishStreaming(metrics: GenerationMetrics? = null) {
        val latestStats = MetricsCollector.stats.value
        val latestMetrics = metrics ?: MetricsCollector.getLastMetrics()
        val throttled = ThermalMonitor.shouldShowCoolingWarning() || latestStats.isThrottled()

        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) {
                    msg.copy(
                        isStreaming = false,
                        metrics = metrics ?: msg.metrics
                    )
                } else msg
            }
            state.copy(
                messages = updatedMessages,
                modelState = ModelState.READY,
                lastMetrics = latestMetrics,
                performanceStats = latestStats,
                liveTokensPerSecond = latestMetrics?.tokensPerSecond ?: 0.0,
                isThermalThrottled = throttled
            )
        }
        streamingMessageId = null
        recalculateContextUsage()
    }

    private fun finishToolCall(metrics: GenerationMetrics? = null) {
        val latestStats = MetricsCollector.stats.value
        val latestMetrics = metrics ?: MetricsCollector.getLastMetrics()

        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) {
                    msg.copy(
                        isStreaming = false,
                        metrics = metrics ?: msg.metrics
                    )
                } else msg
            }
            state.copy(
                messages = updatedMessages,
                modelState = ModelState.READY,
                isToolCallInProgress = false,
                lastMetrics = latestMetrics,
                performanceStats = latestStats
            )
        }
        streamingMessageId = null
        recalculateContextUsage()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ERROR HANDLING
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleError(exception: Exception) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) {
                    msg.copy(
                        content = "Error: ${exception.message}",
                        isStreaming = false,
                        isError = true
                    )
                } else msg
            }
            state.copy(
                messages = updatedMessages,
                modelState = ModelState.READY
            )
        }
        streamingMessageId = null
    }

    private fun handleToolError(toolName: String, exception: Exception) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) {
                    msg.copy(
                        content = "❌ Error executing $toolName: ${exception.message}",
                        isStreaming = false,
                        isError = true,
                        toolCallName = toolName,
                        toolResultText = "Error: ${exception.message}"
                    )
                } else msg
            }
            state.copy(
                messages = updatedMessages,
                modelState = ModelState.READY,
                isToolCallInProgress = false
            )
        }
        streamingMessageId = null
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LIFECYCLE - CRITICAL!
    // ═══════════════════════════════════════════════════════════════════════════

    override fun onCleared() {
        super.onCleared()
        gemmaInference.close()
    }
}
