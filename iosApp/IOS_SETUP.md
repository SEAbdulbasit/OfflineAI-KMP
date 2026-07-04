# iOS Setup for Gemma Offline AI

This guide explains how to run Gemma offline on iOS with Kotlin Multiplatform, CocoaPods for the shared `composeApp` framework, and LiteRT-LM via Swift Package Manager.

## Prerequisites

- Xcode 26.0 or later
- iOS 16.0+ deployment target
- CocoaPods 1.16.2+ (`sudo gem install cocoapods`)
- Ruby 3.0+
- A physical iOS device is strongly recommended for model testing

## Setup Steps

### 1. Install CocoaPods Dependencies

```bash
cd iosApp
pod install
```

### 2. Open the Workspace

Always open `iosApp.xcworkspace` so Xcode can load both CocoaPods and the shared KMP framework.

```bash
open iosApp.xcworkspace
```

### 3. LiteRT-LM — No Manual Step Required

LiteRT-LM is already declared in `iosApp.xcodeproj/project.pbxproj` as a Swift Package Manager dependency. Xcode resolves and downloads it automatically when you open `iosApp.xcworkspace`.

```
Package: https://github.com/google-ai-edge/LiteRT-LM
Version:  from 0.13.1 (upToNextMajorVersion)
Product:  LiteRTLM
```

If you ever need to refresh it manually: **File > Packages > Resolve Package Versions**.

### 4. Download a Gemma Model

LiteRT-LM works best with `.litertlm` packages. `.bin` models are also supported when compatible with the runtime.

Recommended approach:
- Prefer `.litertlm` for new iOS deployments
- Keep filenames simple and exact, for example:
  - `gemma-3n-E2B-it-int4.litertlm`
  - `gemma-2b-it-gpu-int4.bin`

### 5. Add the Model to the App

#### Option A: Finder file sharing (recommended)

1. Connect the iPhone or iPad to your Mac
2. Open **Finder** and select the device
3. Open the **Files** tab
4. Drag the model file into this app's folder

#### Option B: Add to the app bundle (development only)

1. Drag the model file into the Xcode project navigator
2. Check **Copy items if needed**
3. Add it to the `iosApp` target

### 6. Configure the Model Path in the App

Use either:
- The absolute path returned by your own file picker / downloader, or
- The model filename if the file is in a searched app directory

Examples:
- `gemma-3n-E2B-it-int4.litertlm`
- `gemma-2b-it-gpu-int4.bin`

## Model Search Order

The KMP iOS resolver checks these locations in order when you pass a relative filename:

1. `Documents/{modelPath}`
2. `Documents/models/{modelPath}`
3. `Caches/{modelPath}`
4. App bundle
5. Raw `modelPath`
6. `{AppHome}/Documents/{modelPath}`

Absolute paths are used directly.

## How iOS Inference Works

- Kotlin posts `GemmaGenerateRequest` through `NSNotificationCenter`
- Swift listens, initializes LiteRT-LM if needed, and starts streaming
- Swift sends tokens back with `GemmaTokenResponse`
- Swift posts `GemmaGenerationDone` on completion
- Swift posts `GemmaGenerationError` if initialization or generation fails

This replaces the old `UserDefaults` polling bridge and enables token streaming on iOS.

## Troubleshooting

### "Model file not found"

- Verify the filename matches exactly
- Confirm the model exists in one of the searched locations
- If using a bundled file, make sure it is added to the `iosApp` target
- If using a direct path, make sure it is absolute and readable

### "Failed to initialize model" / generation errors

- Confirm the LiteRT-LM package is added to the `iosApp` target
- Verify the model format is supported by your LiteRT-LM version
- Ensure the model path points to a complete, uncorrupted file
- Try restarting the app after changing models so the engine can reinitialize cleanly

### CocoaPods setup issues

```bash
cd iosApp
pod deintegrate
pod cache clean --all
pod install
```

### Package resolution issues in Xcode

- In Xcode, use **File > Packages > Reset Package Caches**
- Then use **File > Packages > Resolve Package Versions**
- Clean the build folder with `Cmd+Shift+K` and rebuild

### Simulator limitations

- Keep `EXCLUDED_ARCHS[sdk=iphonesimulator*] = x86_64` in the Podfile settings
- Simulator support can differ from device behavior for large local models
- If inference fails on simulator, test on a real device before debugging the model itself

### "PBXFileSystemSynchronizedRootGroup unknown ISA" error

Your CocoaPods version is too old for newer Xcode project formats:

```bash
sudo gem install cocoapods
pod --version
```

Use CocoaPods `1.16.2` or later.

## Memory and Performance Notes

- First initialization can take noticeable time, especially on large models
- Reusing the same model path avoids unnecessary engine reinitialization
- Large Gemma models require multiple GB of free storage and significant RAM
- Streaming responses should begin before the full answer is complete

## Notes

- Test on a physical device for the most reliable results
- Keep the app in the foreground during long generations
- If you switch model files, the iOS bridge will recreate the LiteRT-LM engine automatically
