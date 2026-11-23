# iOS flashlight verification — 22 October 2026

## Result

**Build and physical-device installation passed. App launch, model inference, first-response timing and flashlight outcome were not verified.** No performance value should appear as a measured result.

- Device: paired physical iPhone 16 Pro (iPhone17,1), iOS 26.5 (23F77), connected over local network.
- App: `org.abma.offlinelai-kmp-offlinelai-kmp`.
- Built app: `/tmp/offlineai-ios-device/Build/Products/Debug-iphoneos/offlinelai_kmp.app`.
- Device build: `JAVA_HOME=/Users/abdulbasit/Library/Java/JavaVirtualMachines/jbr-21.0.6/Contents/Home xcodebuild -workspace iosApp/iosApp.xcworkspace -scheme iosApp -configuration Debug -destination 'generic/platform=iOS' -derivedDataPath /tmp/offlineai-ios-device build`.
- Build log: `/tmp/offlineai-ios-device-build.log` — `BUILD SUCCEEDED`.
- Install log: `/tmp/offlineai-iphone-install.log` — installation succeeded.

## Launch blocker

`xcrun devicectl device process launch --device 00008140-000678542146801C --terminate-existing org.abma.offlinelai-kmp-offlinelai-kmp --timeout 25`

Launch failed with CoreDevice error 10002 / FBSOpenApplicationErrorDomain 3: iOS rejected the app for signature, entitlements or an untrusted profile. Local `codesign --verify --deep --strict` passed. The embedded profile includes this device and expires 27 October 2026; the signing team is HC2G6RBBJC. Developer-profile trust is the next item to check on the phone, not a proven sole cause.

Evidence: `/tmp/offlineai-iphone-launch.log`, `/tmp/offlineai-iphone-launch.json`, `/tmp/offlineai-device-profile.plist`.

## Model provisioning

The original app container had no model: Documents, caches and preferences were empty. Evidence: `/tmp/offlineai-iphone-container.json`.

The Gemma 3 1B INT4 download returned `GatedRepo` and required authenticated access. A public candidate was downloaded instead:

- Source: https://huggingface.co/litert-community/Qwen3-0.6B-int4/resolve/main/qwen3_0.6b_q4_block32_ekv1280.litertlm
- Local file: `/tmp/offlineai-models/qwen3_0.6b_q4_block32_ekv1280.litertlm`.
- Complete local size: **347,251,840 bytes**.
- SHA-256: `312ce4e6bc0816edf844b9b7e66542648a89440b54822c3581e48a19e9ff5715`, matching publisher LFS metadata in `/tmp/offlineai-qwen-file-metadata.json`.
- Compatibility with the pinned iOS runtime and this app's prompt formatting remains unverified.

Copy to the app's Documents directory **timed out after 180 seconds**. An intermediate listing showed an **81.8 MB partial file** with the final `.litertlm` filename. A subsequent listing timed out, preventing a current contents check. **Do not import this partial device file.** It may remain on the phone. No directory cleanup was performed because newly added user files could not be ruled out.

Evidence: `/tmp/offlineai-iphone-model-copy.log`, `/tmp/offlineai-iphone-model-presence.log`, `/tmp/offlineai-iphone-model-final.log`.

## Next verification steps

1. On the iPhone, check Settings → General → VPN & Device Management and trust the development profile if offered; keep the phone unlocked and preferably connect by USB.
2. Inspect Documents and remove only the incomplete `qwen3_0.6b_q4_block32_ekv1280.litertlm`. Copy the complete local artifact again and verify exact byte size before loading.
3. Launch, import and load the model; record any compatibility error honestly.
4. Run “Turn on flashlight” and “Turn off flashlight”. Record exact model/runtime/device, first nonempty response chunk, final proposed JSON, native handler result and a separate physical observation of the lamp.

Parser tests, successful compilation, native API acceptance and visually observed light are different evidence. None substitutes for a measured end-to-end model-driven run.
