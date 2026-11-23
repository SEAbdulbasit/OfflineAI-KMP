# Offline AI demo edition

Revised copy: [Offline AI with KMP — Original Theme](https://docs.google.com/presentation/d/1MP3JyglYOusu0V24-phTvqXzmvtt5PhkLs1LTySinig/edit).

The 23-slide talk keeps the original theme, cover, AI introduction, and branded closing. It first defines LLMs, training and inference, then compares cloud and device execution and explains offline benefits, boundaries, and costs. Model internals lead into shared KMP architecture and complete worked examples for notes, FAQ answers, and native actions. Four fundamentals slides explain model weights, parameter precision, listed mobile artifacts, and practical output criteria. See [MODEL_GUIDE.md](MODEL_GUIDE.md) for artifact sources and compatibility caveats. The interleaved demonstrations also restore the existing flashlight on/off tool demo. Plan **23:30 of content plus 1:30 contingency**, including **7 minutes of interleaved demonstrations**. [TALK_NOTES.md](TALK_NOTES.md) supplies exact timings, full expected JSON, and source citations. [REFERENCE_TALKS.md](REFERENCE_TALKS.md) records the published talks that informed the structure.

This project pins Android LiteRT-LM **0.14.0** and the iOS wrapper artifact **0.13.1**. Older MediaPipe workshop instructions are not setup instructions for this edition. Function calling is a separate existing chat path; the two added text demos do not dispatch tools.

## Publication preflight

The deck's repository QR opens `https://github.com/SEAbdulbasit/OfflineAI-KMP`. On 21 October 2026, remote `master` and local HEAD were both `46607d425eabdc7ef9b6e5baf30a34cd169a9326`; the new text-demo files, tests, and flashlight improvements remained local uncommitted changes. Before the talk, review and publish the chosen demo revision, then verify the QR destination contains that revision and its setup instructions. Until then, the QR links to the repository, not a published copy of the demonstrated changes.

## Talk sequence

1. Original cover
2. Three offline interactions
3. What is an LLM?
4. Training and inference
5. Cloud AI and on-device AI
6. Why run AI on a phone?
7. Offline after setup
8. What is inside a model?
9. Parameters and quantization
10. Mobile model downloads
11. Tokens and context
12. Shared product behavior with KMP
13. The actual shared API
14. Three commitments, one deliberate omission
15. Ask, check, review
16. Local FAQ: answer or admit absence
17. Same contract, different lifecycle
18. Function calling: model proposes, app executes
19. Flashlight on and off
20. What to verify before shipping
21. Three takeaways
22. Code and references
23. Original branded closing

## Run the samples

1. Build and launch the app. Import and load a compatible text model in Settings. Choose a low temperature when rehearsing structured extraction.
2. From chat, open **Offline demos · Action items & local FAQ**.
3. Open **Action items**, choose **Use sample notes**, then **Extract action items**.
4. Check that Sara's deadline remains unspecified, Omar's is Friday, and Lina's is Thursday. Discussion of a dark theme should not become a commitment. These are expected behaviors to rehearse, not hardcoded outputs.
5. Edit a task, owner, or deadline. Each original evidence sentence remains visible. Results and edits live in memory; there is no persistent task store or export.
6. Switch to **Local FAQ**. Expand **Read bundled reference**. Ask **Where is the workshop?** and inspect the cited venue passage.
7. Ask **What is the Wi-Fi password?** The reference contains no password. Expected model output is `{"sourceId":null,"quote":null}` and the UI should show **Not found in this reference**.
8. Show **Inspect model output** and the measured first-response and total times. First response means first nonempty text chunk, not one tokenizer token.
9. Check **Stop generation** and retry behavior on the target platform. Leaving the screen cancels the Kotlin collection. On iOS, the current notification bridge does not yet forward this cancellation to the native generation task, so do not describe this as verified native inference cancellation.
10. After the model is loaded, manually enable airplane mode and disable Wi-Fi. Repeat all three demos on the physical presentation device. Use a backup recording only after capturing a successful run from the exact device/build.

## Seven minutes of interleaved demonstrations

Keep the 23-slide sequence and **23:30 + 1:30 contingency**. Allocate **30 seconds app preview on slide 2, 2:30 action items after the checks on slide 15, 2 minutes FAQ on slide 16, and 2 minutes flashlight on slide 19**. These timings are already included in the slide table. Prepare the model before presenting; enable airplane mode and explicitly disable Wi-Fi. Briefly show this state before claiming an offline run. Slide 20 uses one minute for the shipping checks; demonstrations remain beside their explanations.

### Flashlight on and off — existing chat tool

Use a physical device with an available torch. From the demos screen, **Flashlight control in chat** opens a prepared draft. In chat, select **Flashlight on** or **Flashlight off**, then send. Selecting a chip or opening the draft alone does not generate or execute an action. Expected structured calls, not verified model captures:

```json
{"tool":"toggle_torch","arguments":{"enable":true}}
{"tool":"toggle_torch","arguments":{"enable":false}}
```

`ToggleTorchTool` declares a required boolean `enable` and calls `AppActionsProvider.toggleTorch(enable)`. It accepts exactly the `enable` key with a JSON boolean; missing, null, quoted, invalid, or extra arguments are rejected without a hardware call. On platform success its result text is **Device accepted flashlight ON request.** or **Device accepted flashlight OFF request.** On failure it reports that the torch may be unavailable, busy, or restricted. Chat preserves the model proposal, arguments, and device result. Verify the physical light, not just the generated text. Point the light away from people and finish with it off. The broader chat parser is permissive, but this flashlight handler now strictly validates its arguments before the hardware call. Device acceptance is not optical proof that the light changed. A simulator cannot establish physical torch behavior.

### Optional substitutes — already implemented

**Copy a useful result to the clipboard.** Ask: “Copy this to the clipboard: Sara will update the slides.” Expected proposal:

```json
{"tool":"copy_to_clipboard","arguments":{"text":"Sara will update the slides."}}
```

Both platform implementations already exist. Paste into a local text field to verify the copied text. Use this as a 30–45 second substitute or Q&A example, not a fourth timed segment. Model selection of the tool remains unverified until rehearsed.

**Current device time** is a fallback when a torch is unavailable: ask “What time is it?” Expected proposal: `{"tool":"get_current_time","arguments":{}}`. The existing handler returns the device time as an ISO-8601 instant. It needs no torch hardware. Keep the main presentation to three demos; substitute either fallback rather than adding a fourth. No speculative feature implementation is needed.

## Function-calling boundary

The app provides tool names, descriptions, parameter descriptions, and examples in its prompt. `ToolSpec` retains a JSON schema as metadata, but the complete schema is not passed to the model as a constrained tool API. A model proposal is parsed in shared Kotlin, routed by the registry, validated by the specific handler, dispatched through native code, and shown as a result in chat.

For `toggle_torch`, the handler requires exactly `enable` with a JSON boolean. Invalid arguments cause no hardware request. The broader chat parser remains permissive. The native result is displayed directly; this implementation does not invoke the model a second time with that result. Device acceptance does not prove the light changed, so physical observation remains part of rehearsal.

This illustrates the KMP split: share product rules, state, feature-specific validation, and UI; isolate inference and device APIs. This does not imply every chat tool has equally strict argument checks. The KMP architecture and shared API introduce the implementation before the notes and FAQ examples. Native lifecycle and function calling then explain the physical action demo.

## How the implementation works

- `DemoSamples.kt`: fictional bundled data, prompts, strict JSON parsing, evidence/citation checks.
- `DemoController.kt`: observable state, streaming, cancellation and monotonic elapsed time.
- `DemoScreen.kt`: shared Compose UI with inputs, editable output, raw-output inspection, empty/error states and source text.
- `ChatViewModel.kt`: supplies the existing inference engine and reserves it while a demo runs.
- `App.kt`: navigation to the new screen on both Android and iOS.

The notes and FAQ demos call the inference stream directly and never dispatch tool calls. Flashlight uses the separate existing chat tool path.

### Notes validation

Expected response shape:

```json
{"items":[{"task":"update the slides","owner":"Sara","deadline":null,"evidence":"Sara will update the slides."}]}
```

Evidence must appear verbatim in the input. Task, owner and deadline values must appear in that evidence. Missing fields use JSON null. Invalid JSON, unsupported values and fabricated evidence produce an error rather than silently repaired output. Exact matching does not establish semantic correctness: the presenter and user must still review commitments and ownership. User edits are not revalidated.

### FAQ validation

The entire small reference is included in each prompt. This is not vector retrieval, PDF ingestion, or a general document search engine. A valid response identifies one bundled section and quotes an exact substring from it. Unknown sources and invented quotes fail validation. A real but irrelevant quote can still pass; this demonstrates the difference between source validation and answer relevance.

## Rehearsal record

Record these for each physical device before presenting:

- Device and OS version
- Exact model filename, size, quantization, input support and context limit
- Runtime version and sampling configuration
- Cold-load time, first response and total generation time over several runs
- Whether both samples, missing-answer behavior and cancellation worked offline

Do not present model-output quality or device latency as verified until the actual model completes these checks. The implementation imposes a 4,000-character notes limit, a 300-character question limit, a 24,000-character output cap and a 20-item result cap. These are UI/demo limits, not token counts or model context limits.

## Automated checks

```bash
./gradlew :composeApp:testDebugUnitTest :composeApp:compileKotlinIosSimulatorArm64 :composeApp:assembleDebug
```

The 10 parser tests cover null deadlines, fabricated evidence, invented deadlines, malformed/tool-shaped output, valid and invalid citations, missing answers, fenced JSON, the cross-person deadline rejection, and the fact that literal matching does not establish a commitment. Separate controller tests cover streaming, unavailable engines, and cancellation before/after generation starts. The full physical-device Xcode build also passed. The app installed on the connected iPhone, but iOS refused launch with a code-signing/trust error. The simulator Xcode build passed and the app launched on iPhone 17 Pro / iOS 26.1. Tab switching, question presets and bundled-reference expansion were checked. No model was loaded for this UI check, so inference output quality and timing remain unverified. Build success does not prove model quality on iOS.

## Visual slide sources

The final talk uses the original-theme Google Slides copy above. Keep its original title/body styling and native editable text, shapes, and code excerpts. The earlier 14-slide raster-background visual edition is superseded and is not the presentation to rehearse.

The actual iOS simulator input captures in `workshop/visuals/ios-notes-source.jpg` and `workshop/visuals/ios-faq-source.jpg` were taken with no model loaded. They demonstrate the interface, not successful inference. Do not label illustrative expected calls or expected outputs as captured results. The original-theme closing retains the speaker's original branding.

Current documentation: [LiteRT-LM](https://developers.google.com/edge/litert-lm) and [Gemma models](https://ai.google.dev/gemma/docs). Match instructions to the project's pinned runtime and compatible model artifact rather than assuming current documentation features are already implemented.

## Presenter evidence boundaries

The saved iOS screenshots show actual simulator input screens with no model loaded. Expected JSON and artifact comparisons are illustrative or sourced examples, not captured inference results. Ten parser tests verify parser behavior, including the cross-person deadline and non-commitment cases; they do not establish model quality. A flashlight result reports native API acceptance, which must be checked against the physical light. Actual model execution, disconnected operation, timing and physical torch behavior require a successful rehearsal on the presentation device.

The current iOS bridge initializes when an engine is needed for the selected model and does not provide Android configuration/cleanup parity. Kotlin collection cancellation does not establish native task cancellation. Artifact download size is not runtime RAM, and the model table does not establish compatibility with the pinned build.
