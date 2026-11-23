package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.flow.Flow
import org.abma.offlinelai_kmp.domain.model.ModelConfig

/**
 * Shared inference contract implemented by the Android and iOS adapters.
 *
 * Both adapters use LiteRT-LM. MediaPipe LLM Inference is in maintenance-only mode;
 * Google recommends migrating to LiteRT-LM.
 * https://developers.google.com/edge/mediapipe/solutions/genai/llm_inference
 *
 * Android bridges SDK callbacks to Flow. iOS bridges Swift streaming through
 * notifications. Model initialization, configuration and cleanup differ by platform.
 */
expect class GemmaInference() {

    /**
     * Prepare a local model for generation.
     *
     * Android initializes the engine here. iOS resolves the file here and initializes
     * its Swift engine lazily at first generation. Loading time and memory use depend
     * on the model, runtime, backend and device; measure them on the target device.
     *
     * @param modelPath Model path, resolved using platform-specific search locations.
     * @param config Inference settings; currently applied by the Android adapter.
     */
    suspend fun loadModel(modelPath: String, config: ModelConfig = ModelConfig())

    /**
     * Stream incremental text chunks, which need not correspond to individual tokens.
     * First-response latency varies with initialization, prompt and device.
     *
     * @param prompt Prompt formatted for the selected model and runtime API.
     * @throws IllegalStateException if no model has been prepared.
     */
    fun generateResponse(prompt: String): Flow<String>

    /**
     * Generate response with system prompt + formatted conversation.
     *
     * This is a convenience method that combines the system prompt
     * with the conversation history. The system prompt allows you
     * to customize the AI's persona and behavior.
     *
     * @param systemPrompt Instructions for the AI (persona, rules, etc.)
     * @param currentPrompt The formatted conversation with turn tokens
     * @return Flow<String> of incremental text chunks
     */
    fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String>

    /**
     * Check whether this adapter has prepared a model.
     * On iOS this confirms path resolution, not native engine initialization.
     */
    fun isModelLoaded(): Boolean

    /**
     * Get this adapter's preparation progress (0.0 to 1.0).
     * These are coarse stages, not measured progress through native initialization.
     */
    fun getLoadingProgress(): Float

    /**
     * Clear this adapter's model state.
     *
     * Android also closes its session and engine. The current iOS adapter does not
     * forward this call to the Swift bridge, so native resource release and cancellation
     * must not be assumed from this shared method alone.
     */
    fun close()
}

/**
 * Wrap a user message in the Gemma turn format used by this app.
 *
 * This template is model-specific, not a universal LLM requirement. Confirm the
 * selected model's chat template and whether its runtime API already applies one.
 * Correct formatting helps interpretation but does not guarantee output quality.
 */
fun formatPrompt(userMessage: String): String = buildString {
    append("<start_of_turn>user\n$userMessage<end_of_turn>\n")
    append("<start_of_turn>model\n")
}
