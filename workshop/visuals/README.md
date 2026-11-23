# Talk visuals

The visual edition uses a 14-slide sequence focused on offline AI with Kotlin Multiplatform.

- `DECK_STORYBOARD.md`: narrative, layout and accuracy constraints.
- `architecture.svg`: shared Kotlin and native inference diagram.
- `request-flow.svg`: on-device request flow.
- `build-visuals.cjs`: reproducible visual assets for Google Slides.
- `slide-*.svg`: scalable, editable artwork sources for the generated slides.
- `slide-*.png`: upload-ready 1440 × 810 slide visuals.
- `ios-notes-source.jpg`, `ios-faq-source.jpg`: actual Device Hub captures of the iPhone 17 Pro simulator on iOS 26.1. The slide renderer crops to the demo content. These are input screens with no model loaded, not generated-result screenshots.

The Google Slides presentation uses these visuals as backgrounds, with separate editable speaker notes. Text inside a background is edited in its SVG/source and uploaded again. This keeps the layout stable in presentation mode.

Regenerate the artwork with Node.js and the bundled Sharp dependency referenced in `build-visuals.cjs`. Add the app captures before rebuilding slides 7 and 9. Never substitute fabricated inference output for a real capture.

## Verification

Both physical-device and simulator Xcode builds succeeded. The simulator app launched; demo navigation, FAQ presets and source expansion were checked. The physical build installed, but iOS rejected launch with a signing/trust error. No model inference result or latency is asserted from these captures.
