package com.whatsapp.bot.rest.serverapi.officialwhatsappapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.whatsapp.bot.rest.serverapi.officialwhatsappapi.model.OfficialWhatsappStatusEnum
import com.whatsapp.bot.rest.serverapi.officialwhatsappapi.model.OfficialWhatsappPayload
import com.whatsapp.bot.rest.serverapi.ReceivedMessage
import com.whatsapp.bot.rest.serverapi.ServerAPIHandler
import com.whatsapp.bot.rest.serverapi.Status
import com.whatsapp.bot.rest.serverapi.StatusEnum
import com.whatsapp.bot.rest.serverapi.ToSendMessage
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK

class OfficialWhatsappAPIHandler: ServerAPIHandler {
    override fun accepts(): Boolean {
        return System.getenv("ServerAPI")?.equals("official", ignoreCase = true) == true
    }

    override fun initialValidation(request: Request): Response? {
        if (request.method != GET) {
            return null
        }

        val urlParams = request.uri.toString().let { url ->
            if (!url.contains("?")) {
                emptyMap()
            } else {
                url.substringAfter("?").split("&").associate {
                    val (key, value) = it.split("=")
                    key to value
                }
            }
        }

        val hubMode = urlParams["hub.mode"]
        return if (hubMode == null || hubMode != "subscribe") {
            null
        } else {
            val verifyToken = urlParams["hub.verify_token"]!!
            if (verifyToken == "123") {
                val challenge = urlParams["hub.challenge"]!!

                Response(OK).body(challenge)
            } else {
                Response(FORBIDDEN)
            }
        }
    }

    override fun getStatus(request: Request): Status {
        val payload = jacksonObjectMapper().readValue<OfficialWhatsappPayload>(request.bodyString())

        val statusObj = payload.getStatusObj()

        return if (statusObj?.status == OfficialWhatsappStatusEnum.FAILED) {
            val error = payload.getError(statusObj)!!

            Status(
                StatusEnum.FAIL,
                error.code,
                error.title,
                error.message
            )
        } else {
            Status(StatusEnum.SUCCESS)
        }
    }

    override fun getMessage(request: Request): ReceivedMessage {
        val payload = jacksonObjectMapper().readValue<OfficialWhatsappPayload>(request.bodyString())

        return TODO()
    }

    override fun sendMessage(messageToSend: ToSendMessage) {
        TODO("Not yet implemented")
    }
}