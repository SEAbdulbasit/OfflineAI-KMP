# Learning revision

Destination: https://docs.google.com/presentation/d/13DefuAK4TPqDlWmC3ih0u-h6dqDOJkHpasNjHoWt0w4/edit

23 slides. Original imported Google Slides text/bullet/code layouts retained. New content uses Arial and the original centered titles, margins and navy background. Code uses an editable monospace panel derived from the original setup slide. The six-line iOS excerpt was visually checked and reduced to fit its panel.

- Model choice now distinguishes general-model prompting from FunctionGemma artifact/template/tool-format integration.
- Slides 12–19 connect prompt, expected output, actual parser entry point, validation, both native APIs, rejected-input test and displayed result.
- Expected output is identified in notes, never presented as captured inference.
- Targeted ToggleTorchToolTest suite passed. Fake-native validation tests are distinguished from physical device results.
- Device evidence is explicitly incomplete: signed iPhone16Pro install succeeded, launch blocked by developer trust. No invented first-response time.
- Developer exercise and exact entry points are in LEARNING_WALKTHROUGH.md.

Visual evidence is in exact-qa/learning-*.png. learning-pass.gs is guarded for the previous 20-slide state and must not be rerun on the finished deck.
