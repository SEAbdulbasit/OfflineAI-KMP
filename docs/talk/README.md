# Offline AI with KMP — presentation website

A locally bundled HTML presentation for Abdul Basit’s 40-minute session. The main path has 25 slides including the final resources/Q&A page. Sixteen optional references preserve earlier examples, including earlier note/FAQ walkthroughs and engineering detail. Total 41 slides.

## Run locally

From the repository root:

```bash
python3 -m http.server 8000 --bind 127.0.0.1
```

Open http://127.0.0.1:8000/docs/talk/. Fonts, scripts and presented images are bundled. No build step or external model service is required to view the slides.

## Controls

- Right / Space: next; Left: previous.
- N: speaker notes (same window; hide when projecting).
- F: fullscreen; R: reset.
- B: switch between the resources/Q&A page and references.
- Hash links use the displayed number, such as `#15` for the weather example.

Main navigation stops at 25. References 26–41 are outside the timed talk. The rejected quiz is removed entirely. The controller discovers boundaries from markup.

## Timing and story

Plan 33 minutes of explanation and the live run, with 2 minutes reserved for audience interaction and device recovery. The live block 24 is 29:00–35:00: plan 4 minutes of demonstration and use the remainder only when needed. Stop retrying by 34:15. Begin questions no later than 35:00 and finish 40:00; if delivery finishes early, begin questions rather than filling the reserve.

The main path is cloud → reasons for offline → model kinds/sizes/labels → bits and quantization → generation → LiteRT-LM and MediaPipe context → model-file formats (.litertlm / .task) → KMP API → Android/Swift initialization → first shared system/user prompt → weather function-call sequence adapted from Google’s guide → full local flashlight request → actual registry source screenshot → Android/Swift generation → shared streaming/completion/error handling → optional Android SDK route → shared dispatch/native execution → limits → live clipboard action → resources/Q&A.

Concrete headings replace the earlier vague labels. Note extraction, FAQ grounding and additional tests/recovery remain optional references. The quiz and generic slogan closing are removed.

## Speaker material and source scope

[Speaker notes](speaker-notes.md), `notes.json` and embedded notes are synchronized. Update all three along with labels/counters/timings when reordering.

[Complete default-chat request](complete-tool-request.md) and its JSON contain the source-derived nine-tool prompt; [teaching request](teaching-tool-request.txt) is the shorter flashlight example shown on 17.

The runtime page concerns MediaPipe’s LLM Inference API specifically: Google marks it maintenance-only and recommends LiteRT-LM. This is not a statement that every MediaPipe task is retired. This project uses LiteRT-LM through Android Kotlin and iOS Swift.

Slide 14 uses source-derived abridged initialization excerpts. Slide 19 compares actual app generation paths with a model already loaded, empty history and omitted error/bridge details. Slide 21 is an available SDK registration option, not current app wiring. Current Android and iOS both use prompted proposals. Slide 18’s source screenshot shows the actual nine-handler application registry; that is separate from SDK ToolSet registration.

The main weather slide adapts Google’s general cloud function-call flow; reference 31 preserves the unmodified official image. Weather values are mock teaching data, and this app does not implement weather lookup. Moving inference onto a phone does not remove a weather service’s network dependency. Native clipboard/flashlight actions are different tool effects.

Name the rehearsed artifact/precision, backend and phone during the live demonstration. Inspect the actual proposal, native result and phone clipboard paste. Do not claim illustrative outputs are measured model results. Archived pages describe their own workflows; they are not guarantees about the main chat path.

## Assets and attribution

Original template CSS and deck-stage.js remain unchanged. See [ATTRIBUTION.md](ATTRIBUTION.md) for the original design/runtime reference. `assets/google-function-calling.png` is the supplied Google Gemini documentation diagram, credited in the notes and source reference with https://ai.google.dev/gemini-api/docs/function-calling. `assets/tool-source-screenshot.jpg` is a real browser capture of the repository’s source registry. The portrait and repository QR belong to this talk.

The main weather sequence is a wide, weather-labelled adaptation of Google’s function-calling diagram for legibility. Google’s unmodified original remains on reference 31. The generic slogan closing has been removed.

The file-format comparison on 11 replaces the repeated cloud/local path. It distinguishes the .litertlm container from precision labels and older .task bundles. Both pinned native runtimes recognize .task; that does not guarantee every model is compatible. Avoid a universal all-platform artifact claim.

Slide 20 teaches the actual shared sealed-result stream: cumulative text, completion/tool handling and errors, with the streaming branch highlighted. Reference 41 shows pinned Kotlin/Swift image requests as SDK integration illustrations. Image handling is not implemented in the current demo.

Slide 15 follows initialization with a complete shared Kotlin system/user request and text-chunk collection. It precedes the weather function-calling example, so the talk introduces ordinary prompting before tools. The timed main path remains 40 minutes.
