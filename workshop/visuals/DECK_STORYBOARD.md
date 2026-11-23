# Offline AI with Kotlin Multiplatform

Visual rebuild for the existing Demo Edition Google Slides copy.

## Design

16:9. Near-black background (#11131C), off-white text (#F7F7FA), violet for shared Kotlin (#9F82FF), green for Android (#82D9BA), blue for iOS (#93BEF2). One type family, 44–52 pt titles, 24–30 pt diagram labels. No paragraphs on screen. Speaker notes carry explanation. Large real app screenshots, with captions that identify simulator vs physical-device capture. No invented generated answers or benchmark numbers.

## Narrative and composition

1. **Offline AI with Kotlin Multiplatform** — Large title left, actual app screen right. Small subtitle: Android and iOS. Speaker identity from original cover.
2. **AI inside the app** — One large phone outline containing input, model, output. Outside: a disconnected cloud. Caption: Model imported before the demo.
3. **Two offline features** — Side-by-side real screenshots. Labels only: Action items / Local FAQ.
4. **Shared features. Native inference.** — Architecture: commonMain Compose UI / DemoController / validation, expect GemmaInference, Android Kotlin and iOS Swift branches. Use architecture.svg as visual reference; rebuild as editable shapes when integration is available.
5. **One streaming contract** — Large three-line Kotlin method signature. A short horizontal diagram below: UI, Flow, native engine. Avoid long code listings.
6. **The on-device request** — Four-step editable diagram: Input / Shared prompt / Local runtime / Validated result. Return path shows streamed chunks. A device boundary encloses all four.
7. **Notes to action items** — Large actual notes demo screenshot. One enlarged source fragment: Sara will update the slides. Caption: Owner, task, deadline, evidence.
8. **Missing information stays missing** — Enlarged three-row illustrative record, clearly labeled example: Sara / update the slides / No deadline. Source evidence alongside. Explain exact-source validation and its semantic limits in notes.
9. **Answers from a local FAQ** — Large actual FAQ screenshot, source document beside it. Diagram connects the selected passage to the answer. Caption: Bundled reference. Exact quote. Source section.
10. **When the source has no answer** — Wi-Fi password question and not-found state. Use actual capture if available, otherwise a visibly labeled behavior diagram, never a fake app result.
11. **The offline boundary** — Local column: Model inference, bundled FAQ, flashlight. Network column: Model download, fresh web content. Use simple icons and short labels. Explain that opening a URL is local but fetching its content is online.
12. **The device sets the budget** — Three large visual axes: Memory / Response time / Output quality. No unmeasured numbers. Notes: model compatibility, context, temperature/backend parity, thermal conditions.
13. **Live demo** — One phone screenshot or neutral stage cue. Three short steps: Load model / Disable connectivity / Run both features. Speaker notes include backup and error states.
14. **KMP shares the feature** — Closing architecture silhouette with shared UI and logic above Android and iOS. Repo link from verified project remote. Takeaway: Native runtimes. Shared product logic.

## Accuracy constraints

The FAQ is a bundled small context, not PDF retrieval or a vector database. Literal evidence checks do not prove semantic correctness. Timings are first nonempty chunk and total generation, not tokens per second. The iOS engine initializes lazily on first generation. Current iOS cancellation disconnects the Kotlin collector and does not establish a demonstrated native-runtime stop guarantee. No screenshot may imply a successful model run that did not happen.
