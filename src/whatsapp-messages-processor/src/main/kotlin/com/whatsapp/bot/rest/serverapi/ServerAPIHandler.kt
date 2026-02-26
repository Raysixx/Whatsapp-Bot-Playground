package com.whatsapp.bot.rest.serverapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response

interface ServerAPIHandler {
    companion object {
        fun defaultSendMessage(messageToSend: ToSendMessage) {
            val client = OkHttp()

            val request = Request(Method.POST, "http://localhost:8081/send-message")
                .header("Content-Type", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(messageToSend))

            val response = client(request)
            if (response.status != org.http4k.core.Status.OK) {
                throw RuntimeException("Error applying request in ${request.uri} - ${response.status.code} ${response.status.description}")
            }
        }
    }

    fun accepts(): Boolean
    fun initialValidation(request: Request): Response?
    fun getStatus(request: Request): Status
    fun getMessage(request: Request): ReceivedMessage
    fun sendMessage(messageToSend: ToSendMessage)
}