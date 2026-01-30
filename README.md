# Gemma Offline AI - KMP

A Kotlin Multiplatform (KMP) app that runs Google's Gemma LLM completely offline on Android and iOS devices using MediaPipe LLM Inference API.

## Features

- 🤖 **Offline AI Chat**: Run Gemma models completely on-device without internet
- 📱 **Cross-Platform**: Shared UI and logic between Android and iOS using Compose Multiplatform
- 💬 **Chat Interface**: Beautiful Material 3 chat UI with streaming responses
- ⚙️ **Configurable**: Adjust model parameters like max tokens and temperature
- 🎨 **Modern Design**: Gemma-inspired color theme with dark/light mode support

## Project Structure

```
composeApp/src/
├── commonMain/           # Shared Kotlin code
│   ├── domain/model/     # Data classes (ChatMessage, ModelConfig)
│   ├── inference/        # GemmaInference expect class
│   └── ui/
│       ├── components/   # Reusable UI components
│       ├── screens/      # Chat & Settings screens
│       ├── theme/        # Material 3 theme
│       └── viewmodel/    # ChatViewModel
├── androidMain/          # Android-specific code
│   └── inference/        # MediaPipe Android implementation
└── iosMain/              # iOS-specific code
    └── inference/        # MediaPipe iOS implementation
```

## Dependencies

- **Compose Multiplatform** 1.10.0 - Shared UI
- **MediaPipe Tasks GenAI** 0.10.21 - On-device LLM inference
- **Kotlinx Coroutines** 1.9.0 - Async operations
- **Kotlinx Serialization** 1.7.3 - JSON serialization
- **Navigation Compose** - Screen navigation

## Getting Started

### Prerequisites

- Android Studio Ladybug or later
- Xcode 15+ (for iOS)
- JDK 11+

### Model Setup

1. Download a Gemma model from [Hugging Face](https://huggingface.co/google):
   - **Gemma 3 1B** (recommended): `gemma-3-1b-it-int4.task`
   - **Gemma 2B**: `gemma-2b-it-gpu-int4.bin`

2. Place the model file on your device:
   - **Android**: `/sdcard/Download/` or app's files directory
   - **iOS**: App's Documents directory

3. Launch the app, go to Settings, and enter the model path

### Build and Run

#### Android
```shell
./gradlew :composeApp:assembleDebug
```

#### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run.

## Architecture

The app uses the **expect/actual** pattern for platform-specific implementations:

- `GemmaInference` (expect) - Common interface for model inference
- `GemmaInference.android.kt` (actual) - MediaPipe Android implementation
- `GemmaInference.ios.kt` (actual) - MediaPipe iOS implementation (requires CocoaPods setup)

## iOS Setup

For iOS, you need to add MediaPipe dependencies. Create a `Podfile` in `iosApp/`:

```ruby
platform :ios, '15.0'

target 'iosApp' do
  use_frameworks!
  pod 'MediaPipeTasksGenAI', '~> 0.10.21'
  pod 'MediaPipeTasksGenAIC', '~> 0.10.21'
end
```

Then run `pod install` in the `iosApp` directory.

## Requirements

| Platform | Minimum | Recommended |
|----------|---------|-------------|
| Android  | API 24 (7.0) | API 30+ with 4GB+ RAM |
| iOS      | 15.0 | iPhone 12+ / iPad Pro |

## License

This project is open source. Gemma models are subject to Google's [Gemma Terms of Use](https://ai.google.dev/gemma/terms).

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
