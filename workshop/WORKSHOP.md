# 🤖 Building Offline AI Apps with Gemma + Kotlin Multiplatform

## Complete Workshop Guide

---

# TABLE OF CONTENTS

1. [Section 1: Introduction to Offline AI](#section-1-introduction-to-offline-ai)
2. [Section 2: Gemma + MediaPipe Architecture](#section-2-gemma--mediapipe-architecture)
3. [Section 3: Gemma Integration](#section-3-gemma-integration)
4. [Section 4: Token Streaming](#section-4-token-streaming)
5. [Section 5: Conversation Memory](#section-5-conversation-memory)
6. [Section 6: Performance + Optimization](#section-6-performance--optimization)
7. [Section 7: Advanced Offline Media Pipeline](#section-7-advanced-offline-media-pipeline)
8. [Section 8: Production Considerations](#section-8-production-considerations)
9. [Section 9: Final Demo](#section-9-final-demo)

---

# SECTION 1: INTRODUCTION TO OFFLINE AI

## 🎯 Learning Objectives

By the end of this section, attendees will:
- Understand what offline AI is and why it matters
- Know the key differences between cloud AI vs on-device AI
- Understand privacy, latency, and cost implications
- Be excited about the possibilities of edge AI

---

## 📚 Concept Explanation

### What is Offline AI?

Offline AI refers to machine learning models that run **entirely on the user's device** without requiring an internet connection. The model weights, inference engine, and all processing happen locally.

```
┌─────────────────────────────────────────────────────────────┐
│                     CLOUD AI (Traditional)                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   📱 Device ──────► 🌐 Internet ──────► ☁️ Cloud Server    │
│       │                                      │              │
│       │              Request                 │ Process      │
│       │             (Your Data)              │ (GPU Farm)   │
│       │                                      │              │
│       ◄───────────── Response ───────────────┘              │
│                                                             │
│   ⚠️ Data leaves device | 💰 API costs | 📶 Requires WiFi  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     ON-DEVICE AI (Offline)                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   📱 Device                                                 │
│   ┌─────────────────────────────────────┐                  │
│   │  Your App                           │                  │
│   │  ┌─────────────────────────────┐    │                  │
│   │  │   🧠 Gemma Model            │    │                  │
│   │  │   ┌───────────────────┐     │    │                  │
│   │  │   │ Input → Process  │     │    │                  │
│   │  │   │    → Output      │     │    │                  │
│   │  │   └───────────────────┘     │    │                  │
│   │  └─────────────────────────────┘    │                  │
│   └─────────────────────────────────────┘                  │
│                                                             │
│   ✅ Data stays on device | 💸 No API costs | ✈️ Works    │
│                                           anywhere          │
└─────────────────────────────────────────────────────────────┘
```

### Why On-Device AI Matters

| Aspect | Cloud AI | On-Device AI |
|--------|----------|--------------|
| **Privacy** | Data sent to servers | Data never leaves device |
| **Latency** | 200-2000ms network delay | 0ms network delay |
| **Cost** | Pay per API call | Free after model download |
| **Availability** | Requires internet | Works offline (airplane, subway, wilderness) |
| **Control** | Vendor dependency | Full ownership |

---

## 🎤 Speaker Notes

### Opening Hook (2 minutes)

> "I want everyone to turn on airplane mode right now. Seriously, do it."
> 
> *Wait for attendees to comply*
> 
> "Now, open ChatGPT... It doesn't work, right? Open Claude... Nothing."
> 
> "By the end of today, you'll have an AI assistant that works perfectly in this exact situation. No internet. No API keys. No cloud bills. Just you and Gemma, running entirely on your phone."

### Key Points to Emphasize

1. **Privacy Revolution**
   - "Your private conversations never leave your device"
   - "No server logs. No data mining. No third-party access."
   - "Perfect for healthcare, legal, financial apps"

2. **Latency = UX**
   - "Cloud AI: 300ms minimum, often 1-2 seconds"
   - "On-device: Start generating in ~50ms"
   - "That's the difference between responsive and laggy"

3. **Economics at Scale**
   - "1 million users × $0.002/request × 10 requests/day = $20,000/day"
   - "On-device: $0/day"
   - "Model download: ~1GB one-time"

4. **Edge AI is the Future**
   - "Apple Intelligence - all on-device"
   - "Google Pixel's AI features - on-device"
   - "The industry is moving this direction"

### Audience Engagement Questions

Ask the audience:
- "Who has built an app that uses OpenAI or similar cloud APIs?"
- "Who has worried about API costs scaling with users?"
- "Who has had users complain about privacy concerns?"

---

## 🎬 Demo Ideas

### Demo 1: Airplane Mode Challenge

**Setup**: Have your app already running with a model loaded

1. Show airplane mode is ON
2. Ask the AI: "Write me a haiku about coding"
3. Watch it stream the response in real-time
4. **Wow moment**: "No internet. No API. Just silicon."

### Demo 2: Speed Comparison (Pre-recorded)

Show a split-screen video:
- Left: Cloud AI with visible network delay
- Right: On-device AI responding immediately

### Demo 3: Privacy Visualization

Show a network monitor (Charles Proxy or similar):
- Cloud AI: Show requests going to OpenAI servers
- On-device AI: Show ZERO network traffic

---

## 🖼️ Slide Ideas

### Slide 1: Title
```
Building Offline AI Apps with Gemma + KMP
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Your AI. Your Device. No Internet Required.
```

### Slide 2: The Problem
```
Every AI app today requires:
📶 Internet connection
💳 API subscription  
🔓 Sending user data to the cloud

What if we could change that?
```

### Slide 3: The Solution
```
On-Device AI with Gemma

✅ 100% offline capability
✅ Zero API costs
✅ Complete privacy
✅ Instant responses
✅ Works everywhere
```

### Slide 4: Real-World Use Cases
```
Perfect for:
🏥 Healthcare apps (HIPAA compliance)
⚖️ Legal document analysis
💰 Financial advisors
🎮 Offline games with AI
📝 Personal journals/diaries
🌍 Apps for regions with poor connectivity
```

### Slide 5: What We'll Build Today
```
┌─────────────────────────┐
│   Offline AI Assistant  │
├─────────────────────────┤
│ • Chat interface        │
│ • Streaming responses   │
│ • Conversation memory   │
│ • Works in airplane mode│
│ • Production-ready arch │
└─────────────────────────┘
```

---

## 🏗️ Architecture Preview

Show attendees what they're building:

```
┌──────────────────────────────────────────────────────────────┐
│                    Compose Multiplatform                      │
│  ┌──────────────────────────────────────────────────────┐    │
│  │                   ChatScreen UI                       │    │
│  └──────────────────────────────────────────────────────┘    │
│                            │                                  │
│                            ▼                                  │
│  ┌──────────────────────────────────────────────────────┐    │
│  │                  ChatViewModel                        │    │
│  │         StateFlow + Coroutines + MVVM                 │    │
│  └──────────────────────────────────────────────────────┘    │
│                            │                                  │
│                            ▼                                  │
│  ┌──────────────────────────────────────────────────────┐    │
│  │               GemmaInference (expect)                 │    │
│  └──────────────────────────────────────────────────────┘    │
│                            │                                  │
├────────────────────────────┼─────────────────────────────────┤
│         Android            │              iOS                 │
│  ┌──────────────────┐      │      ┌──────────────────┐       │
│  │  MediaPipe SDK   │      │      │  MediaPipe SDK   │       │
│  │  (actual impl)   │      │      │  (Swift Bridge)  │       │
│  └──────────────────┘      │      └──────────────────┘       │
│            │               │               │                  │
│            ▼               │               ▼                  │
│  ┌──────────────────┐      │      ┌──────────────────┐       │
│  │   Gemma Model    │      │      │   Gemma Model    │       │
│  │   (1-4 GB)       │      │      │   (1-4 GB)       │       │
│  └──────────────────┘      │      └──────────────────┘       │
└────────────────────────────┴─────────────────────────────────┘
```

---

## ❓ Common Questions to Prepare For

**Q: What about model size and download time?**
> A: Gemma 2B quantized is ~1.4GB. It's a one-time download. We pre-load it for the workshop, but in production you'd download it once when the user opts in.

**Q: What devices can run this?**
> A: Any Android phone from 2018+ with 4GB+ RAM. iPhones from iPhone 11+. We optimize for mid-range and above.

**Q: How does quality compare to GPT-4?**
> A: On-device models are smaller, so they're not as capable for complex reasoning. But for conversations, simple Q&A, summarization—they're excellent. The key is matching use case to model capability.

**Q: Can I fine-tune the model?**
> A: Not on-device, but you can fine-tune Gemma on a server and deploy the fine-tuned model. We'll discuss this in production considerations.

---

## ⏱️ Section Timing

| Activity | Duration |
|----------|----------|
| Opening hook | 2 min |
| What is Offline AI | 5 min |
| Why it matters (Privacy, Latency, Cost) | 8 min |
| Live demo | 5 min |
| Q&A | 5 min |
| **Total** | **25 min** |

---

## 📋 Transition to Section 2

> "Now that you understand WHY we want offline AI, let's dive into HOW it actually works. We'll explore Gemma's architecture and how MediaPipe makes on-device inference possible."

---

# SECTION 2: GEMMA + MEDIAPIPE ARCHITECTURE

## 🎯 Learning Objectives

By the end of this section, attendees will:
- Understand Gemma model basics (without deep ML theory)
- Know what quantization is and why it matters
- Understand the inference pipeline from prompt to response
- Know MediaPipe's role in enabling mobile inference
- Understand the runtime lifecycle

---

## 📚 Concept Explanation

### Meet Gemma

Gemma is Google's family of lightweight, open-source large language models. Think of it as a smaller, mobile-friendly cousin of the models powering Google's AI features.

```
┌─────────────────────────────────────────────────────────────┐
│                    GEMMA MODEL FAMILY                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Gemma 2B         Gemma 7B         Gemma 2 9B              │
│  ┌─────────┐      ┌─────────┐      ┌─────────┐             │
│  │  ████   │      │ ████████│      │█████████│             │
│  │  ████   │      │ ████████│      │█████████│             │
│  │         │      │ ████████│      │█████████│             │
│  └─────────┘      └─────────┘      └─────────┘             │
│                                                             │
│  Parameters: 2B    Parameters: 7B   Parameters: 9B         │
│  Size: ~1.4GB      Size: ~4.5GB     Size: ~6GB             │
│  RAM: 4GB+         RAM: 8GB+        RAM: 12GB+             │
│                                                             │
│  Best for:         Best for:        Best for:              │
│  • Mobile devices  • Tablets        • High-end devices     │
│  • Quick responses • Better quality • Best quality         │
│  • Low memory      • Medium RAM     • Premium devices      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### What is Quantization?

Quantization is the process of reducing model precision to make it smaller and faster.

```
┌─────────────────────────────────────────────────────────────┐
│                      QUANTIZATION                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Original Model (FP32)                                      │
│  ┌────────────────────────────────────────┐                │
│  │ Weight: 3.141592653589793              │                │
│  │ Precision: 32 bits per number          │                │
│  │ Size: ~8GB for 2B parameters           │                │
│  └────────────────────────────────────────┘                │
│                     │                                       │
│                     ▼ Quantize                              │
│                                                             │
│  Quantized Model (INT4)                                     │
│  ┌────────────────────────────────────────┐                │
│  │ Weight: 3.14 (approximation)           │                │
│  │ Precision: 4 bits per number           │                │
│  │ Size: ~1.4GB for 2B parameters         │                │
│  └────────────────────────────────────────┘                │
│                                                             │
│  Result: 6x smaller, 2-4x faster, ~95% quality             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### The Inference Pipeline

Here's what happens when a user sends a message:

```
                        INFERENCE PIPELINE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  USER INPUT                     "What is Kotlin?"
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│                     1. PROMPT FORMATTING                  │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Raw: "What is Kotlin?"                                  │
│                     │                                    │
│                     ▼                                    │
│  Formatted:                                              │
│  ┌────────────────────────────────────────────┐         │
│  │ <start_of_turn>user                        │         │
│  │ What is Kotlin?<end_of_turn>               │         │
│  │ <start_of_turn>model                       │         │
│  └────────────────────────────────────────────┘         │
│                                                          │
│  Why? Gemma was trained with these special tokens       │
│                                                          │
└──────────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│                      2. TOKENIZATION                      │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Text → Numbers (Token IDs)                              │
│                                                          │
│  "What is Kotlin?"                                       │
│       │                                                  │
│       ▼                                                  │
│  [1841, 603, 146583, 235336]                            │
│    │     │      │       │                                │
│   What  is   Kotlin     ?                                │
│                                                          │
│  Model only understands numbers, not text               │
│                                                          │
└──────────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│                  3. MODEL INFERENCE                       │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Token IDs → Neural Network → Next Token Probability     │
│                                                          │
│  ┌─────────┐     ┌─────────────────┐     ┌─────────┐   │
│  │  Input  │ ──► │  Gemma Layers   │ ──► │ Output  │   │
│  │ Tokens  │     │  (Transformer)  │     │ Probs   │   │
│  └─────────┘     └─────────────────┘     └─────────┘   │
│                                                          │
│  Predicts: "Kotlin" → 0.8, "is" → 0.05, "The" → 0.03   │
│  Selects: "Kotlin" (highest probability)                │
│                                                          │
└──────────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│               4. AUTOREGRESSIVE GENERATION                │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Repeat: Generate one token, add to input, repeat       │
│                                                          │
│  Step 1: Input → "Kotlin"                               │
│  Step 2: Input + "Kotlin" → "is"                        │
│  Step 3: Input + "Kotlin is" → "a"                      │
│  Step 4: Input + "Kotlin is a" → "programming"          │
│  ...                                                     │
│  Step N: Input + response → <end_of_turn>               │
│                                                          │
│  This is why generation is sequential (slow)            │
│                                                          │
└──────────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│                  5. TOKEN STREAMING                       │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Each token is emitted immediately to the UI            │
│                                                          │
│  Time 0ms:   "Kotlin"                                   │
│  Time 50ms:  "Kotlin is"                                │
│  Time 100ms: "Kotlin is a"                              │
│  Time 150ms: "Kotlin is a programming"                  │
│  ...                                                     │
│                                                          │
│  User sees response building in real-time!              │
│                                                          │
└──────────────────────────────────────────────────────────┘
       │
       ▼
  FINAL OUTPUT        "Kotlin is a programming language..."

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### MediaPipe's Role

MediaPipe is Google's framework for running ML models on mobile devices. It handles all the complex parts:

```
┌─────────────────────────────────────────────────────────────┐
│                     MEDIAPIPE LLM INFERENCE                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Your Kotlin Code                                           │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  gemmaInference.generateResponse("Hello")             │ │
│  └───────────────────────────────────────────────────────┘ │
│                            │                                │
│                            ▼                                │
│  MediaPipe SDK handles:                                     │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ ✅ Model loading and memory management                 │ │
│  │ ✅ Tokenization (text → token IDs)                     │ │
│  │ ✅ GPU acceleration (where available)                  │ │
│  │ ✅ CPU fallback (when GPU not available)               │ │
│  │ ✅ Memory-efficient inference                          │ │
│  │ ✅ Streaming token output                              │ │
│  │ ✅ Thread safety                                       │ │
│  │ ✅ Platform-specific optimizations                     │ │
│  └───────────────────────────────────────────────────────┘ │
│                            │                                │
│                            ▼                                │
│  Platform Runtime                                           │
│  ┌─────────────────────┐   ┌─────────────────────┐        │
│  │ Android: TFLite GPU │   │ iOS: Core ML / GPU  │        │
│  │ NDK acceleration    │   │ Metal acceleration  │        │
│  └─────────────────────┘   └─────────────────────┘        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Runtime Lifecycle

Understanding the lifecycle is critical for production apps:

```
┌─────────────────────────────────────────────────────────────┐
│                    GEMMA RUNTIME LIFECYCLE                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. INITIALIZATION (Cold Start)                             │
│     ┌─────────────────────────────────────────────────┐    │
│     │ • Load model file from disk (~3-8 seconds)      │    │
│     │ • Allocate memory for weights                   │    │
│     │ • Initialize inference session                  │    │
│     │ • Warm up (optional first inference)            │    │
│     └─────────────────────────────────────────────────┘    │
│                            │                                │
│                            ▼                                │
│  2. READY STATE                                             │
│     ┌─────────────────────────────────────────────────┐    │
│     │ • Model in memory                               │    │
│     │ • Ready to accept prompts                       │    │
│     │ • Low CPU usage while idle                      │    │
│     │ • Memory: ~1.5-2x model size                    │    │
│     └─────────────────────────────────────────────────┘    │
│                            │                                │
│                            ▼                                │
│  3. INFERENCE (Active Generation)                           │
│     ┌─────────────────────────────────────────────────┐    │
│     │ • High CPU/GPU utilization                      │    │
│     │ • Memory stable                                 │    │
│     │ • Tokens generated ~10-30/second                │    │
│     │ • Battery drain during this phase               │    │
│     └─────────────────────────────────────────────────┘    │
│                            │                                │
│                            ▼                                │
│  4. CLEANUP (App Background/Close)                          │
│     ┌─────────────────────────────────────────────────┐    │
│     │ • Release model memory                          │    │
│     │ • Close inference session                       │    │
│     │ • Free GPU resources                            │    │
│     │ • Essential to avoid memory leaks!              │    │
│     └─────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎤 Speaker Notes

### Opening for Section 2 (1 minute)

> "Now let's look under the hood. I want to demystify what's actually happening when we run Gemma on a phone. Don't worry—we're not going into transformer math. We're engineers, we care about HOW to use it, not how to invent it."

### Key Points: Gemma Basics (3 minutes)

> "Gemma comes in different sizes. For mobile, we use Gemma 2B—that's 2 billion parameters. Sounds huge, but after quantization, it's about 1.4GB."
>
> "Why 2B? Because it's the sweet spot. Big enough to be useful, small enough to run on a mid-range phone."

### Key Points: Quantization (3 minutes)

> "Here's the magic trick that makes mobile AI possible: quantization."
> 
> "Imagine you have a number: 3.141592653589793. That's stored as a 32-bit float. Quantization says: 'Actually, 3.14 is close enough.' Now we can use just 4 bits."
>
> "Result? 6x smaller model, 2-4x faster, and—this is the amazing part—only about 5% quality loss. Your users won't notice."

### Key Points: Inference Pipeline (5 minutes)

Walk through each step:

1. **Prompt Formatting**: "Gemma expects specific tokens. We format the user's message with `<start_of_turn>` tags. Miss this, and you get garbage output."

2. **Tokenization**: "The model doesn't see 'Hello', it sees `[17534]`. Every word maps to a number."

3. **Generation**: "Here's the key insight—the model generates ONE token at a time. It's autoregressive. That's why responses build up gradually, and why we can stream."

4. **Streaming**: "Each token is emitted immediately. We don't wait for the full response. This is crucial for UX."

### Key Points: MediaPipe (3 minutes)

> "MediaPipe is our secret weapon. Google built it, maintains it, and optimizes it for every Android and iOS version."
>
> "We don't write GPU kernels. We don't implement tokenizers. We don't manage memory manually. MediaPipe does all of that."
>
> "Our job is to connect the dots: load model, pass prompts, receive tokens, show to user."

### Audience Engagement

Ask:
- "Who's worked with TensorFlow Lite before?"
- "Who's tried running any ML model on mobile?"
- "What do you think is the hardest part of on-device ML?"

---

## 🎬 Demo Ideas

### Demo 1: Model Loading Visualization

Show the app's loading screen:
1. Display loading progress (0% → 100%)
2. Show memory usage increasing in Android Profiler
3. **Talking point**: "This 5-second wait happens once. Then we're ready for instant responses."

### Demo 2: Token-by-Token Breakdown

Use a debugging view or print statements to show:
```
Token 1: "Kotlin"     (50ms)
Token 2: " is"        (48ms)
Token 3: " a"         (52ms)
Token 4: " modern"    (49ms)
...
```
**Talking point**: "Each token takes ~50ms on this device. That's our throughput: ~20 tokens/second."

### Demo 3: Memory Impact

Show Android Studio Profiler:
- Before model load: ~100MB app memory
- After model load: ~1.7GB app memory
- During inference: Stable at ~1.7GB

**Talking point**: "The model lives in RAM. This is why device requirements matter."

---

## 🖼️ Slide Ideas

### Slide 1: Section Title
```
Section 2: Gemma + MediaPipe Architecture
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
How on-device LLMs actually work
```

### Slide 2: Gemma at a Glance
```
Gemma 2B (What we're using)

📊 2 billion parameters
📦 1.4 GB on disk (quantized)
🧠 ~1.7 GB RAM when loaded
⚡ ~20 tokens/second on modern phones
🎯 Great for: chat, Q&A, summarization
```

### Slide 3: Quantization Simplified
```
THE QUANTIZATION TRADE-OFF

Full Precision (FP32)    Quantized (INT4)
━━━━━━━━━━━━━━━━━━━━━    ━━━━━━━━━━━━━━━━━
8 GB model               1.4 GB model
Slow inference           4x faster
High accuracy            ~95% accuracy
Won't fit on phones      ✅ Mobile-friendly
```

### Slide 4: The Magic Formula
```
               USER INPUT
                   │
                   ▼
╔════════════════════════════════════════╗
║          PROMPT FORMATTING             ║
║  "<start_of_turn>user\n..."            ║
╚════════════════════════════════════════╝
                   │
                   ▼
╔════════════════════════════════════════╗
║            TOKENIZATION                ║
║     "Hello" → [17534]                  ║
╚════════════════════════════════════════╝
                   │
                   ▼
╔════════════════════════════════════════╗
║         GEMMA INFERENCE                ║
║   Predict next token (one at a time)   ║
╚════════════════════════════════════════╝
                   │
                   ▼
╔════════════════════════════════════════╗
║         STREAMING OUTPUT               ║
║    Token → UI → Token → UI → ...       ║
╚════════════════════════════════════════╝
                   │
                   ▼
               AI RESPONSE
```

### Slide 5: MediaPipe = Your Best Friend
```
What MediaPipe handles for you:

✅ Model loading & memory management
✅ Tokenization
✅ GPU acceleration
✅ CPU fallback
✅ Streaming callbacks
✅ Thread safety
✅ Platform optimizations

What you write:
loadModel(path)
generateResponse(prompt)
```

### Slide 6: Runtime States
```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  LOADING │ ──► │   READY  │ ◄─► │GENERATING│
│  3-8 sec │     │ Idle,low │     │ Streaming│
│          │     │   CPU    │     │ high CPU │
└──────────┘     └──────────┘     └──────────┘
                       │
                       ▼
                 ┌──────────┐
                 │  CLOSED  │
                 │  Memory  │
                 │ released │
                 └──────────┘
```

---

## 🔧 Code Architecture Preview

Show attendees the code they'll be working with:

```kotlin
// The expect/actual pattern for cross-platform inference
// commonMain - Interface
expect class GemmaInference() {
    suspend fun loadModel(modelPath: String, config: ModelConfig)
    fun generateResponse(prompt: String): Flow<String>
    fun isModelLoaded(): Boolean
    fun close()
}

// androidMain - Android implementation using MediaPipe
actual class GemmaInference {
    private var llmInference: LlmInference? = null
    
    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        // MediaPipe handles all the complexity
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(config.maxTokens)
            .build()
            
        llmInference = LlmInference.createFromOptions(context, options)
    }
    
    actual fun generateResponse(prompt: String): Flow<String> = callbackFlow {
        llmInference?.generateResponseAsync(prompt) { token, done ->
            trySend(token)  // Stream each token
            if (done) close()
        }
        awaitClose { }
    }
}
```

**Talking point**: "This is all the code you need to run Gemma. MediaPipe does the heavy lifting."

---

## ❓ Common Questions

**Q: Why MediaPipe instead of TensorFlow Lite directly?**
> A: MediaPipe wraps TFLite with LLM-specific optimizations: tokenization, streaming, memory management for large models. TFLite alone doesn't handle these.

**Q: Can we use other models like Llama?**
> A: MediaPipe supports Gemma, and recently added Llama/Phi support. The architecture we're building is model-agnostic—swap the model file, minimal code changes.

**Q: What happens if the phone runs out of RAM?**
> A: The OS will kill your app. That's why we check memory before loading and provide fallbacks. We'll cover this in production considerations.

**Q: Is GPU always faster?**
> A: Usually, but not always. On some devices, CPU is competitive because of thermal throttling. MediaPipe picks the best backend automatically.

---

## ⚠️ Common Mistakes to Avoid

1. **Forgetting to format prompts**
   ```kotlin
   // ❌ Wrong - raw text
   generateResponse("Hello")
   
   // ✅ Correct - formatted with turn tokens
   generateResponse("<start_of_turn>user\nHello<end_of_turn>\n<start_of_turn>model\n")
   ```

2. **Not closing the inference session**
   ```kotlin
   // ❌ Wrong - memory leak
   override fun onDestroy() {
       super.onDestroy()
       // Forgot to close!
   }
   
   // ✅ Correct
   override fun onDestroy() {
       super.onDestroy()
       gemmaInference.close()
   }
   ```

3. **Blocking the main thread**
   ```kotlin
   // ❌ Wrong - blocks UI
   fun onButtonClick() {
       val response = gemmaInference.generateResponse(prompt) // Suspend!
   }
   
   // ✅ Correct - use coroutines
   fun onButtonClick() {
       viewModelScope.launch(Dispatchers.IO) {
           gemmaInference.generateResponse(prompt).collect { token ->
               // Handle token
           }
       }
   }
   ```

---

## Troubleshooting Tips

| Problem | Symptom | Solution |
|---------|---------|----------|
| Model not found | Exception on load | Check path resolution, use absolute paths |
| Out of memory | App crash on load | Use smaller model, check available RAM |
| Garbage output | Nonsensical responses | Check prompt formatting with turn tokens |
| Slow generation | <5 tokens/sec | Check thermal state, use quantized model |
| Crash on background | SEGFAULT | Close inference in onCleared() |

---

## 🎤 Live Demo Flow

### Demo 1: Model Loading (5 min)

1. **Show the SettingsScreen model selector**
   > "This shows available models. Let's load one."

2. **Click load and watch the progress**
   > "Notice the progress indicator. This takes 3-8 seconds."

3. **Show Memory Profiler during load**
   > "Watch the memory climb from 100MB to 1.7GB."

4. **Model ready - show status indicator**
   > "Green dot, 'Ready'. Now we can chat."

### Demo 2: Send a Message (5 min)

1. **Type a message and send**
   > "I'll ask: 'What is Kotlin?'"

2. **Watch the streaming response**
   > "See it building word by word. That's token streaming."

3. **Show Logcat output**
   > "Here you can see the formatted prompt and each token arriving."

### Demo 3: Error Handling (3 min)

1. **Try loading a non-existent model**
   > "Let's see what happens with a wrong path..."

2. **Show the error state in UI**
   > "Nice error message, not a crash. Users understand what went wrong."

---

## 📋 Section Summary

### What We Covered

| Topic | Key Takeaway |
|-------|--------------|
| expect/actual pattern | Platform abstraction for shared interface |
| MediaPipe integration | `LlmInference` handles the heavy lifting |
| Model loading | Use `Dispatchers.IO`, track progress |
| Streaming | `callbackFlow` bridges callbacks to Flow |
| Prompt formatting | Turn tokens are essential! |
| Error handling | Graceful degradation, user-friendly messages |
| Memory management | Always call `close()` in `onCleared()` |

### Architecture Checklist

✅ Common interface (`expect class GemmaInference`)  
✅ Platform implementation (`actual class`)  
✅ Async loading with progress  
✅ Streaming response via Flow  
✅ Proper error handling  
✅ Lifecycle-aware cleanup  

---

## ⏱️ Section Timing

| Activity | Duration |
|----------|----------|
| Architecture overview | 10 min |
| GemmaInference walkthrough | 10 min |
| ViewModel walkthrough | 8 min |
| Prompt formatting | 5 min |
| Hands-on exercise | 10 min |
| Common issues | 7 min |
| Live demo | 10 min |
| Q&A | 5 min |
| **Total** | **65 min** |

---

## 📋 Transition to Section 4

> "We now have a working Gemma integration. But there's one thing we glossed over: streaming."
>
> "In the next section, we'll deep-dive into WHY streaming matters for UX, HOW it works under the hood, and advanced patterns for production apps."
>
> "We'll also implement performance metrics so you can measure tokens per second."

---

# SECTION 4: TOKEN STREAMING

## 🎯 Learning Objectives

By the end of this section, attendees will:
- Understand why streaming is essential for AI UX
- Implement real-time token streaming with Kotlin Flow
- Build auto-scrolling chat UI that follows streaming text
- Handle backpressure and cancellation properly
- Implement stop generation functionality

---

## 📚 Why Streaming Matters

### The UX Difference

```
WITHOUT STREAMING (Bad UX)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

User: "Explain Kotlin coroutines"

[Send] ──► [Waiting...] ──► [Waiting...] ──► [5 seconds later]
                 😰                              
              "Is it working?"        "HERE'S A WALL OF TEXT!"

═══════════════════════════════════════════════════════════════

WITH STREAMING (Great UX)                    
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

User: "Explain Kotlin coroutines"

[Send] ──► "Kotlin" ──► "coroutines" ──► "are" ──► "lightweight"
              50ms         50ms           50ms        50ms
                              
              😊 User sees immediate progress!

```

### Key Benefits of Streaming

| Aspect | Without Streaming | With Streaming |
|--------|-------------------|----------------|
| **Perceived latency** | 5-10 seconds | <100ms |
| **User confidence** | "Is it broken?" | "It's working!" |
| **Engagement** | User waits passively | User reads as it appears |
| **Cancellation** | Must wait for completion | Can stop anytime |
| **Memory** | Store full response | Process incrementally |

---

## 🔧 Implementation Deep Dive

### Part 1: The Streaming Flow

**Core Architecture:**

```
┌─────────────────────────────────────────────────────────────┐
│                    STREAMING ARCHITECTURE                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  MediaPipe Callback                   Kotlin Flow           │
│  ┌─────────────────┐                ┌─────────────────┐    │
│  │ generateResponse│                │    callbackFlow │    │
│  │     Async()     │    bridges     │                 │    │
│  │                 │ ─────────────► │   emit tokens   │    │
│  │ callback(token, │                │                 │    │
│  │    done)        │                │   Flow<String>  │    │
│  └─────────────────┘                └─────────────────┘    │
│                                             │               │
│                                             ▼               │
│                                    ┌─────────────────┐     │
│                                    │   ViewModel     │     │
│                                    │   .collect { }  │     │
│                                    └─────────────────┘     │
│                                             │               │
│                                             ▼               │
│                                    ┌─────────────────┐     │
│                                    │   StateFlow     │     │
│                                    │   UI State      │     │
│                                    └─────────────────┘     │
│                                             │               │
│                                             ▼               │
│                                    ┌─────────────────┐     │
│                                    │   Compose UI    │     │
│                                    │   Recomposes    │     │
│                                    └─────────────────┘     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Part 2: callbackFlow Deep Dive

**File: `GemmaInference.android.kt`**

```kotlin
actual fun generateResponse(prompt: String): Flow<String> = callbackFlow {
    val inference = llmInference 
        ?: throw IllegalStateException("Model not loaded")

    try {
        // MediaPipe's async API with callback
        inference.generateResponseAsync(prompt) { partialResult, done ->
            // partialResult = newly generated token(s)
            // done = true when generation is complete
            
            // trySend is non-blocking - won't suspend
            // Returns ChannelResult indicating success/failure
            val result = trySend(partialResult)
            
            if (result.isFailure) {
                // Channel is closed or full - stop generation
                // This handles cancellation gracefully
            }
            
            if (done) {
                close() // Signal Flow completion
            }
        }
    } catch (e: Exception) {
        close(e) // Signal Flow error
    }

    // CRITICAL: Keep Flow alive until MediaPipe signals completion
    // Without this, Flow closes immediately after lambda returns
    awaitClose { 
        // Optional: cleanup when Flow is cancelled
        // Could cancel MediaPipe generation here if supported
    }
}
```

#### 🎤 Workshop Talking Points

> "Let me explain `callbackFlow` - it's how we bridge callback-based APIs to Flow."
>
> **trySend vs send:**
> "`trySend` is non-blocking. In a callback, we can't suspend, so we use `trySend`. If the channel is full or closed, it just returns a failure result."

> **awaitClose:**
> "This is the secret sauce. It suspends the coroutine until the Flow is cancelled or we call `close()`. Without it, the Flow completes immediately."

> **Why Flow, not just callbacks?**
> "Flows integrate with coroutines. We get automatic cancellation, structured concurrency, operators like `map`, `filter`, `catch`, and easy testing."

---

### Part 3: Collecting Streaming Tokens

**File: `GenerateResponseUseCase.kt`**

```kotlin
class GenerateResponseUseCase(
    private val gemmaInference: GemmaInference
) {
    operator fun invoke(
        systemPrompt: String,
        messages: List<ChatMessage>,
        userPrompt: String
    ): Flow<GenerateResponseResult> = flow {
        // ... prompt formatting ...
        
        var fullResponse = ""
        var tokenCount = 0
        val startTime = System.currentTimeMillis()

        gemmaInference.generateResponseWithHistory(systemPrompt, formattedPrompt)
            .collect { token ->
                fullResponse += token
                tokenCount++
                
                // Emit streaming result for UI update
                emit(GenerateResponseResult.Streaming(fullResponse))
            }

        val duration = System.currentTimeMillis() - startTime
        val tokensPerSecond = if (duration > 0) {
            tokenCount * 1000.0 / duration
        } else 0.0
        
        println("⚡ Generated $tokenCount tokens in ${duration}ms")
        println("⚡ Speed: ${"%.1f".format(tokensPerSecond)} tokens/second")

        emit(GenerateResponseResult.Complete(fullResponse, toolCall = null))
        
    }.catch { e ->
        emit(GenerateResponseResult.Error(e as? Exception ?: Exception(e)))
    }
}
```

---

### Part 4: UI Updates with StateFlow

**File: `ChatViewModel.kt`**

```kotlin
private fun generateResponse(prompt: String, history: List<ChatMessage>) {
    viewModelScope.launch(Dispatchers.IO) {
        // 1. Create placeholder message for streaming
        val aiMessage = ChatMessage.ai("", isStreaming = true)
        streamingMessageId = aiMessage.id
        
        _uiState.update { state ->
            state.copy(
                messages = state.messages + aiMessage,
                modelState = ModelState.GENERATING
            )
        }

        // 2. Collect streaming tokens
        generateResponseUseCase(systemPrompt, history, prompt)
            .collect { result ->
                when (result) {
                    is GenerateResponseResult.Streaming -> {
                        // Update the streaming message with new content
                        updateStreamingMessage(result.partialResponse)
                    }
                    is GenerateResponseResult.Complete -> {
                        finishStreaming()
                    }
                    is GenerateResponseResult.Error -> {
                        handleError(result.exception)
                    }
                }
            }
    }
}

private fun updateStreamingMessage(content: String) {
    _uiState.update { state ->
        // Find and update only the streaming message
        val updatedMessages = state.messages.map { msg ->
            if (msg.id == streamingMessageId) {
                msg.copy(content = content)
            } else {
                msg
            }
        }
        state.copy(messages = updatedMessages)
    }
}
```

#### 🎤 Workshop Talking Points

> "Notice the pattern: We create a placeholder message with `isStreaming = true`, then update its content as tokens arrive."

> "The UI sees smooth streaming because each token triggers a state update, which triggers recomposition."

> "We use `msg.id` to identify which message to update. This is more reliable than list indices."

---

### Part 5: Auto-Scrolling Chat

**File: `ChatScreen.kt`**

```kotlin
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel { ChatViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll when messages change OR content updates
    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.content) {
        if (uiState.messages.isNotEmpty()) {
            // Animate scroll to the last message
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // ... rest of UI
}
```

**Advanced: Smooth scrolling during streaming**

```kotlin
// More sophisticated auto-scroll that respects user interaction
LaunchedEffect(uiState.messages) {
    val lastMessage = uiState.messages.lastOrNull()
    
    // Only auto-scroll if:
    // 1. There are messages
    // 2. The last message is streaming (AI is typing)
    // 3. User hasn't scrolled far up (respects manual scrolling)
    if (lastMessage != null && lastMessage.isStreaming) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val lastIndex = uiState.messages.size - 1
        
        // Only scroll if we're near the bottom (within 2 items)
        if (lastIndex - lastVisibleIndex <= 2) {
            listState.animateScrollToItem(lastIndex)
        }
    }
}
```

---

### Part 6: Cancellation Support

**Implementing Stop Generation:**

```kotlin
// In ChatViewModel
private var generationJob: Job? = null

private fun generateResponse(prompt: String, history: List<ChatMessage>) {
    generationJob = viewModelScope.launch(Dispatchers.IO) {
        // ... streaming logic ...
    }
}

fun stopGeneration() {
    generationJob?.cancel()
    generationJob = null
    
    // Mark the current message as complete (not streaming)
    _uiState.update { state ->
        val updatedMessages = state.messages.map { msg ->
            if (msg.id == streamingMessageId) {
                msg.copy(isStreaming = false)
            } else {
                msg
            }
        }
        state.copy(
            messages = updatedMessages,
            modelState = ModelState.READY
        )
    }
    streamingMessageId = null
}
```

**UI for Stop Button:**

```kotlin
// In ChatInputBar
AnimatedVisibility(
    visible = isGenerating,
    enter = fadeIn() + scaleIn(),
    exit = fadeOut() + scaleOut()
) {
    FilledIconButton(
        onClick = onStopGeneration,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Stop generation",
            tint = Color.White
        )
    }
}
```

---

## 🧪 Hands-On Exercise

### Exercise 1: Add Token Counter

**Task:** Display a live token count during streaming.

```kotlin
// Add to ChatUiState
data class ChatUiState(
    // ... existing fields ...
    val currentTokenCount: Int = 0,
    val tokensPerSecond: Double = 0.0
)

// Update in GenerateResponseUseCase or ViewModel
// Display in UI near the message
```

### Exercise 2: Implement Typing Indicator

**Task:** Show a "Gemma is typing..." indicator that pulses.

```kotlin
@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.alpha(alpha)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}
```

### Exercise 3: Smart Auto-Scroll

**Task:** Only auto-scroll if user is near the bottom. If they've scrolled up to read history, don't interrupt them.

---

## 🚨 Common Streaming Issues

### Issue 1: UI Not Updating

**Symptom:** Tokens arrive but UI shows nothing until completion.

**Causes:**
- Collecting on wrong dispatcher
- Not emitting intermediate results
- StateFlow not being observed

**Solution:**
```kotlin
// Ensure UI updates happen on Main dispatcher
viewModelScope.launch(Dispatchers.IO) {
    flow.collect { token ->
        withContext(Dispatchers.Main) {
            updateStreamingMessage(token)
        }
    }
}

// Or let StateFlow handle it (preferred)
// StateFlow updates are automatically thread-safe
```

### Issue 2: Choppy Streaming

**Symptom:** Text appears in bursts, not smoothly.

**Cause:** Too many rapid state updates causing UI jank.

**Solution: Debounce or batch updates**
```kotlin
var pendingContent = ""
var lastUpdateTime = 0L

gemmaInference.generateResponse(prompt).collect { token ->
    pendingContent += token
    val now = System.currentTimeMillis()
    
    // Only update UI every 50ms maximum
    if (now - lastUpdateTime > 50) {
        updateStreamingMessage(pendingContent)
        lastUpdateTime = now
    }
}
// Don't forget final update
updateStreamingMessage(pendingContent)
```

### Issue 3: Memory Growing During Streaming

**Symptom:** Memory increases with each token.

**Cause:** Creating new message objects for every update.

**Solution:** Update in place, don't recreate:
```kotlin
// ❌ Bad - creates new list every time
_uiState.update { state ->
    state.copy(messages = state.messages.map { ... })
}

// ✅ Better - use MutableList internally (carefully)
// Or accept the overhead - it's usually fine for chat apps
```

---

## ⏱️ Section Timing

| Activity | Duration |
|----------|----------|
| Why streaming matters | 5 min |
| callbackFlow explanation | 8 min |
| ViewModel streaming pattern | 7 min |
| Auto-scroll implementation | 5 min |
| Cancellation support | 5 min |
| Hands-on exercise | 10 min |
| Common issues | 5 min |
| **Total** | **45 min** |

---

## 📋 Transition to Section 5

> "Streaming makes our app feel responsive. But right now, every message is independent—Gemma has no memory of earlier conversation."
>
> "In the next section, we'll implement conversation memory so Gemma maintains context across the entire chat."

---

# SECTION 5: CONVERSATION MEMORY

## 🎯 Learning Objectives

By the end of this section, attendees will:
- Understand how LLM context windows work
- Implement conversation history tracking
- Build a prompt builder for multi-turn conversations
- Handle context window limits with trimming strategies
- Implement system prompts for persona customization

---

## 📚 Concept Overview

### The Context Window

LLMs don't have persistent memory. Every generation starts fresh. The "memory" is an illusion created by including conversation history in the prompt.

```
┌─────────────────────────────────────────────────────────────┐
│                    HOW "MEMORY" WORKS                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  What the user thinks:                                      │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ User: "What is Kotlin?"                             │   │
│  │ AI: "Kotlin is a programming language..."          │   │
│  │ User: "Who created it?"  ← AI "remembers" Kotlin   │   │
│  │ AI: "JetBrains created Kotlin in 2011"             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  What actually happens:                                     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ PROMPT FOR SECOND QUESTION:                         │   │
│  │                                                     │   │
│  │ <start_of_turn>user                                 │   │
│  │ What is Kotlin?<end_of_turn>                        │   │
│  │ <start_of_turn>model                                │   │
│  │ Kotlin is a programming language...<end_of_turn>   │   │
│  │ <start_of_turn>user                                 │   │
│  │ Who created it?<end_of_turn>                        │   │
│  │ <start_of_turn>model                                │   │
│  │ ← Generate from here                                │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  The ENTIRE conversation is sent every time!               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Context Window Limits

Every model has a maximum context length (measured in tokens):

| Model | Max Context | Approximate Characters |
|-------|-------------|----------------------|
| Gemma 2B | 8,192 tokens | ~32,000 characters |
| Gemma 7B | 8,192 tokens | ~32,000 characters |
| GPT-4 | 128K tokens | ~500,000 characters |

**When you exceed the limit, bad things happen:**
- Model truncates input (loses early context)
- Generation quality degrades
- Errors or crashes

---

## 🔧 Implementation Deep Dive

### Part 1: Message History Storage

**File: `ChatMessage.kt`**

```kotlin
@OptIn(ExperimentalTime::class)
data class ChatMessage(
    val id: String = generateId(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val tokenCount: Int = 0  // Track tokens for context management
) {
    companion object {
        fun user(content: String) = ChatMessage(
            content = content, 
            isFromUser = true,
            tokenCount = estimateTokens(content)
        )

        fun ai(content: String, isStreaming: Boolean = false) = ChatMessage(
            content = content, 
            isFromUser = false, 
            isStreaming = isStreaming,
            tokenCount = estimateTokens(content)
        )
        
        // Rough token estimation: ~4 characters per token for English
        private fun estimateTokens(text: String): Int = (text.length / 4) + 1
        
        private fun generateId() = "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(10000)}"
    }
}
```

---

### Part 2: Prompt Builder

**File: `PromptBuilder.kt`** (Create this file)

```kotlin
package org.abma.offlinelai_kmp.inference

import org.abma.offlinelai_kmp.domain.model.ChatMessage

/**
 * Builds formatted prompts for Gemma with conversation history.
 */
object PromptBuilder {
    
    private const val MAX_CONTEXT_TOKENS = 7500  // Leave room for response
    private const val TURN_START = "<start_of_turn>"
    private const val TURN_END = "<end_of_turn>"
    
    /**
     * Build a complete prompt with history.
     * 
     * @param systemPrompt Optional system instructions
     * @param messages Conversation history
     * @param currentMessage The new user message
     * @return Formatted prompt string
     */
    fun buildPrompt(
        systemPrompt: String? = null,
        messages: List<ChatMessage>,
        currentMessage: String
    ): String = buildString {
        // 1. System prompt (if provided)
        if (!systemPrompt.isNullOrBlank()) {
            append("${TURN_START}user\n")
            append("System Instructions: $systemPrompt$TURN_END\n")
            append("${TURN_START}model\n")
            append("I understand. I'll follow these instructions.$TURN_END\n")
        }
        
        // 2. Conversation history
        val trimmedMessages = trimToContextLimit(messages)
        for (message in trimmedMessages) {
            val role = if (message.isFromUser) "user" else "model"
            append("$TURN_START$role\n")
            append("${message.content}$TURN_END\n")
        }
        
        // 3. Current user message
        append("${TURN_START}user\n")
        append("$currentMessage$TURN_END\n")
        
        // 4. Start model's turn
        append("${TURN_START}model\n")
    }
    
    /**
     * Trim messages to fit within context window.
     * Strategy: Keep most recent messages, drop oldest first.
     */
    private fun trimToContextLimit(messages: List<ChatMessage>): List<ChatMessage> {
        var totalTokens = 0
        val result = mutableListOf<ChatMessage>()
        
        // Process from newest to oldest
        for (message in messages.reversed()) {
            val messageTokens = message.tokenCount + 20  // +20 for turn tokens
            
            if (totalTokens + messageTokens > MAX_CONTEXT_TOKENS) {
                break  // Stop adding messages
            }
            
            totalTokens += messageTokens
            result.add(0, message)  // Add to front to maintain order
        }
        
        return result
    }
    
    /**
     * Estimate total tokens in a prompt.
     */
    fun estimatePromptTokens(
        systemPrompt: String?,
        messages: List<ChatMessage>,
        currentMessage: String
    ): Int {
        var total = 0
        
        if (!systemPrompt.isNullOrBlank()) {
            total += (systemPrompt.length / 4) + 50  // System + wrapper
        }
        
        total += messages.sumOf { it.tokenCount + 20 }  // Messages + turn tokens
        total += (currentMessage.length / 4) + 20  // Current message
        
        return total
    }
}
```

---

### Part 3: System Prompts

System prompts let you customize the AI's persona and behavior:

```kotlin
// Example system prompts

val DEFAULT_SYSTEM_PROMPT = """
You are a helpful AI assistant running on-device via Gemma. 
You provide concise, accurate responses.
You're friendly but professional.
If you don't know something, admit it rather than making things up.
""".trimIndent()

val CODING_ASSISTANT_PROMPT = """
You are an expert Kotlin developer assistant.
When providing code, always:
- Use Kotlin best practices
- Include comments for complex logic
- Prefer coroutines for async operations
- Follow clean architecture principles
""".trimIndent()

val CREATIVE_WRITER_PROMPT = """
You are a creative writing assistant.
You help with stories, poems, and creative content.
You use vivid language and engaging narratives.
You match the user's preferred style and tone.
""".trimIndent()
```

**Using System Prompts in ViewModel:**

```kotlin
class ChatViewModel : ViewModel() {
    // System prompt can be configurable
    private var systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    
    fun setPersona(persona: Persona) {
        systemPrompt = when (persona) {
            Persona.DEFAULT -> DEFAULT_SYSTEM_PROMPT
            Persona.CODER -> CODING_ASSISTANT_PROMPT
            Persona.CREATIVE -> CREATIVE_WRITER_PROMPT
        }
    }
    
    private fun generateResponse(prompt: String, history: List<ChatMessage>) {
        viewModelScope.launch(Dispatchers.IO) {
            // ... setup ...
            
            val formattedPrompt = PromptBuilder.buildPrompt(
                systemPrompt = systemPrompt,
                messages = history,
                currentMessage = prompt
            )
            
            gemmaInference.generateResponse(formattedPrompt)
                .collect { /* ... */ }
        }
    }
}
```

---

### Part 4: Context Trimming Strategies

When conversations get long, we need to trim intelligently:

```kotlin
/**
 * Different strategies for trimming conversation history.
 */
enum class TrimmingStrategy {
    /** Keep most recent messages. Simple and effective. */
    KEEP_RECENT,
    
    /** Keep first + recent. Maintains initial context. */
    KEEP_FIRST_AND_RECENT,
    
    /** Summarize old messages. Most sophisticated. */
    SUMMARIZE_OLD
}

object ContextTrimmer {
    
    fun trim(
        messages: List<ChatMessage>,
        maxTokens: Int,
        strategy: TrimmingStrategy
    ): List<ChatMessage> {
        return when (strategy) {
            TrimmingStrategy.KEEP_RECENT -> keepRecent(messages, maxTokens)
            TrimmingStrategy.KEEP_FIRST_AND_RECENT -> keepFirstAndRecent(messages, maxTokens)
            TrimmingStrategy.SUMMARIZE_OLD -> summarizeOld(messages, maxTokens)
        }
    }
    
    private fun keepRecent(messages: List<ChatMessage>, maxTokens: Int): List<ChatMessage> {
        var tokens = 0
        return messages.reversed().takeWhile { msg ->
            tokens += msg.tokenCount + 20
            tokens < maxTokens
        }.reversed()
    }
    
    private fun keepFirstAndRecent(messages: List<ChatMessage>, maxTokens: Int): List<ChatMessage> {
        if (messages.size <= 4) return messages
        
        // Always keep first 2 messages (establish context)
        val first = messages.take(2)
        val firstTokens = first.sumOf { it.tokenCount + 20 }
        
        // Fill remaining space with recent messages
        val remainingTokens = maxTokens - firstTokens
        val recent = keepRecent(messages.drop(2), remainingTokens)
        
        return first + recent
    }
    
    private fun summarizeOld(messages: List<ChatMessage>, maxTokens: Int): List<ChatMessage> {
        // Advanced: Could use Gemma to summarize old messages
        // For simplicity, fall back to keepRecent
        // In production, you might generate a summary message
        return keepRecent(messages, maxTokens)
    }
}
```

---

### Part 5: Updating GenerateResponseUseCase

**Modified `GenerateResponseUseCase.kt`:**

```kotlin
class GenerateResponseUseCase(
    private val gemmaInference: GemmaInference
) {
    operator fun invoke(
        systemPrompt: String,
        messages: List<ChatMessage>,
        userPrompt: String
    ): Flow<GenerateResponseResult> = flow {
        
        // Use PromptBuilder for proper formatting
        val formattedPrompt = PromptBuilder.buildPrompt(
            systemPrompt = systemPrompt,
            messages = messages,
            currentMessage = userPrompt
        )
        
        // Log for debugging
        val estimatedTokens = PromptBuilder.estimatePromptTokens(
            systemPrompt, messages, userPrompt
        )
        println("📊 Prompt stats:")
        println("   • Messages in context: ${messages.size}")
        println("   • Estimated tokens: $estimatedTokens")
        println("   • Prompt length: ${formattedPrompt.length} chars")
        
        var fullResponse = ""
        
        gemmaInference.generateResponseWithHistory(systemPrompt, formattedPrompt)
            .collect { token ->
                fullResponse += token
                emit(GenerateResponseResult.Streaming(fullResponse))
            }

        emit(GenerateResponseResult.Complete(fullResponse, toolCall = null))
        
    }.catch { e ->
        emit(GenerateResponseResult.Error(e as? Exception ?: Exception(e)))
    }
}
```

---

## 🎤 Workshop Talking Points

### On Context Windows

> "Here's the key insight: LLMs have no memory. Every single generation, we send the ENTIRE conversation,
