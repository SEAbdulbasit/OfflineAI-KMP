# Offline AI with KMP — focused 40-minute speaker notes

## 1. Cover

0:00–0:30

Today we will run a language model on a phone, connect it to a KMP app, and trace a device action from the user request to native execution. We will start with the model choices, then read the Android and Swift code, and finish with one observable offline action.

Transition → Begin with the familiar cloud request path.

## 2. The cloud round trip

0:30–1:15

Many AI features follow this familiar cloud-backed pattern. The app sends a request to a remote model and receives its answer over the network. Depending on the app, the input could be a question, photo or voice note. Trace the outward arrow, then the return arrow. This inference path depends on connectivity and a remote service, and the request leaves the phone for processing. Model execution adds its own time too.

Presenter cue: Keep this to the diagram and network dependency; explain the reasons for moving it on the next slide.

Source: https://developers.google.com/edge/litert-lm/overview

Transition → Why would we move that inference step onto the phone?

## 3. Why run models offline

1:15–2:15

Imagine opening a useful feature on a train or flight and still being able to use it. That is the availability reason for local inference after model setup. A second reason is a deliberate data boundary: this inference request need not be uploaded to a cloud model. Third, removing the network hop can help responsiveness. Fourth, this path avoids hosted-model request charges.

Those are reasons to evaluate local inference, not automatic wins. We pay in downloads, storage, runtime RAM, compute, battery and heat, and the chosen model must still pass the task. Local inference alone does not make the whole app private or compliant. Device generation can be slower, and fresh online facts may still require another architecture.

Presenter cue: Lead with the use case, then point to the four reasons. Do not promise instant inference or zero total cost.

Source: https://developers.google.com/edge/litert-lm/overview

Transition → Which model capabilities can an app choose?

## 4. Model kinds and capabilities

2:15–3:15

Choose the job first. A text instruction model can draft or extract. A multimodal model accepts other input types, such as images or audio, if the app implements that pipeline. A tool specialist focuses on selecting functions and arguments. Base models learn continuation; instruct models receive further training to follow requests. The examples shown illustrate those roles; they are not a list of verified demo artifacts.

If asked: Base and instruction tuning are another axis. A base model is trained to continue its inputs. An instruct model receives further training to respond to requests such as “extract these tasks.” Input type, tuning and specialization can overlap in one model. They are not mutually exclusive boxes or consequences of parameter count.

These are model-family examples, not a claim that each artifact was loaded or measured in our demo. Local AI also includes embedding, classification, vision and speech models; this talk focuses on generative models and app integration, not implementing every task family.

Sources: https://huggingface.co/google/gemma-3-1b-it ; https://gemma.ai/models/gemma-4/model-card/ ; https://gemma.ai/models/functiongemma/model-card/

Transition → What model families and sizes could we evaluate?

## 5. Mobile model families and sizes

3:15–4:15

M means million learned parameters; B means billion. We will unpack that term on the next slide. Gemma is one option, not the whole mobile-model landscape. Within Gemma 3, 270M and 1B are text-only; 4B adds image input, and the family also includes larger 12B and 27B variants. Do not assume every size fits a phone. Qwen3 has compact 0.6B and 1.7B text models. Llama 3.2 has 1B and 3B text variants. SmolLM2 spans 135M, 360M and 1.7B.

The practical point is that choosing LiteRT-LM does not force us to choose Gemma. Other supported model exports can use the same engine API. A swap can still change the expected chat template, tokenizer, input capabilities and tool format, so evaluate the complete export rather than promising a two-line universal switch. These examples compare learned parameter counts, not download sizes or speed. Pick a candidate for your task, then check the exact converted artifact, chat format, runtime support, license and phone resources. This talk does not claim every listed model ran in our app.

If asked: This is a deliberately small, non-exhaustive set rather than a claim about the latest releases. Gemma 3n E2B/E4B use effective-size naming, which is different from total stored parameters. FunctionGemma is specialized and needs its own protocol. The repository’s app-managed LiteRT-LM route is also distinct from system-provided models such as Gemini Nano or Apple Foundation Models.

Sources: https://gemma.ai/models/gemma-3/ ; https://huggingface.co/Qwen/Qwen3-0.6B ; https://huggingface.co/Qwen/Qwen3-1.7B ; https://huggingface.co/meta-llama/Llama-3.2-1B-Instruct ; https://huggingface.co/HuggingFaceTB/SmolLM2-1.7B-Instruct

Transition → What do the parts of those model names mean?

## 6. What 1B IT and INT4 mean

4:15–5:15

Read Gemma 3, 1B, and IT separately. Gemma 3 names the model family/generation. 1B means approximately one billion numerical parameters learned during training. IT means instruction-tuned. 270M means approximately 270 million parameters. A parameter count is not a download size, a speed benchmark, or a guarantee of quality.

A converted artifact may also carry a precision label. That describes how stored numerical values are represented. The same parameter count at different precisions can have different weight-storage budgets. Check the exact artifact and runtime rather than guessing from a family name.

If asked: A context-window number is different again: it counts how many tokens can be considered during generation, not parameters or bits. We will see tokens after quantization. Some model names report effective rather than total parameters, so treat the simple 1B example as an introduction, not a parser for every model name.

Sources: official Gemma 3 artifact card; https://huggingface.co/docs/transformers/quantization/concept_guide

Transition → How many bits store each numerical value?

## 7. Bits bytes and weight precision

5:15–6:00

For model storage, the useful consequence is the number of codes per weight. Four bits provide 16 possible codes. Eight bits make one byte, so two packed 4-bit weight codes can fit in a byte before scales, metadata and alignment. Those codes represent approximate levels; the original floating-point weights are not simply chopped into decimal digits. FP16, INT8 and INT4 name different numerical representations, not different parameter counts.

Presenter cue: Spend at most 45 seconds here. Establish the byte calculation, then demonstrate the approximation on the quantization slide.

Source: https://developers.google.com/edge/litert/models/post_training_quantization

Transition → How do we map a learned value to one of those smaller codes?

## 8. Quantization and weight storage

6:00–7:30

For a smaller toy example, use 2 bits, which give four codes. Map them to 0.00, 0.25, 0.50 and 0.75. A weight of 0.73 is approximated by 0.75, introducing +0.02 error. Quantization changes representation precision; it does not remove parameters. This is intuition, not the exact algorithm used by a particular Gemma artifact.

Now multiply. One billion weights times four bits is four billion bits. Divide by eight bits per byte to get half a billion bytes: 0.5 decimal GB of idealized INT4 weight payload. FP16 and INT8 give 2.0 and 1.0 GB respectively under the same simplified assumptions.

Real files add scales, metadata and other tensors, and may mix precisions. Runtime RAM also includes caches, buffers and activations. A 0.5 GB weight payload does not mean the app fits into 0.5 GB RAM. Lower precision can affect quality, and hardware/kernel support determines speed. Re-test the actual task on the actual artifact and device.

Sources: https://developers.google.com/edge/litert/quantization/quantization_spec ; https://huggingface.co/docs/transformers/quantization/concept_guide

Transition → How do those saved weights produce text?

## 9. How a prompt becomes streamed text

7:30–9:15

Saved weights were learned during training. Inference uses them with a prompt; a supplied fact changes the current context, not the saved weights. A tokenizer splits text into pieces and maps them to IDs. Tokens may be words, parts of words or punctuation; exact splits depend on the tokenizer. Prefill processes the supplied instructions, note and included history. At each decode step the model scores possible next tokens; the runtime selects one, appends it and repeats until a stop condition or output limit. Sampling can make successive runs differ.

Instructions, input, history and the growing response share a context budget. More context can mean more prefill time and runtime memory. The app chooses what to include and leaves response headroom. Decoded chunks can stream into shared UI state through Kotlin Flow; a chunk need not equal one token.


If asked: Current shared defaults reserve 768 estimated output tokens from a 4096 baseline, leaving 3328 estimated input tokens. History preparation subtracts system/current-message estimates. Counts are heuristic, not the actual artifact tokenizer. iOS native capacity is not explicitly the same Android setting.

Sources: PromptBuilder.kt in the current local working tree; https://huggingface.co/docs/transformers/llm_tutorial

Transition → Which runtime handles this generation on our phones?

## 10. LiteRT-LM and MediaPipe LLM Inference

9:15–10:30

LiteRT is the layer that executes converted model computation on the selected device backend. LiteRT-LM adds the language-model orchestration: model resources, tokenization, prefill and decode through LiteRT, conversation management and streamed text. Our KMP app supplies the prompts and decides what the generated response means for the product. Keeping those layers distinct explains why choosing a model file and choosing a runtime library are separate decisions.

You may also recognize MediaPipe’s LLM Inference API from older examples. That specific API is now maintenance-only; Google recommends migrating it to LiteRT-LM. This does not mean all MediaPipe tasks are retired. This repository already uses LiteRT-LM: Kotlin on Android and a Swift bridge on iOS. Gemma is the model family, LiteRT-LM is the runtime, and KMP shares the application behavior.

Sources: https://developers.google.com/edge/litert-lm/overview ; https://developers.google.com/edge/mediapipe/solutions/genai/llm_inference ; current platform adapters.

Transition → Which model file should we give that runtime?

## 11. Model file formats: .litertlm and .task

10:30–11:45

A model family and precision still do not tell us which file to download. A .litertlm bundle packages converted model components, weights, a tokenizer and metadata for LiteRT-LM. Start with a compatible pre-converted export for the runtime we chose.

Older MediaPipe LLM examples often use .task bundles. Some LiteRT-LM paths also recognize these, so do not treat the suffix as a complete support matrix. Renaming a file does not convert its contents.

For KMP, a compatible bundle can be reused on Android and iOS. The native runtime libraries and initialization remain platform-specific. Match the export to both runtime versions and chosen backends.

If asked: Both pinned native format detectors recognize .task, .litertlm and .tflite. Recognition alone does not establish operator, tokenizer, modality or memory compatibility. INT4 is a numerical representation, while .litertlm is a container format. This talk does not claim a particular bundle was physically verified on every platform. GGUF or Safetensors downloads are not direct substitutes for these adapters; obtain a supported conversion.

Sources: https://developers.google.com/edge/litert-lm/file_builder ; https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.14.0/runtime/util/file_format_util.cc ; https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.13.1/runtime/util/file_format_util.cc

Transition → The bundle is shared data. How do we share the application code while keeping the native engines?

## 12. KMP owns the product rules

11:45–13:00

KMP shares the behavior around the model: Compose UI, prompts, state, parsing, argument checks and routing. The GemmaInference contract is in commonMain. Both platforms provide a Kotlin actual adapter.

Android calls LiteRT-LM's Kotlin SDK. iOS connects its Kotlin adapter to Swift through notifications. Both adapters turn native runtime output into Kotlin Flow. Keep the focus here on ownership and boundaries; the startup-policy slide explains initialization after we see the shared API. Tool selection follows this demo's shared prompted-JSON route on both platforms.

The same clipboard tool can share its name, arguments and routing while Android uses ClipboardManager and iOS uses UIPasteboard. Lifecycle, permissions and hardware still need platform-specific implementation and tests.


Sources:
Local working tree: corresponding project source files.
Sources: current local working tree, reviewed for this revision.
composeApp/src/androidMain/kotlin/org/abma/offlinelai_kmp/inference/GemmaInference.android.kt
composeApp/src/iosMain/kotlin/org/abma/offlinelai_kmp/inference/GemmaInference.ios.kt
iosApp/iosApp/InferenceBridge.swift

Transition → Read the common Kotlin boundary first.

## 13. The shared inference API

13:00–14:15

The shared app sees a small Kotlin boundary: load a compatible local artifact, then collect decoded text chunks through Flow. The expect declaration defines the shared contract; each actual implementation connects to its native runtime.

On Android, callbacks from the Kotlin SDK are forwarded into the Flow. On iOS, the Swift bridge forwards native stream events to the Kotlin adapter, which emits into the same shared Flow. That lets the shared ViewModel update the answer without knowing which native API produced the text. This excerpt is abridged; lifecycle and error handling stay in the implementations.


If asked: The current main chat uses generateConversationResponse to pass system instructions, history and the user prompt as structured roles. The full API also includes updateConfig, generateResponseWithHistory, isModelLoaded, getLoadingProgress and close. Android applies sampler settings. This iOS bridge intentionally uses native sampler defaults. Both current load paths await native initialization, and both forward cancellation/close. These are source-level facts, not a completed physical-device benchmark.

Sources: current local working tree, GemmaInference.kt, GemmaInference.android.kt, GemmaInference.ios.kt and iosApp/iosApp/InferenceBridge.swift.

Transition → Now follow loadModel into each native engine.

## 14. Android and iOS engine initialization

14:15–15:45

The common loadModel method reaches these native initialization paths. Android constructs EngineConfig with the model path, selected backend and cache directory, creates Engine, and calls initialize. The helper is invoked with GPU first and CPU if GPU initialization fails. Swift constructs its EngineConfig with the same kind of model path and cache location, chooses GPU, creates Engine, and awaits initialize.

Both load paths wait for native initialization before reporting ready. Sharing a signature does not establish equal backend support, loading cost or latency. Provision the artifact first and measure initialization, first visible text and completion separately.

Presenter cue: Trace modelPath → config → Engine → initialize on both sides. The snippets are source-derived abridgements; Android token/image limits and error checks are omitted for readability. The full Android EngineConfig is near207–218, initialization immediately after it; Swift initialization is near181–190 in the reviewed working tree.

Sources: GemmaInference.android.kt ; iosApp/iosApp/InferenceBridge.swift.

Transition → The engine is loaded. Let’s send the first system and user prompt through our shared Kotlin API.

## 15. Send the first prompt

15:45–17:00

The engine is ready. Now use the shared Kotlin API to send a real request. The system instructions set the tutor role and a two-sentence response constraint. The user message asks what changes when the model is quantized from FP16 to INT4. We pass empty history because this is a first turn.

The adapters preserve these as system and user roles; we do not manually concatenate special model-token markers here. The selected model’s conversation format still matters. The system instruction guides generation; it does not guarantee that the response follows every constraint.

collect receives decoded text chunks, and answer accumulates them. This is the low-level chunk stream. Later, our shared use case wraps it in cumulative Streaming, Complete and Error results for the ViewModel. That later partialResponse must replace displayed text, rather than be appended again.

Presenter cue: Trace systemPrompt, history and userPrompt, then the highlighted question into collect. The model is already loaded, inference is the shared GemmaInference instance, and this code runs inside a coroutine. This is an illustrative request using the existing API, not a captured model output. UI-state updates and error handling are omitted.

Sources: composeApp/src/commonMain/kotlin/org/abma/offlinelai_kmp/inference/GemmaInference.kt ; Android and iOS GemmaInference actual implementations.

Transition → A prompt can ask the model to explain a concept. What if the question needs information the model does not have, such as today’s weather?

## 16. Weather function calling: request to tool result

17:00–18:30

Ask: what is the weather in Riyadh today? Model weights alone are not a live weather feed. Function calling lets the model request information from a function the app has described.

The tool contract names get_weather, describes current conditions and requires a location string. The model proposes Riyadh as the location; it has not fetched anything yet. The app validates the argument, calls a weather provider, and sends the result back. Only then can the model compose an answer from that fresh result.

The displayed 26°C and sunny are mock values for teaching, not today’s observation or a captured response. This app does not implement the weather tool.

Presenter cue: Trace contract → proposed call → provider lookup → returned result → model answer. Emphasize that the application performs the external call.

If asked: Google’s Gemini guide illustrates weather function calling. Provider APIs differ in exact schema and result messages; the slide’s function syntax is conceptual. A production lookup also needs location disambiguation, units, errors and an observation time. Do not silently turn provider failure into invented weather. A fresh data source is separate from model training.

Source: https://ai.google.dev/gemini-api/docs/function-calling

Diagram credit: Google Gemini API documentation, https://ai.google.dev/gemini-api/docs/function-calling . The supplied original diagram illustrates the general function-call flow, not a weather-specific API. Image licensed under the documentation’s stated terms; attribution retained here and in the slide caption.

Transition → Keep function calling, move inference locally, and change from remote weather to a device flashlight action.
Diagram note: The main sequence is a presentation-width adaptation of the tool branch in Google’s general diagram. Google’s unmodified original is preserved on reference 31. The optional direct-answer/extra-function branches are in that original; this slide traces the weather lookup branch only.


## 17. A complete function-calling request

18:30–20:00

Move the inference step onto the phone. The contract, proposed call and app-owned execution remain the same idea, but today’s live weather would still require a network-backed tool. We now switch to a local device action: turn off the flashlight.

Read the complete small request. The system instructions describe toggle_torch and its required Boolean enable argument, then define the response format. History is empty. The user asks OFF, so the illustrative expected proposal contains enable=false. No native code has run yet.

Presenter cue: Point to SYSTEM, then USER, then the expected proposal. Do not read every instruction aloud. Tool instructions accompany the app’s default-persona chat generations; they are not learned permanently by the model.

If asked: This is a self-contained teaching request, not a raw app capture. The companion complete-tool-request.md contains the source-derived full nine-tool default prompt. The actual builder combines persona, optional pinned context and tool descriptions, then sends system text, prepared history and user text separately. Specialized/custom personas and standalone extraction workflows use different prompts. A cached weather report must show its observation time and may be stale.

Sources: ToolPrompt.kt; ChatViewModel.kt currentCombinedSystemPrompt; PromptBuilder.kt; GenerateResponseUseCase.kt; complete-tool-request.md.

Transition → Where does this tool contract come from in our application code?

## 18. Register the available tool handlers

20:00–21:45

The screenshot is the real createDefaultToolRegistry function. It constructs the shared registry from nine handler instances. Point to ToggleTorchTool, which handles our running flashlight example, and CopyToClipboardTool, which we will use in the live demonstration. The model did not discover these functions; the app selected the available handlers.

Each handler owns a ToolSpec with its name, description and argument contract. The shared prompt builder reads the registry’s specs to describe the tools. After generation, ToolRegistry matches a proposed name to one of these handlers. An unknown name produces an error result. The selected handler validates its inputs before delegating to native code.

This is the app’s shared registry, not LiteRT-LM ToolSet registration. The screenshot shows registration only; we will trace dispatch after the generation examples.

Presenter cue: Point to ToggleTorchTool and CopyToClipboardTool in the actual source capture. Do not read all nine names.

Sources: Tooling.kt ToolRegistry, createDefaultToolRegistry and ToolHandler.spec; ToolPrompt.kt.

Transition → How do both native runtimes receive the same system instructions and user request?

## 19. Android and iOS: the same tool prompt

21:45–23:45

Now the variables have a concrete meaning: systemPrompt contains the tool instructions we just read, and userPrompt asks to turn the flashlight off. The loaded engine is already available. These are shortened new-conversation excerpts, with empty history.

On Android, ConversationConfig receives systemInstruction. The conversation accepts the user message separately and produces text through a callback. On iOS, the Swift config receives a system Message; sendMessageStream emits chunks. Our postToken helper forwards those chunks through notifications into Kotlin Flow.

Both return generated text to shared Kotlin. The shared parser extracts a proposed call, and its handler validates it before a native action. Neither excerpt registers SDK tools or automatically executes a flashlight function.

If asked: The snippets use shorter local names and assume a nonblank systemPrompt. The actual Android callback extracts Content.Text, calls trySend and handles completion/errors within callbackFlow. Swift postToken is a project helper, not a LiteRT-LM API. Real history, sampler configuration, cancellation and cleanup are omitted here; the earlier startup slide and source code cover those concerns. Swift also exposes native tool APIs. Both current platforms choose the shared prompting route.

Sources: GemmaInference.android.kt generateConversationResponse; iosApp/iosApp/InferenceBridge.swift; GemmaInference.ios.kt callbackFlow bridge.

Transition → How does that native text stream update shared UI state, and when is a tool proposal ready to dispatch?

## 20. Streaming into shared UI state

23:45–25:15

The native APIs stream text, but the UI needs more than an ever-growing string. Our shared use case exposes three sealed results: Streaming, Complete and Error. This is our KMP application API, not the Flutter package’s response types.

Follow the highlighted branch. partialResponse already contains the full text received so far, so updateStreamingMessage replaces the displayed content. Adding it with += would repeat earlier prefixes. When the flow emits Complete, the ViewModel either handles the parsed tool proposal or finishes an ordinary answer. Errors have a separate branch. The when expression is exhaustive because all result types are covered.

Presenter cue: Start with the highlighted two lines, then point to completion and error. The surrounding coroutine, history trimming, tool allowlist and live metrics are omitted. Nested result classes are imported for short names, and combinedSystemPrompt is shortened to systemPrompt. The real use case accumulates native chunks before emitting cumulative Streaming results. It extracts a tool proposal only after generation ends.

Sources: composeApp/src/commonMain/kotlin/org/abma/offlinelai_kmp/ui/viewmodel/ChatViewModel.kt:628–654 ; composeApp/src/commonMain/kotlin/org/abma/offlinelai_kmp/domain/usecase/GenerateResponseUseCase.kt:20–39,85–120.

Transition → The preceding code uses our prompted proposal format. What can the Android SDK handle for us instead?

## 21. LiteRT-LM SDK function calling

25:15–26:30

SDK means Software Development Kit: the runtime library and APIs we integrate into our application. Google AI Edge LiteRT-LM’s Kotlin SDK can derive a tool contract from a ToolSet’s annotated Kotlin functions. @Tool supplies a description; @ToolParam supplies the parameter description; Kotlin’s function name and parameter type supply their corresponding contract information. The example deliberately calls the function toggle_torch to match that exposed name.

Presenter cue: The left snippet is an illustrative integration option. Its injected run callback represents app-owned native work and status reporting; torchTools must be instantiated with that implementation. This is not code already wired into our demo. Engine creation, callback implementation and initialization are omitted. Calls that block belong off the UI thread.

The right snippet registers that instance with tools=listOf(tool(torchTools)). automaticToolCalling=false selects manual handling, so the application can inspect reply.toolCalls and apply its checks before dispatch. In automatic mode, the SDK invokes registered functions and returns their results to the model. Manual handling must submit results if another model turn should continue. Neither route establishes intent or permissions.

If asked — app wiring and integration details: Our current Android AND iOS chat implementations instead use the preceding prompted-proposal route. iOS prompting is an app choice; Swift also exposes native tool APIs. SDK tool calls use Map<String,Any?> arguments, while our shared handlers use JsonObject, so normalization is required to reuse those checks. Use a compatible tool-capable artifact/template and pin versions.

Sources: https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.14.0/kotlin/java/com/google/ai/edge/litertlm/Tool.kt ; https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.14.0/kotlin/java/com/google/ai/edge/litertlm/Config.kt ; pinned v0.14.0 Kotlin getting_started.md; current local app code.

Transition → Return to the prompted route this app implements and follow a proposal into native execution.

## 22. A complete tool-call lifecycle

26:30–28:00

Trace the same flashlight-OFF request, rather than another abstract API. First define the name and Boolean argument in the shared ToolSpec. The prompt builder includes those instructions so the model knows the available tool and parameter. The model returns a proposal, not a completed action.

In this app, extractToolCall normalizes generated text into a shared ToolCall with a JsonObject. The registered torch handler requires exactly the enable key and a real JSON Boolean. Invalid requests stop before native execution. A valid request reaches the platform adapter, which can still fail because hardware or permissions are unavailable. Show the resulting status in the UI.

Our app displays the native result and ends this operation. Returning that result to the model for another turn would require an additional conversation loop.

Presenter cue: Read the literal proposal on the slide. Follow its tool name through extractToolCall into ToggleTorchTool, then follow the enable value into the native call. Contrast a native success with a missing-hardware error; neither outcome is known at proposal time.

If asked — continuing conversations: ChatViewModel.handleToolCall currently does not feed its result into another model turn. A continuing loop must submit the result, handle subsequent responses and enforce loop limits. Android SDK automatic mode offers orchestration; manual mode leaves it to the app. SDK arguments need adaptation before reuse of this app’s JsonObject checks.

Sources: current local ToolPrompt.kt, Tooling.kt, GenerateResponseUseCase.kt and ChatViewModel.kt; pinned LiteRT-LM v0.14.0 Kotlin getting_started.md.

Transition → What can still go wrong after parsing and type checks?

## 23. What validation does not prove

28:00–29:00

A correctly typed proposal can still contradict the user. If the user says “Do not change the flashlight” and the model proposes enable=true, the Boolean shape can pass while the action is wrong. Each handler needs its own validation, and consequential actions may need review before execution. The current chat dispatches automatically after its implemented checks; confirmation is a product recommendation.

Native execution can also fail because the device has no available torch or access is restricted. Report the API result or error; do not treat a generated success sentence as evidence of a device effect.

Sources: Tooling.kt ; ChatViewModel.kt ; current native action adapters.

Transition → Inspect a real proposed call and its observable device effect.

## 24. Live clipboard proof

29:00–35:00

Before switching examples: the flashlight call showed the dispatch path. For the live run, use another registered device tool whose result everyone can inspect: copy_to_clipboard.

Ask someone for a two-digit number to make this input specific to the live run. Use it instead of 47 in the displayed request.

The phone has airplane mode on and Wi-Fi off. This demonstrated path uses local inference. Watch three things: the actual copy_to_clipboard proposal, the app's device result, then the phrase appearing when pasted into an empty local field on the phone. A generated “Copied!” sentence is not proof of a clipboard effect.

If the model proposes the wrong action, show the failure honestly. The important pattern is an inspectable proposal followed by an observable native result.

Presenter cues — preparation, not spoken script:
Name the actual rehearsed artifact filename/precision, backend and phone before starting. These must come from the real setup; a model-family example or estimated size is not a measured setup.
Rehearse the exact build, artifact and phone first. Provision and load the model before disconnecting. Mirror by USB without tethering. With scrcpy use --no-clipboard-autosync. Type and long-press Paste on the phone; a laptop paste shortcut can inject its clipboard.
Expected shape, not captured output: <<{"tool":"copy_to_clipboard","arguments":{"text":"Essex lantern <number>"}}>>.
Planned demonstration: four minutes. The remainder of this slot is reserved for audience interaction, device delays and recovery; do not fill it with another unplanned live feature. At 34:15 stop retrying and show the resources/Q&A page by 35:00. If the run finishes early, begin questions rather than adding filler.

Transition → Keep the repository QR visible and open questions.

## 25. Code, setup and questions

35:00–40:00 | Q&A

The repository contains the shared prompt, parser, handlers and native adapters. Follow ToolPrompt.kt for tool instructions, GenerateResponseUseCase.kt for extracting proposals, and Tooling.kt for registry and handler behavior. The live run used the actual artifact, backend and phone named during the demonstration. These are the sources to inspect if you want to reproduce that path. Let’s take questions.

Presenter cue: Keep the project QR visible. Open Q&A no later than 35:00; finish at 40:00. Optional references begin at 26.

Source: https://github.com/SEAbdulbasit/OfflineAI-KMP

## 26. Reference: A useful local feature

OPTIONAL REFERENCE — outside the timed forward talk

Here is the useful feature: turn a short meeting note into a draft the user can inspect. The screenshot is our real iOS-simulator input screen before inference. It is UI evidence only; it is not a captured successful model response. The separate draft is illustrative expected output.

Sara will update the slides, but no deadline was agreed. We want that task, owner Sara, and deadline null. We do not want to infer a deadline from another person's sentence or turn a casual discussion into a commitment. That small, checkable transformation is our running example.




Sources: workshop/visuals/ios-notes-source.jpg; DemoSamples.kt in the current local project.

## 27. Reference: Walkthrough: note to draft

OPTIONAL REFERENCE — outside the timed forward talk

Read the two fictional commitments. Ask whose task has Friday as a deadline. Sara owns the slide update; no deadline was agreed for that task. Omar owns the Android recording with Friday as its deadline.

The right side is one illustrative expected item, not a captured model result and not the complete response. The app expects an items array. Keep task and evidence as exact source substrings; keep absent deadline null. The original sample includes further commitments and discussion, omitted here to focus the example.

The parser checks that evidence occurs in the source, that task occurs in the evidence, and that non-null owner and deadline occur there too. This can reject unsupported fields, but an overly broad evidence span can still contain the wrong person’s deadline. Exact matches do not establish semantic attribution or whether a statement is a commitment. Keep the review step and evaluate those cases explicitly.


Sources: DemoSamples.kt notesPrompt and parseNotes, DemoScreen.kt Action Items to Review.

## 28. Reference: Walkthrough: local reference

OPTIONAL REFERENCE — outside the timed forward talk

This is a second narrow feature: answer only from a supplied local reference. The fictional workshop section has ID venue and says the workshop is in Room Cedar on level 2. The prompt includes the sections directly. There is no embedding pipeline, vector database, PDF ingestion or implemented retrieval system in this demo.

For the location question, an expected response names the known section and copies an exact quote. For a Wi-Fi password question, no supplied section contains that information, so the expected response is null/null. These examples are teaching outputs, not measured model results.

If asked — parser details: parseFaq checks that the sourceId exists and the quote is a nonblank exact substring of that section. It also handles null/null as no answer. Those checks do not prove the quote answers the question: a model can select a real but irrelevant sentence. Evaluate supported, unsupported and irrelevant-quote cases separately.




Sources: DemoSamples.kt sections, faqPrompt and parseFaq; DemoController.kt.

## 29. Reference: A reusable evaluation method

OPTIONAL REFERENCE — outside the timed forward talk

Keep the same flashlight feature while testing three different things. First a requested action: “Turn off the flashlight” should produce enable=false, and the native effect must be inspected. Second a no-action case: “Do not change the flashlight” should not dispatch a tool at all. Third invalid arguments: a quoted Boolean or extra key must be rejected before native execution.

These are expected test cases, not a claimed model pass rate. Check selection and intent, argument validation, and the actual device outcome separately. A valid JSON shape or generated success message proves only part of the chain.

Record the exact artifact, runtime, backend and phone. Measure cold load, first visible chunk and total completion separately. A Boolean argument passing validation does not establish hardware success.

If asked: The note-extraction workflow needs its own cases for commitments, missing fields and unrelated deadlines. That is a different task’s test set; do not switch examples while explaining the tool-call tests here.

Sources: ToggleTorchTool in Tooling.kt; ToggleTorchToolTest.kt; current metrics and platform tool adapters.

## 30. Reference: Failure and recovery

OPTIONAL REFERENCE — outside the timed forward talk

Walk the audience through the actual recovery paths. Model readiness is checked before running; the model-required UI guides the user to Settings. The workflow preserves its input. A new run clears the previous result, and failed validation shows an error rather than a new validated result. Raw generated text remains available for inspection. The user can shorten or edit the input and retry.

A rejected torch argument is a separate boundary: it produces an invalid-request tool result before native execution. Do not generalize this strict guard to every tool; clipboard parsing is currently simpler.

Cancellation produces “Generation stopped. You can edit the input and try again.” The controller releases the generation slot and restores its non-running state. This slide describes implemented branches, not a claimed set of observed model failures. A complete manual alternative is a recommended product extension.


Sources: current DemoController.kt, DemoScreen.kt and Tooling.kt.

## 31. Reference: Google’s original function-calling diagram

BACKUP — Google’s original function-calling diagram

This is Google’s unmodified general diagram from its Gemini API guide, not a weather-specific diagram. The main weather slide uses a wide, weather-labelled adaptation so the arrows and text are readable on a presentation screen. In the original, Gemini may answer directly, propose a function, or continue with another function after receiving a result. The application executes functions.

Source: https://ai.google.dev/gemini-api/docs/function-calling
Image: https://www.gstatic.com/cloud-cms/6fae8435a773b720d5db0c8746efb318.png


## 32. Reference: app-owned execution

OPTIONAL REFERENCE — outside the timed forward talk

BACKUP — App-owned execution responsibilities

Only step two belongs to the model. First, the app describes its tools in the prompt; the model does not discover Kotlin functions by itself. Second, the model proposes a name and arguments. Third, the registry checks the name and the registered handler checks its inputs. Our torch handler requires a JSON Boolean and exactly the allowed key. Validation strength is handler-specific; the clipboard handler currently uses simpler extraction.

A schema in a prompt does not enforce those rules or establish intent. Fourth, the native API runs subject to permissions and hardware. Fifth, the app reports the actual API result or error.

This app displays the result directly. Feeding it into another model turn would require loop limits, state and recovery handling.


Sources:
https://gemma.ai/models/functiongemma/formatting/
Local working tree: see the corresponding project source file.

## 33. Reference: the Swift bridge

OPTIONAL REFERENCE — outside the timed forward talk

BACKUP — iOS Swift Bridge (iosApp/iosApp/InferenceBridge.swift)

This is the Swift side behind the same `Flow<String>` contract. `InferenceBridge` creates a LiteRT-LM conversation, loops over `conversation.sendMessageStream(message)`, and calls our helper `postToken(chunk.toString)`.

`postToken` is a project helper—not a LiteRT-LM API—that posts `GemmaTokenResponse` via `NotificationCenter`, which `GemmaInference.ios.kt` observes inside `callbackFlow`.

Current lifecycle: Kotlin loadModel resolves the artifact and waits for GemmaLoadDone or GemmaLoadError from Swift native initialization. Unfinished collection cancellation posts GemmaCancelRequest, and close posts GemmaCloseRequest. Swift cancels the active conversation/task and clears its engine reference on close. Kotlin includes sampling values in the generation payload, but Swift intentionally leaves samplerConfig nil and uses the model/runtime defaults. The bridge now creates a structured conversation with system/history messages. Native tool APIs also exist in the local Swift wrapper, but this app does not register them: it prompts for custom JSON and routes the parsed proposal through shared handlers. Describe these implemented paths separately from physical-device verification.

Sources:
Local working tree: corresponding project source file.
Local working tree: corresponding project source file.
https://developers.google.com/edge/litert-lm/swift

## 34. Reference: a structured draft

OPTIONAL REFERENCE — outside the timed forward talk

BACKUP — Structured Text Draft (DemoSamples.kt)

Here's the expected JSON shape behind our note-to-action-items demo in `DemoSamples.kt`. The fictional rehearsal note has Sara (no deadline), Omar (Friday), Lina (Thursday), plus a dark-theme discussion with no commitment.

`notesPrompt` asks for JSON, null for missing fields, and an exact evidence sentence copied from the note. The parser uses ignoreUnknownKeys=false to reject unknown fields; that is parser configuration, not prompt enforcement. `parseNotes` verifies the JSON shape and checks that each supplied task, owner and deadline matches text inside its evidence, and that `evidence` appears verbatim in the source note.

Sources:
Local working tree: corresponding project source file.

## 35. Reference: source checks and meaning

OPTIONAL REFERENCE — outside the timed forward talk

BACKUP — Source Checks vs. Meaning (DemoSamples.kt & DemoSamplesTest.kt)

Deterministic substring checks catch a lot, but not everything.
- Sara + `null` passes and is a real commitment.
- Sara + `Friday` is rejected *if* the evidence is Sara's own sentence (because "Friday" isn't in it)—though a multi-sentence evidence span could still smuggle in Omar's Friday.
- The dark-theme sentence passes substring checks because the words are really in the note, even though nobody committed to building it.

That's why `DemoSamplesTest.kt` explicitly documents this boundary: deterministic checks verify grounding in text, not semantic intent.

Sources:
Local working tree: corresponding project source file.
Local working tree: corresponding project source file.

## 36. Reference: compatible deployment

OPTIONAL REFERENCE — outside the timed forward talk

BACKUP — Model & Runtime Compatibility

Four things have to line up: exact converted artifact, pinned runtime version, chat template/input types, and device RAM + context budget + license.

In this repo: Kotlin 2.4.0, Compose Multiplatform 1.11.1, Android LiteRT-LM 0.14.0 (`libs.versions.toml`), and iOS `CLiteRTLM.xcframework` 0.13.1 (`LiteRTWrapper/Package.swift`).

If asked about system models: this app uses an app-managed compatible model artifact through LiteRT-LM. The on-device configurations of Gemini Nano / ML Kit GenAI and Apple's Foundation Models are separate system-model routes; availability, APIs and guarantees differ from this app-managed runtime.

Sources:
https://developers.google.com/edge/litert-lm/overview
https://gemma.ai/models/functiongemma/formatting/
Local working tree: corresponding project source file.
Local working tree: corresponding project source file.

## 37. Reference: Model families

OPTIONAL REFERENCE — outside the timed forward talk

BACKUP — Model-family examples

Gemma 3 1B IT is a compact text-only instruction-tuned example. Gemma 4 E2B also accepts image and audio inputs, so using those capabilities needs a matching artifact, runtime and app input pipeline.

FunctionGemma 270M specializes in tool use and has its own call format. It is not interchangeable with this project's prompted JSON envelope; supporting its protocol needs an appropriate integration.

These are selection examples, not measured performance results from this app. Start with a compatible artifact that passes task checks on the actual target phone. File size alone does not establish runtime RAM fit.

Use only if asked about model families; return to Q&A.



Sources:
https://gemma.ai/models/gemma-3/
https://gemma.ai/models/gemma-4/model-card/
https://gemma.ai/models/functiongemma/model-card/
https://gemma.ai/models/functiongemma/formatting/

## 38. Reference: a local source or null

OPTIONAL REFERENCE — outside the timed forward talk

BACKUP — Local Reference Answers (DemoSamples.kt)

Our second bundled demo is a tiny local FAQ with four short fictional sections (`venue`, `setup`, `offline`, `help`) injected straight into the prompt—no vector database, no embeddings, and no PDF parser.

For "Where is the workshop?", the expected JSON quotes the exact sentence from `[venue]`. For "What is the Wi-Fi password?" (which isn't in the reference), it must return `null` for both `sourceId` and `quote`. `parseFaq` checks that `sourceId` exists and the quote appears verbatim in that section—great for small, curated offline references.

A quote existing in the source does not prove it answers the question. Check relevance separately; an unsupported question should produce the null response rather than an invented answer.

Sources:
Local working tree: corresponding project source file.

## 39. Reference: choose a bounded task

OPTIONAL REFERENCE — outside the timed forward talk

BACKUP — Choosing a bounded task

Start with narrow transformations: extracting fields, classifying short text, or drafting a concise summary. Our note-to-task draft is useful because its fields can be checked against the source. Small local models are not a substitute for fresh external facts or dependable unrestricted multi-step reasoning.

The tradeoff is a product decision per feature: evaluate task quality, target-device resources and required information. A missing model or a failed generation should leave a usable manual workflow. That is a UX recommendation; the current screenshot shows the app's model-required state. Local and remote features can coexist.


Sources: the task checks in DemoSamples.kt and Tooling.kt; model cards linked in the model-selection appendix.

## 40. Reference: Native startup policies

OPTIONAL REFERENCE — outside the timed forward talk

The common method loads a model on both platforms, but startup policy differs. Android tries GPU, then CPU if initialization fails, and attempts warm-up. This iOS bridge initializes on GPU and preserves native sampler defaults.

Both current implementations await initialization and forward cancellation and close. Equal method signatures still do not establish equal resource cost or behavior.

Define the same phases before measuring: artifact provisioned, engine initialized, any warm-up, first visible chunk, then completion. Measure each device rather than comparing unlike phases.


If asked: This is a lesson drawn from the current implementation, not a claim that we observed a particular device failure or speedup. The iOS bridge shown has no Android-style CPU-fallback sequence. Swift samplerConfig is intentionally nil so the model/runtime defaults are used. Both adapters now forward cancel/close; older notes saying otherwise are obsolete.

Sources: current local working tree, GemmaInference.android.kt, GemmaInference.ios.kt and iosApp/iosApp/InferenceBridge.swift.

## 41. Reference: image requests on Android and iOS

OPTIONAL REFERENCE — outside the timed forward talk

These are SDK integration illustrations, not the current app’s image pipeline. The current native initialization and shared chat API are text-only. Adding an image argument alone cannot enable a text-only model or engine.

Use a compatible multimodal .litertlm export. Configure visionBackend with the supported backend before initializing the engine: Backend.GPU() in the pinned Kotlin API, .gpu in Swift. Both snippets assume that work is already complete and imagePath is a readable absolute local path. Resolve or copy picker URIs into an accessible file first. Kotlin also supports Content.ImageBytes(ByteArray); Swift supports .imageData(Data).

Android sends Contents containing image and text and collects Flow<Message>. The use block keeps the conversation open until collection completes. Swift sends a Message containing image and text and iterates an AsyncThrowingStream<Message,Error>. Release the Swift conversation when finished; its deinitializer deletes the native handle. Cancel active work and handle errors in production code; these details are omitted here. Neither SDK uses the Flutter-specific TextResponse/ThinkingResponse classes in the reference image. Separate reasoning or tool events require the actual model/API protocol; do not present arbitrary text as a typed reasoning channel.

Sources: https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.14.0/docs/api/kotlin/getting_started.md ; https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.14.0/kotlin/java/com/google/ai/edge/litertlm/Message.kt ; vendored iosApp/LiteRTWrapper/Sources/LiteRTLM/Message.swift, Conversation.swift and Config.swift.

Presenter cue: Open only if asked about extending beyond text. Highlight the image content line on each platform. Return to Q&A.
