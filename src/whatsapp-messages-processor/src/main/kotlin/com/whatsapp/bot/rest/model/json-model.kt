package com.whatsapp.bot.rest.model

import com.fasterxml.jackson.annotation.JsonAnySetter

abstract class DefaultParent {
    val notMappedAttributes = mutableMapOf<String, Any>()

    @JsonAnySetter
    private fun setNotMappedAttribute(key: String, value: Any) { notMappedAttributes[key] = value }
}