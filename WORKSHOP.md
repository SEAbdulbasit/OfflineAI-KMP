# Workshop: Offline AI in Kotlin Multiplatform (KMP)

This workshop will guide you through integrating on-device LLM inference using Google MediaPipe in a KMP application.

## Prerequisites
- Android Studio / IntelliJ IDEA with KMP plugin
- Xcode (for iOS)
- A Gemma 2b/7b `.bin` model file (MediaPipe format)

## Workshop Steps

### Step 1: Define the Common Inference Interface
File: `composeApp/src/commonMain/kotlin/.../inference/GemmaInference.kt`
- Define the `expect class GemmaInference` with methods for loading, generating responses, and cleanup.

### Step 2: Android Implementation
File: `composeApp/src/androidMain/kotlin/.../inference/GemmaInference.android.kt`
- Implement `actual class GemmaInference`.
- Use MediaPipe `LlmInference` (tasks-genai) to load the model and generate responses.
- Use `callbackFlow` to handle inference results.

### Step 3: iOS Implementation & Swift Bridge
File: `composeApp/src/iosMain/kotlin/.../inference/GemmaInference.ios.kt` & `iosApp/iosApp/InferenceBridge.swift`
- KMP doesn't directly support MediaPipe's C++ based iOS SDK easily, so we use a Swift bridge.
- **Kotlin side**: Send a `NSNotification` with the prompt and poll `NSUserDefaults` for the response.
- **Swift side**: Listen for notifications, use MediaPipe Tasks GenAI (Swift), and store the result back in `UserDefaults`.

### Step 4: Integration with ViewModel
File: `composeApp/src/commonMain/kotlin/.../ui/viewmodel/ChatViewModel.kt`
- Initialize `GemmaInference` in the ViewModel.
- Call `loadModel` and `generateResponse` based on UI actions.

### Step 5: Advanced - Tool Calling (Function Calling)
File: `composeApp/src/commonMain/kotlin/.../tools/ToolPrompt.kt`
- Implement prompt engineering to teach the model how to use tools.
- Parse the model's output to detect tool calls and execute them.

---

## Getting Started
1. Switch to the `workshop-offline-ai-kmp-start` branch.
2. Follow the TODOs in the codebase for each step.
