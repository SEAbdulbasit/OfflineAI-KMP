# iOS Setup for Gemma Offline AI

This guide explains how to set up MediaPipe LLM Inference for iOS to run Gemma models offline.

## Prerequisites

- Xcode 26.0 or later
- iOS 16.0+ deployment target
- CocoaPods 1.16.2+ (`sudo gem install cocoapods`)
- Ruby 3.0+

## Setup Steps

### 1. Install CocoaPods Dependencies

```bash
cd iosApp
pod install
```

### 2. Open the Workspace

**Important**: Always open `iosApp.xcworkspace` (NOT `iosApp.xcodeproj`)

```bash
open iosApp.xcworkspace
```

### 3. Download the Gemma Model

Download the Gemma model from Kaggle:

1. Go to [Kaggle Gemma Models](https://www.kaggle.com/models/google/gemma/tfLite/)
2. Download the appropriate model:
   - **For CPU (recommended for compatibility)**: `gemma-2b-it-cpu-int4.task`
   - **For GPU (better performance)**: `gemma-2b-it-gpu-int4.bin`

### 4. Add Model to App

#### Option A: Add to App Bundle (for development)
1. Drag the model file into Xcode project navigator
2. Ensure "Copy items if needed" is checked
3. Add to the iosApp target

#### Option B: Download to Documents (for production)
The app will look for models in:
- `Documents/` directory
- `Documents/models/` directory
- App bundle
- Caches directory

You can implement a download manager to fetch models on first launch.

### 5. Configure the Model Path

In your Settings screen, use the model filename:
- `gemma-2b-it-cpu-int4.task` (CPU version)
- `gemma-2b-it-gpu-int4.bin` (GPU version)

## Model Locations (Search Order)

The iOS implementation searches for models in this order:
1. `{Documents}/{modelPath}`
2. `{Documents}/models/{modelPath}`
3. App Bundle
4. `{Caches}/{modelPath}`
5. Direct path

## Troubleshooting

### "Model file not found"
- Ensure the model is in one of the searched locations
- Check the filename matches exactly (case-sensitive)
- Verify the file was properly copied to the app

### "Failed to load model"
- Ensure you have enough device storage
- Try with a smaller model first
- Check device compatibility (A12 chip or later recommended)

### Build Errors with CocoaPods
```bash
cd iosApp
pod deintegrate
pod cache clean --all
pod install
```

### Xcode 26 Linker Errors

#### "framework 'UIUtilities' not found"
This is a MediaPipe auto-linking issue. Solutions:

1. **Clean build folder**: `Cmd+Shift+K` then `Cmd+Shift+Opt+K`
2. **Update CocoaPods**:
   ```bash
   sudo gem install cocoapods
   cd iosApp
   pod deintegrate
   pod install
   ```
3. **Check Podfile** has static linkage:
   ```ruby
   use_frameworks! :linkage => :static
   ```

#### "SwiftUICore not found" / "not an allowed client"
This is an Xcode 26 compatibility issue with private frameworks:

1. **Update Xcode** to the latest version
2. **Clean derived data**:
   ```bash
   rm -rf ~/Library/Developer/Xcode/DerivedData/*
   ```
3. **Rebuild**:
   ```bash
   cd iosApp
   pod deintegrate
   pod install
   # Then rebuild in Xcode
   ```

#### "Undefined symbol: _LlmInferenceEngine_Session_*"
This indicates MediaPipe framework isn't linking correctly:

1. **Verify static library linking** in Xcode:
   - Select your target → Build Phases
   - Check "Link Binary With Libraries" includes MediaPipeTasksGenAI/C

2. **Add force_load flag** (if needed):
   In Xcode → Target → Build Settings → Other Linker Flags:
   ```
   -force_load $(PODS_ROOT)/MediaPipeTasksGenAIC/frameworks/genai_libraries/libMediaPipeTasksGenAIC_device.a
   ```

3. **Try device build** instead of simulator (some features are device-only)

### "PBXFileSystemSynchronizedRootGroup unknown ISA" Error
Your CocoaPods version doesn't support Xcode 26's new project format:

```bash
sudo gem install cocoapods
# Verify version >= 1.16.2
pod --version
```

## Memory Requirements

| Model | RAM Required | Storage |
|-------|-------------|---------|
| Gemma 2B INT4 | ~2.5 GB | ~1.5 GB |
| Gemma 7B INT4 | ~5 GB | ~4.5 GB |

## Device Compatibility

- **Minimum**: iPhone 12, iPad (9th gen)
- **Recommended**: iPhone 14+, iPad Pro M1+

## Notes

- First model load may take 30-60 seconds
- Subsequent loads are faster due to caching
- Keep the app in foreground during generation
- Generation speed: ~10-30 tokens/second depending on device
- **Simulator builds may have limited functionality** - test on device for full features
