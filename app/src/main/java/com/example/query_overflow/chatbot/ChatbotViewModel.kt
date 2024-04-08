package com.example.query_overflow.chatbot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.generationConfig
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ChatbotViewModel(
    generativeModel: GenerativeModel,
    private val chatRepository: ChatbotRepository,
) : ViewModel() {

    private var tts: TextToSpeech? = null

    val textToSpeechEnabled = chatRepository.readTTS().map{ textToSpeechPreferences ->
        textToSpeechPreferences[booleanPreferencesKey("TTSPreferences")] ?: false
    }.stateIn(
        scope = viewModelScope, // Coroutine scope
        started = SharingStarted.WhileSubscribed(), // When to start collecting
        initialValue = false // Initial value
    )

    private val enToZH = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.CHINESE)
        .build()
    private val zhToEN = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.CHINESE)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()

    private val chat = generativeModel.startChat()
    private val _uiState: MutableStateFlow<ChatUiState> =
        MutableStateFlow(ChatUiState(chat.history.map { content ->
            // Map the initial messages
            ChatMessage(
                text = content.parts.first().asTextOrNull() ?: "",
                participant = if (content.role == "user") Participant.USER else Participant.MODEL,
                isPending = false
            )
        }))

    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()

    suspend fun getChatRoomName(roomID: Int): String {
        return withContext(Dispatchers.IO) {
            chatRepository.getChatRoomName(roomID)
        }
    }


    suspend fun loadChatHistory(chatID: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getAllChatMessages(chatID).collect {
                _uiState.value = ChatUiState(it.map { chatData ->
                    ChatMessage(
                        text = chatData.text,
                        participant = if (chatData.msgType == "user") Participant.USER else Participant.MODEL,
                        isPending = false
                    )
                })
            }
        }
    }

    fun sendMessage(userMessage: String, chatID: Int, context: Context, ttsEnabled: Boolean) {
        // Add a pending message
        _uiState.value.addMessage(
            ChatMessage(
                text = userMessage,
                participant = Participant.USER,
                isPending = true
            )
        )

        val userMsg = ChatData(
            text = userMessage,
            msgType = "user",
            roomId = chatID
        )

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)

                _uiState.value.replaceLastPendingMessage()

                val botRespondMsg = ChatData(
                    text = "No Message yet",
                    msgType = "model",
                    roomId = chatID
                )
                response.text?.let { modelResponse ->
                    _uiState.value.addMessage(
                        ChatMessage(
                            text = modelResponse,
                            participant = Participant.MODEL,
                            isPending = false
                        )
                    )

                    botRespondMsg.text = modelResponse

                    if (ttsEnabled) {
                        tts = TextToSpeech(context) { status ->
                            if (status != TextToSpeech.ERROR) {
                                tts?.language = Locale.UK
                                tts?.speak(modelResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                    }
                }
                insertSingleMessages(userMsg, botRespondMsg)
            } catch (e: Exception) {
                // Define ChatData
                val botRespondMsg = ChatData(
                    text = "No Message yet",
                    msgType = "error",
                    roomId = chatID
                )
                _uiState.value.replaceLastPendingMessage()
                e.localizedMessage?.let {
                    ChatMessage(
                        text = it,
                        participant = Participant.ERROR
                    )
                }?.let {
                    _uiState.value.addMessage(
                        it
                    )
                }
                botRespondMsg.text = (e.localizedMessage?.toString() ?: insertSingleMessages(userMsg, botRespondMsg)).toString()
            }
        }
    }

    private suspend fun newInsertType(chatData: ChatData) {
        withContext(Dispatchers.IO) {
            chatRepository.insert(chatData)
        }
    }

    private suspend fun insertSingleMessages(chatMessages: ChatData, botRespondMsg: ChatData) {
        withContext(Dispatchers.IO) {
            // Perform database operation here
            newInsertType(chatMessages)
            newInsertType(botRespondMsg)
        }
    }

    fun copyTextToClipboard(textCopied: String, context: Context) {
        val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        // When setting the clipboard text.
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", textCopied))
    }

    fun translateText(text: String, targetLang: String, callback: (String) -> Unit) {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        if (targetLang == "zh") {
            val translator = Translation.getClient(enToZH)
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    translator.translate(text)
                        .addOnSuccessListener { translatedText ->
                            callback(translatedText)
                        }
                        .addOnFailureListener {
                            callback("Failed to translate text")
                        }
                }
                .addOnFailureListener {
                    callback("Failed to download model")
                }
        } else {
            val translator = Translation.getClient(zhToEN)
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    translator.translate(text)
                        .addOnSuccessListener { translatedText ->
                            callback(translatedText)
                        }
                        .addOnFailureListener {
                            callback("Failed to translate text")
                        }
                }
                .addOnFailureListener {
                    callback("Failed to download model")
                }
        }
    }

    fun speakText(text: String, context: Context, selectedLanguage: String) {
        if (selectedLanguage == "EN") {
            tts = TextToSpeech(context) { status ->
                if (status != TextToSpeech.ERROR) {
                    tts?.language = Locale.UK
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        } else {
            tts = TextToSpeech(context) { status ->
                if (status != TextToSpeech.ERROR) {
                    tts?.language = Locale.CHINESE
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }
    }

    fun startVoiceInput(context: Context, onResult: (String) -> Unit) {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        if (speechRecognizer == null) {
            Toast.makeText(context, "Speech recognition is not available", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {
                Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {}

            override fun onResults(results: Bundle?) {
                val voiceInputText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0) ?: ""
                onResult(voiceInputText)
                speechRecognizer.destroy()
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
        Log.d("Speech", "Listening")
    }
}

class ChatbotViewModelFactory(private val repository: ChatbotRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatbotViewModel::class.java)) {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.0-pro",
                apiKey = "YOUR_API_KEY",
                generationConfig = generationConfig {
                    temperature = 0.7f
                }
            )
            @Suppress("UNCHECKED_CAST")
            return ChatbotViewModel(generativeModel, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

