# Mobile model reference

Checked **21 October 2026** against model publishers and the LiteRT Community artifact repositories. These are examples for the talk, not a tested compatibility list for this app.

## Models, parameters and quantization

A model combines an architecture and learned numerical parameters. Its tokenizer converts input into tokens; the runtime executes the model. “1B” means roughly one billion parameters, not one billion facts. Weights are learned parameter values, normally fixed during inference; adding text to a prompt does not retrain them.

Quantization stores values with fewer bits. It can reduce storage and memory traffic, but may change accuracy or output quality. Speed depends on the quantization method, runtime and hardware. [Hugging Face quantization guide](https://huggingface.co/docs/transformers/quantization/concept_guide).

Illustrative weight-value arithmetic, using decimal units:

- 1 billion × 16 bits ÷ 8 = **2 GB**.
- 1 billion × 4 bits ÷ 8 = **500 MB**.

This excludes scales, metadata, tensors kept at other precisions, runtime buffers and context/KV cache. It is not a download-size or total-RAM prediction.

## Four concrete downloadable artifacts

Sizes below are the repositories’ rounded displayed file sizes, not measured peak memory. Use the **Artifact** column label; these builds do not all use the same quantization recipe.

| Model | Parameter label | Exact artifact | Listed size | Source |
|---|---|---|---:|---|
| Gemma 3 1B IT | 1B | `gemma3-1b-it-int4.litertlm` | 584 MB | [LiteRT Community files](https://huggingface.co/litert-community/Gemma3-1B-IT/tree/main) |
| Gemma 3n E2B IT | E2B effective | `gemma-3n-E2B-it-int4.litertlm` | 3.66 GB | [Google files](https://huggingface.co/google/gemma-3n-E2B-it-litert-lm/tree/main) |
| Gemma 4 E2B IT | E2B effective | `gemma-4-E2B-it.litertlm` | 2.59 GB | [Exact artifact](https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/blob/main/gemma-4-E2B-it.litertlm) |
| Qwen3 0.6B | 0.6B | `qwen3_0.6b_q4_block32_ekv1280.litertlm` | 347 MB | [Exact artifact](https://huggingface.co/litert-community/Qwen3-0.6B-int4/blob/main/qwen3_0.6b_q4_block32_ekv1280.litertlm) |

Gemma 3 and 3n sizes were verified from public file listings; downloading their gated artifacts requires accepting the relevant terms. The separate Gemma 4 `gemma-4-E2B-it-gpu.litertlm` is listed at **2.01 GB**; do not interchange its filename or size with the 2.59 GB artifact. [Gemma 4 file listing](https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/tree/main).

## Effective parameters are not total stored weights

Gemma 3n E2B has over five billion parameters loaded under standard execution. Parameter skipping and per-layer embedding (PLE) caching can reduce its effective memory load to about 1.91B parameters. “E2B” therefore does not imply a 1 GB four-bit download. [Gemma 3n overview](https://ai.google.dev/gemma/docs/gemma-3n).

Gemma 4 E2B/E4B target mobile and edge devices and also use PLE. Its mobile QAT recipe uses mixed precision, including targeted two-bit decoder layers; do not describe all of its weights as uniform INT4. Google’s memory estimates exclude context-cache costs and depend on the deployment environment. [Gemma 4 overview and quantization guidance](https://ai.google.dev/gemma/docs/core).

## Other families and system-managed models

[Qwen3.5 0.8B](https://huggingface.co/Qwen/Qwen3.5-0.8B) is an available image/text model; its publisher positions this small variant for prototyping and task-specific work. A LiteRT-LM 0.8B artifact was not verified in this review. [SmolLM2](https://huggingface.co/HuggingFaceTB/SmolLM2-1.7B-Instruct) offers 135M, 360M and 1.7B text models. Their ordinary Transformers checkpoints are not automatically loadable by this app.

[Gemini Nano](https://developer.android.com/ai/gemini-nano) is accessed through Android's AICore and ML Kit APIs. [Apple Foundation Models](https://developer.apple.com/videos/play/wwdc2025/286/) exposes a system model with availability checks. These are separate integration routes, not interchangeable model files for the current LiteRT-LM adapters.

## What this app actually supports

The repository pins Android LiteRT-LM **0.14.0** in `gradle/libs.versions.toml` and the iOS wrapper binary **0.13.1** in `iosApp/LiteRTWrapper/Package.swift`. A `.litertlm` extension alone does not establish compatibility with either pinned version, backend, model architecture or prompt template. The shared helper currently applies Gemma-style turns; a different family may require different formatting. This reference does not establish successful inference with any of the four listed artifacts on the current build.

The demo path is text-only. Android configures a 4,096-token engine budget; a model card's larger advertised context does not change that application setting. iOS initialization and configuration differ from Android. Do not transfer model-wide modality, context or acceleration claims to this app without checking its adapter and target device.

## Explain output quality through the task

More parameters can increase capacity, but parameter count alone does not rank models across training data, architectures or tuning. Quantization can change outputs; a lower-bit model is not guaranteed to be faster or worse on every task. Compare the same prompts and exact model variants on the target device.

For this talk, record: valid JSON, correct owner/deadline, faithful evidence, missing-answer behavior, first nonempty text chunk, total time, and peak memory if measured. Distinguish parser unit-test results from actual model generation. A valid quote can be irrelevant; an exact sentence can describe a discussion rather than a commitment. Human review remains necessary.
