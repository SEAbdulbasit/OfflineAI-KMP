# 🤖 Workshop Guide - Sections 6-9

## Advanced Topics & Production Ready

---

# SECTION 6: PERFORMANCE + OPTIMIZATION

## 🎯 Learning Objectives

By the end of this section, attendees will:
- Measure and display tokens per second
- Understand quantization tradeoffs
- Implement performance metrics collection
- Optimize for device constraints
- Handle thermal throttling gracefully

---

## 📚 Concept Overview

### Performance Metrics That Matter

```
┌─────────────────────────────────────────────────────────────┐
│                 KEY PERFORMANCE METRICS                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. TIME TO FIRST TOKEN (TTFT)                             │
│     ┌─────────────────────────────────────────────────┐    │
│     │ User sends message → First token appears        │    │
│     │ Target: < 200ms                                 │    │
│     │ Measures: Model readiness + initial processing  │    │
│     └─────────────────────────────────────────────────┘    │
│                                                             │
│  2. TOKENS PER SECOND (TPS)                                │
│     ┌─────────────────────────────────────────────────┐    │
│     │ How fast tokens are generated                   │    │
│     │ Target: 15-30 tokens/sec on modern phones       │    │
│     │ Measures: Inference speed                       │    │
│     └─────────────────────────────────────────────────┘    │
│                                                             │
│  3. MEMORY FOOTPRINT                                       │
│     ┌─────────────────────────────────────────────────┐    │
│     │ RAM used during inference                       │    │
│     │ Target: < 2GB for Gemma 2B                      │    │
│     │ Measures: Device compatibility                  │    │
│     └─────────────────────────────────────────────────┘    │
│                                                             │
│  4. MODEL LOAD TIME                                        │
│     ┌─────────────────────────────────────────────────┐    │
│     │ Time from "load" to "ready"                     │    │
│     │ Target: 3-8 seconds                             │    │
│     │ Measures: Cold start experience                 │    │
│     └─────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Implementation

### Part 1: Performance Metrics Data Class

**File: `PerformanceMetrics.kt`**

```kotlin
package org.abma.offlinelai_kmp.domain.model

import kotlinx.datetime.Clock

/**
 * Captures performance metrics for a single generation.
 */
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
            val tps = if (totalTime > 0) {
                responseTokens * 1000.0 / totalTime
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
}

/**
 * Aggregated performance statistics.
 */
data class PerformanceStats(
    val averageTokensPerSecond: Double,
    val averageTimeToFirstToken: Long,
    val totalGenerations: Int,
    val totalTokensGenerated: Int
) {
    companion object {
        fun fromMetrics(metrics: List<GenerationMetrics>): PerformanceStats {
            if (metrics.isEmpty()) {
                return PerformanceStats(0.0, 0L, 0, 0)
            }
            
            return PerformanceStats(
                averageTokensPerSecond = metrics.map { it.tokensPerSecond }.average(),
                averageTimeToFirstToken = metrics.map { it.timeToFirstTokenMs }.average().toLong(),
                totalGenerations = metrics.size,
                totalTokensGenerated = metrics.sumOf { it.responseTokens }
            )
        }
    }
}
```

### Part 2: Metrics Collector

**File: `MetricsCollector.kt`**

```kotlin
package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.abma.offlinelai_kmp.domain.model.GenerationMetrics
import org.abma.offlinelai_kmp.domain.model.PerformanceStats

/**
 * Collects and aggregates performance metrics.
 */
object MetricsCollector {
    
    private val _metrics = mutableListOf<GenerationMetrics>()
    private val _currentStats = MutableStateFlow(PerformanceStats(0.0, 0L, 0, 0))
    val stats: StateFlow<PerformanceStats> = _currentStats.asStateFlow()
    
    // For current generation tracking
    private var generationStartTime: Long = 0
    private var firstTokenTime: Long = 0
    private var tokenCount: Int = 0
    private var promptTokenCount: Int = 0
    
    fun startGeneration(estimatedPromptTokens: Int) {
        generationStartTime = System.currentTimeMillis()
        firstTokenTime = 0
        tokenCount = 0
        promptTokenCount = estimatedPromptTokens
    }
    
    fun onTokenGenerated() {
        if (firstTokenTime == 0L) {
            firstTokenTime = System.currentTimeMillis()
        }
        tokenCount++
    }
    
    fun endGeneration() {
        val endTime = System.currentTimeMillis()
        
        if (firstTokenTime == 0L) firstTokenTime = endTime
        
        val metrics = GenerationMetrics.calculate(
            promptTokens = promptTokenCount,
            responseTokens = tokenCount,
            startTime = generationStartTime,
            firstTokenTime = firstTokenTime,
            endTime = endTime
        )
        
        _metrics.add(metrics)
        _currentStats.value = PerformanceStats.fromMetrics(_metrics)
        
        // Log for debugging
        println("📊 Generation complete:")
        println("   • Tokens: $tokenCount")
        println("   • TTFT: ${metrics.timeToFirstTokenMs}ms")
        println("   • Speed: ${"%.1f".format(metrics.tokensPerSecond)} tok/s")
    }
    
    fun reset() {
        _metrics.clear()
        _currentStats.value = PerformanceStats(0.0, 0L, 0, 0)
    }
}
```

### Part 3: Performance Display UI

**File: `PerformanceOverlay.kt`**

```kotlin
@Composable
fun PerformanceOverlay(
    stats: PerformanceStats,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isGenerating || stats.totalGenerations > 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = modifier
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tokens per second
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${"%.1f".format(stats.averageTokensPerSecond)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "tok/s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Time to first token
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.averageTimeToFirstToken}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ms TTFT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Total tokens
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.totalTokensGenerated}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "tokens",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

---

## 📊 Optimization Strategies

### Quantization Comparison

```
┌─────────────────────────────────────────────────────────────┐
│              QUANTIZATION COMPARISON                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Format    Size      Speed       Quality    Best For        │
│  ─────────────────────────────────────────────────────────  │
│  FP32      8 GB      Baseline    100%      Servers only    │
│  FP16      4 GB      1.5x        99%       High-end GPU    │
│  INT8      2 GB      2x          97%       Flagship phones │
│  INT4      1.4 GB    4x          95%       Most phones ✓   │
│                                                             │
│  RECOMMENDATION: Use INT4 for mobile, INT8 if device       │
│  has plenty of RAM and you need slightly better quality.   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Device-Specific Optimization

```kotlin
/**
 * Determine optimal model configuration based on device.
 */
object DeviceOptimizer {
    
    fun getRecommendedConfig(): ModelConfig {
        val availableRam = getAvailableRamMb()
        val cpuCores = Runtime.getRuntime().availableProcessors()
        
        return when {
            availableRam > 8000 -> ModelConfig(
                maxTokens = 4096,
                temperature = 0.8f,
                topK = 50
            )
            availableRam > 4000 -> ModelConfig(
                maxTokens = 2048,
                temperature = 0.8f,
                topK = 40
            )
            else -> ModelConfig(
                maxTokens = 1024,  // Reduce for low memory
                temperature = 0.8f,
                topK = 30
            )
        }
    }
    
    private fun getAvailableRamMb(): Long {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        return maxMemory / (1024 * 1024)
    }
    
    fun isDeviceCapable(): Boolean {
        return getAvailableRamMb() >= 3000  // Minimum 3GB
    }
}
```

### Thermal Management

```kotlin
/**
 * Monitor and respond to thermal state.
 */
object ThermalMonitor {
    
    private var consecutiveSlowGenerations = 0
    
    fun onGenerationComplete(tokensPerSecond: Double, expectedTps: Double = 15.0) {
        if (tokensPerSecond < expectedTps * 0.5) {
            consecutiveSlowGenerations++
            
            if (consecutiveSlowGenerations >= 3) {
                println("⚠️ Thermal throttling detected!")
                println("   Consider showing user a 'Device warming up' message")
            }
        } else {
            consecutiveSlowGenerations = 0
        }
    }
    
    fun shouldShowCoolingWarning(): Boolean {
        return consecutiveSlowGenerations >= 3
    }
    
    fun reset() {
        consecutiveSlowGenerations = 0
    }
}
```

---

## ⏱️ Section Timing

| Activity | Duration |
|----------|----------|
| Metrics overview | 5 min |
| Implementation walkthrough | 10 min |
| Optimization strategies | 10 min |
| Hands-on exercise | 10 min |
| **Total** | **35 min** |

---

# SECTION 7: ADVANCED OFFLINE MEDIA PIPELINE

## 🎯 Learning Objectives

Build an impressive demo feature: **Offline Document Summarizer**

Attendees will:
- Process PDF/text files entirely offline
- Chunk long documents for LLM processing
- Implement summarization pipeline
- Handle large documents gracefully

---

## 📚 Feature: Offline Document Summarizer

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│               OFFLINE DOCUMENT SUMMARIZER                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┐     ┌──────────────────────────────────┐  │
│  │   INPUT     │     │      PROCESSING PIPELINE          │  │
│  │             │     │                                    │  │
│  │  📄 PDF     │────►│  1. Extract Text                  │  │
│  │  📝 TXT     │     │  2. Chunk into segments           │  │
│  │  📋 DOC     │     │  3. Summarize each chunk          │  │
│  │             │     │  4. Combine summaries             │  │
│  └─────────────┘     │  5. Final summary                 │  │
│                      │                                    │  │
│                      └──────────────────────────────────┘  │
│                                     │                       │
│                                     ▼                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    OUTPUT                             │  │
│  │  "This document discusses... Key points are..."      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  ✅ Works completely offline                               │
│  ✅ Handles documents up to ~50 pages                      │
│  ✅ Progressive summarization                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Implementation

### Part 1: Document Chunker

```kotlin
package org.abma.offlinelai_kmp.documents

/**
 * Chunks long documents into processable segments.
 */
object DocumentChunker {
    
    private const val MAX_CHUNK_TOKENS = 1500  // Leave room for prompt
    private const val OVERLAP_TOKENS = 100     // Overlap for continuity
    
    /**
     * Split text into chunks that fit within context window.
     */
    fun chunk(text: String): List<String> {
        val words = text.split(Regex("\\s+"))
        val chunks = mutableListOf<String>()
        
        var currentChunk = StringBuilder()
        var currentTokens = 0
        
        for (word in words) {
            val wordTokens = (word.length / 4) + 1
            
            if (currentTokens + wordTokens > MAX_CHUNK_TOKENS) {
                chunks.add(currentChunk.toString().trim())
                
                // Start new chunk with overlap
                val overlapWords = currentChunk.toString()
                    .split(" ")
                    .takeLast(OVERLAP_TOKENS / 4)
                    .joinToString(" ")
                
                currentChunk = StringBuilder(overlapWords).append(" ")
                currentTokens = OVERLAP_TOKENS
            }
            
            currentChunk.append(word).append(" ")
            currentTokens += wordTokens
        }
        
        if (currentChunk.isNotBlank()) {
            chunks.add(currentChunk.toString().trim())
        }
        
        return chunks
    }
    
    /**
     * Get stats about the document.
     */
    fun getDocumentStats(text: String): DocumentStats {
        val words = text.split(Regex("\\s+")).size
        val estimatedTokens = (text.length / 4) + 1
        val chunks = chunk(text)
        
        return DocumentStats(
            wordCount = words,
            estimatedTokens = estimatedTokens,
            chunkCount = chunks.size,
            canProcessDirectly = estimatedTokens < MAX_CHUNK_TOKENS
        )
    }
}

data class DocumentStats(
    val wordCount: Int,
    val estimatedTokens: Int,
    val chunkCount: Int,
    val canProcessDirectly: Boolean
)
```

### Part 2: Summarization Use Case

```kotlin
package org.abma.offlinelai_kmp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.abma.offlinelai_kmp.documents.DocumentChunker
import org.abma.offlinelai_kmp.inference.GemmaInference

sealed class SummarizationResult {
    data class Progress(val stage: String, val progress: Float) : SummarizationResult()
    data class ChunkSummary(val chunkIndex: Int, val summary: String) : SummarizationResult()
    data class Complete(val summary: String) : SummarizationResult()
    data class Error(val exception: Exception) : SummarizationResult()
}

class SummarizeDocumentUseCase(
    private val gemmaInference: GemmaInference
) {
    
    private val chunkPrompt = """
        Summarize the following text in 2-3 sentences. 
        Focus on the key points and main ideas.
        Be concise but preserve important details.
        
        Text:
    """.trimIndent()
    
    private val combinePrompt = """
        Combine these summaries into a coherent overall summary.
        Maintain the key points from each section.
        Write 3-5 sentences that capture the essence of the full document.
        
        Section summaries:
    """.trimIndent()
    
    operator fun invoke(documentText: String): Flow<SummarizationResult> = flow {
        try {
            val stats = DocumentChunker.getDocumentStats(documentText)
            
            emit(SummarizationResult.Progress(
                "Analyzing document (${stats.wordCount} words)...",
                0.1f
            ))
            
            if (stats.canProcessDirectly) {
                // Short document - summarize directly
                emit(SummarizationResult.Progress("Summarizing...", 0.5f))
                
                val summary = summarizeChunk(documentText)
                emit(SummarizationResult.Complete(summary))
                
            } else {
                // Long document - chunk and summarize
                val chunks = DocumentChunker.chunk(documentText)
                val chunkSummaries = mutableListOf<String>()
                
                chunks.forEachIndexed { index, chunk ->
                    val progress = 0.2f + (0.6f * index / chunks.size)
                    emit(SummarizationResult.Progress(
                        "Summarizing section ${index + 1}/${chunks.size}...",
                        progress
                    ))
                    
                    val summary = summarizeChunk(chunk)
                    chunkSummaries.add(summary)
                    
                    emit(SummarizationResult.ChunkSummary(index, summary))
                }
                
                // Combine summaries
                emit(SummarizationResult.Progress(
                    "Creating final summary...",
                    0.9f
                ))
                
                val combinedText = chunkSummaries.joinToString("\n\n")
                val finalSummary = combineSummaries(combinedText)
                
                emit(SummarizationResult.Complete(finalSummary))
            }
            
        } catch (e: Exception) {
            emit(SummarizationResult.Error(e))
        }
    }
    
    private suspend fun summarizeChunk(text: String): String {
        val prompt = """
            $chunkPrompt
            
            $text
            
            Summary:
        """.trimIndent()
        
        val formatted = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
        
        val response = StringBuilder()
        gemmaInference.generateResponse(formatted).collect { token ->
            response.append(token)
        }
        
        return response.toString().trim()
    }
    
    private suspend fun combineSummaries(summaries: String): String {
        val prompt = """
            $combinePrompt
            
            $summaries
            
            Final summary:
        """.trimIndent()
        
        val formatted = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
        
        val response = StringBuilder()
        gemmaInference.generateResponse(formatted).collect { token ->
            response.append(token)
        }
        
        return response.toString().trim()
    }
}
```

### Part 3: UI for Document Summarizer

```kotlin
@Composable
fun DocumentSummarizerScreen(
    viewModel: SummarizerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // File picker button
        Button(
            onClick = { viewModel.pickDocument() },
            enabled = !uiState.isProcessing
        ) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Select Document")
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Progress indicator
        if (uiState.isProcessing) {
            Column {
                Text(
                    text = uiState.currentStage,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = uiState.progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Summary result
        uiState.summary?.let { summary ->
            Spacer(Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
```

---

## 🎤 Workshop Talking Points

> "This is where on-device AI really shines. Imagine summarizing sensitive legal documents without ever sending them to a server."

> "Notice the chunking strategy - we can handle documents much longer than the 8K context window by summarizing in pieces."

> "This same pattern works for: meeting transcripts, research papers, long emails, code documentation..."

---

## ⏱️ Section Timing

| Activity | Duration |
|----------|----------|
| Feature overview | 5 min |
| Chunking explanation | 8 min |
| Implementation walkthrough | 12 min |
| Live demo | 10 min |
| **Total** | **35 min** |

---

# SECTION 8: PRODUCTION CONSIDERATIONS

## 🎯 Key Topics

- Model updates and versioning
- Storage management
- Memory and lifecycle handling
- Error recovery strategies
- Battery and thermal considerations
- Offline-first UX patterns

---

## 📚 Production Checklist

### 1. Model Versioning

```kotlin
/**
 * Track model versions for updates.
 */
data class ModelVersion(
    val modelId: String,
    val version: String,
    val downloadedAt: Long,
    val fileSize: Long,
    val checksum: String
)

object ModelVersionManager {
    
    private val preferences: SharedPreferences // Platform-specific
    
    fun isUpdateAvailable(currentVersion: String, latestVersion: String): Boolean {
        return compareVersions(currentVersion, latestVersion) < 0
    }
    
    fun shouldPromptUpdate(lastPromptTime: Long): Boolean {
        val daysSincePrompt = (System.currentTimeMillis() - lastPromptTime) / (24 * 60 * 60 * 1000)
        return daysSincePrompt >= 7  // Prompt weekly at most
    }
}
```

### 2. Storage Management

```kotlin
object StorageManager {
    
    fun getModelStorageInfo(): StorageInfo {
        val modelsDir = getModelsDirectory()
        val totalSize = modelsDir.listFiles()?.sumOf { it.length() } ?: 0
        val availableSpace = getAvailableSpace()
        
        return StorageInfo(
            usedByModels = totalSize,
            availableSpace = availableSpace,
            canDownloadModel = availableSpace > 2_000_000_000 // 2GB minimum
        )
    }
    
    fun cleanupOldModels(keepCurrent: String) {
        val modelsDir = getModelsDirectory()
        modelsDir.listFiles()?.forEach { file ->
            if (file.name != keepCurrent) {
                file.delete()
            }
        }
    }
}
```

### 3. Lifecycle Management

```kotlin
class ChatViewModel : ViewModel() {
    
    private val gemmaInference = GemmaInference()
    
    init {
        // Listen to app lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    // App going to background - consider unloading
                    if (shouldUnloadOnBackground()) {
                        unloadModel()
                    }
                }
            }
        )
    }
    
    private fun shouldUnloadOnBackground(): Boolean {
        // Unload if memory pressure or user preference
        val memoryPressure = getMemoryPressureLevel()
        return memoryPressure > 0.8f
    }
    
    override fun onCleared() {
        super.onCleared()
        gemmaInference.close()  // Always clean up!
    }
}
```

### 4. Error Recovery

```kotlin
sealed class InferenceError {
    object ModelNotLoaded : InferenceError()
    object OutOfMemory : InferenceError()
    object ThermalThrottling : InferenceError()
    data class GenerationFailed(val cause: Throwable) : InferenceError()
}

object ErrorRecovery {
    
    fun handleError(error: InferenceError): RecoveryAction {
        return when (error) {
            is InferenceError.ModelNotLoaded -> RecoveryAction.LoadModel
            is InferenceError.OutOfMemory -> RecoveryAction.ClearMemoryAndRetry
            is InferenceError.ThermalThrottling -> RecoveryAction.WaitAndRetry(delayMs = 5000)
            is InferenceError.GenerationFailed -> RecoveryAction.ShowErrorAndRetry
        }
    }
}

sealed class RecoveryAction {
    object LoadModel : RecoveryAction()
    object ClearMemoryAndRetry : RecoveryAction()
    data class WaitAndRetry(val delayMs: Long) : RecoveryAction()
    object ShowErrorAndRetry : RecoveryAction()
}
```

### 5. Battery Considerations

```kotlin
object BatteryAwareInference {
    
    fun shouldAllowInference(context: Context): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging
        
        // Allow if charging or battery > 15%
        return isCharging || batteryLevel > 15
    }
    
    fun getRecommendedMaxTokens(batteryLevel: Int): Int {
        return when {
            batteryLevel > 50 -> 2048
            batteryLevel > 25 -> 1024
            else -> 512  // Conserve battery
        }
    }
}
```

### 6. Offline-First UX

```kotlin
@Composable
fun OfflineFirstUI(
    modelState: ModelState,
    isOnline: Boolean,
    content: @Composable () -> Unit
) {
    Column {
        // Offline indicator badge
        AnimatedVisibility(visible = !isOnline) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Offline Mode - AI works locally",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        
        content()
    }
}
```

---

## ⏱️ Section Timing

| Activity | Duration |
|----------|----------|
| Production checklist | 10 min |
| Error handling | 5 min |
| Lifecycle management | 5 min |
| Demo and discussion | 5 min |
| **Total** | **25 min** |

---

# SECTION 9: FINAL DEMO

## 🎯 The Showcase

A polished demonstration of everything built in the workshop.

---

## 🎬 Demo Script

### Setup (Before Demo)

1. ✅ Device fully charged
2. ✅ Airplane mode ON
3. ✅ Model pre-loaded
4. ✅ Chat history cleared
5. ✅ Profiler ready (optional)

### Demo Flow (10 minutes)

#### Part 1: The Offline Promise (2 min)

> "Remember at the start I asked you to turn on airplane mode? Let's do that again."

*Show airplane mode status*

> "No WiFi. No cellular. Just you and Gemma."

#### Part 2: Natural Conversation (3 min)

*Type:* "Hello! What can you help me with?"

*Watch response stream*

> "Notice how fast that first token appeared. That's on-device latency."

*Type:* "Can you explain Kotlin coroutines in simple terms?"

*Watch response*

> "See how it maintains context? Ask a follow-up..."

*Type:* "How are they different from threads?"

> "It remembers we were talking about coroutines. That's conversation memory working."

#### Part 3: Performance Metrics (2 min)

*Point to performance overlay*

> "We're generating about 20 tokens per second. Time to first token under 100ms."

> "This is running entirely on the CPU/GPU in this phone. No cloud involved."

#### Part 4: Advanced Feature Demo (2 min)

*Open document summarizer*

> "Let me show you something cool. I have a 10-page document here..."

*Load and summarize*

> "It chunks the document, summarizes each part, then combines them. All offline."

#### Part 5: The Challenge (1 min)

> "Here's my challenge to you: Go home tonight, load this app, turn off your WiFi, and see what you can build."

> "The future of AI isn't just in the cloud. It's in your pocket."

---

## 🎉 Closing Slide

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│            🎉 CONGRATULATIONS! 🎉                           │
│                                                             │
│    You've built an Offline AI App with:                    │
│                                                             │
│    ✅ On-device Gemma inference                            │
│    ✅ Real-time token streaming                            │
│    ✅ Conversation memory                                  │
│    ✅ Performance metrics                                  │
│    ✅ Production-ready architecture                        │
│                                                             │
│    All with Kotlin Multiplatform!                          │
│                                                             │
│    ─────────────────────────────────────────────────────   │
│                                                             │
│    Links:                                                   │
│    📦 Project: github.com/aspect-dev/OfflineAI-KMP        │
│    📚 Gemma: ai.google.dev/gemma                          │
│    🔧 MediaPipe: developers.google.com/mediapipe          │
│                                                             │
│    Questions? Find me after the session!                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Full Workshop Timing Summary

| Section | Topic | Duration |
|---------|-------|----------|
| 1 | Introduction | 25 min |
| 2 | Architecture | 30 min |
| 3 | Gemma Integration | 65 min |
| 4 | Token Streaming | 45 min |
| | **BREAK** | 15 min |
| 5 | Conversation Memory | 50 min |
| 6 | Performance | 35 min |
| 7 | Advanced Pipeline | 35 min |
| 8 | Production | 25 min |
| 9 | Final Demo | 15 min |
| | **Total** | **~5.5 hours** |

*Adjust sections based on actual time available*

---

## 🙏 Thank You!

Questions, feedback, or want to collaborate?

**[Your contact info here]**

---

**End of Workshop Guide** 🎉

