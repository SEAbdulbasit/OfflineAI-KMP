# Presentation attribution

The typography, initial slide components and `deck-stage.js` runtime were adapted from [Akansha Jain’s on-device AI presentation](https://akanshajain.dev/talks/on-device-ai/). The talk content, KMP-specific explanations, diagrams, examples, presenter notes and supplemental controls were revised for this project. The untouched reference presentation and working backup archives are not part of this website source.

The bundled Google Sans Flex and Google Sans Code font files were obtained from Google Fonts endpoints; original asset URLs are recorded in [assets/provenance.json](assets/provenance.json).

The portrait and QR assets came from Abdul Basit’s supplied slides. `app-notes-input.jpg` is the project’s iOS-simulator input-screen capture before inference. The screenshot is UI evidence, not a captured model response.

Technical references are placed beside the relevant explanation in [speaker-notes.md](speaker-notes.md). The weather function-calling pattern follows [Google’s function-calling guide](https://ai.google.dev/gemini-api/docs/function-calling), using explicitly illustrative weather data.

The function-calling sequence diagram is reproduced from [Google’s Gemini API function-calling guide](https://ai.google.dev/gemini-api/docs/function-calling). The [original diagram image](https://www.gstatic.com/cloud-cms/6fae8435a773b720d5db0c8746efb318.png) is bundled as `assets/google-function-calling.png`; it illustrates the general tool-call loop rather than a weather-specific API.

`assets/tool-source-screenshot.jpg` is a browser screenshot of the project’s actual `createDefaultToolRegistry()` source, rendered with syntax highlighting for the talk. The capture lists the registered handlers without altering their names or order.

The main weather sequence is a wide, weather-labelled adaptation of Google’s function-calling diagram for legibility. Google’s unmodified original remains on reference29. The generic slogan closing has been removed.
