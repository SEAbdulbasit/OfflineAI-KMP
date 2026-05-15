package org.abma.offlinelai_kmp.inference

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.domain.model.ModelConfig

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: Android Implementation of GemmaInference
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * This is the ACTUAL implementation for Android using Google's MediaPipe SDK.
 *
 * MediaPipe does the heavy lifting:
 * - Model loading and memory management
 * - Tokenization (converting text to numbers)
 * - GPU/CPU inference optimization
 * - Streaming token output
 *
 * WE just need to:
 * 1. Configure MediaPipe options
 * 2. Bridge its callback API to Kotlin Flow
 * 3. Handle lifecycle (cleanup)
 *
 * KEY INSIGHT: MediaPipe uses callbacks, but Kotlin prefers Flows.
 * The `callbackFlow` builder bridges this gap beautifully.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
actual class GemmaInference {

    // MediaPipe's inference engine - null when no model is loaded
    private var llmInference: LlmInference? = null

    // Track loading state for UI
    private var isLoaded = false
    private var loadingProgress = 0f

    /**
     * Load a Gemma model using MediaPipe.
     *
     * WORKSHOP NOTES:
     * - withContext(Dispatchers.IO): Model loading is heavy I/O, keep off main thread!
     * - loadingProgress: Update UI during the 3-8 second load time
     * - ModelPathResolver: Handles finding the model file in various locations
     *
     * WHAT HAPPENS INSIDE MediaPipe:
     * 1. Reads 1.4GB model file from disk
     * 2. Allocates GPU/CPU memory
     * 3. Initializes inference session
     * 4. Performs optional warm-up
     */
    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        withContext(Dispatchers.IO) {
            try {
                // ═══ PHASE 1: Resolve model path ═══
                loadingProgress = 0.1f

                // ModelPathResolver checks common locations for the model file
                // This is helpful because model can be in:
                // - App's files directory
                // - External storage
                // - Downloads folder
                // - /data/local/tmp (for development)
                val resolvedPath = ModelPathResolver.resolve(modelPath)
                    ?: throw IllegalArgumentException(
                        "Model file not found: $modelPath\n\n" +
                                "Searched in:\n${
                                    ModelPathResolver.getSearchPaths(modelPath).joinToString("\n") { " • $it" }
                                }"
                    )

                // ═══ PHASE 2: Configure MediaPipe ═══
                loadingProgress = 0.3f

                // Build the inference options
                // - setModelPath: Where the model file lives
                // - setMaxTokens: Maximum response length (tokens ≈ words × 1.3)
                // - setMaxTopK: Consider top K most likely tokens
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(resolvedPath)
                    .setMaxTokens(config.maxTokens)
                    .setMaxTopK(config.topK)
                    .build()

                // ═══ PHASE 3: Create inference engine ═══
                loadingProgress = 0.6f

                // THIS IS THE SLOW PART
                // MediaPipe loads ~1.4GB model into ~1.7GB of RAM
                // Takes 3-8 seconds depending on device speed
                llmInference = LlmInference.createFromOptions(
                    AndroidContextProvider.applicationContext,
                    options
                )

                // ═══ PHASE 4: Ready! ═══
                loadingProgress = 1.0f
                isLoaded = true

            } catch (e: Exception) {
                // Reset state on failure
                isLoaded = false
                loadingProgress = 0f
                throw e  // Re-throw so caller can handle (show error UI)
            }
        }
    }

    /**
     * Generate streaming response using callbackFlow.
     *
     * WORKSHOP: Understanding callbackFlow
     *
     * MediaPipe uses CALLBACKS: generateResponseAsync(prompt) { result, done -> ... }
     * Kotlin prefers FLOWS: flow.collect { token -> ... }
     *
     * callbackFlow BRIDGES these two worlds:
     * 1. Start a callback-based operation
     * 2. Each callback invocation -> trySend(value) into the Flow
     * 3. When done -> close() the Flow
     * 4. awaitClose keeps Flow alive until MediaPipe finishes
     *
     * WHY trySend instead of send?
     * - send is suspending, but callbacks can't suspend
     * - trySend is non-blocking, returns immediately
     * - If channel is full/closed, it just returns failure (handled gracefully)
     */
    actual fun generateResponse(prompt: String): Flow<String> = callbackFlow {
        // Ensure model is loaded
        val inference = llmInference
            ?: throw IllegalStateException("Model not loaded. Call loadModel() first.")

        try {
            // Start async generation with MediaPipe
            // This callback will fire for EACH token generated
            inference.generateResponseAsync(prompt) { partialResult, done ->
                // ═══ CALLBACK FIRES HERE ═══
                // partialResult = the next token(s) generated
                // done = true when generation is complete

                // Send token into the Flow channel
                // trySend is non-blocking - safe to call from callback
                trySend(partialResult)

                // When MediaPipe signals completion, close the Flow
                if (done) {
                    close()
                }
            }
        } catch (e: Exception) {
            // Close Flow with error - will be caught by .catch() operator
            close(e)
        }

        // ═══ CRITICAL: Keep Flow alive ═══
        // Without awaitClose, the Flow completes immediately after
        // the lambda above returns - but callbacks haven't fired yet!
        // awaitClose suspends until close() is called in the callback.
        awaitClose {
            // Optional cleanup when Flow is cancelled
            // Could cancel MediaPipe generation here if API supported it
        }
    }

    /**
     * Generate with system prompt prepended.
     *
     * WORKSHOP NOTE: Simple concatenation works because:
     * - systemPrompt contains persona/instructions
     * - currentPrompt has the formatted conversation with turn tokens
     * - MediaPipe tokenizes the combined string
     */
    actual fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String> = generateResponse(systemPrompt + currentPrompt)

    actual fun isModelLoaded(): Boolean = isLoaded

    actual fun getLoadingProgress(): Float = loadingProgress

    /**
     * Clean up resources.
     *
     * ⚠️ WORKSHOP: MEMORY MANAGEMENT IS CRITICAL!
     *
     * The Gemma model uses ~1.7GB of RAM.
     * If you don't call close():
     * - That memory is NEVER freed
     * - After a few screen rotations or navigation events, OOM crash!
     *
     * BEST PRACTICE: Call in ViewModel.onCleared()
     * ```kotlin
     * override fun onCleared() {
     *     super.onCleared()
     *     gemmaInference.close()  // ← Don't forget this!
     * }
     * ```
     */
    actual fun close() {
        llmInference?.close()  // Release MediaPipe resources
        llmInference = null    // Clear reference
        isLoaded = false       // Reset state
        loadingProgress = 0f
    }
}
