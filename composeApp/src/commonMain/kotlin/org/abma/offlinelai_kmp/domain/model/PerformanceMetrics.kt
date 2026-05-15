package org.abma.offlinelai_kmp.domain.model

import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Captures performance metrics for a single LLM generation.
 *
 * Workshop: This class helps measure and understand on-device AI performance.
 * Key metrics:
 * - Time To First Token (TTFT): User-perceived latency
 * - Tokens Per Second (TPS): Generation speed
 * - Total time: Overall response time
 */
@OptIn(ExperimentalTime::class)
data class GenerationMetrics(
    val promptTokens: Int,
    val responseTokens: Int,
    val timeToFirstTokenMs: Long,
    val totalGenerationTimeMs: Long,
    val tokensPerSecond: Double,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    companion object {
        fun calculate(
            promptTokens: Int,
            responseTokens: Int,
            startTime: Long,
            firstTokenTime: Long,
            endTime: Long
        ): GenerationMetrics {
            val totalTime = endTime - startTime
            val generationTime = endTime - firstTokenTime

            val tps = if (generationTime > 0) {
                responseTokens * 1000.0 / generationTime
            } else 0.0

            return GenerationMetrics(
                promptTokens = promptTokens,
                responseTokens = responseTokens,
                timeToFirstTokenMs = firstTokenTime - startTime,
                totalGenerationTimeMs = totalTime,
                tokensPerSecond = tps
            )
        }
    }

    /**
     * Human-readable summary for debugging.
     */
    fun toDebugString(): String = buildString {
        appendLine("📊 Generation Metrics:")
        appendLine("   Prompt: $promptTokens tokens")
        appendLine("   Response: $responseTokens tokens")
        appendLine("   TTFT: ${timeToFirstTokenMs}ms")
        appendLine("   Total: ${totalGenerationTimeMs}ms")
        appendLine("   Speed: ${formatDouble(tokensPerSecond)} tok/s")
    }
}

private fun formatDouble(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return rounded.toString()
}

/**
 * Aggregated performance statistics across multiple generations.
 */
data class PerformanceStats(
    val averageTokensPerSecond: Double,
    val averageTimeToFirstToken: Long,
    val minTokensPerSecond: Double,
    val maxTokensPerSecond: Double,
    val totalGenerations: Int,
    val totalTokensGenerated: Int,
    val totalTimeMs: Long
) {
    companion object {
        fun fromMetrics(metrics: List<GenerationMetrics>): PerformanceStats {
            if (metrics.isEmpty()) {
                return PerformanceStats(
                    averageTokensPerSecond = 0.0,
                    averageTimeToFirstToken = 0L,
                    minTokensPerSecond = 0.0,
                    maxTokensPerSecond = 0.0,
                    totalGenerations = 0,
                    totalTokensGenerated = 0,
                    totalTimeMs = 0L
                )
            }

            return PerformanceStats(
                averageTokensPerSecond = metrics.map { it.tokensPerSecond }.average(),
                averageTimeToFirstToken = metrics.map { it.timeToFirstTokenMs }.average().toLong(),
                minTokensPerSecond = metrics.minOf { it.tokensPerSecond },
                maxTokensPerSecond = metrics.maxOf { it.tokensPerSecond },
                totalGenerations = metrics.size,
                totalTokensGenerated = metrics.sumOf { it.responseTokens },
                totalTimeMs = metrics.sumOf { it.totalGenerationTimeMs }
            )
        }
    }

    /**
     * Check if performance is degraded (possible thermal throttling).
     */
    fun isThrottled(expectedTps: Double = 15.0): Boolean {
        return averageTokensPerSecond < expectedTps * 0.5
    }

    /**
     * Human-readable summary.
     */
    fun toSummaryString(): String = buildString {
        appendLine("📈 Performance Summary:")
        appendLine("   Avg Speed: ${formatDouble(averageTokensPerSecond)} tok/s")
        appendLine("   Range: ${formatDouble(minTokensPerSecond)} - ${formatDouble(maxTokensPerSecond)} tok/s")
        appendLine("   Avg TTFT: ${averageTimeToFirstToken}ms")
        appendLine("   Generations: $totalGenerations")
        appendLine("   Total Tokens: $totalTokensGenerated")
    }
}

