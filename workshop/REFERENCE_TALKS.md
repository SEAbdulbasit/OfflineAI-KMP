# Published mobile/on-device AI talks — research checked 21 October 2026

Evidence is official session descriptions, transcripts/chapters where available, and official-channel video metadata. This is not a claim to have watched every minute. Dates below distinguish video publication from conference dates. No performance numbers are imported into this project.

## 1. Small language models with Google AI Edge
- Google I/O 2025; Matthias Grundmann, Mark Sherwood, Mogan Shieh.
- Official video published **21 May 2025**: [Watch Google AI Edge session](https://www.youtube.com/watch?v=xLmJJk1gbuE)
- Session: [Open reference](https://io.google/2025/explore/technical-session-4/)
- Verified scope: local language models with audio, RAG and function calling; CPU/GPU/NPU acceleration and testing on real devices through AI Edge Portal.
- Our adaptation: show a concrete user task first, then the model-to-app boundary. Explain that model output must be parsed and dispatched by code. Do not imply this repo implements the talk's RAG/audio pipeline or all accelerators. Its two added demos use text and bundled context.

## 2. Gemini Nano on Android: Building with on-device gen AI
- Google I/O 2025; Caren Chang, Taj Darra, Sa-ryong Kang.
- Official Android Developers video published **21 May 2025**: [Watch Android on-device GenAI session](https://www.youtube.com/watch?v=mP9QESmEDls)
- Session: [Open reference](https://io.google/2025/explore/technical-session-14/)
- Verified scope: selecting on-device use cases; ML Kit GenAI APIs for summarizing, proofreading, rewriting and image descriptions.
- Our adaptation: lead with bounded useful transformations rather than generic chat. Notes → action items fits this pattern. Explain ecosystem choice in one sentence: this app brings a compatible Gemma model through LiteRT-LM; it is not using Gemini Nano/ML Kit or AICore.

## 3. Meet the Foundation Models framework
- Apple WWDC25; official Apple Developer video published **9 June 2025**.
- Session/transcript: [Open reference](https://developer.apple.com/videos/play/wwdc2025/286/)
- Official video: [Open reference](https://www.youtube.com/watch?v=mJMvFyBvZEk)
- Verified chapters: model 2:05; guided generation 5:20; snapshot streaming 7:45; tool calling 11:28; stateful sessions 16:11; developer experience 21:02.
- Our adaptation: present a schema and resulting UI rather than a wall of generated prose. Compare a valid exact evidence quote with a rejected invented one. Critical distinction: this app uses prompt-requested JSON plus validation after generation, NOT Apple's guided generation or constrained decoding guarantees. Its Flow emits incremental strings, not Foundation Models snapshot streaming.

## 4. Code-along: Bring on-device AI to your app using the Foundation Models framework
- Apple WWDC25; official Apple Developer video published **9 June 2025**.
- Official video: [Open reference](https://www.youtube.com/watch?v=XuX66Oljro0)
- Verified chapters: prompt engineering 2:30; tool calling 11:19; streaming output 20:32; profiling 24:32.
- Our adaptation: one coherent feature progresses from input to generation to UI to measurement. Run Notes and then FAQ; reveal raw output only after users see the useful result. Profile first chunk and total time on this exact build; do not borrow Apple's latency or API behavior. Keep Apple framework as inspiration, not our implementation.

## 5. Tiny Models, Big Impact: On-Device AI for Real Apps
- Hammad Akram; KotlinConf 2026 (conference **20–22 May 2026**, exact talk day not confirmed).
- Official Kotlin by JetBrains video published **23 July 2026**: [Watch KotlinConf session](https://www.youtube.com/watch?v=Rqj4QCdwBMc)
- Session: [Open reference](https://2026.kotlinconf.com/talks/1086800/)
- Verified scope from official description: model choices, quantization, coroutine-based inference pipelines; summarization, classification, personalization and intent extraction without a server.
- Our adaptation: make the KMP value concrete with shared prompts/state/validation plus native adapters. Show a compact hardware budget: model, context, memory, output quality. No need to add another model survey or claim that all models work everywhere.

## Concrete repo-grounded demonstration sequence (6–7 minutes)
1. Before stage: import a compatible model; record exact filename/runtime/device. Load it, enable airplane mode, explicitly disable Wi-Fi, and rehearse. Current saved screenshots only verify iOS inputs, not model execution.
2. Action items (~2 min): use fictional notes. Highlight Sara has no agreed deadline; distinguish discussion of a dark theme from a commitment. Show the exact evidence and edit a result. Expected behavior must be validated on real inference; do not hardcode success.
3. Local FAQ (~2 min): ask where the workshop is, show the source passage, then ask the Wi-Fi password. Explain full bundled-reference prompting, not retrieval/vector RAG. A missing answer and an invalid citation are different states.
4. Under the hood (~1 min): reveal raw JSON → parse → exact-source check → UI. Explain that source matching cannot establish semantic relevance.
5. Engineering reality (~1 min): show measured first nonempty chunk and total generation time. Note cold versus warm runs. Shared Kotlin is real; Android/iOS native initialization and cancellation still differ.
6. Optional existing tool demo (~30–60s): flashlight only, after physical-device rehearsal. Explain model proposes action, app dispatches it. Websites and Maps can need connectivity, so don't use them to prove offline behavior.

## Deck application
Keep original theme and cover. Use a short LLM introduction (tokens, context, generate-not-retrieve), one architecture diagram, real input/result screenshots honestly labeled, the above demo sequence, and one limitations slide. Avoid adding third-party screenshots or benchmark figures merely as decoration. Cite these talks in speaker notes/resources; talk patterns are adaptations, not copied results.

Current runtime clarification: the original 2025 Google sessions are historical sources for presentation patterns. For current integration use https://developers.google.com/edge/litert-lm/overview . MediaPipe LLM Inference is now maintenance-only: https://developers.google.com/edge/mediapipe/solutions/genai/llm_inference .
