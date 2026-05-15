package org.abma.offlinelai_kmp.domain.model

import kotlinx.serialization.Serializable

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: Model Configuration
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * These parameters control how Gemma generates responses.
 *
 * @property maxTokens Maximum tokens to generate in a response.
 *   - More tokens = longer possible responses
 *   - More tokens = more memory during generation
 *   - Typical: 1024-2048 for chat, 4096 for long-form
 *
 * @property temperature Controls randomness/creativity (0.0 to 1.0).
 *   - 0.0 = Deterministic, always picks most likely token
 *   - 0.5 = Balanced between focused and creative
 *   - 1.0 = Maximum randomness, more creative but less coherent
 *   - Typical: 0.7-0.8 for chat
 *
 * @property topK Limits token selection to top K most likely tokens.
 *   - Lower = More focused, safer outputs
 *   - Higher = More diverse, potentially creative
 *   - Typical: 40 for general use
 *
 * WORKSHOP TIP: Experiment with these values!
 * - Factual assistant: temperature=0.3, topK=30
 * - Creative writer: temperature=0.9, topK=50
 */
@Serializable
data class ModelConfig(
    val maxTokens: Int = 2048,
    val temperature: Float = 0.8f,
    val topK: Int = 40
)

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: Model State Machine
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * The model goes through these states:
 *
 * NOT_LOADED ──► LOADING ──► READY ◄──► GENERATING
 *                   │                        │
 *                   └──► ERROR ◄─────────────┘
 *
 * State transitions:
 * - NOT_LOADED → LOADING: User taps "Load Model"
 * - LOADING → READY: Model successfully loaded
 * - LOADING → ERROR: Load failed (file not found, OOM, etc.)
 * - READY → GENERATING: User sends a message
 * - GENERATING → READY: Response completed
 * - GENERATING → ERROR: Generation failed
 */
enum class ModelState {
    /** No model loaded in memory. Can't generate. */
    NOT_LOADED,

    /** Model is being loaded (3-8 seconds). Show progress UI. */
    LOADING,

    /** Model is loaded and ready to generate. */
    READY,

    /** Something went wrong. Show error UI. */
    ERROR,

    /** Currently generating a response. Show streaming UI. */
    GENERATING
}

