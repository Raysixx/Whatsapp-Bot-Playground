package com.whatsapp.bot.rest.serverapi

import com.fasterxml.jackson.annotation.JsonProperty
import com.whatsapp.bot.rest.model.DefaultParent

data class ReceivedMessage(
    val id: ID,
    val from: String,
    val to: String,
    val notifyName: String?,
    val author: String?,
    val timestamp: Long,
    val body: String,
    val type: TypeEnum,
    val fromMe: Boolean,
    val hasMedia: Boolean?,
    val deviceType: String?,
    val isForwarded: Boolean
)

data class ToSendMessage(
    @JsonProperty("message")
    val body: String,

    @JsonProperty("number")
    val to: String
)

data class ID(
    val fromMe: Boolean,
    val remote: String,
    val id: String
): DefaultParent()

data class Status(
    val status: StatusEnum,
    val errorCode: Int? = null,
    val errorTitle: String? = null,
    val errorMessage: String? = null
)