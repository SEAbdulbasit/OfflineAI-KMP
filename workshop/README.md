# 📚 Workshop Materials

Welcome to the **Building Offline AI Apps with Gemma + Kotlin Multiplatform** workshop!

## 📁 Contents

### Workshop Documentation

| File | Description | Audience |
|------|-------------|----------|
| [WORKSHOP.md](./WORKSHOP.md) | Sections 1-5: Introduction through Conversation Memory | Instructor + Attendees |
| [WORKSHOP_PART2.md](./WORKSHOP_PART2.md) | Sections 6-9: Performance, Advanced, Production, Final Demo | Instructor + Attendees |
| [SPEAKER_NOTES.md](./SPEAKER_NOTES.md) | Detailed speaking notes for presenters | Instructor |
| [ATTENDEE_GUIDE.md](./ATTENDEE_GUIDE.md) | Quick reference guide for attendees | Attendees |
| [DEMO_SCRIPT.md](./DEMO_SCRIPT.md) | Step-by-step demo instructions | Instructor |
| [SLIDES.md](./SLIDES.md) | Marp-compatible presentation slides | Instructor |

### Code Files Added for Workshop

| File | Purpose |
|------|---------|
| `inference/PromptBuilder.kt` | Prompt formatting & context management |
| `inference/MetricsCollector.kt` | Performance metrics tracking |
| `domain/model/PerformanceMetrics.kt` | Metrics data classes |

### Code Navigation

| File | Description |
|------|-------------|
| [CODE_GUIDE.md](./CODE_GUIDE.md) | Maps workshop sections to code files, architecture patterns |

## 🚀 Quick Start

### For Instructors

1. Read through `WORKSHOP.md` completely
2. Review `SPEAKER_NOTES.md` for timing and talking points
3. Practice demos using `DEMO_SCRIPT.md`
4. Convert `SLIDES.md` to presentation format:
   ```bash
   npx @marp-team/marp-cli SLIDES.md -o presentation.pdf
   ```

### For Attendees

1. Start with `ATTENDEE_GUIDE.md` for setup
2. Follow along with `WORKSHOP.md` during the session
3. Use the Quick Reference sections for coding help

## 📅 Workshop Structure

| Section | Topic | Duration |
|---------|-------|----------|
| 1 | Introduction to Offline AI | 25 min |
| 2 | Gemma + MediaPipe Architecture | 30 min |
| 3 | Gemma Integration | 45 min |
| 4 | Token Streaming | 30 min |
| 5 | Conversation Memory | 30 min |
| 6 | Performance + Optimization | 25 min |
| 7 | Advanced Offline Pipeline | 30 min |
| 8 | Production Considerations | 20 min |
| 9 | Final Demo | 25 min |

**Total: ~4-4.5 hours** (with breaks)

## 📦 Required Materials

### Pre-Workshop Downloads

- [ ] Gemma 2B INT4 model (~1.4GB): [Kaggle](https://www.kaggle.com/models/google/gemma) or [HuggingFace](https://huggingface.co/google/gemma-2b-it)
- [ ] Android Studio Ladybug+
- [ ] JDK 17+

### Device Requirements

| Platform | Minimum | Recommended |
|----------|---------|-------------|
| Android | 4GB RAM, API 24 | 6GB+ RAM, API 30+ |
| iOS | iPhone 11 | iPhone 12+ |

## 🎯 Learning Outcomes

By the end of this workshop, attendees will be able to:

1. ✅ Run Gemma LLM completely offline on mobile devices
2. ✅ Integrate MediaPipe LLM Inference API
3. ✅ Build streaming responses with Flow/Coroutines
4. ✅ Implement conversation memory and context management
5. ✅ Measure and optimize inference performance
6. ✅ Handle production concerns (lifecycle, memory, errors)
7. ✅ Build impressive demo features

## 🔧 Troubleshooting

### Common Setup Issues

| Issue | Solution |
|-------|----------|
| Gradle sync fails | Check JDK version, update Android Studio |
| Model not found | Verify file path, check file exists |
| Out of memory | Close apps, use smaller model |
| Build fails on iOS | Run `pod install` in iosApp folder |

### During Workshop

- Instructor: Have backup screen recordings ready
- Attendees: Use `ATTENDEE_GUIDE.md` troubleshooting section

## 📝 Feedback

After the workshop, please provide feedback to help improve future sessions!

---

**Happy Building! 🤖**

*Made with ❤️ for the Kotlin community*

