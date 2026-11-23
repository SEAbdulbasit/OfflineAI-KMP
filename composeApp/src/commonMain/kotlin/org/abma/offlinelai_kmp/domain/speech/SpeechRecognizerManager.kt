package org.abma.offlinelai_kmp.domain.speech

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

expect class SpeechRecognizerManager {
    val isRecording: StateFlow<Boolean>
    val recognizedText: StateFlow<String>
    val error: StateFlow<String?>

    fun startListening()
    fun stopListening()
}

@Composable
expect fun rememberSpeechRecognizerManager(): SpeechRecognizerManager
