package com.example.query_overflow.chatbot

import androidx.annotation.WorkerThread
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow

class ChatbotRepository(
    private val chatbotDao: ChatbotDAO,
    private val dataStore: DataStore<Preferences>
) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(chatData: ChatData) {
        chatbotDao.insert(chatData)
    }

    fun getAllChatMessages(roomID: Int): Flow<List<ChatData>> {
        return chatbotDao.getChatData(roomID)
    }

    fun getAllRoomsID(): Flow<List<ChatRoom>> {
        return chatbotDao.getAllRoomsID()
    }

    fun createChatRoom() {
        chatbotDao.createRoom(ChatRoom())
    }

    fun deleteRoom(roomID: Int) {
        chatbotDao.deleteRoom(roomID)
    }

    fun editRoom(roomID: Int, newName: String) {
        chatbotDao.editRoomName(roomID, newName)
    }

    fun getChatRoomName(roomID: Int): String {
        return chatbotDao.getChatRoomName(roomID)
    }

    suspend fun saveTTSPreference(tts: Boolean) {
        dataStore.edit { textToSpeechPreferences ->
            textToSpeechPreferences[booleanPreferencesKey("TTSPreferences")] = tts
        }
    }

    fun readTTS(): Flow<Preferences> {
        return dataStore.data
    }
}