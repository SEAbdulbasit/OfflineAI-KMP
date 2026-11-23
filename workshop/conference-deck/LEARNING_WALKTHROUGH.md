# Flashlight: one request, end to end

This project uses shared, prompt-based tool proposals on Android and iOS. It does not yet demonstrate an integrated FunctionGemma tool pipeline. General models require compatible artifacts and chat templates plus examples; FunctionGemma requires its documented tool declaration/call format and task evaluation, often task-specific tuning. Do not swap model files and assume the custom JSON parser is compatible.

All paths below are relative to the repository root. Kotlin package directory: `org/abma/offlinelai_kmp`.

1. `composeApp/src/commonMain/kotlin/org/abma/offlinelai_kmp/tools/ToolPrompt.kt`: `buildSystemPrompt` includes tool descriptions, output format and ON/OFF examples. The instructions are prepended in this integration; “systemPrompt” does not imply a separately enforced privileged role.
2. `.../domain/usecase/GenerateResponseUseCase.kt`: accumulate streaming chunks; call `extractToolCall(fullResponse)` on completion.
3. `.../tools/ToolPrompt.kt`: permissive extraction and recovery. Parsing does not prove correct intent.
4. `.../tools/Tooling.kt`: `ToolRegistry` rejects unknown tools; `ToggleTorchTool` requires exactly `enable` with a JSON Boolean. Quoted booleans and extra fields are rejected before invoking the platform.
5. `composeApp/src/androidMain/kotlin/org/abma/offlinelai_kmp/tools/AppActionsProvider.android.kt`: select flash-capable camera, call `CameraManager.setTorchMode`.
6. `composeApp/src/iosMain/kotlin/org/abma/offlinelai_kmp/tools/AppActionsProvider.ios.kt`: availability checks, configuration lock, `setTorchMode`, unlock in finally.
7. `.../ui/viewmodel/ChatViewModel.kt`: show model proposal and device result separately. No second inference call creates the confirmation.

## Reproduce the deterministic failure

```sh
./gradlew :composeApp:testDebugUnitTest --tests '*ToggleTorchToolTest'
```

Passed in this review. The test injects a fake platform action. Eight malformed argument objects, including `{"enable":"true"}`, produce no action and a hardware-call counter of zero. This verifies application validation, not model accuracy or physical hardware.

## Exercise: copy_text(text)

- Define a `ToolHandler` and JSON schema in `Tooling.kt`; require one nonempty string, reject extra arguments.
- Register it in `createDefaultToolRegistry()` and add examples in `ToolPrompt.kt`.
- Add the shared declaration to `tools/AppActionsProvider.kt`; implement Android clipboard and iOS pasteboard actions in each `actual` provider.
- Inject the action into the handler as `ToggleTorchTool` does. Test correct input, wrong type, missing/extra fields, unavailable platform and cancellation.
- On each device evaluate exact requests, paraphrases, unrelated requests and ambiguous requests. A schema-valid wrong action is still a failure.

## Device evidence and measurement protocol

Verified: signed build and installation on iPhone 16 Pro, iOS 26.5. Launch blocked by developer-profile trust. The app initially had no model. **No inference latency or physical torch outcome has been measured.**

When unblocked, record exact model artifact/checksum, runtime version/backend, device/OS, cold or warm state and network state. Start timing immediately before inference; first response is the first nonempty output chunk, not necessarily a token. Also record completion time, raw output, parsed tool/arguments, validation result, native acceptance and physical ON/OFF observation. Keep model-loading time separate. Do not infer tokens/sec from chunk count.

## Primary references

- https://ai.google.dev/gemma/docs/functiongemma/formatting-and-best-practices
- https://ai.google.dev/gemma/docs/mobile-actions
- https://developers.google.com/edge/litert-lm/overview
- https://developers.google.com/edge/litert-lm/swift
