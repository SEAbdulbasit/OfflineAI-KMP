---
marp: true
theme: default
paginate: true
backgroundColor: #1a1a2e
color: #eaeaea
---

<!-- 
This is a Marp-compatible slideshow. 
You can convert this to a presentation using:
  npx @marp-team/marp-cli SLIDES.md -o presentation.pdf
  
Or use VS Code with Marp extension.
Or copy content to your preferred slide tool.
-->

# 🤖 Building Offline AI Apps
## Gemma + Kotlin Multiplatform

**Your AI. Your Device. No Internet Required.**

---

# 👋 Welcome!

Today you'll learn to:

- Run **Gemma LLM** completely offline on mobile
- Integrate **MediaPipe** for on-device inference
- Build **streaming responses** in real-time
- Implement **conversation memory**
- Apply **production patterns** for offline AI

---

# ✈️ Quick Challenge

**Turn on airplane mode.**

*Now open ChatGPT or Claude.*

**Nothing works, right?**

---

# 🎯 By End of Today...

That same phone in airplane mode will run a fully functional AI assistant.

**No internet. No API keys. No cloud bills.**

---

# Section 1
# Introduction to Offline AI

---

# What is Offline AI?

**Traditional Cloud AI:**
```
📱 → 🌐 Internet → ☁️ Server → 🌐 → 📱
     Your data leaves your device
```

**On-Device AI:**
```
📱 [Your App + 🧠 Model]
    Data never leaves
```

---

# Why Does This Matter?

| Aspect | Cloud AI | On-Device AI |
|--------|----------|--------------|
| **Privacy** | Data sent to servers | Data stays on device |
| **Latency** | 200-2000ms | ~50ms to first token |
| **Cost** | Pay per request | Free forever |
| **Availability** | Needs internet | Works anywhere |

---

# 🔒 Privacy: The Game Changer

Perfect for:
- 🏥 Healthcare apps (HIPAA)
- ⚖️ Legal documents
- 💰 Financial data
- 📝 Personal journals
- 🔐 Enterprise secrets

**Your data. Your device. Period.**

---

# ⚡ Latency = User Experience

Cloud AI minimum floor:
```
~300ms network round-trip
Often 1-2 seconds total
```

On-device:
```
~50ms to first token
User perceives "instant"
```

---

# 💰 The Cost Reality

Simple math for 1M users:

```
1,000,000 users
× 10 requests/day
× $0.002/request
= $20,000/day
= $600,000/month
```

On-device: **$0**

---

# 🌍 Works Everywhere

- ✈️ Airplane mode
- 🚇 Subway tunnels
- 🏔️ Remote hiking
- 🌐 Low connectivity regions

**AI that goes where you go.**

---

# 🎬 LIVE DEMO

*[Turn on airplane mode]*

*[Generate AI response]*

**Pure on-device magic ✨**

---

# Section 2
# Gemma + MediaPipe Architecture

---

# Meet Gemma

Google's open-source LLM family optimized for mobile.

| Model | Parameters | Size (INT4) | RAM Needed |
|-------|------------|-------------|------------|
| Gemma 2B | 2 billion | 1.4 GB | 4 GB |
| Gemma 7B | 7 billion | 4.5 GB | 10 GB |

**We use Gemma 2B for mobile.**

---

# 🎯 What is Quantization?

Original: `3.141592653589793` (32-bit)
Quantized: `3.14` (4-bit)

**Result:**
- 6x smaller model
- 4x faster inference
- ~95% quality retained

**This is the magic enabling mobile AI.**

---

# The Inference Pipeline

```
User Input: "What is Kotlin?"
        ↓
┌─────────────────────────┐
│   Prompt Formatting     │
│   + turn tokens         │
└─────────────────────────┘
        ↓
┌─────────────────────────┐
│     Tokenization        │
│   "Hello" → [17534]     │
└─────────────────────────┘
        ↓
┌─────────────────────────┐
│   Model Inference       │
│   One token at a time   │
└─────────────────────────┘
        ↓
    Token Stream → UI
```

---

# ⚠️ Prompt Formatting is Critical

```
✅ Correct:
<start_of_turn>user
What is Kotlin?<end_of_turn>
<start_of_turn>model

❌ Wrong (will produce garbage):
What is Kotlin?
```

---

# Tokenization

Text → Numbers the model understands.

```
"What is Kotlin?"
    ↓
[1841, 603, 146583, 235336]
  │     │      │       │
 What  is   Kotlin     ?
```

---

# Autoregressive Generation

The model generates **ONE token at a time**.

```
Step 1: "What is Kotlin?" → "Kotlin"
Step 2: "...Kotlin" → "is"  
Step 3: "...Kotlin is" → "a"
Step 4: "...Kotlin is a" → "modern"
... repeat until done
```

---

# Token Streaming = Great UX

Don't wait for full response!

```
Time 0ms:   "K"
Time 50ms:  "Kotlin"
Time 100ms: "Kotlin is"
Time 150ms: "Kotlin is a"
...
```

**Users see progress immediately.**

---

# MediaPipe: Your Best Friend

MediaPipe handles:
- ✅ Model loading
- ✅ Tokenization
- ✅ GPU acceleration
- ✅ Streaming callbacks
- ✅ Memory management
- ✅ Platform optimization

**You write minimal code.**

---

# Our Code = Simple

```kotlin
val options = LlmInferenceOptions.builder()
    .setModelPath(path)
    .setMaxTokens(2048)
    .build()
    
llmInference = LlmInference.createFromOptions(ctx, opts)

llmInference.generateResponseAsync(prompt) { token, done ->
    emit(token)
}
```

**That's it.**

---

# Runtime Lifecycle

```
┌──────────┐      ┌──────────┐      ┌──────────┐
│ LOADING  │  →   │  READY   │  ↔   │GENERATING│
│  3-8 sec │      │  Idle    │      │ High CPU │
└──────────┘      └──────────┘      └──────────┘
                       │
                       ↓
                 ┌──────────┐
                 │  CLOSED  │
                 │ Memory   │
                 │ released │
                 └──────────┘
```

---

# ⚠️ Critical: Cleanup

```kotlin
override fun onCleared() {
    super.onCleared()
    gemmaInference.close()  // ← REQUIRED!
}
```

**Forget this = 1.7GB memory leak.**

---

# Memory Reality Check

| State | Memory Usage |
|-------|-------------|
| App idle | ~100 MB |
| Model loaded | ~1.7 GB |
| Generating | ~1.7 GB (stable) |
| After close() | ~100 MB |

---

# 🎬 LIVE DEMO

*[Show Android Studio Profiler]*

Watch memory during:
1. Model load
2. Generation  
3. Cleanup

---

# Quick Reference

```
MODEL LOADING
─────────────────────────────────
LlmInference.createFromOptions(ctx, opts)

PROMPT FORMAT
─────────────────────────────────  
<start_of_turn>user
message<end_of_turn>
<start_of_turn>model

STREAMING
─────────────────────────────────
generateResponseAsync(prompt) { token, done ->
    ...
}

CLEANUP
─────────────────────────────────
llmInference.close()
```

---

# 🎉 Section 1-2 Complete!

You now understand:
- ✅ Why offline AI matters
- ✅ How Gemma works
- ✅ The inference pipeline
- ✅ MediaPipe's role
- ✅ Runtime lifecycle

---

# Next: Let's Code! 💻

**Section 3: Gemma Integration**

Time to implement this ourselves.

*Open Android Studio...*

---

<!-- End of Sections 1-2 slides -->

