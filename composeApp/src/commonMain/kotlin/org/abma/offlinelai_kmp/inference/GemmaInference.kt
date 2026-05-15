package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.flow.Flow
import org.abma.offlinelai_kmp.domain.model.ModelConfig

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: GemmaInference - The Core Inference Interface
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * This is the EXPECT class - the shared interface that both Android and iOS implement.
 *
 * The expect/actual pattern is KMP's way of handling platform differences:
 * - "expect" declares WHAT the interface looks like (this file)
 * - "actual" provides HOW it works on each platform (GemmaInference.android.kt, etc.)
 *
 * WHY THIS PATTERN?
 * LiteRT-LM has different APIs on Android (Kotlin) vs iOS (Swift).
 * This abstraction lets our ViewModel use the same interface on both platforms.
 *
 * NOTE: This project uses LiteRT-LM for on-device LLM inference.
 * LiteRT-LM replaces the deprecated MediaPipe LLM Inference API.
 * https://ai.google.dev/edge/litert-lm/overview
 *
 * KEY METHODS:
 * - loadModel(): Load the Gemma model into memory (3-8 seconds)
 * - generateResponse(): Stream tokens from the model
 * - close(): Free memory (CRITICAL - forgetting this = 1.7GB leak!)
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
expect class GemmaInference() {

    /**
     * Load a Gemma model from disk into memory.
     *
     * This operation:
     * - Reads ~1.4GB model file from disk
     * - Allocates ~1.7GB RAM for weights
     * - Initializes the inference engine
     * - Takes 3-8 seconds depending on device
     *
     * IMPORTANT: Always call on Dispatchers.IO, never on Main thread!
     *
     * @param modelPath Path to the model file (absolute or will search common locations)
     * @param config Configuration for inference (maxTokens, temperature, topK)
     * @throws IllegalArgumentException if model file not found
     * @throws OutOfMemoryError if device doesn't have enough RAM
     */
    suspend fun loadModel(modelPath: String, config: ModelConfig = ModelConfig())

    /**
     * Generate a streaming response from the model.
     *
     * Returns a Flow that emits tokens as they're generated.
     * Each emission is the NEXT token(s), not the full response.
     *
     * WORKSHOP NOTE: This is the magic of streaming!
     * Instead of waiting 5 seconds for a complete response,
     * users see tokens appear immediately (~50ms to first token).
     *
     * @param prompt The formatted prompt (MUST include turn tokens!)
     * @return Flow<String> that emits tokens until generation completes
     * @throws IllegalStateException if model not loaded
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
     * @return Flow<String> of generated tokens
     */
    fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String>

    /**
     * Check if a model is currently loaded and ready.
     */
    fun isModelLoaded(): Boolean

    /**
     * Get the current loading progress (0.0 to 1.0).
     * Useful for showing loading UI during model initialization.
     */
    fun getLoadingProgress(): Float

    /**
     * Release the model and free memory.
     *
     * ⚠️ CRITICAL: You MUST call this when you're done!
     *
     * Forgetting to call close() means:
     * - ~1.7GB of RAM is never freed
     * - The OS will eventually kill your app
     * - Users will see "Out of Memory" crashes
     *
     * Best practice: Call in ViewModel.onCleared()
     */
    fun close()
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: Prompt Formatting Helper
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Gemma was trained with special tokens that mark conversation turns.
 * If you don't use these tokens, you'll get GARBAGE output!
 *
 * CORRECT FORMAT:
 * ```
 * <start_of_turn>user
 * Hello, how are you?<end_of_turn>
 * <start_of_turn>model
 * ```
 *
 * The model then generates its response after "<start_of_turn>model\n"
 *
 * WRONG (will produce nonsense):
 * ```
 * Hello, how are you?
 * ```
 * ═══════════════════════════════════════════════════════════════════════════════
 */
fun formatPrompt(userMessage: String): String = buildString {
    append("<start_of_turn>user\n$userMessage<end_of_turn>\n")
    append("<start_of_turn>model\n")
}
