package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiRepository
import com.example.data.GeminiResponseResult
import com.example.model.CharacterProfile
import com.example.model.ChatMessage
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonaViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiRepository = GeminiRepository()
    val voiceManager = VoiceManager(application.applicationContext)

    private val _characterProfile = MutableStateFlow(CharacterProfile.PRESETS.first())
    val characterProfile: StateFlow<CharacterProfile> = _characterProfile.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    private val _callTranscript = MutableStateFlow<String>("")
    val callTranscript: StateFlow<String> = _callTranscript.asStateFlow()

    private val _lastAiVoiceReply = MutableStateFlow<String>("")
    val lastAiVoiceReply: StateFlow<String> = _lastAiVoiceReply.asStateFlow()

    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    private val _fullscreenImage = MutableStateFlow<ChatMessage?>(null)
    val fullscreenImage: StateFlow<ChatMessage?> = _fullscreenImage.asStateFlow()

    val isApiKeyAvailable: Boolean
        get() = geminiRepository.isApiKeyConfigured

    fun completeOnboarding(profile: CharacterProfile) {
        _characterProfile.value = profile
        voiceManager.setVoiceTone(profile.voiceTone)
        _isOnboarded.value = true

        // Initial greeting message
        val greeting = ChatMessage(
            text = profile.greetingMessage,
            isUser = false
        )
        _messages.value = listOf(greeting)
    }

    fun updateProfile(newProfile: CharacterProfile) {
        _characterProfile.value = newProfile
        voiceManager.setVoiceTone(newProfile.voiceTone)
    }

    fun openProfileDialog() {
        _showProfileDialog.value = true
    }

    fun closeProfileDialog() {
        _showProfileDialog.value = false
    }

    fun openFullscreenImage(message: ChatMessage) {
        _fullscreenImage.value = message
    }

    fun closeFullscreenImage() {
        _fullscreenImage.value = null
    }

    fun sendMessage(userText: String, isFromVoice: Boolean = false) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(
            text = userText.trim(),
            isUser = true,
            isVoiceMessage = isFromVoice
        )
        _messages.value = _messages.value + userMessage
        _isTyping.value = true

        viewModelScope.launch {
            val result = geminiRepository.sendMessage(
                conversationHistory = _messages.value,
                userMessage = userText,
                profile = _characterProfile.value
            )

            _isTyping.value = false

            when (result) {
                is GeminiResponseResult.Success -> {
                    val aiMessage = ChatMessage(
                        text = result.replyText,
                        isUser = false,
                        imageBase64 = result.generatedImageBase64,
                        imageDrawableRes = result.generatedImageDrawable,
                        isPhotoCard = result.generatedImageBase64 != null || result.generatedImageDrawable != null,
                        photoCaption = result.imageCaption,
                        isVoiceMessage = isFromVoice
                    )
                    _messages.value = _messages.value + aiMessage

                    if (_isCallActive.value) {
                        _lastAiVoiceReply.value = result.replyText
                        voiceManager.speak(result.replyText)
                    }
                }
                is GeminiResponseResult.Error -> {
                    val errorMessage = ChatMessage(
                        text = "*Parece que hubo una breve interferencia en la señal.* ${result.message}",
                        isUser = false
                    )
                    _messages.value = _messages.value + errorMessage
                }
            }
        }
    }

    fun requestSelfie() {
        sendMessage("¡Por favor mándame una foto o selfie tuya en este momento! 📸")
    }

    fun startCall() {
        _isCallActive.value = true
        _callTranscript.value = ""
        _lastAiVoiceReply.value = "Conectando con ${_characterProfile.value.name}..."

        // Greet upon call start
        viewModelScope.launch {
            val callGreeting = "¡Hola! Te escucho clarito. Dime qué tienes en mente."
            _lastAiVoiceReply.value = callGreeting
            voiceManager.speak(callGreeting)
        }
    }

    fun endCall() {
        voiceManager.stopSpeaking()
        voiceManager.stopListening()
        _isCallActive.value = false
    }

    fun handleVoiceInput(recognizedText: String) {
        _callTranscript.value = recognizedText
        sendMessage(recognizedText, isFromVoice = true)
    }

    fun speakMessage(text: String) {
        voiceManager.speak(text)
    }

    fun clearChat() {
        val greeting = ChatMessage(
            text = _characterProfile.value.greetingMessage,
            isUser = false
        )
        _messages.value = listOf(greeting)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
