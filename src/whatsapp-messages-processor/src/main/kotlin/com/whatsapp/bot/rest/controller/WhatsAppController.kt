package com.whatsapp.bot.rest.controller

import com.whatsapp.bot.historycontrol.HistoryControl
import com.whatsapp.bot.processing.MessagesProcessor
import com.whatsapp.bot.responsecontrol.ResponseControl
import com.whatsapp.bot.rest.serverapi.ServerAPIHandler
import com.whatsapp.bot.rest.serverapi.StatusEnum
import com.whatsapp.bot.rest.serverapi.ToSendMessage
import com.whatsapp.bot.rest.serverapi.TypeEnum
import com.whatsapp.bot.rest.serverapi.baileys.BaileysAPIHandler
import com.whatsapp.bot.rest.serverapi.whatsappwebjsapi.WhatsappWebJsAPIHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object WhatsAppController {
    private val logger = KotlinLogging.logger(this::class.qualifiedName!!)

    private val serverAPI: ServerAPIHandler = ServiceLoader.load(ServerAPIHandler::class.java).firstOrNull { it.accepts() } ?: BaileysAPIHandler()
    private val messagesProcessor = MessagesProcessor()
    private val historyControl = HistoryControl(serverAPI::sendMessage)
    private val responseControl = ResponseControl(serverAPI::sendMessage)

    private val processingChats = ConcurrentHashMap<String, Boolean>()

    object RequestHandler: HttpHandler {
        override fun invoke(request: Request): Response {
            return try {
                val status = serverAPI.getStatus(request)
                if (status.status == StatusEnum.FAIL) {
                    logger.error {
                        "Error receiving message: \n" +
                        (status.errorCode?.let { "Code: $it\n" } ?: "") +
                        (status.errorTitle?.let { "Title: $it\n" } ?: "") +
                        (status.errorMessage?.let { "Message: $it\n" } ?: "")
                    }

                    return Response(OK)
                }

                val message = serverAPI.getMessage(request)
                val isGroup = message.id.remote.endsWith("g.us")
                val isChatTextMessage = message.type == TypeEnum.CHAT

                if (!isGroup && isChatTextMessage) {
                    val isProcessing = processingChats.putIfAbsent(message.id.remote, true) ?: false
                    if (!isProcessing) {
                        try {
                            val history = historyControl.getHistory(message.id.remote)

                            if (message.fromMe) {
                                if (messagesProcessor.isMessageGeneratedByBot(message, history)) {
                                    historyControl.saveHistoryEntry(message)
                                }
                            } else {
                                val messageAnswer = messagesProcessor.getMessageAnswer(message, history)
                                if (messageAnswer != null) {
                                    when {
                                        messageAnswer.isLastAnswer -> historyControl.resetChat(message.id.remote)

                                        messageAnswer.isGoingBack -> {
                                            val lastThreeHistories = history.let {
                                                it.subList(it.size - 3, it.size)
                                            } // Last question (already answered) and current question

                                            historyControl.deleteHistories(lastThreeHistories)
                                        }

                                        messageAnswer.isRepeatingLastQuestion -> {
                                            val lastTwoHistories = history.let {
                                                it.subList(it.size - 2, it.size)
                                            } // Last question (already answered) and current question

                                            historyControl.deleteHistories(lastTwoHistories)

                                            if (messageAnswer.denialAnswer != null) {
                                                responseControl.addToQueue(
                                                    ToSendMessage(
                                                        messageAnswer.denialAnswer,
                                                        message.from
                                                    )
                                                )
                                            }
                                        }

                                        else -> historyControl.saveHistoryEntry(message)
                                    }

                                    responseControl.addToQueue(
                                        ToSendMessage(
                                            messageAnswer.answer,
                                            message.from
                                        )
                                    )
                                }
                            }
                        } finally {
                            processingChats.remove(message.id.remote)
                        }
                    }
                }

                Response(OK)
            } catch (e: Exception) {
                e.printStackTrace()

                // Apparently there is a bug in Undertow server that throws another exception when I try to return code 500 here,
                // and since the whatsapp-web-proxy doesn't care about the response, I'm just returning 200
                Response(OK)
            }
        }
    }

    val handler: HttpHandler = routes(
        "whatsapp" bind GET to RequestHandler,
        "whatsapp" bind POST to RequestHandler
    )
}
