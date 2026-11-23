package org.abma.offlinelai_kmp.domain.speech

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionResult
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class SpeechRecognizerManager {
    private val _isRecording = MutableStateFlow(false)
    actual val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    actual val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    actual val error: StateFlow<String?> = _error.asStateFlow()

    private var speechRecognizer: SFSpeechRecognizer? = null
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest? = null
    private var recognitionTask: SFSpeechRecognitionTask? = null
    private val audioEngine = AVAudioEngine()

    init {
        speechRecognizer = SFSpeechRecognizer()
        // Ensure on-device recognition if possible
        speechRecognizer?.supportsOnDeviceRecognition = true
    }

    actual fun startListening() {
        if (_isRecording.value) return

        if (recognitionTask != null) {
            recognitionTask?.cancel()
            recognitionTask = null
        }

        val audioSession = AVAudioSession.sharedInstance()
        var sessionError: NSError? = null
        
        audioSession.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            mode = AVAudioSessionModeMeasurement,
            options = 0u,
            error = null
        )
        audioSession.setActive(true, error = null)

        recognitionRequest = SFSpeechAudioBufferRecognitionRequest().apply {
            shouldReportPartialResults = true
            requiresOnDeviceRecognition = true
        }

        val inputNode = audioEngine.inputNode
        val request = recognitionRequest ?: return

        recognitionTask = speechRecognizer?.recognitionTaskWithRequest(request) { result, error ->
            var isFinal = false
            if (result != null) {
                _recognizedText.value = result.bestTranscription.formattedString
                isFinal = result.isFinal()
            }

            if (error != null || isFinal) {
                audioEngine.stop()
                inputNode.removeTapOnBus(0u)
                recognitionRequest = null
                recognitionTask = null
                _isRecording.value = false
                
                if (error != null) {
                    _error.value = error.localizedDescription
                }
            }
        }

        val recordingFormat = inputNode.outputFormatForBus(0u)
        inputNode.installTapOnBus(0u, 1024u, recordingFormat) { buffer, _ ->
            recognitionRequest?.appendAudioPCMBuffer(buffer!!)
        }

        audioEngine.prepare()
        audioEngine.startAndReturnError(null)

        _error.value = null
        _recognizedText.value = ""
        _isRecording.value = true
    }

    actual fun stopListening() {
        if (!_isRecording.value) return
        
        audioEngine.stop()
        audioEngine.inputNode.removeTapOnBus(0u)
        recognitionRequest?.endAudio()
        recognitionTask?.cancel()
        
        _isRecording.value = false
    }
}

@Composable
actual fun rememberSpeechRecognizerManager(): SpeechRecognizerManager {
    return remember { SpeechRecognizerManager() }
}
