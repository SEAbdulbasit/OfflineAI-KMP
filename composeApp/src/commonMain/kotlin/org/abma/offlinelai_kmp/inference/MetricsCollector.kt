package org.abma.offlinelai_kmp.inference

import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.abma.offlinelai_kmp.domain.model.GenerationMetrics
import org.abma.offlinelai_kmp.domain.model.PerformanceStats

/**
 * Collects and aggregates performance metrics for LLM inference.
 *
 * Workshop: Use this singleton to track performance across the app.
 *
 * Usage:
 * ```kotlin
 * // At start of generation
 * MetricsCollector.startGeneration(estimatedPromptTokens)
 *
 * // For each token received
 * MetricsCollector.onTokenGenerated()
 *
 * // At end of generation
 * MetricsCollector.endGeneration()
 *
 * // Observe stats in UI
 * val stats by MetricsCollector.stats.collectAsState()
 * ```
 */
@OptIn(ExperimentalTime::class)
object MetricsCollector {

    private val _metrics = mutableListOf<GenerationMetrics>()
    private val _currentStats = MutableStateFlow(PerformanceStats.fromMetrics(emptyList()))

    /**
     * Observable performance statistics.
     */
    val stats: StateFlow<PerformanceStats> = _currentStats.asStateFlow()

    /**
     * Current generation's live token count.
     */
    private val _liveTokenCount = MutableStateFlow(0)
    val liveTokenCount: StateFlow<Int> = _liveTokenCount.asStateFlow()

    /**
     * Current generation's live TPS (updated during generation).
     */
    private val _liveTokensPerSecond = MutableStateFlow(0.0)
    val liveTokensPerSecond: StateFlow<Double> = _liveTokensPerSecond.asStateFlow()

    // Internal tracking
    private var generationStartTime: Long = 0
    private var firstTokenTime: Long = 0
    private var tokenCount: Int = 0
    private var promptTokenCount: Int = 0
    private var isGenerating: Boolean = false

    private fun currentTimeMs(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * Call when starting a new generation.
     *
     * @param estimatedPromptTokens Estimated token count of the prompt
     */
    fun startGeneration(estimatedPromptTokens: Int) {
        generationStartTime = currentTimeMs()
        firstTokenTime = 0
        tokenCount = 0
        promptTokenCount = estimatedPromptTokens
        isGenerating = true

        _liveTokenCount.value = 0
        _liveTokensPerSecond.value = 0.0

        println("⏱️ Generation started (prompt ~$estimatedPromptTokens tokens)")
    }

    /**
     * Call for each token generated.
     */
    fun onTokenGenerated() {
        if (!isGenerating) return

        val now = currentTimeMs()

        if (firstTokenTime == 0L) {
            firstTokenTime = now
            println("⏱️ First token at ${now - generationStartTime}ms")
        }

        tokenCount++
        _liveTokenCount.value = tokenCount

        // Calculate live TPS (from first token)
        val generationTime = now - firstTokenTime
        if (generationTime > 0L) {
            _liveTokensPerSecond.value = tokenCount * 1000.0 / generationTime
        }
    }

    /**
     * Call when generation is complete.
     */
    fun endGeneration() {
        if (!isGenerating) return

        val endTime = currentTimeMs()

        if (firstTokenTime == 0L) {
            firstTokenTime = endTime
        }

        val metrics = GenerationMetrics.calculate(
            promptTokens = promptTokenCount,
            responseTokens = tokenCount,
            startTime = generationStartTime,
            firstTokenTime = firstTokenTime,
            endTime = endTime
        )

        _metrics.add(metrics)
        _currentStats.value = PerformanceStats.fromMetrics(_metrics)

        isGenerating = false

        // Log metrics
        println(metrics.toDebugString())

        // Check for potential issues
        if (metrics.tokensPerSecond < 10.0 && tokenCount > 10) {
            println("⚠️ Low performance detected - possible thermal throttling")
        }
    }

    /**
     * Cancel current generation tracking without recording metrics.
     */
    fun cancelGeneration() {
        isGenerating = false
        _liveTokenCount.value = 0
        _liveTokensPerSecond.value = 0.0
        println("⏱️ Generation cancelled")
    }

    /**
     * Get the most recent generation's metrics.
     */
    fun getLastMetrics(): GenerationMetrics? = _metrics.lastOrNull()

    /**
     * Get all recorded metrics.
     */
    fun getAllMetrics(): List<GenerationMetrics> = _metrics.toList()

    /**
     * Reset all collected metrics.
     */
    fun reset() {
        _metrics.clear()
        _currentStats.value = PerformanceStats.fromMetrics(emptyList())
        _liveTokenCount.value = 0
        _liveTokensPerSecond.value = 0.0
        isGenerating = false
        println("📊 Metrics reset")
    }

    /**
     * Check if we're currently tracking a generation.
     */
    fun isCurrentlyGenerating(): Boolean = isGenerating
}

private fun formatDouble(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return rounded.toString()
}

/**
 * Monitor for thermal throttling detection.
 */
object ThermalMonitor {

    private var consecutiveSlowGenerations = 0
    private const val THROTTLE_THRESHOLD = 3
    private const val EXPECTED_TPS = 15.0

    /**
     * Call after each generation to track performance trends.
     */
    fun onGenerationComplete(tokensPerSecond: Double) {
        if (tokensPerSecond < EXPECTED_TPS * 0.5) {
            consecutiveSlowGenerations++

            if (consecutiveSlowGenerations >= THROTTLE_THRESHOLD) {
                println("🔥 Thermal throttling likely - $consecutiveSlowGenerations consecutive slow generations")
            }
        } else {
            consecutiveSlowGenerations = 0
        }
    }

    /**
     * Check if we should warn the user about device temperature.
     */
    fun shouldShowCoolingWarning(): Boolean {
        return consecutiveSlowGenerations >= THROTTLE_THRESHOLD
    }

    /**
     * Get recommended wait time before next generation (ms).
     */
    fun getRecommendedCooldownMs(): Long {
        return when {
            consecutiveSlowGenerations >= 5 -> 10_000L
            consecutiveSlowGenerations >= 3 -> 5_000L
            else -> 0L
        }
    }

    /**
     * Reset throttle tracking.
     */
    fun reset() {
        consecutiveSlowGenerations = 0
    }
}

