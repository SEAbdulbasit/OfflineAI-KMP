# Offline AI with KMP — speaker notes

[Original-theme presentation](https://docs.google.com/presentation/d/1MP3JyglYOusu0V24-phTvqXzmvtt5PhkLs1LTySinig/edit).

**23:30 planned + 1:30 contingency = 25:00.** Seven demonstration minutes are included: 30-second app preview on slide 2, 2:30 notes run on slide 15, two-minute FAQ run on slide 16, and two-minute flashlight run on slide 19. Define LLM and inference before comparing cloud and device execution, then explain the local benefits and costs before model internals and implementation.

| Slide | Topic | Duration including demo | Talk clock |
|---|---|---:|---:|
| 1 | Original cover | 0:30 | 0:00–0:30 |
| 2 | Three offline interactions | 0:45 | 0:30–1:15 |
| 3 | What is an LLM? | 0:45 | 1:15–2:00 |
| 4 | Training and inference | 1:00 | 2:00–3:00 |
| 5 | Cloud AI and on-device AI | 0:45 | 3:00–3:45 |
| 6 | Why run AI on a phone? | 1:00 | 3:45–4:45 |
| 7 | Offline after setup | 0:45 | 4:45–5:30 |
| 8 | What is inside a model? | 0:45 | 5:30–6:15 |
| 9 | Parameters and quantization | 0:45 | 6:15–7:00 |
| 10 | Mobile model downloads | 0:45 | 7:00–7:45 |
| 11 | Tokens and context | 0:45 | 7:45–8:30 |
| 12 | Shared product behavior with KMP | 1:00 | 8:30–9:30 |
| 13 | The actual shared API | 0:45 | 9:30–10:15 |
| 14 | Three commitments, one deliberate omission | 0:45 | 10:15–11:00 |
| 15 | Ask, check, review | 3:00 | 11:00–14:00 |
| 16 | Local FAQ: answer or admit absence | 2:30 | 14:00–16:30 |
| 17 | Same contract, different lifecycle | 0:30 | 16:30–17:00 |
| 18 | Function calling: model proposes, app executes | 1:00 | 17:00–18:00 |
| 19 | Flashlight on and off | 3:00 | 18:00–21:00 |
| 20 | What to verify before shipping | 1:00 | 21:00–22:00 |
| 21 | Three takeaways | 0:45 | 22:00–22:45 |
| 22 | Code and references | 0:30 | 22:45–23:15 |
| 23 | Original branded closing | 0:15 | 23:15–23:30 |

## 1. Original cover

0:30 — 0:00–0:30. Today we will build useful AI features on a phone without sending each request to a model server. We will first define the model and explain what local execution changes, then connect that idea to shared Kotlin, real application checks, and three demonstrations. The goal is to understand both the useful behavior and the engineering boundary.

## 2. Three offline interactions

0:45 — 0:30–1:15. Spend 15 seconds introducing three interactions: meeting notes become reviewed tasks, a local FAQ supplies a quote or admits an absent answer, and a flashlight request becomes a native action. Spend 30 seconds showing the actual input screens and navigation. These screenshots are iOS simulator inputs, not generated results. We will return to each feature after explaining its mechanism. This bounded-feature approach is also discussed in Google’s on-device GenAI session: https://io.google/2025/explore/technical-session-14/ .

## 3. What is an LLM?

0:45 — 1:15–2:00. An LLM—a large language model—learns patterns in text during training. Given a prompt, it generates a continuation. For the visible prompt “The workshop is in …”, “Room Cedar” is an illustrative possible continuation. It may be wrong: plausible text is not evidence of the actual location. A useful app supplies relevant context and checks its output. Before discussing where this runs, distinguish training from using a prepared model.

## 4. Training and inference

1:00 — 2:00–3:00. Follow the two paths on screen. Training: examples are used to learn parameters, producing a trained model. Inference: a prompt plus that trained model generates an output, which becomes the response. For “The workshop is in …”, the model uses its prepared parameters and supplied context to generate a continuation; this request does not retrain it. Our app performs inference, and that computation can happen on a server or on the phone. The next slide compares those two locations.

## 5. Cloud AI and on-device AI

0:45 — 3:00–3:45. Compare the same task in two places. Cloud: the phone sends a request over the network, a server runs the model, and the response returns. On device: the phone passes the request to a local runtime and model, then receives the output locally. The basic task is unchanged; computation and the resource cost move. “On device” describes where execution happens. Whether the whole feature works offline also depends on its model, data, and any additional services being available locally.

## 6. Why run AI on a phone?

1:00 — 3:45–4:45. Choose local execution when keeping inference requests on the device, working without connectivity after setup, and avoiding a hosted inference fee fit the product. The tradeoff is that the phone pays with storage, memory, battery, thermal load, and variable hardware performance. Smaller or specialized models may be sufficient, but quality and speed must meet the task’s requirements. Local is not automatically faster, free, or compliant. Putting computation on the phone is why the model’s size and precision matter.

## 7. Offline after setup

0:45 — 4:45–5:30. Before the talk, download or import a compatible model and make the needed source data available. During these demos, prompts, the model, source checks, and results stay within the device path. Show airplane mode enabled and Wi-Fi off before claiming an offline run. A separate website or map action may still need network data. Logs, backups, and other app services need their own review; local inference is a boundary we design, not a blanket guarantee.

## 8. What is inside a model?

0:45 — 5:30–6:15. A model combines architecture—the arrangement of calculations—with learned parameters or weights. Training adjusts those numbers; inference uses them. Here is a toy calculation, not an LLM: output = weight × input + bias = 0.8 × 2 + 0.1 = 1.7. The learned values are 0.8 and 0.1, and the input is 2. Real language models connect many such numerical operations and use a tokenizer and inference runtime. A parameter is not a fact or a dictionary entry. “1B” describes roughly one billion parameters, not one billion facts.

## 9. Parameters and quantization

0:45 — 6:15–7:00. For a hypothetical model containing exactly one billion stored values, 16 bits per value requires 2 GB, 8 bits requires 1 GB, and 4 bits requires 0.5 GB. This is decimal, ideal weight-value storage: parameters × bits ÷ 8. It is not a model download-size or runtime RAM claim. Files can contain scales, metadata and mixed-precision tensors; execution also needs caches and buffers. Quantization represents values with fewer bits without necessarily reducing the parameter count. Quality can change, and acceleration depends on the backend and hardware. Do not multiply an effective E2B label as if it counted every stored weight. Background: https://huggingface.co/docs/transformers/quantization/concept_guide and https://ai.google.dev/gemma/docs/core .

## 10. Mobile model downloads

0:45 — 7:00–7:45. These are specific listed .litertlm files, not theoretical weight arithmetic: Gemma 3 1B IT, gemma3-1b-it-int4.litertlm, 584 MB; Gemma 3n E2B IT, gemma-3n-E2B-it-int4.litertlm, 3.66 GB; Gemma 4 E2B IT, gemma-4-E2B-it.litertlm, 2.59 GB; Qwen3 0.6B, qwen3_0.6b_q4_block32_ekv1280.litertlm, 347 MB. Listed artifact sizes were checked 21 October 2026. Download size is not runtime RAM, and compatibility with this app’s pinned runtime has not been verified. E2B denotes effective parameters, not a complete stored-weight count. Verified artifact sources are https://huggingface.co/litert-community/Gemma3-1B-IT/tree/main ; https://huggingface.co/google/gemma-3n-E2B-it-litert-lm/tree/main ; https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/blob/main/gemma-4-E2B-it.litertlm ; https://huggingface.co/litert-community/Qwen3-0.6B-int4/blob/main/qwen3_0.6b_q4_block32_ekv1280.litertlm . These are importable-artifact candidates, not a claim that every phone or this build can run them. System-model APIs such as Gemini Nano or Apple Foundation Models are separate integration routes.

## 11. Tokens and context

0:45 — 7:45–8:30. Tokens are tokenizer-defined pieces of text. The request includes instructions, source notes, and room for output. This app limits notes to 4,000 characters; the Android engine sets maxNumTokens to 4,096. Those are different units and do not establish identical iOS behavior. Longer inputs require more resources, and the selected artifact and configuration determine the usable budget. Now that we know what the phone must run, we can decide which parts of the feature to share.

## 12. Shared product behavior with KMP

1:00 — 8:30–9:30. Share product rules, state, and UI; isolate native inference and device APIs. Follow input through Compose UI, DemoController, prompt construction, GemmaInference, the Android Kotlin adapter or iOS Swift bridge, then shared parsing and review. Gemma is the model family, LiteRT-LM executes compatible artifacts, and KMP shares feature behavior. The two text features use source checks; the torch handler validates its own arguments. Other tools do not automatically inherit those guarantees. This is KMP’s practical value: one feature implementation above explicit native boundaries. Related KotlinConf session: https://2026.kotlinconf.com/talks/1086800/ ; https://www.youtube.com/watch?v=Rqj4QCdwBMc .

## 13. The actual shared API

0:45 — 9:30–10:15. The actual expect-class excerpt provides loadModel and generateResponse returning Flow<String>. Shared UI consumes incremental text without depending on the native callback API. A chunk is not necessarily one tokenizer token. The adapter supplies the platform implementation. This contract lets us build the same task and review screen on both platforms while preserving native integration below it. Now follow a real input through that feature.

## 14. Three commitments, one deliberate omission

0:45 — 10:15–11:00. Use the fictional bundled notes. Sara will update the slides, with no agreed deadline. Omar will record the Android demo by Friday. Lina will test the iOS build by Thursday. The team discussed a dark theme but made no commitment. Expected output—not a captured run—is {"items":[{"task":"update the slides","owner":"Sara","deadline":null,"evidence":"Sara will update the slides."},{"task":"record the Android demo","owner":"Omar","deadline":"Friday","evidence":"Omar will record the Android demo by Friday."},{"task":"test the iOS build","owner":"Lina","deadline":"Thursday","evidence":"Lina will test the iOS build by Thursday."}]}. Highlight Sara’s null and the omitted discussion rather than reading every field aloud.

## 15. Ask, check, review

3:00 — 11:00–14:00. Use 30 seconds to connect request and checks. The prompt requests a JSON schema and exact evidence; code checks that fields appear in that evidence. Sara/null passes. Sara/Friday fails because Friday is not in her sentence. A dark-theme discussion can pass literal matching while still being the wrong kind of item. These parser cases are tested, so the result remains a draft for review. Then spend 2:30 running the notes example: inspect the source, missing deadline, raw output, and one edit. Prompt-requested JSON plus validation is different from constrained generation; Apple’s schema-to-UI session provides a useful comparison: https://developer.apple.com/videos/play/wwdc2025/286/ .

## 16. Local FAQ: answer or admit absence

2:30 — 14:00–16:30. Use 30 seconds to explain the second contract, then two minutes to run it. Source: “The Offline AI workshop takes place in Room Cedar on level 2.” For “Where is the workshop?”, expected JSON is {"sourceId":"venue","quote":"The Offline AI workshop takes place in Room Cedar on level 2."}. For “What is the Wi-Fi password?”, expected JSON is {"sourceId":null,"quote":null}. All four small reference sections go into the prompt; no vector search is involved. During the run, inspect the exact quote and the missing-answer state. Matching source text helps, but an irrelevant real quote can still pass.

## 17. Same contract, different lifecycle

0:30 — 16:30–17:00. The shared contract does not make lifecycle identical. Android initializes during loading and tries GPU before CPU. The iOS bridge initializes when generation needs an engine for the selected model; configuration and cleanup differ. In this version, stopping Kotlin collection does not establish native iOS task cancellation. These differences belong in device testing. So far, output becomes information for review. Next, it proposes an action.

## 18. Function calling: model proposes, app executes

1:00 — 17:00–18:00. Function calling means the model proposes a named action with structured arguments, and application code handles that proposal. The app describes toggle_torch as turning the flashlight on or off; its handler requires enable to be a JSON boolean. ToolSpec stores schema metadata, but this implementation prompts with descriptions and examples rather than sending the complete schema through a constrained tool API. Follow the steps: model proposal, Kotlin parsing and handler validation, native dispatch, then UI result. The broader parser is permissive; the torch handler is strict about its arguments. This app displays the result directly without invoking the model again. Related Google AI Edge talk: https://io.google/2025/explore/technical-session-4/ .

## 19. Flashlight on and off

3:00 — 18:00–21:00. Use one minute to explain the call and two minutes to demonstrate it. Expected on proposal: {"tool":"toggle_torch","arguments":{"enable":true}}; off uses false. The handler rejects a quoted boolean, null, missing or extra arguments without calling hardware. A valid request reaches AppActionsProvider.toggleTorch(enable). The UI retains the proposal, arguments, and native result, such as “Device accepted flashlight ON request.” Select a chip and send; selecting alone does not execute. Show the real light changing and finish with it off. Native acceptance is not optical proof. If the torch is unavailable, use the existing device-time tool within this allocation. We have now seen model output become either reviewed information or a checked native request.

## 20. What to verify before shipping

1:00 — 21:00–22:00. Before shipping, verify three layers. Task quality: Sara’s missing deadline, faithful evidence, absent FAQ answers, and correct action arguments. Device behavior: actual native results, first nonempty chunk, total completion, memory, cold versus warm runs, and thermal conditions. Offline boundary: prepared model and sources, with connectivity disabled for the test. Record the exact artifact, runtime, backend, and device; leave unmeasured fields blank. The project pins Android LiteRT-LM 0.14.0 and the iOS wrapper artifact 0.13.1. Test those implementations, not a generic model-family promise.

## 21. Three takeaways

0:45 — 22:00–22:45. The lesson is a sequence of decisions: choose a bounded task with a useful missing-answer behavior; share the product rules and UI with KMP; verify the native implementation on the actual device. Model generation supplies a proposal, application code checks the relevant contract, and the user or device result supplies the final evidence. Local execution can be valuable when that complete path meets the product’s needs.

## 22. Code and references

0:30 — 22:45–23:15. Start in DemoSamples for prompt and validation rules, DemoController for streaming state, and GemmaInference for the platform contract. Repository: https://github.com/SEAbdulbasit/OfflineAI-KMP . Current runtime docs: https://developers.google.com/edge/litert-lm . Published Google I/O, WWDC, and KotlinConf references with their specific lessons are collected in workshop/REFERENCE_TALKS.md. Their API behavior and benchmarks are not results from this project. Talks: Small language models with Google AI Edge, I/O 2025: https://io.google/2025/explore/technical-session-4/ ; Meet the Foundation Models framework, WWDC25: https://developer.apple.com/videos/play/wwdc2025/286/ ; Tiny Models, Big Impact, KotlinConf 2026: https://www.youtube.com/watch?v=Rqj4QCdwBMc .

## 23. Original branded closing

0:15 — 23:15–23:30. Choose a bounded task. Share the product behavior with KMP. Verify the native implementation on the real device. Thank you. Planned content ends at 23:30, leaving 1:30 for questions or demonstration delays.

## Evidence and rehearsal

Ten parser tests cover the action-item and FAQ cases, including cross-person deadline rejection and the non-commitment limitation. The flashlight handler validates the argument before a native request. These checks do not establish model quality or physical torch behavior. The simulator input screenshots were captured without a loaded model; actual offline inference and timing need successful rehearsal on the presentation device.

See [DEMO_EDITION.md](DEMO_EDITION.md) for the runbook, [MODEL_GUIDE.md](MODEL_GUIDE.md) for model-selection details, and [REFERENCE_TALKS.md](REFERENCE_TALKS.md) for published talk sources.

## Optional substitutes: clipboard or current device time

The existing chat tool `copy_to_clipboard` already has Android and iOS implementations. Ask “Copy this to the clipboard: Sara will update the slides.” Expected call: `{"tool":"copy_to_clipboard","arguments":{"text":"Sara will update the slides."}}`. Paste into a local text field to verify the native result. This is an optional substitution for part of the flashlight segment or a Q&A example, not extra scheduled content. Tool selection still needs model rehearsal; do not claim it has been verified.

If the physical torch is unavailable, ask “What time is it?” Expected proposal: `{"tool":"get_current_time","arguments":{}}`. The existing handler returns the current device time as an ISO-8601 instant. This is a fallback within the existing demo block, not a fourth main demo. Model selection of either fallback still requires rehearsal.
