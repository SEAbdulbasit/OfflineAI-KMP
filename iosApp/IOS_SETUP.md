# iOS Setup for Gemma Offline AI

This guide explains how to set up MediaPipe LLM Inference for iOS to run Gemma models offline.

## Prerequisites

- Xcode 15.0 or later
- iOS 15.0+ deployment target
- CocoaPods installed (`sudo gem install cocoapods`)

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

## Memory Requirements

| Model | RAM Required | Storage |
|-------|-------------|---------|
| Gemma 2B INT4 | ~2.5 GB | ~1.5 GB |
| Gemma 7B INT4 | ~5 GB | ~4.5 GB |

## Device Compatibility

- **Minimum**: iPhone 11, iPad (8th gen)
- **Recommended**: iPhone 13+, iPad Pro M1+

## Notes

- First model load may take 30-60 seconds
- Subsequent loads are faster due to caching
- Keep the app in foreground during generation
- Generation speed: ~10-30 tokens/second depending on device
