package com.example.query_overflow.chatbot

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatbotDAO {
    @Query("SELECT * FROM message_table WHERE roomId = :roomId ORDER BY id ASC")
    fun getChatData(roomId: Int): Flow<List<ChatData>>

    @Query("SELECT * FROM message_table WHERE roomId = :roomId ORDER BY id ASC")
    fun getChatDataTest(roomId: Int): Flow<List<ChatData>>

    @Query("SELECT * FROM room_table")
    fun getAllRoomsID(): Flow<List<ChatRoom>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(chatData: ChatData)

    @Query("DELETE FROM message_table")
    suspend fun delete()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun createRoom(chatRoom: ChatRoom)

    @Query("DELETE FROM room_table WHERE id = :roomId")
    fun deleteRoom(roomId: Int)

    @Query("UPDATE room_table SET roomName = :newName WHERE id = :roomId")
    fun editRoomName(roomId: Int, newName: String)

    @Query("SELECT roomName FROM room_table WHERE id = :roomId")
    fun getChatRoomName(roomId: Int): String
}