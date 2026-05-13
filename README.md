# 🤖 Gemma Offline AI - KMP

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-purple?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Compose-Multiplatform-blue?logo=jetpack-compose" alt="Compose Multiplatform">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS-green" alt="Platforms">
</p>

A **Kotlin Multiplatform** (KMP) application that runs Google's **Gemma LLM** completely offline on Android and iOS devices. Built with Compose Multiplatform for a shared UI experience, powered by MediaPipe LLM Inference API for on-device AI.

---

## 🔍 How It Works

This project demonstrates how to run Large Language Models (LLMs) on-device using a shared Kotlin codebase.

### 1. Shared UI & State
- **Compose Multiplatform**: The entire UI (Chat, Settings, Components) is written once in `commonMain`.
- **ViewModel**: A shared `ChatViewModel` manages the conversation state, model loading, and tool execution using `StateFlow`.

### 2. The Inference Interface
We use Kotlin's `expect/actual` mechanism to define a common `GemmaInference` interface. This allows the shared ViewModel to trigger AI actions without knowing the platform-specific details.

### 3. Android Implementation
- Uses the **MediaPipe Tasks GenAI** Android library.
- Implementation is found in `androidMain`.
- It directly interacts with the `LlmInference` Java API.

### 4. iOS Implementation (The Swift Bridge)
Since MediaPipe's iOS SDK is best used via Swift, we use a lightweight bridge:
1. **Kotlin (`iosMain`)**: Sends a `NSNotification` with the prompt.
2. **Swift (`iosApp`)**: The `InferenceBridge.swift` listens for notifications and runs inference using the native MediaPipe Swift SDK.
3. **Communication**: Partial tokens are passed back to Kotlin via `NSUserDefaults`, where they are collected into a `Flow` for the UI.

### 5. Tool Calling (Function Calling)
The app teaches the LLM to use local tools through **Prompt Engineering**:
- A **System Prompt** defines available tools (e.g., `get_current_time`, `open_url`).
- If the LLM needs a tool, it outputs a special token: `<<{"tool": "name", "arguments": {}}>>`.
- The `ChatViewModel` detects this, executes the native Kotlin function, and displays the result.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Compose Multiplatform UI                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ ChatScreen   │  │ SettingsScreen│  │ Components          │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                         ViewModel Layer                          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ ChatViewModel (StateFlow, Coroutines, Tool Registry)     │   │
│  └──────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                    Platform Abstraction (expect)                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐   │
│  │ GemmaInference   │  │ ModelRepository  │  │ FilePicker   │   │
│  └──────────────────┘  └──────────────────┘  └──────────────┘   │
├────────────────────────┬────────────────────────────────────────┤
│     Android (actual)   │           iOS (actual)                  │
│  ┌──────────────────┐  │  ┌──────────────────────────────────┐  │
│  │ MediaPipe SDK    │  │  │ MediaPipe Tasks (Swift)          │  │
│  │ LlmInference     │  │  │ Notification Bridge              │  │
│  └──────────────────┘  │  └──────────────────────────────────┘  │
└────────────────────────┴────────────────────────────────────────┘
```

---

## 🚀 Getting Started

### 1. Prerequisites
- Android Studio / IntelliJ IDEA
- Xcode (for iOS)
- A Gemma `.bin` model (MediaPipe format) from [Kaggle](https://www.kaggle.com/models/google/gemma)

### 2. Installation
1. Clone the repo.
2. Android: Run `composeApp` on a physical device.
3. iOS: Run `pod install` in `iosApp/`, then open `.xcworkspace` in Xcode.

### 3. Load Model
Go to **Settings**, import your `.bin` file, and wait for it to load.

---

## 🛠️ Tech Stack
- **Kotlin Multiplatform**
- **Compose Multiplatform**
- **MediaPipe LLM Inference**
- **Kotlinx Coroutines & Flow**
- **Kotlinx Serialization**

---

<p align="center">
  Made for Workshop Purposes 🎓
</p>
