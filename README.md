# 🤖 Gemma Offline AI - KMP

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.1.0-purple?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Compose-1.7.3-blue?logo=jetpack-compose" alt="Compose Multiplatform">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS-green" alt="Platforms">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
</p>

A **Kotlin Multiplatform** (KMP) application that runs Google's **Gemma LLM** completely offline on Android and iOS devices. Built with Compose Multiplatform for a shared UI experience, powered by MediaPipe LLM Inference API for on-device AI.

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
| 🚀 **Native Performance** | Platform-specific optimizations via MediaPipe |

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
│  │ MediaPipe SDK    │  │  │ MediaPipeTasksGenAI (CocoaPods)  │  │
│  │ LlmInference     │  │  │ Swift Bridge                     │  │
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
│   ├── inference/                 # GemmaInference.android.kt (MediaPipe)
│   ├── picker/                    # FilePicker.android.kt, AttachmentPicker.android.kt
│   └── repository/                # ModelRepository.android.kt
│
└── iosMain/                       # iOS-specific implementations
    ├── inference/                 # GemmaInference.ios.kt (MediaPipe via CocoaPods)
    ├── picker/                    # FilePicker.ios.kt, AttachmentPicker.ios.kt
    └── repository/                # ModelRepository.ios.kt
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|-------------|---------|
| Android Studio | Ladybug (2024.2.1) or later |
| Xcode | 15.0+ (for iOS) |
| JDK | 17+ |
| Kotlin | 2.1.0 |

### 1. Clone the Repository

```bash
git clone https://github.com/aspect-dev/OfflineAI-KMP.git
cd OfflineAI-KMP
```

### 2. Download a Gemma Model

Download a compatible model from [Kaggle](https://www.kaggle.com/models/google/gemma) or [Hugging Face](https://huggingface.co/google):

| Model | Size | Recommended For |
|-------|------|-----------------|
| `gemma-2b-it-gpu-int4.bin` | ~1.4 GB | Most devices |
| `gemma-3n-E2B-it.task` | ~1.8 GB | Newer devices |
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
4. Select your downloaded `.bin` or `.task` file
5. The model will be copied to app storage and loaded

---

## 📱 Platform Requirements

| Platform | Minimum | Recommended | Notes |
|----------|---------|-------------|-------|
| **Android** | API 24 (7.0) | API 30+ | 4GB+ RAM, GPU support preferred |
| **iOS** | iOS 15.0 | iOS 16+ | iPhone 12+ / iPad Pro for best performance |

### Device Recommendations

- **Android**: Pixel 6+, Samsung Galaxy S21+, or equivalent
- **iOS**: iPhone 12 or newer, iPad Pro (M1/M2)

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

| Library | Version | Purpose |
|---------|---------|---------|
| Compose Multiplatform | 1.7.3 | Shared UI framework |
| MediaPipe Tasks GenAI | 0.10.24 | On-device LLM inference |
| Kotlinx Coroutines | 1.9.0 | Async operations & Flow |
| Kotlinx Serialization | 1.7.3 | JSON serialization |
| Lifecycle ViewModel | 2.8.4 | MVVM architecture |
| Navigation Compose | 2.8.0 | Screen navigation |

### iOS CocoaPods Setup

The `Podfile` in `iosApp/` includes:

```ruby
platform :ios, '15.0'

target 'iosApp' do
  use_frameworks!
  pod 'MediaPipeTasksGenAI', '0.10.24'
  pod 'MediaPipeTasksGenAIC', '0.10.24'
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
- [MediaPipe](https://developers.google.com/mediapipe) - ML inference framework
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
