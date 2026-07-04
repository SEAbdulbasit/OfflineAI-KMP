# 🤖 Gemma Offline AI - KMP

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.4.0-purple?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Compose-1.11.0-blue?logo=jetpack-compose" alt="Compose Multiplatform">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS-green" alt="Platforms">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
</p>

A **Kotlin Multiplatform** (KMP) application that runs Google's **Gemma LLM** completely offline on Android and iOS devices. Built with Compose Multiplatform for a shared UI experience, powered by **LiteRT-LM** on both platforms for unified on-device AI inference.

<p align="center">
  <em>Your conversations stay private. No internet required. 100% on-device AI.</em>
</p>

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔒 **Fully Offline** | Run Gemma models completely on-device without internet connection |
| 📱 **Cross-Platform** | Single codebase for Android & iOS using Compose Multiplatform |
| 💬 **Real-time Streaming** | See AI responses as they're generated token by token |
| 📎 **Attachments** | Attach images and PDFs to your messages |
| 🎨 **Modern UI** | Beautiful Material 3 design with dark/light theme support |
| ⚙️ **Configurable** | Adjust temperature, max tokens, and top-p parameters |
| 💾 **Model Management** | Import, load, and manage multiple models |
| 🚀 **Native Performance** | Platform-specific optimizations via LiteRT-LM |

---

## 📸 Screenshots

<!-- Add screenshots here -->
| Chat Screen | Settings | Dark Mode |
|-------------|----------|-----------|
| Chat Interface | Model Management | Theme Support |

---

## 🏗️ Architecture

The app follows a clean architecture pattern with **expect/actual** mechanism for platform-specific implementations:

```
┌─────────────────────────────────────────────────────────────────┐
│                        Compose Multiplatform UI                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ ChatScreen   │  │ SettingsScreen│  │ Components          │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                         ViewModel Layer                          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ ChatViewModel (StateFlow, Coroutines)                    │   │
│  └──────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                         Domain Layer                             │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐                 │
│  │ ChatMessage│  │ Attachment │  │ ModelConfig│                 │
│  └────────────┘  └────────────┘  └────────────┘                 │
├─────────────────────────────────────────────────────────────────┤
│                    Platform Abstraction (expect)                 │
│  ┌──────────────────┐  ┌──────────────────┐                     │
│  │ GemmaInference   │  │ AttachmentPicker │                     │
│  └──────────────────┘  └──────────────────┘                     │
├────────────────────────┬────────────────────────────────────────┤
│     Android (actual)   │           iOS (actual)                  │
│  ┌──────────────────┐  │  ┌──────────────────────────────────┐  │
│  │ LiteRT-LM SDK    │  │  │ LiteRT-LM Swift SDK (SPM)        │  │
│  │ Engine           │  │  │ Native Swift Bridge              │  │
│  └──────────────────┘  │  └──────────────────────────────────┘  │
└────────────────────────┴────────────────────────────────────────┘
```

### Project Structure

```
composeApp/src/
├── commonMain/                    # Shared Kotlin code (95%+ shared)
│   ├── domain/
│   │   ├── model/                 # ChatMessage, Attachment, ModelConfig, ModelState
│   │   └── repository/            # ModelRepository (expect)
│   ├── inference/                 # GemmaInference (expect)
│   ├── picker/                    # FilePicker, AttachmentPicker (expect)
│   └── ui/
│       ├── components/            # EmptyStateView, LoadingIndicator
│       ├── screens/               # ChatScreen, SettingsScreen
│       ├── theme/                 # Material 3 Theme, ExtendedColors
│       └── viewmodel/             # ChatViewModel, ChatUiState
│
├── androidMain/                   # Android-specific implementations
│   ├── inference/                 # GemmaInference.android.kt (LiteRT-LM)
│   ├── picker/                    # FilePicker.android.kt, AttachmentPicker.android.kt
│   └── repository/                # ModelRepository.android.kt
│
└── iosMain/                       # iOS-specific implementations
    ├── inference/                 # GemmaInference.ios.kt (LiteRT-LM)
    ├── picker/                    # FilePicker.ios.kt, AttachmentPicker.ios.kt
    └── repository/                # ModelRepository.ios.kt
```

---

## ✅ Why LiteRT-LM over MediaPipe?

LiteRT-LM is now the inference layer for **both Android and iOS**, replacing MediaPipe completely across the project.

### Unified SDK

- Single inference stack across Android (Kotlin) and iOS (Swift)
- No more platform divergence in the AI layer
- Shared concepts, model handling, and streaming behavior across both apps

### Performance improvements

- **Multi-Token Prediction (MTP)** for speculative decoding and faster token generation
- **GPU acceleration** with OpenCL on Android and Metal on iOS
- **NPU support** on devices with neural processing units
- **Smarter caching** for faster subsequent model load times

### Modern API design

- **Android**: First-class Kotlin coroutine `Flow` support for streaming with minimal bridging
- **iOS**: Native Swift `async/await` and `AsyncStream` support
- True push-based token streaming with no polling workarounds

### Multi-modal support

- Text, image, and audio inputs supported out of the box

### Tool use / Function calling

- Models can call defined Kotlin or Swift functions directly

### Active development

- MediaPipe LLM Inference API is superseded by LiteRT-LM
- LiteRT-LM receives ongoing updates and platform improvements

### Consistent model format

- `.litertlm` models work across both Android and iOS

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|-------------|---------|
| Android Studio | Meerkat (2025.1.1) or later |
| Xcode | 16.0+ (for iOS) |
| JDK | 17+ |
| Kotlin | 2.4.0 |

### 1. Clone the Repository

```bash
git clone https://github.com/aspect-dev/OfflineAI-KMP.git
cd OfflineAI-KMP
```

### 2. Download a Gemma Model

Download a compatible model from [Kaggle](https://www.kaggle.com/models/google/gemma) or [Hugging Face](https://huggingface.co/google).

> **Recommended:** Use `.litertlm` models when available for the best cross-platform LiteRT-LM experience. `.bin` models are also supported.

| Model | Size | Recommended For |
|-------|------|-----------------|
| `gemma-2b-it-gpu-int4.bin` | ~1.4 GB | Most devices |
| `gemma-3n-E2B-it.litertlm` | ~1.8 GB | Newer devices |
| `gemma-7b-it-gpu-int4.bin` | ~4.5 GB | High-end devices |

### 3. Build & Run

#### Android

```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Or run directly
./gradlew :composeApp:installDebug
```

#### iOS

```bash
# Install CocoaPods dependencies
cd iosApp
pod install
cd ..

# Build Kotlin framework
./gradlew :composeApp:linkPodDebugFrameworkIosArm64
```

Then open `iosApp/iosApp.xcworkspace` in Xcode and run.

### 4. Load a Model

1. Launch the app
2. Go to **Settings** (gear icon)
3. Tap **"Browse Files to Import Model"**
4. Select your downloaded `.litertlm` or `.bin` file
5. The model will be copied to app storage and loaded

---

## 📱 Platform Requirements

| Platform | Minimum | Recommended | Notes |
|----------|---------|-------------|-------|
| **Android** | API 24 (7.0) | API 34+ | 4GB+ RAM, GPU support preferred |
| **iOS** | iOS 16.0 | iOS 17+ | iPhone 12+ / iPad Pro for best performance |

### Device Recommendations

- **Android**: Pixel 7+, Samsung Galaxy S22+, or equivalent
- **iOS**: iPhone 12 or newer, iPad Pro (M1/M2/M4)

---

## ⚙️ Configuration

### Model Parameters

| Parameter | Range | Default | Description |
|-----------|-------|---------|-------------|
| **Temperature** | 0.0 - 1.0 | 0.7 | Controls randomness (lower = focused, higher = creative) |
| **Max Tokens** | 256 - 4096 | 2048 | Maximum response length |
| **Top-p** | 0.0 - 1.0 | 0.9 | Nucleus sampling threshold |

### Theme

The app automatically follows system theme preferences. Supports:
- 🌞 Light Mode
- 🌙 Dark Mode

---

## 🔧 Technical Details

### Dependencies

| Library | Version | Platform | Purpose |
|---------|---------|----------|---------|
| Compose Multiplatform | 1.11.0 | Both | Shared UI framework |
| LiteRT-LM | 0.11.0 | Android | On-device LLM inference |
| LiteRT-LM Swift | 0.13.1+ | iOS | On-device LLM inference (GPU/Metal, streaming) |
| Kotlinx Coroutines | 1.11.0 | Both | Async operations & Flow |
| Kotlinx Serialization | 1.11.0 | Both | JSON serialization |
| Lifecycle ViewModel | 2.10.0 | Both | MVVM architecture |
| Navigation Compose | 2.10.0 | Both | Screen navigation |

### iOS Setup

LiteRT-LM is declared directly in `iosApp.xcodeproj/project.pbxproj` via **Swift Package Manager** — no manual Xcode steps required. When you open `iosApp.xcworkspace`, Xcode resolves and downloads LiteRT-LM automatically.

```
Package: https://github.com/google-ai-edge/LiteRT-LM
Version:  from 0.13.1 (upToNextMajorVersion)
Product:  LiteRTLM
```

CocoaPods is still used only for integrating the `composeApp` Kotlin framework. The `Podfile` no longer includes MediaPipe dependencies:

```ruby
source 'https://cdn.cocoapods.org'

platform :ios, '16.0'
use_frameworks! :linkage => :static

target 'iosApp' do
  pod 'composeApp', :path => '../composeApp'
end
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is open source under the MIT License. See [LICENSE](LICENSE) for details.

**Note**: Gemma models are subject to Google's [Gemma Terms of Use](https://ai.google.dev/gemma/terms).

---

## 🙏 Acknowledgments

- [Google Gemma](https://ai.google.dev/gemma) - The on-device LLM
- [LiteRT-LM](https://ai.google.dev/edge/litert-lm/overview) - On-device ML inference framework (Android & iOS)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) - Cross-platform development
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) - Shared UI framework
- [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery) - UI inspiration

---

<p align="center">
  Made with ❤️ using Kotlin Multiplatform
</p>

<p align="center">
  <a href="https://kotlinlang.org/docs/multiplatform.html">Learn more about Kotlin Multiplatform</a>
</p>
