# 📁 Workshop Code Guide

## Quick Reference: Workshop Sections → Code Files

This guide maps each workshop section to the relevant code files for easy navigation during the workshop.

---

## Section 3: Gemma Integration

### Core Files

| File | Purpose | Section |
|------|---------|---------|
| `inference/GemmaInference.kt` | expect interface (shared) | Section 3.1 |
| `inference/GemmaInference.android.kt` | Android MediaPipe implementation | Section 3.2 |
| `inference/ModelPathResolver.kt` | Model file discovery (expect) | Section 3.3 |
| `inference/ModelPathResolver.android.kt` | Android path resolution | Section 3.3 |
| `domain/model/ModelConfig.kt` | Configuration options | Section 3.4 |

### Code Flow
```
User clicks "Load Model"
  → ChatViewModel.loadModel()
    → LoadModelUseCase()
      → GemmaInference.loadModel()
        → ModelPathResolver.resolve()
        → MediaPipe LlmInference.createFromOptions()
  → UI shows "Ready"
```

---

## Section 4: Token Streaming

### Core Files

| File | Purpose | Section |
|------|---------|---------|
| `inference/GemmaInference.android.kt` | callbackFlow streaming | Section 4.1 |
| `domain/usecase/GenerateResponseUseCase.kt` | Prompt building + streaming | Section 4.2 |
| `ui/viewmodel/ChatViewModel.kt` | StateFlow + UI updates | Section 4.3 |

### Code Flow
```
User sends message
  → ChatViewModel.sendMessage()
    → ChatMessage.ai("", isStreaming=true) // placeholder
    → generateResponse()
      → GenerateResponseUseCase()
        → GemmaInference.generateResponse() // callbackFlow
          → MediaPipe.generateResponseAsync() // tokens
            → trySend(token) // into Flow
        → collect { } // accumulate tokens
          → emit(Streaming(fullResponse))
      → collect { Streaming ->
        → updateStreamingMessage(content)
          → _uiState.update { messages.map { ... } }
      }
  → finishStreaming()
```

---

## Section 5: Conversation Memory

### Core Files

| File | Purpose | Section |
|------|---------|---------|
| `inference/PromptBuilder.kt` | Prompt formatting + context | Section 5.1 |
| `domain/model/ChatMessage.kt` | Message data model | Section 5.2 |
| `domain/usecase/GenerateResponseUseCase.kt` | History formatting | Section 5.3 |

### Prompt Format
```
<start_of_turn>user
System Instructions: [system prompt]<end_of_turn>
<start_of_turn>model
I understand.<end_of_turn>
<start_of_turn>user
[message 1]<end_of_turn>
<start_of_turn>model
[response 1]<end_of_turn>
<start_of_turn>user
[current message]<end_of_turn>
<start_of_turn>model
← Model generates from here
```

---

## Section 6: Performance

### Core Files

| File | Purpose | Section |
|------|---------|---------|
| `inference/MetricsCollector.kt` | Token/second tracking | Section 6.1 |
| `domain/model/PerformanceMetrics.kt` | Metrics data classes | Section 6.2 |

---

## Key Architecture Patterns

### 1. expect/actual (Platform Abstraction)

```kotlin
// commonMain - Interface
expect class GemmaInference() {
    fun generateResponse(prompt: String): Flow<String>
}

// androidMain - Implementation
actual class GemmaInference {
    actual fun generateResponse(prompt: String): Flow<String> = callbackFlow {
        // MediaPipe integration
    }
}
```

### 2. callbackFlow (Callback → Flow Bridge)

```kotlin
fun generateResponse(prompt: String): Flow<String> = callbackFlow {
    mediapipenference.generateResponseAsync(prompt) { token, done ->
        trySend(token)  // Non-blocking, callback-safe
        if (done) close()
    }
    awaitClose { /* cleanup */ }
}
```

### 3. StateFlow (Reactive UI State)

```kotlin
// ViewModel
private val _uiState = MutableStateFlow(ChatUiState())
val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

// Update (thread-safe)
_uiState.update { it.copy(messages = newMessages) }

// UI (Compose)
val state by viewModel.uiState.collectAsState()
```

### 4. Streaming Message Pattern

```kotlin
// 1. Create placeholder
val msg = ChatMessage.ai("", isStreaming = true)
streamingId = msg.id
messages += msg

// 2. Update content as tokens arrive
messages = messages.map { 
    if (it.id == streamingId) it.copy(content = newContent) 
    else it 
}

// 3. Finish streaming
messages = messages.map { 
    if (it.id == streamingId) it.copy(isStreaming = false) 
    else it 
}
```

---

## File Quick Links

### Common (Shared) Code
```
composeApp/src/commonMain/kotlin/org/abma/offlinelai_kmp/
├── domain/
│   ├── model/
│   │   ├── ChatMessage.kt          ← Message data model
│   │   ├── ModelConfig.kt          ← Config + ModelState enum
│   │   └── PerformanceMetrics.kt   ← Performance tracking
│   └── usecase/
│       └── GenerateResponseUseCase.kt  ← Prompt building + streaming
├── inference/
│   ├── GemmaInference.kt           ← expect interface
│   ├── ModelPathResolver.kt        ← expect interface
│   ├── PromptBuilder.kt            ← Prompt formatting helpers
│   └── MetricsCollector.kt         ← Performance metrics
└── ui/
    └── viewmodel/
        ├── ChatAction.kt           ← UI action definitions
        └── ChatViewModel.kt        ← Main orchestrator
```

### Android-Specific
```
composeApp/src/androidMain/kotlin/org/abma/offlinelai_kmp/
└── inference/
    ├── GemmaInference.android.kt   ← MediaPipe implementation
    └── ModelPathResolver.android.kt ← Android file paths
```

---

## Debugging Tips

### Enable Verbose Logging
The `GenerateResponseUseCase` already includes debug output:
```
═══════════════════════════════════════════════════════════
📤 SENDING TO LLM:
───────────────────────────────────────────────────────────
System Prompt (xxx chars):
...
```

Look for these in Logcat during the workshop.

### Check Model Path
If loading fails, check `ModelPathResolver.getSearchPaths()` output to see where we looked for the model.

### Memory Issues
Use Android Studio Profiler to watch memory during:
- Model load (~1.7GB spike)
- Generation (should stay flat)
- After close() (should drop)

---

## Workshop Checklist

Before starting:
- [ ] Model file downloaded (~1.4GB)
- [ ] Model copied to `/data/local/tmp/llm/` or app storage
- [ ] Android Studio profiler ready
- [ ] Device with 4GB+ RAM connected
- [ ] Logcat filtered to app package

Happy coding! 🚀

