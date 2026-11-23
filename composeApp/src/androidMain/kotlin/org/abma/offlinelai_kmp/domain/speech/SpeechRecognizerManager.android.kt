package org.abma.offlinelai_kmp.domain.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class SpeechRecognizerManager(private val context: Context) {
    private val _isRecording = MutableStateFlow(false)
    actual val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    actual val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    actual val error: StateFlow<String?> = _error.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    actual fun startListening() {
        if (isListening) return
        
        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _isRecording.value = false
                            isListening = false
                        }
                        override fun onError(error: Int) {
                            _isRecording.value = false
                            isListening = false
                            _error.value = "Error code: $error"
                        }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                _recognizedText.value = matches[0]
                            }
                            _isRecording.value = false
                            isListening = false
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                _recognizedText.value = matches[0]
                            }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            
            _error.value = null
            _recognizedText.value = ""
            speechRecognizer?.startListening(intent)
            _isRecording.value = true
            isListening = true
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to start speech recognizer"
            _isRecording.value = false
            isListening = false
        }
    }

    actual fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isRecording.value = false
            isListening = false
        }
    }
}

@Composable
actual fun rememberSpeechRecognizerManager(): SpeechRecognizerManager {
    val context = LocalContext.current
    return remember { SpeechRecognizerManager(context) }
}
