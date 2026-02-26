package com.whatsapp.bot.persistence

class MemoryPersistence: Persister {
    private val chatHistories = mutableMapOf<String, MutableList<Persister.ChatHistoryEntry>>()

    override fun accepts(): Boolean {
        return System.getenv("Persister").let {
            it == null ||
            it.equals("MemoryPersistence", ignoreCase = true)
        }
    }

    override fun saveHistoryEntry(fromMe: Boolean, chatId: String, from: String, senderName: String?, message: String, timestamp: Long) {
        val history = chatHistories.getOrPut(chatId) {
            mutableListOf()
        }

        val index = history.maxOfOrNull { it.index } ?: 0

        history.add(
            Persister.ChatHistoryEntry(
                fromMe,
                chatId,
                from,
                senderName,
                message,
                index + 1,
                timestamp
            )
        )
    }

    override fun getHistory(chatId: String): List<Persister.ChatHistoryEntry> {
        return chatHistories[chatId] ?: emptyList()
    }

    override fun deleteHistory(entry: Persister.ChatHistoryEntry) {
        chatHistories.forEach { (_, entries) ->
            entries.remove(entry)
        }
    }

    override fun deleteHistories(entries: List<Persister.ChatHistoryEntry>) {
        chatHistories.forEach { (_, currentEntries) ->
            currentEntries.removeAll(entries)
        }
    }

    override fun clearHistory(chatId: String) {
        chatHistories.remove(chatId)
    }

    override fun getActiveChatsCount(): Int {
        return chatHistories.size
    }
}