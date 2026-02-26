package com.whatsapp.bot.historycontrol

import com.whatsapp.bot.App.persister
import com.whatsapp.bot.persistence.Persister
import com.whatsapp.bot.rest.serverapi.ReceivedMessage
import com.whatsapp.bot.rest.serverapi.ToSendMessage
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class HistoryControl(private val sendMessageFunc: (ToSendMessage) -> Unit) {
    companion object {
        private const val WARN_ALMOST_TIMEOUT_WHEN_MISSING = 30L // Seconds
        private const val CHAT_ALMOST_FINISHED_MSG = "Chat automático se encerrará com mais $WARN_ALMOST_TIMEOUT_WHEN_MISSING segundos de inatividade."

        private const val TIMEOUT = 180L // Seconds
        private const val CHAT_FINISHED_MSG = "Chat automático finalizado."
    }

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val chatsTimeout = mutableMapOf<String, Pair<ScheduledFuture<*>, ScheduledFuture<*>>>()

    fun saveHistoryEntry(message: ReceivedMessage) {
        saveHistoryEntry(message.fromMe, message.id.remote, message.from, message.notifyName, message.body, message.timestamp)
    }

    fun saveHistoryEntry(fromMe: Boolean, chatId: String, from: String, senderName: String?, message: String, timestamp: Long) {
        if (!fromMe) {
            chatsTimeout[chatId]?.let {
                it.first.cancel(false)
                it.second.cancel(false)
            }

            chatsTimeout[chatId] =
                executor.schedule({ warnAlmostTimeout(chatId) }, TIMEOUT - WARN_ALMOST_TIMEOUT_WHEN_MISSING, TimeUnit.SECONDS) to
                executor.schedule({ resetChat(chatId, sendMessage = true) }, TIMEOUT, TimeUnit.SECONDS)
        }

        persister.saveHistoryEntry(fromMe, chatId, from, senderName, message, timestamp)
    }

    fun getHistory(chatId: String) = persister.getHistory(chatId)

    fun deleteHistory(entry: Persister.ChatHistoryEntry) = persister.deleteHistory(entry)

    fun deleteHistories(entries: List<Persister.ChatHistoryEntry>) = persister.deleteHistories(entries)

    fun deleteLastHistory(chatId: String) {
        persister.getHistory(chatId).lastOrNull()?.let {
            persister.deleteHistory(it)
        }
    }

    private fun warnAlmostTimeout(chatId: String) {
        sendMessageFunc(
            ToSendMessage(
                CHAT_ALMOST_FINISHED_MSG,
                chatId
            )
        )
    }

    fun resetChat(chatId: String, sendMessage: Boolean = false) {
        if (persister.getHistory(chatId).isEmpty()) {
            return
        }

        persister.clearHistory(chatId)
        chatsTimeout[chatId]?.let {
            it.first.cancel(false)
            it.second.cancel(false)
        }

        if (sendMessage) {
            sendMessageFunc(
                ToSendMessage(
                    CHAT_FINISHED_MSG,
                    chatId
                )
            )
        }
    }
}