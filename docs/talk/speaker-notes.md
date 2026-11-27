# Offline AI with KMP — 40-minute speaker notes

## 1. Cover

0:00–0:30

Today we’ll move an AI feature from the cloud onto a phone. We’ll turn a short note into a reviewable draft, understand the model choices behind it, and use KMP to share the rules while native runtimes do the work. We’ll finish by checking a real offline device action.

Presenter cue: Keep this promise under thirty seconds.

Transition → Begin with the familiar cloud-model round trip.


## 2. The cloud round trip

0:30–1:15

Many AI features follow this familiar cloud-backed pattern. The app sends a request to a remote model and receives its answer over the network. Depending on the app, the input could be a question, photo or voice note. Trace the outward arrow, then the return arrow. This inference path depends on connectivity and a remote service, and the request leaves the phone for processing. Model execution adds its own time too.

Presenter cue: Keep this to the diagram and network dependency; explain the reasons for moving it on the next slide.

Source: https://developers.google.com/edge/litert-lm/overview

Transition → What would make us move this inference step off the cloud?


## 3. Why run models offline

1:15–2:15

Imagine opening a useful feature on a train or flight and still being able to use it. That is the availability reason for local inference after model setup. A second reason is a deliberate data boundary: this inference request need not be uploaded to a cloud model. Third, removing the network hop can help responsiveness. Fourth, this path avoids hosted-model request charges.

Those are reasons to evaluate local inference, not automatic wins. We pay in downloads, storage, runtime RAM, compute, battery and heat, and the chosen model must still pass the task. Local inference alone does not make the whole app private or compliant. Device generation can be slower, and fresh online facts may still require another architecture.

Presenter cue: Lead with the use case, then point to the four reasons. Do not promise instant inference or zero total cost.

Source: https://developers.google.com/edge/litert-lm/overview

Transition → How does the local path change the architecture?


## 4. Inference on the phone

2:15–3:00

Now keep the user’s goal and move inference onto the phone. The model file holds learned numerical values. The runtime is the software that loads that file and runs generation. Follow the request into the local runtime and the reply back to the app. Once the compatible model and tokenizer are provisioned, this inference path can work without a cloud-model call. We will unpack those numerical values and text pieces shortly.

Presenter cue: Trace the local boundary. Keep resource and privacy tradeoffs on the preceding slide rather than repeating them here. Provisioning or updating the model may still need a connection.

Source: https://developers.google.com/edge/litert-lm/overview

Transition → Give those benefits a concrete job: turn a short note into a useful draft.


## 5. A useful local feature

3:00–3:45

Here is the useful feature: turn a short meeting note into a draft the user can inspect. The screenshot is our real iOS-simulator input screen before inference. It is UI evidence only; it is not a captured successful model response. The separate draft is illustrative expected output.

Sara will update the slides, but no deadline was agreed. We want that task, owner Sara, and deadline null. We do not want to infer a deadline from another person's sentence or turn a casual discussion into a commitment. That small, checkable transformation is our running example.

Presenter cue: Keep this feature preview under one minute. Do not read the entire screenshot or imply its missing-model state is a successful inference run. If a real recorded result becomes available, label its exact setup separately.


Sources: workshop/visuals/ios-notes-source.jpg; DemoSamples.kt in the current local project.

Transition → Which kinds of local model can do that job?


## 6. Model kinds and capabilities

3:45–4:30

Choose the job first. A text instruction model can draft or extract. A multimodal model accepts other input types, such as images or audio, if the app implements that pipeline. A tool specialist focuses on selecting functions and arguments. Base models learn continuation; instruct models receive further training to follow requests. The examples shown illustrate those roles; they are not a list of verified demo artifacts.

If asked: Base and instruction tuning are another axis. A base model is trained to continue its inputs. An instruct model receives further training to respond to requests such as “extract these tasks.” Input type, tuning and specialization can overlap in one model. They are not mutually exclusive boxes or consequences of parameter count.

These are model-family examples, not a claim that each artifact was loaded or measured in our demo. Local AI also includes embedding, classification, vision and speech models; this talk focuses on generative models and app integration, not implementing every task family.

Sources: https://huggingface.co/google/gemma-3-1b-it ; https://gemma.ai/models/gemma-4/model-card/ ; https://gemma.ai/models/functiongemma/model-card/

Transition → What actual model families and sizes could we evaluate?


## 7. Mobile model families and sizes

4:30–5:30

M means million learned parameters; B means billion. We will unpack that term on the next slide. Gemma is one option, not the whole mobile-model landscape. Within Gemma 3, 270M and 1B are text-only; 4B adds image input, and the family also includes larger 12B and 27B variants. Do not assume every size fits a phone. Qwen3 has compact 0.6B and 1.7B text models. Llama 3.2 has 1B and 3B text variants. SmolLM2 spans 135M, 360M and 1.7B.

These examples compare learned parameter counts, not download sizes or speed. Pick a candidate for your task, then check the exact converted artifact, chat format, runtime support, license and phone resources. This talk does not claim every listed model ran in our app.

If asked: This is a deliberately small, non-exhaustive set rather than a claim about the latest releases. Gemma 3n E2B/E4B use effective-size naming, which is different from total stored parameters. FunctionGemma is specialized and needs its own protocol. The repository’s app-managed LiteRT-LM route is also distinct from system-provided models such as Gemini Nano or Apple Foundation Models.

Sources: https://gemma.ai/models/gemma-3/ ; https://huggingface.co/Qwen/Qwen3-0.6B ; https://huggingface.co/Qwen/Qwen3-1.7B ; https://huggingface.co/meta-llama/Llama-3.2-1B-Instruct ; https://huggingface.co/HuggingFaceTB/SmolLM2-1.7B-Instruct

Transition → Before choosing by size, what do the parts of a model label actually mean?


## 8. Read a model label

5:30–6:30

Read Gemma 3, 1B, and IT separately. Gemma 3 names the model family/generation. 1B means approximately one billion numerical parameters learned during training. IT means instruction-tuned. 270M means approximately 270 million parameters. A parameter count is not a download size, a speed benchmark, or a guarantee of quality.

A converted artifact may also carry a precision label. That describes how stored numerical values are represented. The same parameter count at different precisions can have different weight-storage budgets. Check the exact artifact and runtime rather than guessing from a family name.

If asked: A context-window number is different again: it counts how many tokens can be considered during generation, not parameters or bits. We will see tokens after quantization. Some model names report effective rather than total parameters, so treat the simple 1B example as an introduction, not a parser for every model name.

Sources: official Gemma 3 artifact card; https://huggingface.co/docs/transformers/quantization/concept_guide

Transition → What does that precision label mean in bits and bytes?


## 9. Bits bytes and weight precision

6:30–7:30

A bit has two possible values: zero or one. Two bits produce four patterns: 00, 01, 10 and 11. Four bits provide sixteen codes; eight bits provide 256. Eight bits make one byte. For a simple quantizer, these codes map onto a limited set of numerical levels.

FP16 means 16-bit floating point. INT8 and INT4 mean 8-bit and 4-bit integer representations. A smaller representation needs fewer bytes per stored weight. Quantization uses those codes to approximate numerical values.

This is separate from parameter count, context length and a phone’s 64-bit architecture.

If asked: Real formats combine integer codes with scales and sometimes zero-points. Not every tensor uses the same precision, and runtime computation can use a different precision from stored weights.

Sources: https://huggingface.co/docs/transformers/quantization/concept_guide ; https://developers.google.com/edge/litert/quantization/quantization_spec

Transition → How do those limited binary codes approximate learned numerical weights?


## 10. Quantization and weight storage

7:30–9:00

Use the four codes we just introduced. In this toy 2-bit quantizer, they map to 0.00, 0.25, 0.50 and 0.75. A weight of 0.73 is approximated by 0.75, introducing +0.02 error. Quantization changes representation precision; it does not remove parameters. This is intuition, not the exact algorithm used by a particular Gemma artifact.

Now multiply. One billion weights times four bits is four billion bits. Divide by eight bits per byte to get half a billion bytes: 0.5 decimal GB of idealized INT4 weight payload. FP16 and INT8 give 2.0 and 1.0 GB respectively under the same simplified assumptions.

Real files add scales, metadata and other tensors, and may mix precisions. Runtime RAM also includes caches, buffers and activations. A 0.5 GB weight payload does not mean the app fits into 0.5 GB RAM. Lower precision can affect quality, and hardware/kernel support determines speed. Re-test the actual task on the actual artifact and device.

Sources: https://developers.google.com/edge/litert/quantization/quantization_spec ; https://huggingface.co/docs/transformers/quantization/concept_guide

Transition → The weights are stored. How does the runtime turn a prompt into generated text?


## 11. Generation

9:00–10:15

Saved weights were learned during training. Inference uses them with a prompt; a supplied fact changes the current context, not the saved weights. A tokenizer splits text into pieces and maps them to IDs. Tokens may be words, parts of words or punctuation; exact splits depend on the tokenizer. Prefill processes the supplied instructions, note and included history. At each decode step the model scores possible next tokens; the runtime selects one, appends it and repeats until a stop condition or output limit. Sampling can make successive runs differ.

Instructions, input, history and the growing response share a context budget. More context can mean more prefill time and runtime memory. The app chooses what to include and leaves response headroom. Decoded chunks can stream into shared UI state through Kotlin Flow; a chunk need not equal one token.


If asked: Current shared defaults reserve 768 estimated output tokens from a 4096 baseline, leaving 3328 estimated input tokens. History preparation subtracts system/current-message estimates. Counts are heuristic, not the actual artifact tokenizer. iOS native capacity is not explicitly the same Android setting.

Sources: PromptBuilder.kt in the current local working tree; https://huggingface.co/docs/transformers/llm_tutorial

Transition → Which runtime supplies those generation steps in this project?


## 12. What LiteRT-LM is

10:15–11:00

LiteRT-LM is the Google AI Edge runtime and API layer we use for local language-model inference. It loads a compatible model, runs generation and streams output back to the app. Point across the diagram: model file, runtime, app.

Keep three roles separate: Gemma is a model family, LiteRT-LM is the runtime, and KMP shares our app behavior. Android connects through the Kotlin SDK; iOS connects through Swift. Now those adapters in the next diagram have a concrete purpose.

If asked: LiteRT-LM builds on LiteRT for language-model inference. Supported model packages and CPU/GPU backends depend on the pinned runtime/platform. The current app’s Android GPU-to-CPU startup policy and iOS GPU path are described later. Do not assume every family in our model table has a compatible artifact for this exact app build.

Sources: https://developers.google.com/edge/litert-lm/overview ; GemmaInference.android.kt, GemmaInference.ios.kt and InferenceBridge.swift.

Transition → Now place that runtime behind the Android and iOS adapters in our KMP architecture.


## 13. KMP owns the product rules

11:00–12:15

KMP shares the behavior around the model: Compose UI, prompts, state, parsing, argument checks and routing. The GemmaInference contract is in commonMain. Both platforms provide a Kotlin actual adapter.

Android calls LiteRT-LM's Kotlin SDK. iOS connects its Kotlin adapter to Swift through notifications. Both adapters turn native runtime output into Kotlin Flow. Keep the focus here on ownership and boundaries; the startup-policy slide explains initialization after we see the shared API. Tool selection follows this demo's shared prompted-JSON route on both platforms.

The same clipboard tool can share its name, arguments and routing while Android uses ClipboardManager and iOS uses UIPasteboard. Lifecycle, permissions and hardware still need platform-specific implementation and tests.


Sources:
Local working tree: corresponding project source files.
Sources: current local working tree, reviewed for this revision.
composeApp/src/androidMain/kotlin/org/abma/offlinelai_kmp/inference/GemmaInference.android.kt
composeApp/src/iosMain/kotlin/org/abma/offlinelai_kmp/inference/GemmaInference.ios.kt
iosApp/iosApp/InferenceBridge.swift

Transition → Put a small Kotlin boundary between shared behavior and native runtimes.


## 14. The shared inference API

12:15–13:15

The shared app sees a small Kotlin boundary: load a compatible local artifact, then collect decoded text chunks through Flow. The expect declaration defines the shared contract; each actual implementation connects to its native runtime.

This excerpt is abridged. A chunk need not be one token, and matching method signatures do not establish identical native behavior. Keep product rules in shared code and test startup, cancellation and effects on both platforms.


If asked: The current main chat uses generateConversationResponse to pass system instructions, history and the user prompt as structured roles. The full API also includes updateConfig, generateResponseWithHistory, isModelLoaded, getLoadingProgress and close. Android applies sampler settings. This iOS bridge intentionally uses native sampler defaults. Both current load paths await native initialization, and both forward cancellation/close. These are source-level facts, not a completed physical-device benchmark.

Sources: current local working tree, GemmaInference.kt, GemmaInference.android.kt, GemmaInference.ios.kt and iosApp/iosApp/InferenceBridge.swift.

Transition → What does loading that model actually involve on each platform?


## 15. Native startup policies

13:15–14:15

The common method loads a model on both platforms, but startup policy differs. Android tries GPU, then CPU if initialization fails, and attempts warm-up. This iOS bridge initializes on GPU and preserves native sampler defaults.

Both current implementations await initialization and forward cancellation and close. Equal method signatures still do not establish equal resource cost or behavior.

Define the same phases before measuring: artifact provisioned, engine initialized, any warm-up, first visible chunk, then completion. Measure each device rather than comparing unlike phases.


If asked: This is a lesson drawn from the current implementation, not a claim that we observed a particular device failure or speedup. The iOS bridge shown has no Android-style CPU-fallback sequence. Swift samplerConfig is intentionally nil so the model/runtime defaults are used. Both adapters now forward cancel/close; older notes saying otherwise are obsolete.

Sources: current local working tree, GemmaInference.android.kt, GemmaInference.ios.kt and iosApp/iosApp/InferenceBridge.swift.

Transition → Once the runtime is ready, follow the text into the note-to-draft feature.


## 16. Walkthrough: note to draft

14:15–15:30

Read the two fictional commitments. Ask whose task has Friday as a deadline. Sara owns the slide update; no deadline was agreed for that task. Omar owns the Android recording with Friday as its deadline.

The right side is one illustrative expected item, not a captured model result and not the complete response. The app expects an items array. Keep task and evidence as exact source substrings; keep absent deadline null. The original sample includes further commitments and discussion, omitted here to focus the example.

The parser checks that evidence occurs in the source, that task occurs in the evidence, and that non-null owner and deadline occur there too. This can reject unsupported fields, but an overly broad evidence span can still contain the wrong person’s deadline. Exact matches do not establish semantic attribution or whether a statement is a commitment. Keep the review step and evaluate those cases explicitly.


Sources: DemoSamples.kt notesPrompt and parseNotes, DemoScreen.kt Action Items to Review.

Transition → The same source discipline can answer a question from a bundled local reference.


## 17. Walkthrough: local reference

15:30–16:45

This is a second narrow feature: answer only from a supplied local reference. The fictional workshop section has ID venue and says the workshop is in Room Cedar on level 2. The prompt includes the sections directly. There is no embedding pipeline, vector database, PDF ingestion or implemented retrieval system in this demo.

For the location question, an expected response names the known section and copies an exact quote. For a Wi-Fi password question, no supplied section contains that information, so the expected response is null/null. These examples are teaching outputs, not measured model results.

If asked — parser details: parseFaq checks that the sourceId exists and the quote is a nonblank exact substring of that section. It also handles null/null as no answer. Those checks do not prove the quote answers the question: a model can select a real but irrelevant sentence. Evaluate supported, unsupported and irrelevant-quote cases separately.

KMP shares prompt construction, source data and response checks; platform runtimes supply generation. Presenter cue: This slot is a static walkthrough. Read both expected responses and ask why an existing quote could still be irrelevant. Keep the only live-device block on slide 27; an extra app demo would require trading time from that block.


Sources: DemoSamples.kt sections, faqPrompt and parseFaq; DemoController.kt.

Transition → If the answer is not in our supplied context, how can the app obtain fresh information?


## 18. Cloud function calling: fresh weather

16:45–18:00

Ask: what is the weather in Riyadh today? Model weights alone are not a live weather feed. Function calling lets the model request information from a function the app has described.

The tool contract names get_weather, describes current conditions and requires a location string. The model proposes Riyadh as the location; it has not fetched anything yet. The app validates the argument, calls a weather provider, and sends the result back. Only then can the model compose an answer from that fresh result.

The displayed 26°C and sunny are mock values for teaching, not today’s observation or a captured response. This app does not implement the weather tool.

Presenter cue: Trace contract → proposed call → provider lookup → returned result → model answer. Emphasize that the application performs the external call.

If asked: Google’s Gemini guide illustrates weather function calling. Provider APIs differ in exact schema and result messages; the slide’s function syntax is conceptual. A production lookup also needs location disambiguation, units, errors and an observation time. Do not silently turn provider failure into invented weather. A fresh data source is separate from model training.

Source: https://ai.google.dev/gemini-api/docs/function-calling

Transition → The model can also run locally. Which tools would still need a connection?


## 19. Function calling on the device

18:00–18:45

Move inference onto the phone; keep the tool contract, proposal, checks and execution. That move does not make a live weather API work offline. A cached report needs a timestamp and may be stale.

Now switch to a device-local job: turn off the flashlight. The local model proposes toggle_torch with enable=false, and the app validates it before native execution.

Android offers SDK tool APIs. Our current iOS AND Android code instead uses shared prompt instructions and parsing. Swift also has native tool APIs; prompting is our project choice.

Presenter cue: Keep this overview within 45 seconds. Leave the annotation and SDK code walkthrough for slide 21.

If asked: The weather example explains obtaining new information; the torch example explains a local effect. Function calling can serve either purpose. Native tool availability is separate from whether a particular tool depends on the network. The Swift wrapper has ConversationConfig.tools and a Tool protocol. The current app displays its tool result without feeding it into an automatic next model turn.

Sources: ToolPrompt.kt, Tooling.kt; LiteRTWrapper Config.swift and Tool.swift; https://ai.google.dev/gemini-api/docs/function-calling

Transition → Read the full local tool contract in the system prompt alongside the user’s flashlight request.


## 20. A complete function-calling request

18:45–20:30

Read this as a complete small teaching request. The system role sets behavior, supplies one tool’s name and description, explains its required enable argument, and defines the response contract. History is empty because this is a new conversation. The user role asks to turn the flashlight off; the expected proposal therefore has enable=false. This is an illustrative expected response, not a captured successful model run.

The wording is deliberately self-contained and compact, not a verbatim capture of the app’s larger default prompt. The companion complete-tool-request.md includes a source-derived full default-persona request with the complete registry, all generated rules/examples, no pinned context and empty history.

If asked — actual app assembly: In the default-persona chat path, currentCombinedSystemPrompt combines persona instructions, optional pinned messages and tool instructions for each generation. GenerateResponseUseCase passes that system text, prepared history and current user text separately through generateConversationResponse. Android supplies systemInstruction; Swift creates a systemMessage. Tool instructions consume context budget alongside history and the user message. They are not permanent training or a discovery mechanism.

The actual ToolPrompt builder lists names, descriptions and parameter names with required/optional descriptions. It does not serialize the entire JSON schema or automatically expose every schema type. The torch examples and Boolean rule clarify its argument; the handler enforces the strict type/key checks. Specialized/custom chat personas and the separate note/FAQ workflows do not receive the default chat tool registry.

Sources: ToolPrompt.kt buildSystemPrompt/extractParamDetails; ChatViewModel.kt currentCombinedSystemPrompt/generateResponse; PromptBuilder.kt buildCombinedSystemPrompt; GenerateResponseUseCase.kt; both platform adapters and InferenceBridge.swift.

Transition → Now compare the prompting approach with the Android SDK’s tool APIs.


## 21. LiteRT-LM SDK function calling

20:30–22:15

SDK means Software Development Kit: the runtime library and APIs we integrate into our application. Google AI Edge LiteRT-LM’s Kotlin SDK can derive a tool contract from a ToolSet’s annotated Kotlin functions. @Tool supplies a description; @ToolParam supplies the parameter description; Kotlin’s function name and parameter type supply their corresponding contract information. The example deliberately calls the function toggle_torch to match that exposed name.

Presenter cue: The left snippet is an illustrative integration option. Its injected run callback represents app-owned native work and status reporting; torchTools must be instantiated with that implementation. This is not code already wired into our demo. Engine creation, callback implementation and initialization are omitted. Calls that block belong off the UI thread.

The right snippet registers that instance with tools=listOf(tool(torchTools)). automaticToolCalling=false selects manual handling, so the application can inspect reply.toolCalls and apply its checks before dispatch. In automatic mode, the SDK invokes registered functions and returns their results to the model. Manual handling must submit results if another model turn should continue. Neither route establishes intent or permissions.

If asked — app wiring and integration details: Our current Android AND iOS chat implementations instead use the preceding prompted-proposal route. iOS prompting is an app choice; Swift also exposes native tool APIs. SDK tool calls use Map<String,Any?> arguments, while our shared handlers use JsonObject, so normalization is required to reuse those checks. Use a compatible tool-capable artifact/template and pin versions.

Sources: https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.14.0/kotlin/java/com/google/ai/edge/litertlm/Tool.kt ; https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.14.0/kotlin/java/com/google/ai/edge/litertlm/Config.kt ; pinned v0.14.0 Kotlin getting_started.md; current local app code.

Transition → Follow our app’s prompted route from proposal to shared checks and native execution.


## 22. A complete tool-call lifecycle

22:15–23:45

Trace the same flashlight-OFF request, rather than another abstract API. First define the name and Boolean argument. A runtime can expose that definition through SDK registration; our current app exposes it through shared prompt instructions. The model returns a proposal, not a completed action.

In this app, extractToolCall normalizes generated text into a shared ToolCall with a JsonObject. The registered torch handler requires exactly the enable key and a real JSON Boolean. Invalid requests stop before native execution. A valid request reaches the platform adapter, which can still fail because hardware or permissions are unavailable. Show the resulting status in the UI.

Our app displays the native result and ends this operation. Returning that result to the model for another turn would require an additional conversation loop.

Presenter cue: Read the literal proposal on the slide. Follow its tool name through extractToolCall into ToggleTorchTool, then follow the enable value into the native call. Contrast a native success with a missing-hardware error; neither outcome is known at proposal time. Leave the argument quiz unanswered until the next slide.

If asked — continuing conversations: ChatViewModel.handleToolCall currently does not feed its result into another model turn. A continuing loop must submit the result, handle subsequent responses and enforce loop limits. Android SDK automatic mode offers orchestration; manual mode leaves it to the app. SDK arguments need adaptation before reuse of this app’s JsonObject checks.

Sources: current local ToolPrompt.kt, Tooling.kt, GenerateResponseUseCase.kt and ChatViewModel.kt; pinned LiteRT-LM v0.14.0 Kotlin getting_started.md.

Transition → Before any native call, which candidate arguments should the handler accept?


## 23. Argument quiz

23:45–25:00

Which proposals should be rejected? Give the audience fifteen seconds. A supplies a JSON Boolean. B supplies a quoted string, and C adds an undefined key. We reject B and C; A passes this argument guard.

The excerpt safely casts to JsonPrimitive, then excludes strings before reading booleanOrNull. That exclusion matters: booleanOrNull alone can parse the content of a quoted “true.” We also require exactly the enable key. Missing values, arrays, objects, strings and extra keys therefore fail before a native call. The real handler uses the same checks.

Passing the guard does not prove intent or hardware success. The API still reports the execution outcome.


Presenter cue: Keep the guard hidden during the fifteen-second vote. Press Right/Space or click Next once to reveal it, then explain B and C. Advance again to continue. The displayed guard is abridged from ToggleTorchTool's checks.

If asked: Other registered tools need their own checks. The clipboard tool currently has simpler string extraction, so do not imply universal strict schema enforcement.

Sources:
Local working tree: see the corresponding project source file.

Transition → Passing the guard proves the input shape. Does it prove the user wanted that action?


## 24. Limits

25:00–26:00

The happy path hides the difficult cases. If the user says “Do NOT turn the flashlight on,” a model may still propose enable=true. A type guard can pass while the action contradicts the request.

A quoted sentence about discussing dark mode also does not establish a commitment. Test negative and no-action cases, check each handler, and add review for consequential effects. The current chat dispatches automatically after its implemented checks; confirmation is a design recommendation.

Local weights do not supply fresh facts. Provide current local references or choose another architecture when needed.


Sources:
https://gemma.ai/models/gemma-3/
https://gemma.ai/models/functiongemma/formatting/
Local working tree: see the corresponding project source file.

Transition → That gap gives us a test plan: check task quality, proposals and actual effects separately.


## 25. A reusable evaluation method

26:00–27:45

Give attendees a method they can use on their own feature. First, a positive case: Sara's explicit commitment yields the task and owner, with null for the missing deadline. Second, a negative case: discussing dark theme does not invent a commitment. Third, an invalid tool request: a quoted Boolean or extra field is rejected before a native call. These are proposed expected test cases, not a claimed model pass rate.

Inspect separate properties: response shape, correct task/tool and arguments, consistency with the user's intent, and the actual native outcome. A valid JSON object or a generated success sentence proves only a small part of that chain.

Record the exact artifact/hash, runtime, backend, phone and OS. Measure cold load, first nonempty visible chunk and total completion separately; inspect memory on the device. Do not transfer another talk's benchmarks to this build.


Sources: DemoSamples.kt, DemoSamplesTest.kt, Tooling.kt and MetricsCollector.kt in the current local working tree.

Transition → A test also needs an expected recovery path. What can the user do when a check fails?


## 26. Failure and recovery

27:45–29:15

Walk the audience through the actual recovery paths. Model readiness is checked before running; the model-required UI guides the user to Settings. The workflow preserves its input. A new run clears the previous result, and failed validation shows an error rather than a new validated result. Raw generated text remains available for inspection. The user can shorten or edit the input and retry.

A rejected torch argument is a separate boundary: it produces an invalid-request tool result before native execution. Do not generalize this strict guard to every tool; clipboard parsing is currently simpler.

Cancellation produces “Generation stopped. You can edit the input and try again.” The controller releases the generation slot and restores its non-running state. This slide describes implemented branches, not a claimed set of observed model failures. A complete manual alternative is a recommended product extension.


Sources: current DemoController.kt, DemoScreen.kt and Tooling.kt.

Transition → Now inspect a real proposal and verify its native effect on a prepared device.


## 27. Live clipboard proof

29:15–33:15

Ask someone for a two-digit number to make this input specific to the live run. Use it instead of 47 in the displayed request.

The phone has airplane mode on and Wi-Fi off. This demonstrated path uses local inference. Watch three things: the actual copy_to_clipboard proposal, the app's device result, then the phrase appearing when pasted into an empty local field on the phone. A generated “Copied!” sentence is not proof of a clipboard effect.

If the model proposes the wrong action, show the failure honestly. The important pattern is an inspectable proposal followed by an observable native result.

Presenter cues — preparation, not spoken script:
Name the actual rehearsed artifact filename/precision, backend and phone before starting. These must come from the real setup; a model-family example or estimated size is not a measured setup.
Rehearse the exact build, artifact and phone first. Provision and load the model before disconnecting. Mirror by USB without tethering. With scrcpy use --no-clipboard-autosync. Type and long-press Paste on the phone; a laptop paste shortcut can inject its clipboard.
Expected shape, not captured output: <<{"tool":"copy_to_clipboard","arguments":{"text":"Essex lantern <number>"}}>>.
At 32:30 stop retrying. Explain the observed outcome and move to the takeaways by 33:15.

Transition → What should attendees reuse in their own offline feature?


## 28. Three things to take home

33:15–35:00

Choose one bounded task and a compatible artifact that passes checks on a real target phone. Share the product rules—prompts, parsing, validation and routing—in commonMain. Prove the effect with no-action tests, rejected-argument tests and an observable native result.

For our note example, a useful draft keeps a missing deadline null and remains reviewable. For a device action, a generated claim is not enough: inspect the proposal and read the native outcome. That is the reusable pattern: model proposes, shared Kotlin checks, native code executes.

Presenter cue: Keep the close concise, then leave the project QR on Q&A for scanning. At 35:00, open questions; references 30–37 are optional.

Sources: current project GemmaInference.kt, Tooling.kt and DemoSamples.kt.

Transition → Keep the project QR visible and open questions.


## 29. Questions

35:00–40:00 | Q&A

Keep the project QR visible for at least twelve seconds, then take questions.

Backup guide: 30 execution responsibilities; 31 Swift bridge; 32 structured draft; 33 source checks versus meaning; 34 compatibility; 35 model-family details; 36 local reference; 37 task-fit guidance. Optional, outside the timed forward talk.

Source: https://github.com/SEAbdulbasit/OfflineAI-KMP


## 30. Reference: app-owned execution

BACKUP — App-owned execution responsibilities

Only step two belongs to the model. First, the app describes its tools in the prompt; the model does not discover Kotlin functions by itself. Second, the model proposes a name and arguments. Third, the registry checks the name and the registered handler checks its inputs. Our torch handler requires a JSON Boolean and exactly the allowed key. Validation strength is handler-specific; the clipboard handler currently uses simpler extraction.

A schema in a prompt does not enforce those rules or establish intent. Fourth, the native API runs subject to permissions and hardware. Fifth, the app reports the actual API result or error.

This app displays the result directly. Feeding it into another model turn would require loop limits, state and recovery handling.


Sources:
https://gemma.ai/models/functiongemma/formatting/
Local working tree: see the corresponding project source file.

## 31. Reference: the Swift bridge

BACKUP — iOS Swift Bridge (iosApp/iosApp/InferenceBridge.swift)

This is the Swift side behind the same `Flow<String>` contract. `InferenceBridge` creates a LiteRT-LM conversation, loops over `conversation.sendMessageStream(message)`, and calls our helper `postToken(chunk.toString)`.

`postToken` is a project helper—not a LiteRT-LM API—that posts `GemmaTokenResponse` via `NotificationCenter`, which `GemmaInference.ios.kt` observes inside `callbackFlow`.

Current lifecycle: Kotlin loadModel resolves the artifact and waits for GemmaLoadDone or GemmaLoadError from Swift native initialization. Unfinished collection cancellation posts GemmaCancelRequest, and close posts GemmaCloseRequest. Swift cancels the active conversation/task and clears its engine reference on close. Kotlin includes sampling values in the generation payload, but Swift intentionally leaves samplerConfig nil and uses the model/runtime defaults. The bridge now creates a structured conversation with system/history messages. Native tool APIs also exist in the local Swift wrapper, but this app does not register them: it prompts for custom JSON and routes the parsed proposal through shared handlers. Describe these implemented paths separately from physical-device verification.

Sources:
Local working tree: corresponding project source file.
Local working tree: corresponding project source file.
https://developers.google.com/edge/litert-lm/swift

## 32. Reference: a structured draft

BACKUP — Structured Text Draft (DemoSamples.kt)

Here's the expected JSON shape behind our note-to-action-items demo in `DemoSamples.kt`. The fictional rehearsal note has Sara (no deadline), Omar (Friday), Lina (Thursday), plus a dark-theme discussion with no commitment.

`notesPrompt` asks for JSON, null for missing fields, and an exact evidence sentence copied from the note. The parser uses ignoreUnknownKeys=false to reject unknown fields; that is parser configuration, not prompt enforcement. `parseNotes` verifies the JSON shape and checks that each supplied task, owner and deadline matches text inside its evidence, and that `evidence` appears verbatim in the source note.

Sources:
Local working tree: corresponding project source file.

## 33. Reference: source checks and meaning

BACKUP — Source Checks vs. Meaning (DemoSamples.kt & DemoSamplesTest.kt)

Deterministic substring checks catch a lot, but not everything.
- Sara + `null` passes and is a real commitment.
- Sara + `Friday` is rejected *if* the evidence is Sara's own sentence (because "Friday" isn't in it)—though a multi-sentence evidence span could still smuggle in Omar's Friday.
- The dark-theme sentence passes substring checks because the words are really in the note, even though nobody committed to building it.

That's why `DemoSamplesTest.kt` explicitly documents this boundary: deterministic checks verify grounding in text, not semantic intent.

Sources:
Local working tree: corresponding project source file.
Local working tree: corresponding project source file.

## 34. Reference: compatible deployment

BACKUP — Model & Runtime Compatibility

Four things have to line up: exact converted artifact, pinned runtime version, chat template/input types, and device RAM + context budget + license.

In this repo: Kotlin 2.4.0, Compose Multiplatform 1.11.1, Android LiteRT-LM 0.14.0 (`libs.versions.toml`), and iOS `CLiteRTLM.xcframework` 0.13.1 (`LiteRTWrapper/Package.swift`).

If asked about system models: this app uses an app-managed compatible model artifact through LiteRT-LM. The on-device configurations of Gemini Nano / ML Kit GenAI and Apple's Foundation Models are separate system-model routes; availability, APIs and guarantees differ from this app-managed runtime.

Sources:
https://developers.google.com/edge/litert-lm/overview
https://gemma.ai/models/functiongemma/formatting/
Local working tree: corresponding project source file.
Local working tree: corresponding project source file.

## 35. Model families

BACKUP — Model-family examples

Gemma 3 1B IT is a compact text-only instruction-tuned example. Gemma 4 E2B also accepts image and audio inputs, so using those capabilities needs a matching artifact, runtime and app input pipeline.

FunctionGemma 270M specializes in tool use and has its own call format. It is not interchangeable with this project's prompted JSON envelope; supporting its protocol needs an appropriate integration.

These are selection examples, not measured performance results from this app. Start with a compatible artifact that passes task checks on the actual target phone. File size alone does not establish runtime RAM fit.

Use only if asked about model families; return to Q&A.

Presenter cue: Before presenting, rehearse and record the exact demo artifact filename/hash, device, backend and observed behavior. Name the verified setup aloud; do not substitute an estimated file-size range for a measurement.

Sources:
https://gemma.ai/models/gemma-3/
https://gemma.ai/models/gemma-4/model-card/
https://gemma.ai/models/functiongemma/model-card/
https://gemma.ai/models/functiongemma/formatting/

## 36. Reference: a local source or null

BACKUP — Local Reference Answers (DemoSamples.kt)

Our second bundled demo is a tiny local FAQ with four short fictional sections (`venue`, `setup`, `offline`, `help`) injected straight into the prompt—no vector database, no embeddings, and no PDF parser.

For "Where is the workshop?", the expected JSON quotes the exact sentence from `[venue]`. For "What is the Wi-Fi password?" (which isn't in the reference), it must return `null` for both `sourceId` and `quote`. `parseFaq` checks that `sourceId` exists and the quote appears verbatim in that section—great for small, curated offline references.

A quote existing in the source does not prove it answers the question. Check relevance separately; an unsupported question should produce the null response rather than an invented answer.

Sources:
Local working tree: corresponding project source file.

## 37. Reference: choose a bounded task

BACKUP — Choosing a bounded task

Start with narrow transformations: extracting fields, classifying short text, or drafting a concise summary. Our note-to-task draft is useful because its fields can be checked against the source. Small local models are not a substitute for fresh external facts or dependable unrestricted multi-step reasoning.

The tradeoff is a product decision per feature: evaluate task quality, target-device resources and required information. A missing model or a failed generation should leave a usable manual workflow. That is a UX recommendation; the current screenshot shows the app's model-required state. Local and remote features can coexist.


Sources: the task checks in DemoSamples.kt and Tooling.kt; model cards linked in the model-selection appendix.
