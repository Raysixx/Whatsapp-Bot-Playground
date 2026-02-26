package com.whatsapp.bot.rest.serverapi.baileys.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class RawBaileysIdDeserializer: JsonDeserializer<String>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): String {
        return if (parser.text.contains(":")) {
            "${parser.text.substringBefore("@").substringBeforeLast(":")}@${parser.text.substringAfter("@")}"
        } else {
            parser.text
        }
    }
}