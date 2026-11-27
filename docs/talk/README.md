# Offline AI with KMP — presentation website

A bundled HTML presentation for Abdul Basit’s 40-minute session: 26 main slides including resources/Q&A, followed by 16 optional references. Total 42 slides.

## Run locally

From the repository root:

```bash
python3 -m http.server 8000 --bind 127.0.0.1
```

Open http://127.0.0.1:8000/docs/talk/. Fonts, scripts and presented images are bundled; viewing the slides requires no model service or build step.

## Controls

- Right / Space: next; Left: previous.
- N: speaker notes; hide when projecting.
- F: fullscreen; R: reset.
- B: switch between Q&A and optional references.
- Hash links use displayed slide numbers, such as `#20` for the shared clipboard implementation.

Main navigation stops at 26. References 27–42 are outside the timed talk. Boundaries are discovered from markup.

## Story and timing

Cloud requests → why offline → model kinds and families → parameters/precision/quantization → generation → LiteRT-LM/MediaPipe → model-file formats → KMP API and native initialization → first prompt → function-calling introduction → weather → local tool request → registry → shared clipboard implementation → native prompt delivery → shared streaming → SDK alternative → engineering lessons → live clipboard demo → takeaways/Q&A.

Explanation/code runs 0:00–30:00. The live demo is 30:00–35:00: plan four minutes plus one minute for interaction/recovery. Stop retries by 34:15. Open Q&A at 35:00 and finish at 40:00. If delivery finishes early, begin questions rather than filling the reserve.

The demo asks: **Copy 'Hello from offline AI' to my clipboard.** Watch the actual proposal, native result and paste on the phone. Name the rehearsed model artifact/precision, backend and device. Provision and load the model before disconnecting. Do not present illustrative outputs as measured model results.

## Notes and code scope

`speaker-notes.md`, `notes.json` and embedded notes are synchronized. Update all three when changing the story. Counters, timings and source references must follow reordering.

- 14: source-derived Android/Swift initialization.
- 15: illustrative first shared Kotlin system/user request.
- 17: weather sequence adapted from Google’s guide; values are mock and the app implements no weather tool. Original diagram: reference 32.
- 18: complete minimal local tool request; `teaching-tool-request.txt` is the text source. `complete-tool-request.md/json` preserve the fuller nine-tool request.
- 19: actual registry source screenshot.
- 20: source-derived CopyToClipboardTool execution, abridged ToolSpec and Android/iOS Kotlin actual effect excerpts. Clipboard checks are simpler than the torch guard; notes explain the boundary.
- 21: actual native generation excerpts with empty history, model already loaded and omitted bridge/error details.
- 22: actual shared sealed-result handling. Native chunks are deltas; partialResponse is cumulative.
- 23: available Android SDK registration alternative, not current app wiring. Both current platforms use prompted proposals; Swift also has native tool APIs.
- 24: engineering decisions supported by source, not invented crash/performance anecdotes.
- 25: live clipboard action.
- 26: concrete takeaways, QR and Q&A.
- 42: optional Kotlin/Swift image-request illustrations. They require a compatible multimodal model and vision initialization; the current demo has no image-input pipeline.

MediaPipe’s legacy LLM Inference API is maintenance-only with migration guidance to LiteRT-LM. This is not a statement about every MediaPipe task. Both pinned native LiteRT-LM versions recognize .task and .litertlm; a suffix alone does not establish model compatibility.

## Design and attribution

The original template CSS and core deck-stage.js remain unchanged. New code excerpts use the existing typography and violet palette. See [ATTRIBUTION.md](ATTRIBUTION.md) and `assets/provenance.json` for sources. The portrait and repository QR belong to this talk.
