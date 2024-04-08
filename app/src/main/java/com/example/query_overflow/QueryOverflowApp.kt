package com.example.query_overflow

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.query_overflow.chatbot.ChatbotRepository
import com.example.query_overflow.chatbot.ChatbotRoomDatabase
import kotlinx.coroutines.GlobalScope

class QueryOverflowApp : Application() {
    private val chatbotDao by lazy { ChatbotRoomDatabase.getDatabase(this, GlobalScope).chatbotDao() }

    private val Context.dataStore by preferencesDataStore(
        name = "TTSPreferences"
    )

    val repository by lazy { ChatbotRepository(chatbotDao, dataStore) }
}