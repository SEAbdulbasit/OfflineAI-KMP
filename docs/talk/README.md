# Offline AI with KMP — presentation website

A self-contained HTML slide deck for Abdul Basit’s 40-minute talk: 35 minutes of teaching/demo and 5 minutes of Q&A. There are 37 slides: 29 in the main path including Q&A, plus 8 optional references. Scripts, fonts and images are bundled locally.

## Run locally

From the repository root:

```bash
python3 -m http.server 8000 --bind 127.0.0.1
```

Open [the presentation](http://127.0.0.1:8000/docs/talk/). After the server is running, presenting does not require a cloud model or remote asset downloads.

This is a static site. It can also be served from a subdirectory on any static host; it has no build step. Adding these files to GitHub does not enable a hosted GitHub Pages site automatically.

## Controls

- Right / Space: advance; Left: previous slide.
- N: show or hide speaker notes. Notes appear in the same browser window, so hide them on the projected screen.
- F: request fullscreen; R: reset.
- B: switch between Q&A and the references.
- Direct links use the slide number, for example `#18` for the weather example.

Slide 23 reveals its quiz answer on the first advance. Main navigation stops at Q&A 29. References 30–37 are outside the timed forward talk.

## Content and speaker material

The story moves from cloud inference and why offline to model families, parameters, bit precision, quantization and generation; then LiteRT-LM, shared Kotlin/native adapters, useful text features and function calling. Weather 18 explains the cloud function/tool-result loop before local device actions 19, the complete request 20, Kotlin SDK APIs 21 and the application lifecycle 22.

- [Speaker notes](speaker-notes.md): timed explanation, transitions, optional detail and sources.
- [Complete default-chat request](complete-tool-request.md), also available as [JSON](complete-tool-request.json): source-derived system instructions for all nine tools, empty history and an OFF request.
- [Teaching request](teaching-tool-request.txt): the shorter complete example shown on slide 20.

The embedded notes in `index.html`, `notes.json` and `speaker-notes.md` are synchronized. If changing notes, update all three. Slide labels, counters and speaker timings should be updated together when reordering. The navigation detects the quiz, Q&A and reference boundaries from the markup.

## Evidence and implementation scope

The app screenshot shows the input screen before inference. Drafts, FAQ answers and weather values are illustrative expected outputs, not captured successful runs. The weather tool explains the cloud pattern; this app does not implement it. Local inference does not make a live weather lookup work offline.

Android’s SDK tool-registration example is an available integration option; this project’s current chat uses prompted proposals on Android and iOS. Swift also exposes native tool APIs. Implementation details describe the local app revision used to prepare the talk; check the exact build and runtime versions when reproducing it.

Rehearse the exact model artifact, backend and phone before presenting. The live block is 29:15–33:15; stop retries at 32:30 and start Q&A at 35:00.

## Attribution

See [ATTRIBUTION.md](ATTRIBUTION.md) for the design/runtime reference and asset sources.
