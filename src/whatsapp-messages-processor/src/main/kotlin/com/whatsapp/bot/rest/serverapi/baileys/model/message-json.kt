package com.whatsapp.bot.rest.serverapi.baileys.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.whatsapp.bot.rest.model.DefaultParent

data class BaileysPayload(
    val key: BaileysID,
    val messageTimestamp: Long,
    val pushName: String?,
    val message: BaileysMessage,

    @JsonDeserialize(using = RawBaileysIdDeserializer::class)
    val meId: String,

    @JsonDeserialize(using = RawBaileysIdDeserializer::class)
    val meLid: String?
): DefaultParent()

data class BaileysID(
    val remoteJid: String,
    val remoteJidAlt: String?,
    val fromMe: Boolean,
    val id: String,
    val participant: String?
): DefaultParent()

data class BaileysMessage(
    val conversation: String?,
    val extendedTextMessage: BaileysExtendedTextMessage?,
    val messageContextInfo: BaileysMessageContextInfo?
): DefaultParent()

data class BaileysExtendedTextMessage(
    val text: String
): DefaultParent()

data class BaileysMessageContextInfo(
    val isForwarded: Boolean = false
): DefaultParent()