package com.whatsapp.bot.rest.serverapi

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonValue

interface WhatsappEnum {
    @get:JsonValue
    val whatsappValue: String
}

enum class TypeEnum(override val whatsappValue: String) : WhatsappEnum {
    CHAT("chat"),
    IMAGE("image"),
    VIDEO("video"),
    AUDIO("audio"),
    PTT("ptt"),
    DOCUMENT("document"),
    STICKER("sticker"),
    LOCATION("location"),
    CONTACT("contact"),
    CONTACTS("contacts"),
    VCARD("vcard"),
    GROUP_NOTIFICATION("group_notification"),
    BROADCAST_NOTIFICATION("broadcast_notification"),
    E2E_NOTIFICATION("e2e_notification"),
    NOTIFICATION_TEMPLATE("notification_template"),
    CALL_LOG("call_log"),
    REACTION("reaction"),
    BUTTONS_RESPONSE("buttons_response"),
    LIST_RESPONSE("list_response"),
    PAYMENT("payment"),
    REVOKED("revoked"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");
}


enum class StatusEnum {
    SUCCESS,
    FAIL
}