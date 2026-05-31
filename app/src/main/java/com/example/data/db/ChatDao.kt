package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY createdAt DESC")
    fun getAllThreads(): Flow<List<ChatThread>>

    @Query("SELECT * FROM chat_threads WHERE id = :id LIMIT 1")
    suspend fun getThreadById(id: Int): ChatThread?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ChatThread): Long

    @Query("UPDATE chat_threads SET title = :title WHERE id = :threadId")
    suspend fun updateThreadTitle(threadId: Int, title: String)

    @Delete
    suspend fun deleteThread(thread: ChatThread)

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun clearMessagesForThread(threadId: Int)
}
