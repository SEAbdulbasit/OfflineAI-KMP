# 🎬 Demo Script - Sections 1 & 2

## Pre-Demo Setup

### 30 Minutes Before Workshop

1. **Device preparation**
   ```
   - Charge device to 80%+
   - Clear recent apps
   - Turn OFF battery saver
   - Set display timeout to 10 minutes
   - Enable developer options
   - Enable USB debugging
   ```

2. **App preparation**
   ```
   - Install latest debug build
   - Verify model is pre-loaded
   - Test one inference to ensure it works
   - Clear chat history
   ```

3. **Screen sharing setup**
   ```
   - Install scrcpy or Vysor for phone mirroring
   - Test screen share works
   - Adjust resolution if needed (1080p recommended)
   ```

4. **Android Studio setup**
   ```
   - Open project
   - Open ChatViewModel.kt
   - Open GemmaInference.android.kt
   - Open Memory Profiler (but don't attach yet)
   ```

---

## DEMO 1: Airplane Mode Challenge

**When**: Section 1, after explaining why offline AI matters  
**Duration**: 5 minutes  
**Goal**: WOW factor - show that AI works without internet

### Script

**[Start screen sharing your phone]**

> "Let me show you this in action."

**[Open Settings → Airplane mode → Turn ON]**

> "Airplane mode is now ON. You can see the icon there."

**[Open the OfflineAI app]**

> "This is our app. Runs on Compose Multiplatform."

**[Point to the loaded model indicator]**

> "The model is already loaded - see here? Gemma 2B, ready to go."

**[Type in the message field]**

```
Type: "Write a haiku about programming"
```

> "Let me ask it something creative..."

**[Tap send]**

> "Watch this space..."

**[The response streams in]**

> "See that? Building character by character. That's token streaming."
> "No internet. No API call. Just silicon and math."

**[Wait for completion]**

> "And there we have it. A complete response, generated entirely on this device."

**[Pause dramatically]**

> "THIS is what we're building today."

### Backup Plan

If demo fails:
- Have a screen recording ready as backup
- Gracefully say "Sometimes live demos have their own plans - let me show you a recording"

---

## DEMO 2: Response Quality

**When**: Section 1, during Q&A if asked about quality  
**Duration**: 2 minutes  
**Goal**: Show that responses are useful and coherent

### Script

> "Someone asked about quality. Let me show you."

**[Type a technical question]**

```
Type: "Explain the difference between val and var in Kotlin"
```

> "Let's ask a technical question we can verify."

**[Send and wait for response]**

> "See? It correctly identifies that val is read-only and var is mutable."
> "Is it GPT-4 level? No. Is it useful? Absolutely."

---

## DEMO 3: Memory Visualization

**When**: Section 2, during lifecycle discussion  
**Duration**: 5 minutes  
**Goal**: Show real memory usage and lifecycle states

### Setup Before Demo

1. In Android Studio:
   - Open Profiler tab (View → Tool Windows → Profiler)
   - Connect your device
   - Don't start profiling yet

2. On device:
   - App installed
   - Model NOT loaded (or close and restart app)

### Script

**[Switch to Android Studio - Profiler view]**

> "Let me show you what happens under the hood."

**[Start profiling the app]**

> "I'm connecting the Android Studio Profiler to our app."

**[Point to memory graph]**

> "Right now, the app is using about 100 megabytes. Normal Android app."

**[On device: Tap load model]**

> "Now I'm loading the model. Watch this memory graph..."

**[Memory climbs]**

> "See it climbing? 300 megabytes... 600... 900... 1.2 gigabytes..."

**[Memory stabilizes around 1.7GB]**

> "And it stabilizes around 1.7 gigabytes. The entire Gemma model is now in RAM."

**[Point out the flat line]**

> "Notice it's flat now. The model is loaded and waiting. Low CPU, stable memory."

**[On device: Send a message]**

> "Now let's generate a response..."

**[Point to CPU spike]**

> "See that CPU spike? The GPU is working now, generating tokens."

**[Point to memory staying flat]**

> "But memory stays flat. Generation doesn't need more RAM."

**[Wait for generation to complete]**

**[On device: Close the inference session or background the app]**

> "Now I'll close the model..."

**[Memory drops]**

> "And watch the memory drop. Back to around 100 megabytes."
> "This is proper cleanup. If you don't call close(), you have a 1.7 gigabyte memory leak."

---

## DEMO 4: Token Timing (Optional)

**When**: Section 2, if time permits  
**Duration**: 3 minutes  
**Goal**: Show tokens/second in real-time

### Option A: Add Debug Output

If you've added debug logging:

**[Show Logcat in Android Studio]**

```
D/GemmaInference: Token generated in 48ms: "Kotlin"
D/GemmaInference: Token generated in 52ms: "is"
D/GemmaInference: Token generated in 47ms: "a"
```

> "Each token takes about 50 milliseconds. That's roughly 20 tokens per second."

### Option B: Eyeball It

> "Watch the response building. Count with me... one-potato, two-potato..."
> "In that second, we generated about 15-20 tokens."
> "That's our throughput on this device."

---

## Demo Emergency Kit

### If Model Won't Load

```
Say: "Looks like we're having a loading issue. This actually happens
in production sometimes - let me show you the error handling we'll build."
```

Show the error state in the UI if you've implemented it.

### If Response is Garbage

```
Say: "Interesting! This is actually demonstrating what happens when
prompt formatting is wrong. Let me fix that..."
```

### If Device Overheats

```
Say: "And this is a perfect example of thermal throttling - the device
is protecting itself. We'll discuss this in the performance section."
```

### If App Crashes

```
Say: "This is why we'll spend time on proper lifecycle management
and error handling. Let me restart..."
```

---

## Demo Device Recommendations

### Ideal Demo Devices

| Device | Why |
|--------|-----|
| Pixel 6 or newer | Fast, stock Android, reliable |
| Samsung Galaxy S21+ | Popular, good performance |
| OnePlus 9+ | Fast, clean UI |

### Avoid

- Devices with <4GB RAM
- Older budget phones
- Devices with heavy manufacturer skins (more variables)

---

## Screen Mirroring Options

### scrcpy (Recommended - Free)

```bash
# Install
brew install scrcpy

# Run
scrcpy
```

### Vysor

- Chrome extension
- Easy setup
- Free tier has watermark

### Android Studio Built-in

- Running Devices window
- Can mirror without extra software
- May have lag

---

## Pre-Recorded Backup Videos

Create these backups just in case:

1. **airplane_demo.mp4** (30 sec)
   - Turn on airplane mode
   - Send message
   - Receive streaming response

2. **memory_profiler.mp4** (60 sec)
   - Load model, show memory climb
   - Generate response, show CPU spike
   - Close model, show memory drop

3. **comparison.mp4** (30 sec)
   - Side-by-side: cloud AI with delay vs local AI immediate

---

## Post-Demo Transition

After completing Section 1-2 demos:

> "Now you've seen it work. You understand the architecture. You know what's happening under the hood."

> "Time to build it yourself. Open Android Studio, navigate to the inference package, and let's start coding Section 3."

---

**Remember**: Demo failures happen! Have backups ready, stay calm, and turn issues into teaching moments. 🎬

