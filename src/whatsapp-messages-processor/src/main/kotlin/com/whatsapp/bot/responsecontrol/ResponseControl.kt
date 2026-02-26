package com.whatsapp.bot.responsecontrol

import com.whatsapp.bot.rest.serverapi.ToSendMessage
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ResponseControl(private val sendMessageFunc: (ToSendMessage) -> Unit) {
    init {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(::sendMessage, 5, 5, TimeUnit.SECONDS)
    }

    private val queue = LinkedBlockingQueue<ToSendMessage>()

    fun addToQueue(toSendMessage: ToSendMessage) {
        queue.add(toSendMessage)
    }

    private fun sendMessage() {
        val nextMessage = queue.poll() ?: return
        sendMessageFunc(nextMessage)
    }
}