# 🚀 Workshop Quick Start Guide

## Attendee Setup Checklist

Before the workshop begins, ensure you have:

- [ ] **Android Studio** Ladybug (2024.2.1) or later installed
- [ ] **JDK 17+** configured
- [ ] **Android device** or emulator with **4GB+ RAM**
- [ ] Workshop project cloned and synced
- [ ] Gemma model file pre-downloaded (instructor will provide)

---

## Project Structure Overview

```
OfflineAI-KMP/
├── composeApp/
│   └── src/
│       ├── commonMain/          ← Shared code (what we'll focus on)
│       │   └── kotlin/
│       │       ├── domain/      ← Models & repositories
│       │       ├── inference/   ← GemmaInference (expect)
│       │       └── ui/          ← Compose screens & ViewModels
│       ├── androidMain/         ← Android implementations
│       │   └── kotlin/
│       │       └── inference/   ← GemmaInference.android.kt
│       └── iosMain/             ← iOS implementations
├── iosApp/                      ← iOS native host app
└── workshop/                    ← Workshop materials (you are here!)
```

---

## Key Files You'll Work With

| File | Purpose |
|------|---------|
| `GemmaInference.kt` | Common interface for AI inference |
| `GemmaInference.android.kt` | Android MediaPipe implementation |
| `ChatViewModel.kt` | UI state management & business logic |
| `ChatScreen.kt` | Chat UI (already built) |
| `ModelConfig.kt` | Model configuration parameters |

---

## Quick Reference: Gemma Prompt Format

```
<start_of_turn>user
Your message here<end_of_turn>
<start_of_turn>model
```

⚠️ **Always use this format!** Raw text will produce garbage output.

---

## Quick Reference: Key APIs

### Loading a Model
```kotlin
val options = LlmInference.LlmInferenceOptions.builder()
    .setModelPath(modelPath)
    .setMaxTokens(2048)
    .setMaxTopK(40)
    .build()

llmInference = LlmInference.createFromOptions(context, options)
```

### Generating a Response (Streaming)
```kotlin
llmInference.generateResponseAsync(prompt) { partialResult, done ->
    // partialResult = next token(s)
    // done = true when generation complete
}
```

### Cleaning Up
```kotlin
llmInference.close()  // Always call to free memory!
```

---

## Workshop Flow

| Section | Topic | What You'll Do |
|---------|-------|----------------|
| 1 | Introduction | Listen & understand offline AI concepts |
| 2 | Architecture | Learn Gemma + MediaPipe internals |
| 3 | **Integration** | **Implement model loading & inference** |
| 4 | **Streaming** | **Build real-time token streaming** |
| 5 | **Memory** | **Implement conversation history** |
| 6 | Performance | Measure & optimize |
| 7 | Advanced | Build impressive demo feature |
| 8 | Production | Learn deployment best practices |
| 9 | Final Demo | Show off your creation! |

---

## Troubleshooting Common Issues

### "Model file not found"
- Check the model path is correct
- Ensure the file exists at that location
- On Android, use `context.filesDir` for app-local files

### "Out of memory" crash
- Close other apps
- Use a smaller quantized model
- Check your device has 4GB+ RAM

### Garbage/nonsense output
- You forgot prompt formatting!
- Wrap user input with `<start_of_turn>` tokens

### Very slow generation (<5 tokens/sec)
- Device may be thermal throttling
- Let it cool down
- Ensure you're using quantized model (INT4)

### App crashes when backgrounding
- You forgot to call `gemmaInference.close()`
- Add cleanup in `ViewModel.onCleared()`

---

## Helpful Kotlin Patterns

### StateFlow + Streaming
```kotlin
private val _response = MutableStateFlow("")

fun generateResponse(prompt: String) {
    viewModelScope.launch(Dispatchers.IO) {
        llmInference.generateResponseAsync(prompt) { token, done ->
            _response.update { it + token }
        }
    }
}
```

### Safe Model Loading
```kotlin
suspend fun loadModel(path: String): Result<Unit> = runCatching {
    withContext(Dispatchers.IO) {
        // Loading code here
    }
}
```

### Cleanup Pattern
```kotlin
class ChatViewModel : ViewModel() {
    private val gemmaInference = GemmaInference()
    
    override fun onCleared() {
        super.onCleared()
        gemmaInference.close()  // Critical!
    }
}
```

---

## Workshop Commands

Build & run Android:
```bash
./gradlew :composeApp:installDebug
```

Build iOS framework:
```bash
./gradlew :composeApp:linkPodDebugFrameworkIosArm64
```

Check for errors:
```bash
./gradlew :composeApp:build
```

---

## Model Information

| Model | File | Size | Min RAM | Tokens/sec |
|-------|------|------|---------|------------|
| Gemma 2B INT4 | `gemma-2b-it-gpu-int4.bin` | 1.4 GB | 4 GB | ~20 |
| Gemma 2B INT8 | `gemma-2b-it-gpu-int8.bin` | 2.7 GB | 6 GB | ~15 |
| Gemma 7B INT4 | `gemma-7b-it-gpu-int4.bin` | 4.5 GB | 10 GB | ~8 |

For this workshop, we use **Gemma 2B INT4**.

---

## Questions?

Ask the instructor or check:
- Workshop Slack channel
- GitHub Issues
- MediaPipe documentation: https://developers.google.com/mediapipe

---

**Let's build something amazing! 🤖**

