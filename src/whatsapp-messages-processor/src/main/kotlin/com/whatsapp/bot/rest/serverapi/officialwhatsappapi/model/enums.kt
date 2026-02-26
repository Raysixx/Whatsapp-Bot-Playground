package com.whatsapp.bot.rest.serverapi.officialwhatsappapi.model

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonValue

interface OfficialWhatsappEnum {
    @get:JsonValue
    val whatsappValue: String
}

enum class OfficialWhatsappFieldEnum(protected val value: String, override val whatsappValue: String = value): OfficialWhatsappEnum {
    MESSAGES("messages") { override val whatsappValue = value },

    @JsonEnumDefaultValue
    NOT_MAPPED("notMapped") { override val whatsappValue = value }
}

enum class OfficialWhatsappStatusEnum(protected val value: String): OfficialWhatsappEnum {
    FAILED("failed") { override val whatsappValue = value },

    @JsonEnumDefaultValue
    NOT_MAPPED("notMapped") { override val whatsappValue = value }
}