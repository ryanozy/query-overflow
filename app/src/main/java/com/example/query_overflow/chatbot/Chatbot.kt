package com.example.query_overflow.chatbot

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "room_table")
data class ChatRoom(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "roomName")
    var roomName: String = "New Room"
)

@Entity(
    tableName = "message_table",
    foreignKeys = [ForeignKey(
        entity = ChatRoom::class,
        parentColumns = ["id"],
        childColumns = ["roomId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["roomId"])]
)
data class ChatData(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "text")
    var text: String = "",

    @ColumnInfo(name = "msgType")
    var msgType: String,

    @ColumnInfo(name = "roomId")
    var roomId: Int = 0
)