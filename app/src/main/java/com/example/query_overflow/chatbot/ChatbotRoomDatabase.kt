package com.example.query_overflow.chatbot

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ChatData::class, ChatRoom::class], version = 1, exportSchema = false)
abstract class ChatbotRoomDatabase : RoomDatabase() {

    abstract fun chatbotDao(): ChatbotDAO

    companion object {
        @Volatile
        private var INSTANCE: ChatbotRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ChatbotRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatbotRoomDatabase::class.java,
                    "chatbot_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(ChatbotDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class ChatbotDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.chatbotDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(chatbotDao: ChatbotDAO) {
            chatbotDao.delete()
        }
    }
}