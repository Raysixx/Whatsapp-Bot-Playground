package com.whatsapp.bot.rest.serverapi.whatsappwebjsapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.whatsapp.bot.rest.serverapi.ReceivedMessage
import com.whatsapp.bot.rest.serverapi.ServerAPIHandler
import com.whatsapp.bot.rest.serverapi.Status
import com.whatsapp.bot.rest.serverapi.StatusEnum
import com.whatsapp.bot.rest.serverapi.ToSendMessage
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request

/**
 * This is the impl when using whatsapp-web-proxy-puppeteer
 */
class WhatsappWebJsAPIHandler: ServerAPIHandler {
    override fun accepts(): Boolean {
        return System.getenv("ServerAPI")?.equals("whatsapp-web.js", ignoreCase = true) == true
    }

    override fun initialValidation(request: Request) = null

    override fun getStatus(request: Request) = Status(StatusEnum.SUCCESS)

    override fun getMessage(request: Request): ReceivedMessage {
        return jacksonObjectMapper().apply {
            enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
        }.readValue<ReceivedMessage>(request.bodyString())
    }

    override fun sendMessage(messageToSend: ToSendMessage) = ServerAPIHandler.defaultSendMessage(messageToSend)
}