package org.abma.offlinelai_kmp.inference

import org.abma.offlinelai_kmp.domain.model.GenerationMetrics
import org.abma.offlinelai_kmp.domain.model.PerformanceStats
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MetricsCollectorTest {

    @BeforeTest
    fun setUp() {
        MetricsCollector.reset()
        ThermalMonitor.reset()
    }

    @Test
    fun generationMetricsCalculateComputesTtftTotalTimeAndTps() {
        val metrics = GenerationMetrics.calculate(
            promptTokens = 120,
            responseTokens = 40,
            startTime = 1_000L,
            firstTokenTime = 1_250L,
            endTime = 3_250L
        )

        assertEquals(120, metrics.promptTokens)
        assertEquals(40, metrics.responseTokens)
        assertEquals(250L, metrics.timeToFirstTokenMs)
        assertEquals(2_250L, metrics.totalGenerationTimeMs)
        // 40 tokens over 2000ms (endTime - firstTokenTime) = 20.0 tok/s
        assertEquals(20.0, metrics.tokensPerSecond)
        assertContains(metrics.toDebugString(), "20.0 tok/s")
    }

    @Test
    fun performanceStatsAggregatesMultipleRunsAndDetectsThrottling() {
        val emptyStats = PerformanceStats.fromMetrics(emptyList())
        assertEquals(0, emptyStats.totalGenerations)
        assertEquals(0.0, emptyStats.averageTokensPerSecond)

        val run1 = GenerationMetrics(
            promptTokens = 50,
            responseTokens = 20,
            timeToFirstTokenMs = 200L,
            totalGenerationTimeMs = 1_200L,
            tokensPerSecond = 20.0
        )
        val run2 = GenerationMetrics(
            promptTokens = 80,
            responseTokens = 30,
            timeToFirstTokenMs = 300L,
            totalGenerationTimeMs = 1_300L,
            tokensPerSecond = 30.0
        )

        val stats = PerformanceStats.fromMetrics(listOf(run1, run2))
        assertEquals(2, stats.totalGenerations)
        assertEquals(50, stats.totalTokensGenerated)
        assertEquals(2_500L, stats.totalTimeMs)
        assertEquals(250L, stats.averageTimeToFirstToken)
        assertEquals(25.0, stats.averageTokensPerSecond)
        assertEquals(20.0, stats.minTokensPerSecond)
        assertEquals(30.0, stats.maxTokensPerSecond)
        assertFalse(stats.isThrottled(expectedTps = 15.0))
        assertTrue(stats.isThrottled(expectedTps = 60.0))
    }

    @Test
    fun metricsCollectorTracksLifecycleCancellationAndReset() {
        assertFalse(MetricsCollector.isCurrentlyGenerating())
        assertNull(MetricsCollector.endGeneration())

        MetricsCollector.startGeneration(estimatedPromptTokens = 64)
        assertTrue(MetricsCollector.isCurrentlyGenerating())
        assertEquals(0, MetricsCollector.liveTokenCount.value)

        MetricsCollector.onTokenGenerated("Hello")
        MetricsCollector.onTokenGenerated(" world, this is a longer streaming chunk!")
        assertTrue(MetricsCollector.liveTokenCount.value >= 2)

        val completed = assertNotNull(MetricsCollector.endGeneration())
        assertFalse(MetricsCollector.isCurrentlyGenerating())
        assertEquals(64, completed.promptTokens)
        assertEquals(completed.responseTokens, MetricsCollector.stats.value.totalTokensGenerated)
        assertEquals(1, MetricsCollector.getAllMetrics().size)
        assertEquals(completed, MetricsCollector.getLastMetrics())

        // Cancel a subsequent run without recording it
        MetricsCollector.startGeneration(estimatedPromptTokens = 32)
        MetricsCollector.onTokenGenerated("partial")
        MetricsCollector.cancelGeneration()
        assertFalse(MetricsCollector.isCurrentlyGenerating())
        assertEquals(0, MetricsCollector.liveTokenCount.value)
        assertEquals(1, MetricsCollector.getAllMetrics().size)

        MetricsCollector.reset()
        assertTrue(MetricsCollector.getAllMetrics().isEmpty())
        assertEquals(0, MetricsCollector.stats.value.totalGenerations)
    }

    @Test
    fun thermalMonitorTriggersCooldownWarningAfterConsecutiveSlowGenerations() {
        assertFalse(ThermalMonitor.shouldShowCoolingWarning())
        assertEquals(0L, ThermalMonitor.getRecommendedCooldownMs())

        ThermalMonitor.onGenerationComplete(4.0)
        ThermalMonitor.onGenerationComplete(5.0)
        assertFalse(ThermalMonitor.shouldShowCoolingWarning())

        ThermalMonitor.onGenerationComplete(6.0)
        assertTrue(ThermalMonitor.shouldShowCoolingWarning())
        assertEquals(5_000L, ThermalMonitor.getRecommendedCooldownMs())

        // Fast generation resets consecutive slow counter
        ThermalMonitor.onGenerationComplete(22.0)
        assertFalse(ThermalMonitor.shouldShowCoolingWarning())
        assertEquals(0L, ThermalMonitor.getRecommendedCooldownMs())
    }
}
