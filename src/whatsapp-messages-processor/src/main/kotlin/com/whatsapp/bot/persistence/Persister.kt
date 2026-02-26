package com.whatsapp.bot.persistence

interface Persister {
    data class ChatHistoryEntry(
        val fromMe: Boolean,
        val chatId: String,
        val senderId: String,
        val senderName: String?,
        val message: String,
        val index: Int,
        val timestamp: Long
    )

    fun accepts(): Boolean
    fun saveHistoryEntry(fromMe: Boolean, chatId: String, from: String, senderName: String?, message: String, timestamp: Long)
    fun getHistory(chatId: String): List<ChatHistoryEntry>
    fun deleteHistory(entry: ChatHistoryEntry)
    fun deleteHistories(entries: List<ChatHistoryEntry>)
    fun clearHistory(chatId: String)
}