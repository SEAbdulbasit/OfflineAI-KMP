package org.abma.offlinelai_kmp.inference

import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.InputData
import com.google.ai.edge.litertlm.ResponseCallback
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.Session
import com.google.ai.edge.litertlm.SessionConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.domain.model.ModelConfig

private const val TAG = "GemmaInference"

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: Android Implementation of GemmaInference
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * This is the ACTUAL implementation for Android using Google's LiteRT-LM SDK.
 *
 * LiteRT-LM (replaces deprecated MediaPipe LLM Inference API) does the heavy lifting:
 * - Model loading and memory management
 * - Tokenization (converting text to numbers)
 * - GPU/CPU inference optimization
 * - Streaming token output
 *
 * WE just need to:
 * 1. Configure LiteRT-LM options
 * 2. Bridge its callback API to Kotlin Flow
 * 3. Handle lifecycle (cleanup)
 *
 * KEY INSIGHT: LiteRT-LM uses callbacks (ResponseCallback), but Kotlin prefers Flows.
 * The `callbackFlow` builder bridges this gap beautifully.
 *
 * NOTE: LiteRT-LM replaces the deprecated MediaPipe LLM Inference API.
 * Migration guide: https://ai.google.dev/edge/litert-lm/overview
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
actual class GemmaInference {

    // LiteRT-LM's inference engine - null when no model is loaded
    private var engine: Engine? = null

    // Current session for inference - recreated for each generation
    private var currentSession: Session? = null

    // Store config for creating sessions
    private var currentModelConfig: ModelConfig? = null

    // Track loading state for UI
    private var isLoaded = false
    private var loadingProgress = 0f

    // Track which backend is being used
    private var currentBackend: String = "unknown"

    /**
     * Load a Gemma model using LiteRT-LM.
     *
     * WORKSHOP NOTES:
     * - withContext(Dispatchers.IO): Model loading is heavy I/O, keep off main thread!
     * - loadingProgress: Update UI during the 3-8 second load time
     * - ModelPathResolver: Handles finding the model file in various locations
     *
     * WHAT HAPPENS INSIDE LiteRT-LM:
     * 1. Reads ~1.4GB model file from disk
     * 2. Allocates GPU/CPU memory
     * 3. Initializes the inference engine
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

                Log.d(TAG, "Loading model from: $resolvedPath")

                // ═══ PHASE 2: Configure LiteRT-LM ═══
                loadingProgress = 0.3f

                // Store config for later session creation
                currentModelConfig = config

                // ═══ PHASE 3: Try GPU first, fall back to CPU if needed ═══
                loadingProgress = 0.5f

                // Track errors from each backend attempt
                var gpuError: String? = null
                var cpuError: String? = null

                val newEngine = tryLoadWithBackend(resolvedPath, config, Backend.GPU(), "GPU") { error ->
                    gpuError = error
                } ?: tryLoadWithBackend(resolvedPath, config, Backend.CPU(), "CPU") { error ->
                    cpuError = error
                }

                if (newEngine == null) {
                    val errorDetails = buildString {
                        append("Failed to load model with both GPU and CPU backends.\n\n")
                        append("Model: $resolvedPath\n\n")
                        gpuError?.let { append("GPU Error: $it\n\n") }
                        cpuError?.let { append("CPU Error: $it\n\n") }

                        // Add troubleshooting hints
                        if (gpuError?.contains("Permission denied") == true ||
                            cpuError?.contains("Permission denied") == true) {
                            append("⚠️ Permission issue detected.\n")
                            append("Please grant 'All files access' permission in Settings.")
                        }
                    }
                    throw RuntimeException(errorDetails)
                }

                engine = newEngine

                // ═══ PHASE 4: Warm-up (Optional but recommended) ═══
                loadingProgress = 0.95f
                Log.d(TAG, "Warming up model...")
                // Create a dummy session and run a tiny inference
                // This ensures GPU shaders are compiled and memory is ready
                try {
                    val warmUpConfig = SessionConfig(SamplerConfig(topK = 1, topP = 0.95, temperature = 0.0))
                    val warmUpSession = newEngine.createSession(warmUpConfig)
                    warmUpSession.generateContent(listOf(InputData.Text("Warm up")))
                    warmUpSession.close()
                    Log.d(TAG, "Warm-up complete")
                } catch (e: Exception) {
                    Log.w(TAG, "Warm-up failed (ignoring): ${e.message}")
                }

                // ═══ PHASE 5: Ready! ═══
                loadingProgress = 1.0f
                isLoaded = true
                Log.i(TAG, "Model loaded successfully using $currentBackend backend")

            } catch (e: Exception) {
                // Reset state on failure
                isLoaded = false
                loadingProgress = 0f
                Log.e(TAG, "Failed to load model: ${e.message}", e)
                throw e  // Re-throw so caller can handle (show error UI)
            }
        }
    }

    /**
     * Try to load the model with a specific backend.
     * Returns null if the backend fails, allowing fallback to another backend.
     * @param onError callback to report the error message if loading fails
     */
    private fun tryLoadWithBackend(
        modelPath: String,
        config: ModelConfig,
        backend: Backend,
        backendName: String,
        onError: (String) -> Unit = {}
    ): Engine? {
        return try {
            Log.d(TAG, "Attempting to load model with $backendName backend...")
            Log.d(TAG, "Model path: $modelPath")

            // Check if file exists and is readable
            val modelFile = java.io.File(modelPath)
            if (!modelFile.exists()) {
                val error = "Model file does not exist: $modelPath"
                Log.e(TAG, error)
                onError(error)
                return null
            }
            if (!modelFile.canRead()) {
                val error = "Cannot read model file (permission denied): $modelPath"
                Log.e(TAG, error)
                onError(error)
                return null
            }
            Log.d(TAG, "Model file exists and is readable, size: ${modelFile.length()} bytes")

            val engineConfig = EngineConfig(
                modelPath = modelPath,
                backend = backend,
                visionBackend = null,
                audioBackend = null,
                maxNumTokens = 4096, // Increased from config.maxTokens for better context handling
                maxNumImages = null,
                cacheDir = AndroidContextProvider.applicationContext.cacheDir.absolutePath
            )

            val newEngine = Engine(engineConfig)
            newEngine.initialize()

            currentBackend = backendName
            Log.i(TAG, "Successfully initialized with $backendName backend")
            newEngine

        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            Log.e(TAG, "$backendName backend failed: $errorMsg", e)
            onError(errorMsg)
            null
        }
    }

    /**
     * Generate streaming response using callbackFlow.
     *
     * WORKSHOP: Understanding callbackFlow
     *
     * LiteRT-LM uses CALLBACKS: ResponseCallback { onNext, onDone, onError }
     * Kotlin prefers FLOWS: flow.collect { token -> ... }
     *
     * callbackFlow BRIDGES these two worlds:
     * 1. Start a callback-based operation
     * 2. Each callback invocation -> trySend(value) into the Flow
     * 3. When done -> close() the Flow
     * 4. awaitClose keeps Flow alive until LiteRT-LM finishes
     *
     * WHY trySend instead of send?
     * - send is suspending, but callbacks can't suspend
     * - trySend is non-blocking, returns immediately
     * - If channel is full/closed, it just returns failure (handled gracefully)
     */
    actual fun generateResponse(prompt: String): Flow<String> = callbackFlow {
        // Ensure model is loaded
        val currentEngine = engine
            ?: throw IllegalStateException("Model not loaded. Call loadModel() first.")

        val config = currentModelConfig ?: ModelConfig()
        var activeSession: Session? = null

        try {
            Log.d(TAG, "Starting generation for prompt: ${prompt.take(50)}...")
            
            // Create a new session for this generation
            val samplerConfig = SamplerConfig(
                topK = config.topK,
                topP = 0.95,
                temperature = config.temperature.toDouble(),
                seed = 0
            )
            val sessionConfig = SessionConfig(samplerConfig)
            activeSession = currentEngine.createSession(sessionConfig)
            
            // Store in property only for external cancellation (like close())
            currentSession = activeSession

            // Prepare input data
            val inputData = listOf(InputData.Text(prompt))

            // Start streaming generation
            activeSession.generateContentStream(inputData, object : ResponseCallback {
                override fun onNext(token: String) {
                    Log.v(TAG, "onNext: token received (${token.length} chars)")
                    // Use trySend and log if it fails (channel full)
                    val result = trySend(token)
                    if (result.isFailure) {
                        Log.w(TAG, "onNext: failed to send token (channel full/closed)")
                    }
                }

                override fun onDone() {
                    Log.d(TAG, "onDone: generation completed")
                    close()
                }

                override fun onError(error: Throwable) {
                    Log.e(TAG, "onError: generation failed", error)
                    close(error)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error starting generation", e)
            close(e)
        }

        // Keep flow alive until close() is called
        awaitClose {
            Log.d(TAG, "awaitClose: cleaning up session")
            activeSession?.cancelProcess()
            activeSession?.close()
            if (currentSession == activeSession) {
                currentSession = null
            }
        }
    }.buffer(capacity = 64) // Add buffer to handle rapid token bursts without blocking the collector

    /**
     * Generate with system prompt prepended.
     *
     * WORKSHOP NOTE: Simple concatenation works because:
     * - systemPrompt contains persona/instructions
     * - currentPrompt has the formatted conversation with turn tokens
     * - LiteRT-LM tokenizes the combined string
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
        currentSession?.close()
        currentSession = null
        engine?.close()       // Release LiteRT-LM resources
        engine = null         // Clear reference
        currentModelConfig = null
        isLoaded = false      // Reset state
        loadingProgress = 0f
    }
}
