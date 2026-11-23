package org.abma.offlinelai_kmp.demo

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

data class DemoTiming(val firstChunkMs: Long?, val totalMs: Long)
data class DemoUiState(
    val kind: DemoKind = DemoKind.NOTES,
    val notes: String = DemoSamples.notes,
    val question: String = DemoSamples.questions.first(),
    val running: Boolean = false,
    val rawResponse: String = "",
    val items: List<ActionItem>? = null,
    val answer: FaqAnswer? = null,
    val timing: DemoTiming? = null,
    val error: String? = null
)

/** Shares the chat's engine. acquire/release serialize demos with chat and model loading. */
class DemoController(
    private val scope: CoroutineScope,
    private val acquire: () -> Boolean,
    private val release: () -> Unit,
    private val generate: (String) -> Flow<String>
) {
    private val _state = MutableStateFlow(DemoUiState())
    val state = _state.asStateFlow()
    private var job: Job? = null

    fun select(kind: DemoKind) { if (!_state.value.running) _state.update { it.copy(kind = kind, rawResponse = "", timing = null, error = null) } }
    fun updateNotes(text: String) {
        if (!_state.value.running) _state.update { it.copy(notes = text.take(DemoSamples.MAX_NOTES_LENGTH), items = null, rawResponse = "", timing = null, error = null) }
    }
    fun updateQuestion(text: String) {
        if (!_state.value.running) _state.update { it.copy(question = text.take(DemoSamples.MAX_QUESTION_LENGTH), answer = null, rawResponse = "", timing = null, error = null) }
    }
    fun editItem(index: Int, item: ActionItem) {
        if (!_state.value.running) _state.update { it.copy(items = it.items?.mapIndexed { i, old -> if (i == index) item else old }) }
    }
    fun cancel() { job?.cancel() }

    fun run() {
        val input = _state.value
        if (input.running) return
        if ((if (input.kind == DemoKind.NOTES) input.notes else input.question).isBlank()) {
            _state.update { it.copy(error = "Enter ${if (input.kind == DemoKind.NOTES) "notes" else "a question"} first.") }
            return
        }
        if (!acquire()) {
            _state.update { it.copy(error = "Load a model in Settings and wait for any current generation to finish.") }
            return
        }
        _state.update { it.copy(running = true, items = null, answer = null, rawResponse = "", timing = null, error = null) }
        job = scope.launch(Dispatchers.IO) {
            val start = TimeSource.Monotonic.markNow()
            var firstChunkMs: Long? = null
            var raw = ""
            try {
                val prompt = if (input.kind == DemoKind.NOTES) DemoSamples.notesPrompt(input.notes) else DemoSamples.faqPrompt(input.question)
                generate(prompt).collect { chunk ->
                    if (chunk.isNotEmpty() && firstChunkMs == null) firstChunkMs = start.elapsedNow().inWholeMilliseconds
                    raw += chunk
                    require(raw.length <= 24_000) { "Response is too long. Try again with shorter input." }
                    _state.update { it.copy(rawResponse = raw) }
                }
                val timing = DemoTiming(firstChunkMs, start.elapsedNow().inWholeMilliseconds)
                if (input.kind == DemoKind.NOTES) {
                    val items = DemoSamples.parseNotes(raw, input.notes)
                    _state.update { it.copy(items = items, timing = timing) }
                } else {
                    val answer = DemoSamples.parseFaq(raw)
                    _state.update { it.copy(answer = answer, timing = timing) }
                }
            } catch (cancelled: CancellationException) {
                _state.update { it.copy(error = "Generation stopped. You can edit the input and try again.") }
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(error = "Could not validate the response. Try again or use shorter input. ${error.message.orEmpty().take(160)}") }
            }
        }.also { generation ->
            // Runs even if cancellation happens before the coroutine body starts.
            generation.invokeOnCompletion {
                release()
                _state.update { it.copy(running = false) }
            }
        }
    }
}
