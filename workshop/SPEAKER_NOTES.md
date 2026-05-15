# 🎤 Speaker Notes - Sections 1 & 2

## Pre-Workshop Checklist

Before starting:
- [ ] Model pre-loaded on demo device
- [ ] Android Studio open with project
- [ ] Device in airplane mode (for dramatic effect)
- [ ] Profiler ready to show memory
- [ ] Slides loaded
- [ ] Water nearby (you'll be talking a lot!)

---

# SECTION 1: INTRODUCTION (25 minutes)

## Opening Hook (2 min)

**[Slide: Title]**

> "Good morning/afternoon! I want to start with something different. Everyone, take out your phones and turn on airplane mode."

*Wait 10-15 seconds*

> "Now try to open ChatGPT or Claude. Go ahead."

*Wait for reactions*

> "Nothing works, right? That AI assistant everyone relies on is completely useless without WiFi."

*Pause for effect*

> "By the end of today, you'll build an AI assistant that works PERFECTLY in airplane mode. No internet. No API keys. No monthly bills. Just you and Gemma running entirely on-device."

**[Switch to next slide]**

---

## What is Offline AI? (5 min)

**[Slide: Cloud AI vs On-Device AI diagram]**

> "Let's talk about the fundamental shift we're making here."

*Point to Cloud AI section*

> "This is how most AI apps work today. When you type a message in ChatGPT, that message travels across the internet to a server farm probably in Virginia, gets processed on a massive GPU cluster, and the response comes back."

> "Your data leaves your device. Always."

*Point to On-Device AI section*

> "Here's what we're building: The entire AI model—every weight, every parameter—lives on your phone. When you type a message, it never leaves your device. The processing happens right there in your hand."

**[Pause for emphasis]**

> "This is not a new concept—your keyboard autocomplete does this. Siri has done this for simple commands. But running a REAL language model locally? That's new. That's what frameworks like MediaPipe have made possible."

---

## Why Should You Care? (8 min)

### Privacy (3 min)

**[Slide: Privacy comparison]**

> "Let's talk about why this matters, starting with privacy."

> "Think about the apps you're building or want to build:"
> - "Healthcare apps that discuss medical conditions"
> - "Financial advisors that handle sensitive data"  
> - "Personal journals or therapy apps"
> - "Enterprise apps with proprietary information"

> "With cloud AI, you're trusting a third party with all that data. Yes, there are privacy policies. Yes, they say they don't train on your data. But the data DID leave the device. It DID hit their servers. There ARE logs somewhere."

> "With on-device AI, there's nothing to trust. The data physically cannot leave. It's not a policy, it's physics."

### Latency (2 min)

**[Slide: Latency comparison]**

> "Second: Latency. And I don't just mean speed—I mean user experience."

> "Cloud AI has a fundamental floor of about 200-300ms just for the network round trip. Often it's 1-2 seconds before you see the first token."

> "On-device? First token in about 50-100ms. Users start seeing responses almost immediately."

> "When you're building conversational interfaces, this matters enormously. The difference between 'instant' and 'laggy' is often just 200 milliseconds."

### Cost (2 min)

**[Slide: Cost comparison]**

> "Third: Let's talk money."

*Show calculation on slide*

> "Simple math: 1 million users, 10 requests per day, at $0.002 per request. That's $20,000 per day. $600,000 per month. Just in API costs."

> "On-device? Zero. Nothing. The model download is free from Kaggle. Your inference is free. Forever."

> "For indie developers, this is the difference between 'sustainable hobby project' and 'I need VC funding to cover my API bills.'"

### Availability (1 min)

> "And finally: It just works. Everywhere. Airplane mode. Subway tunnels. Remote areas. Developing countries with spotty internet."

---

## Live Demo (5 min)

**[Put phone on display/screen share]**

> "Let me show you what we're building."

*Show airplane mode is ON*

> "Device is in airplane mode. You can see it right there."

*Open the app*

> "This is a Compose Multiplatform app. Same codebase runs on Android and iOS."

*Type a message*

> "Let me ask it something: 'What is Kotlin?'"

*Send and watch it stream*

> "Watch this space... See how it's building the response word by word? That's token streaming. No internet needed."

*Wait for complete response*

> "And there we go. A complete, coherent response generated entirely on this device."

**[Pause]**

> "THIS is what you're building today."

---

## Q&A Break (5 min)

> "Before we dive into the architecture, any questions about what we're trying to achieve?"

**Likely questions and answers:**

**Q: "What about model size?"**
> "Great question. The Gemma 2B model we're using is about 1.4GB after quantization. It's a one-time download. Think of it like downloading a game—users do it once, then it's there forever."

**Q: "What devices can run this?"**  
> "Any Android phone from roughly 2018 onwards with 4GB of RAM. iPhone 11 and newer for iOS. Most phones in the market today can handle it."

**Q: "Is the quality as good as ChatGPT?"**
> "Honest answer: No. These are smaller models optimized for size. For complex reasoning tasks, cloud models are still better. But for conversation, Q&A, summarization, text generation—they're remarkably good. And improving rapidly."

---

## Transition

> "Now that you understand WHY we're building this, let's talk about HOW. In the next section, we'll open the hood and look at the Gemma and MediaPipe architecture."

---

# SECTION 2: ARCHITECTURE (30 minutes)

## Opening (1 min)

**[Slide: Section 2 Title]**

> "Let's look under the hood. My goal here is to give you a mental model of what's happening when we run Gemma."

> "I'm NOT going to teach you transformer architecture or attention mechanisms. That's a research course. We're engineers—we care about how to USE this effectively."

---

## Gemma Overview (3 min)

**[Slide: Gemma Model Family]**

> "Gemma is Google's open-source LLM family. Think of it as the little sibling of the models powering Bard/Gemini."

> "The key number to know: 2 billion parameters. That's Gemma 2B."

> "What's a parameter? Think of it as a 'knob' the model learned during training. 2 billion knobs, all tuned to understand and generate language."

> "Why 2B? Because it's the sweet spot for mobile. Small enough to fit in phone RAM, capable enough to be genuinely useful."

---

## Quantization (3 min)

**[Slide: Quantization diagram]**

> "Here's the magic that makes mobile AI possible: quantization."

> "Normally, each of those 2 billion parameters is stored as a 32-bit floating point number. That's 4 bytes per parameter, times 2 billion, equals 8 gigabytes. Way too big."

*Point to "before and after" on slide*

> "Quantization is basically saying: 'We don't need 32 bits of precision for each number. Let's use 4 bits instead.'"

> "3.141592653589793 becomes 3.14. We lose some precision, but..."

*Emphasize*

> "...the model goes from 8GB to 1.4GB. It runs 4x faster. And the quality drop is only about 5%. Your users won't notice."

> "This is one of the biggest breakthroughs enabling on-device AI. Without quantization, we'd need server hardware."

---

## Inference Pipeline (7 min)

**[Slide: Pipeline diagram - walk through each box]**

> "Now let's trace what happens when a user sends a message. This is the core mental model you need."

### Step 1: Prompt Formatting

> "User types 'What is Kotlin?' We can't send that directly to the model."

> "Gemma was trained with special tokens marking who's speaking. We have to wrap the message."

*Show format on slide*

```
<start_of_turn>user
What is Kotlin?<end_of_turn>
<start_of_turn>model
```

> "If you skip this formatting, you'll get garbage output. This is the #1 mistake people make."

### Step 2: Tokenization

> "Next, we convert text to numbers. The model doesn't understand text—it only sees integers."

> "'What is Kotlin?' becomes [1841, 603, 146583, 235336]."

> "This mapping is defined by the tokenizer, which is baked into MediaPipe. We don't need to worry about it."

### Step 3: Model Inference

> "Here's where the actual AI happens. Those token IDs go into the neural network."

> "The model doesn't generate the whole response at once. It predicts ONE token at a time."

> "Given 'What is Kotlin' as input, it predicts the probability distribution for the next token. Maybe 'Kotlin' has 0.8 probability, 'is' has 0.05."

> "We pick the highest probability token—'Kotlin'."

### Step 4: Autoregressive Loop

> "Now we add 'Kotlin' to the input and repeat. Now given 'What is Kotlin? Kotlin', predict next. We get 'is'."

> "Repeat this hundreds or thousands of times until we see a stop token."

> "This is why generation is sequential. Each token depends on all previous tokens. We can't parallelize this loop."

### Step 5: Streaming

> "Here's the key UX insight: We don't wait for the full response."

> "Every time we generate a token, we emit it immediately to the UI. The user sees the response building character by character."

> "This perception of speed is crucial. Even if generation takes 10 seconds, users feel responsive because they see progress immediately."

---

## MediaPipe's Role (3 min)

**[Slide: MediaPipe architecture]**

> "Now, you might be thinking: 'This sounds complex. Do I have to implement all this?'"

> "Absolutely not. This is where MediaPipe comes in."

*List what MediaPipe handles*

> "MediaPipe is Google's ML framework for mobile. For LLMs, it handles:"
> - "Model loading and memory management"
> - "Tokenization"  
> - "GPU acceleration where available"
> - "CPU fallback"
> - "Streaming callbacks"
> - "Platform-specific optimizations"

> "Our code is literally: load model, send prompt, receive tokens."

*Show code snippet*

```kotlin
val llmInference = LlmInference.createFromOptions(context, options)
llmInference.generateResponseAsync(prompt) { token, done ->
    emit(token)
}
```

> "That's it. Everything else is handled for us."

---

## Runtime Lifecycle (4 min)

**[Slide: State diagram]**

> "Understanding the lifecycle is critical for production apps."

### Loading State

> "First: Loading. When we load the model, we're reading 1.4GB from disk into memory and initializing the inference engine."

> "This takes 3-8 seconds depending on the device. It's a cold start cost."

> "Strategy: Load the model early. Maybe when the app starts, maybe when the user navigates to the chat screen. Don't wait until they click 'send'."

### Ready State

> "Once loaded, the model sits in RAM ready to go. CPU usage is near zero while idle."

> "But—and this is important—it's using about 1.7GB of RAM just sitting there. Keep this in mind."

### Generating State

> "During inference, CPU and/or GPU are maxed out. The device gets warm. Battery drains faster."

> "Typical speed: 10-30 tokens per second on modern phones."

### Cleanup

> "Finally: When you're done or the app backgrounds, you MUST close the inference session."

> "If you don't, you have a 1.7GB memory leak. The OS will eventually kill your app."

```kotlin
override fun onCleared() {
    gemmaInference.close()  // Critical!
}
```

---

## Demo: Memory Visualization (5 min)

**[Open Android Studio Profiler during demo]**

> "Let me show you this lifecycle in action."

*Show memory profiler*

> "Right now the app is at about 100MB. Let me load the model."

*Load model*

> "Watch the memory graph... See it climbing? 500MB... 800MB... 1.2GB... 1.5GB..."

> "Now it's stable around 1.7GB. The model is loaded."

*Generate a response*

> "Now I'll send a message. Watch the CPU graph..."

> "See the spike? GPU is working. And watch the memory—it stays flat. No memory growth during generation."

*Close inference*

> "Now I'll close the session..."

> "Memory drops back down. This is proper cleanup."

---

## Q&A (5 min)

> "Questions on the architecture before we start coding?"

**Likely questions:**

**Q: "Can I use Llama instead of Gemma?"**
> "MediaPipe recently added support for other models. The patterns we learn today are model-agnostic."

**Q: "What if the phone doesn't have enough RAM?"**
> "We check available memory before loading. If there's not enough, we show an error and suggest using a smaller model or freeing memory."

**Q: "Is GPU always used?"**
> "Not always. MediaPipe picks the best backend automatically. On some devices, CPU is actually competitive because of thermal throttling on the GPU."

---

## Transition to Section 3

> "Now you have the mental model. You understand prompts, tokens, streaming, and the lifecycle."

> "Time to get hands dirty. In the next section, we'll implement Gemma integration step by step. Open your IDEs!"

---

# TIMING SUMMARY

| Section | Subsection | Time | Running Total |
|---------|------------|------|---------------|
| **1** | Opening hook | 2 min | 2 min |
| | What is offline AI | 5 min | 7 min |
| | Why it matters | 8 min | 15 min |
| | Live demo | 5 min | 20 min |
| | Q&A | 5 min | 25 min |
| **2** | Gemma overview | 3 min | 28 min |
| | Quantization | 3 min | 31 min |
| | Pipeline walkthrough | 7 min | 38 min |
| | MediaPipe role | 3 min | 41 min |
| | Lifecycle | 4 min | 45 min |
| | Demo | 5 min | 50 min |
| | Q&A | 5 min | 55 min |

**Total: ~55 minutes for Sections 1-2**

---

# EMERGENCY TALKING POINTS

If you're running short on time:
- Skip the memory profiler demo, just explain verbally
- Combine the two Q&A sessions into one at the end
- Shorten the "why it matters" section

If you have extra time:
- Deep dive into quantization types (INT4 vs INT8)
- Show different model sizes and their tradeoffs
- Take more questions
- Let attendees try loading the model themselves

---

# COMMON MISTAKES TO WATCH FOR

When attendees start coding, watch for:

1. **Not formatting prompts** - Most common! Results in gibberish output
2. **Blocking main thread** - Must use coroutines
3. **Not closing inference** - Memory leaks
4. **Wrong model path** - Check file exists before loading

---

**Good luck! You've got this! 🚀**

