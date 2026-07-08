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
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.domain.repository.ModelRepository
import org.abma.offlinelai_kmp.domain.usecase.*
import org.abma.offlinelai_kmp.inference.GemmaInference
import org.abma.offlinelai_kmp.tools.ToolCall
import org.abma.offlinelai_kmp.tools.ToolRegistry
import org.abma.offlinelai_kmp.tools.buildSystemPrompt
import org.abma.offlinelai_kmp.tools.createDefaultToolRegistry

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: UI State - Single Source of Truth
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Compose observes this state and recomposes when it changes.
 * All UI information is in ONE place - easy to reason about and debug.
 *
 * KEY FIELDS:
 * - messages: The chat history (both user and AI messages)
 * - modelState: Loading, ready, generating, error, etc.
 * - currentInput: What the user is typing
 * - errorMessage: For snackbar/error display
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val modelState: ModelState = ModelState.NOT_LOADED,
    val loadingProgress: Float = 0f,
    val currentInput: String = "",
    val errorMessage: String? = null,
    val currentModelPath: String? = null,
    val loadedModels: List<LoadedModel> = emptyList(),
    val isToolCallInProgress: Boolean = false
)

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: ChatViewModel - The Orchestrator
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * This ViewModel is the HEART of the app. It:
 * 1. Holds the UI state (messages, model status, input)
 * 2. Coordinates model loading
 * 3. Handles message sending and streaming
 * 4. Manages lifecycle (CRITICAL: closes GemmaInference in onCleared)
 *
 * ARCHITECTURE PATTERN: MVI (Model-View-Intent)
 * - UI sends Actions (intents)
 * - ViewModel processes actions
 * - ViewModel updates State
 * - UI recomposes based on new state
 *
 * KEY WORKSHOP CONCEPTS:
 * 1. StateFlow for reactive UI updates
 * 2. viewModelScope for coroutine lifecycle
 * 3. Dispatchers.IO for background work
 * 4. Streaming updates via updateStreamingMessage()
 * 5. Resource cleanup in onCleared()
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
class ChatViewModel : ViewModel() {

    // ═══════════════════════════════════════════════════════════════════════════
    // STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    /** Mutable state - internal only. Use _uiState.update { } for thread-safe updates. */
    private val _uiState = MutableStateFlow(ChatUiState())

    /** Immutable state exposed to UI. Compose collects this as: val state by viewModel.uiState.collectAsState() */
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // ═══════════════════════════════════════════════════════════════════════════
    // DEPENDENCIES
    // ═══════════════════════════════════════════════════════════════════════════

    /** The Gemma inference engine - platform-specific implementation via expect/actual */
    private val gemmaInference by lazy { GemmaInference() }

    /** Repository for tracking loaded models */
    private val modelRepository by lazy { ModelRepository() }

    /** Tool registry for function calling (advanced feature) */
    private val toolRegistry: ToolRegistry by lazy { createDefaultToolRegistry() }

    /**
     * Track which message is currently streaming.
     * When tokens arrive, we update THIS message's content.
     */
    private var streamingMessageId: String? = null

    /** Active generation coroutine job - cancelled on stop */
    private var generationJob: Job? = null

    // ═══════════════════════════════════════════════════════════════════════════
    // USE CASES (Clean Architecture)
    // ═══════════════════════════════════════════════════════════════════════════

    private val loadModelUseCase by lazy { LoadModelUseCase(gemmaInference, modelRepository) }
    private val generateResponseUseCase by lazy { GenerateResponseUseCase(gemmaInference) }
    private val executeToolUseCase by lazy { ExecuteToolUseCase(toolRegistry) }
    private val getLoadedModelsUseCase by lazy { GetLoadedModelsUseCase(modelRepository) }
    private val removeModelUseCase by lazy { RemoveModelUseCase(modelRepository) }

    /** System prompt for AI persona - includes tool definitions */
    private val systemPrompt: String by lazy {
        buildSystemPrompt(toolRegistry.specs())
    }

    init {
        // Load list of available models on startup
        onAction(ChatAction.RefreshModels)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ACTION HANDLER (MVI Pattern)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Central action handler - all UI events go through here.
     *
     * WORKSHOP: Why this pattern?
     * - Single entry point for all actions
     * - Easy to log/debug
     * - Clear mapping of intent to behavior
     */
    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.LoadModel -> loadModel(action.path, action.config)
            is ChatAction.RemoveModel -> removeLoadedModel(action.path)
            is ChatAction.UpdateInput -> updateInput(action.text)
            is ChatAction.SendMessage -> sendMessage()
            is ChatAction.StopGeneration -> stopGeneration()
            is ChatAction.ClearChat -> clearChat()
            is ChatAction.DismissError -> dismissError()
            is ChatAction.RefreshModels -> refreshLoadedModels()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MODEL MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private fun refreshLoadedModels() {
        getLoadedModelsUseCase().onSuccess { models ->
            val currentPath = modelRepository.getCurrentModelPath()
            _uiState.update {
                it.copy(
                    loadedModels = models,
                    currentModelPath = currentPath
                )
            }
        }
    }

    /**
     * Load a Gemma model.
     *
     * WORKSHOP: Key Points
     * 1. Run on Dispatchers.IO (heavy I/O operation)
     * 2. Update UI state to LOADING immediately
     * 3. Handle success/failure with appropriate state updates
     */
    private fun loadModel(modelPath: String, config: ModelConfig = ModelConfig()) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(modelState = ModelState.LOADING, errorMessage = null) }

            loadModelUseCase(modelPath, config)
                .onSuccess { loadedModel ->
                    _uiState.update {
                        it.copy(
                            modelState = ModelState.READY,
                            loadingProgress = 1f,
                            currentModelPath = loadedModel.path,
                            loadedModels = modelRepository.getLoadedModels()
                        )
                    }
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

    // ═══════════════════════════════════════════════════════════════════════════
    // CHAT INPUT HANDLING
    // ═══════════════════════════════════════════════════════════════════════════

    private fun updateInput(input: String) {
        _uiState.update { it.copy(currentInput = input) }
    }

    /**
     * Send the current input as a message.
     *
     * WORKSHOP: The Message Flow
     * 1. Validate input (not empty, model ready)
     * 2. Create user message and add to UI immediately
     * 3. Clear input field
     * 4. Set state to GENERATING
     * 5. Start generation (separate function)
     */
    private fun sendMessage() {
        val input = _uiState.value.currentInput.trim()

        // Guard clauses
        if (input.isEmpty()) return
        if (_uiState.value.modelState != ModelState.READY) return

        // Create user message
        val userMessage = ChatMessage.user(input)

        // Update UI: add message, clear input, set generating state
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                currentInput = "",
                modelState = ModelState.GENERATING
            )
        }

        // Start AI response generation
        // Note: Pass history WITHOUT the message we just added (dropLast(1))
        // because the user message is in the prompt itself
        generateResponse(input, _uiState.value.messages.dropLast(1))
    }

    private fun clearChat() {
        _uiState.update { it.copy(messages = emptyList()) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Cancel the current generation and finalize the streaming message */
    private fun stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        finishStreaming()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RESPONSE GENERATION - THE CORE STREAMING LOGIC
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Generate an AI response with streaming.
     *
     * WORKSHOP: Streaming Pattern
     *
     * 1. Create PLACEHOLDER message (empty, isStreaming=true)
     * 2. Add placeholder to UI immediately
     * 3. Store its ID for updates
     * 4. Collect from generateResponseUseCase Flow
     * 5. For each Streaming result: update placeholder's content
     * 6. On Complete: mark as not streaming, set state READY
     * 7. On Error: show error in message
     *
     * WHY THIS PATTERN?
     * - User sees immediate feedback (typing indicator)
     * - Content builds up smoothly
     * - We only update ONE message in the list each time
     * - Much better UX than waiting for complete response
     */
    private fun generateResponse(prompt: String, history: List<ChatMessage>) {
        generationJob = viewModelScope.launch(Dispatchers.IO) {
            // ═══ STEP 1: Create placeholder message ═══
            val aiMessage = ChatMessage.ai("", isStreaming = true)
            streamingMessageId = aiMessage.id

            _uiState.update { state ->
                state.copy(messages = state.messages + aiMessage)
            }

            // ═══ STEP 2: Collect streaming tokens ═══
            generateResponseUseCase(systemPrompt, history, prompt)
                .collect { result ->
                    when (result) {
                        // ═══ STREAMING: Update message content ═══
                        is GenerateResponseResult.Streaming -> {
                            updateStreamingMessage(result.partialResponse)
                        }

                        // ═══ COMPLETE: Finalize message ═══
                        is GenerateResponseResult.Complete -> {
                            if (result.toolCall != null) {
                                handleToolCall(result.toolCall)
                            } else {
                                finishStreaming()
                            }
                        }

                        // ═══ ERROR: Show error in message ═══
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

    private fun handleToolCall(toolCall: ToolCall) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isToolCallInProgress = true) }

            executeToolUseCase(
                toolCall = toolCall,
                loadedModels = _uiState.value.loadedModels,
                currentModelPath = _uiState.value.currentModelPath
            ).collect { result ->
                when (result) {
                    is ExecuteToolResult.Executing -> {
                        updateStreamingMessage("🔧 Calling ${result.toolName}...\n\n")
                    }
                    is ExecuteToolResult.Streaming -> {
                        updateStreamingMessage(result.toolDisplay + result.partialResponse)
                    }
                    is ExecuteToolResult.Complete -> {
                        updateStreamingMessage(result.toolDisplay + result.response)
                        finishToolCall()
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

    /**
     * Update the currently streaming message's content.
     *
     * WORKSHOP: This is called for EVERY token!
     *
     * Since content is CUMULATIVE (full response so far),
     * we just replace the message content entirely.
     * Compose efficiently diffs and only re-renders what changed.
     */
    private fun updateStreamingMessage(content: String) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) msg.copy(content = content)
                else msg
            }
            state.copy(messages = updatedMessages)
        }
    }

    /** Mark streaming complete and set state to READY */
    private fun finishStreaming() {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) msg.copy(isStreaming = false)
                else msg
            }
            state.copy(
                messages = updatedMessages,
                modelState = ModelState.READY
            )
        }
        streamingMessageId = null
    }

    private fun finishToolCall() {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) msg.copy(isStreaming = false)
                else msg
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
                        isError = true
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

    /**
     * ⚠️ WORKSHOP CRITICAL: Resource Cleanup
     *
     * onCleared() is called when the ViewModel is destroyed.
     * This happens when:
     * - User navigates away from the screen
     * - Configuration change (if not using SavedStateHandle)
     * - App is killed by system
     *
     * WE MUST close GemmaInference here!
     *
     * If we don't:
     * - 1.7GB of RAM stays allocated
     * - Memory leak grows with each screen visit
     * - Eventually: OutOfMemoryError crash
     *
     * This is the #1 mistake developers make with on-device AI.
     */
    override fun onCleared() {
        super.onCleared()
        gemmaInference.close()  // ← NEVER forget this!
    }
}
