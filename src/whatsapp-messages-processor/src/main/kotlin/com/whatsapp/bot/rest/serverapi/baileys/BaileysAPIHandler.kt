package com.whatsapp.bot.rest.serverapi.baileys

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.whatsapp.bot.rest.serverapi.ID
import com.whatsapp.bot.rest.serverapi.ReceivedMessage
import com.whatsapp.bot.rest.serverapi.ServerAPIHandler
import com.whatsapp.bot.rest.serverapi.Status
import com.whatsapp.bot.rest.serverapi.StatusEnum
import com.whatsapp.bot.rest.serverapi.ToSendMessage
import com.whatsapp.bot.rest.serverapi.TypeEnum
import com.whatsapp.bot.rest.serverapi.baileys.model.BaileysPayload
import org.http4k.core.Request

/**
 * This is the impl when using whatsapp-web-proxy-websocket
 */
class BaileysAPIHandler: ServerAPIHandler {
    override fun accepts(): Boolean {
        return System.getenv("ServerAPI").let {
            it == null ||
            it.equals("baileys", ignoreCase = true)
        }
    }

    override fun initialValidation(request: Request) = null

    override fun getStatus(request: Request) = Status(StatusEnum.SUCCESS)

    override fun getMessage(request: Request): ReceivedMessage {
        val baileysMessage = jacksonObjectMapper().apply {
            enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
        }.readValue<BaileysPayload>(request.bodyString())

        val from = if (baileysMessage.key.fromMe) {
            baileysMessage.meLid ?: baileysMessage.meId
        } else {
            baileysMessage.key.remoteJid
        }

        val to = if (baileysMessage.key.fromMe) {
            baileysMessage.key.remoteJid
        } else {
            baileysMessage.meLid ?: baileysMessage.meId
        }

        val text = baileysMessage.message.conversation ?: baileysMessage.message.extendedTextMessage?.text

        return ReceivedMessage(
            id = ID(
                fromMe = baileysMessage.key.fromMe,
                remote = baileysMessage.key.remoteJid,
                id = baileysMessage.key.id
            ),
            from,
            to,
            notifyName = baileysMessage.pushName,
            author = baileysMessage.key.participant?.takeIf { it.isNotEmpty() },
            baileysMessage.messageTimestamp,
            body = text ?: "",
            type = if (text != null) TypeEnum.CHAT else TypeEnum.UNKNOWN,
            fromMe = baileysMessage.key.fromMe,
            hasMedia = null,
            deviceType = null,
            isForwarded = baileysMessage.message.messageContextInfo?.isForwarded ?: false
        )
    }

    override fun sendMessage(messageToSend: ToSendMessage) = ServerAPIHandler.defaultSendMessage(messageToSend)
}