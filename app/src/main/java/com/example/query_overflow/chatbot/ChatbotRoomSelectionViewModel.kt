package com.example.query_overflow.chatbot

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatbotRoomSelectionViewModel(
    private val chatRepository: ChatbotRepository
) : ViewModel() {

    private val _groupFlow = MutableStateFlow<List<ChatRoom>>(emptyList())
    val groupFlow: StateFlow<List<ChatRoom>> = _groupFlow

    val textToSpeechEnabled = chatRepository.readTTS().map{ textToSpeechPreferences ->
        textToSpeechPreferences[booleanPreferencesKey("TTSPreferences")] ?: false
    }.stateIn(
        scope = viewModelScope, // Coroutine scope
        started = SharingStarted.WhileSubscribed(), // When to start collecting
        initialValue = false // Initial value
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getAllRoomsID().collectLatest {
                _groupFlow.value = it
            }
        }
    }

    fun createRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.createChatRoom()
        }
    }

    fun deleteRoom(roomID: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.deleteRoom(roomID)
        }
    }

    fun editRoomName(roomID: Int, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.editRoom(roomID, newName)
        }
    }

    fun saveTTSPreference(tts: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.saveTTSPreference(tts)
        }
    }
}

class ChatbotRoomSelectionViewModelFactory(
    private val chatRepository: ChatbotRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatbotRoomSelectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatbotRoomSelectionViewModel(chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}