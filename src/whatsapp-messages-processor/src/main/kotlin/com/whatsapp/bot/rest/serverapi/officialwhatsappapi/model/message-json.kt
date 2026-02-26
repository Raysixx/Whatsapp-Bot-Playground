package com.whatsapp.bot.rest.serverapi.officialwhatsappapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.whatsapp.bot.rest.model.DefaultParent

data class OfficialWhatsappPayload(
    @JsonProperty("object")
    val obj: String,

    @JsonProperty("entry")
    val entries: List<OfficialWhatsappEntry>
): DefaultParent() {
    private fun <T> getFromChange(func: (OfficialWhatsappChange) -> List<T>): T? {
        val objs = entries.flatMap { entry ->
            entry.changes.flatMap { change ->
                func(change)
            }
        }

        return when (objs.size) {
            0 -> null
            1 -> objs.first()
            else -> throw RuntimeException("More than 1 object, what to do ?")
        }
    }

    val isMessage = getFromChange { listOf(it.field == OfficialWhatsappFieldEnum.MESSAGES) }!!

    fun getMessageObj(): OfficialWhatsappMessage? {
        return getFromChange { change ->
            if (change.field != OfficialWhatsappFieldEnum.MESSAGES || change.value.messages == null) {
                emptyList()
            } else {
                change.value.messages
            }
        }
    }

    fun getMessageText(): String? {
        return getMessageObj()?.text?.body
    }

    fun getContactName(): String? {
        return getFromChange { change ->
            if (change.value.contacts == null) {
                emptyList()
            } else {
                change.value.contacts.map {
                    it.profile.name
                }
            }
        }
    }

    fun getContactNumber(): String? {
        return getFromChange { change ->
            if (change.value.contacts == null) {
                emptyList()
            } else {
                change.value.contacts.map {
                    it.waId
                }
            }
        }
    }

    fun getStatusObj(): OfficialWhatsappStatus? {
        return getFromChange { change ->
            if (change.value.statuses == null) {
                emptyList()
            } else {
                change.value.statuses.map {
                    it
                }
            }
        }
    }

    fun getStatus(): OfficialWhatsappStatusEnum? {
        return getStatusObj()?.status
    }

    fun getError(status: OfficialWhatsappStatus? = getStatusObj()): OfficialWhatsappError? {
        return if (status?.errors == null) {
            null
        } else {
            val statuses = status.errors.map {
                it
            }

            return when (statuses.size) {
                0 -> null
                1 -> statuses.first()
                else -> throw RuntimeException("More than 1 object, what to do ?")
            }
        }
    }
}

data class OfficialWhatsappEntry(
    val id: String,
    val changes: List<OfficialWhatsappChange>
): DefaultParent()

data class OfficialWhatsappChange(
    val value: OfficialWhatsappChangeValue,
    val field: OfficialWhatsappFieldEnum
): DefaultParent()

data class OfficialWhatsappChangeValue(
    @JsonProperty("messaging_product")
    val messagingProduct: String,

    val metadata: OfficialWhatsappMetadata,
    val contacts: List<OfficialWhatsappContact>? = null,
    val messages: List<OfficialWhatsappMessage>? = null,
    val statuses: List<OfficialWhatsappStatus>? = null
): DefaultParent()

data class OfficialWhatsappMetadata(
    @JsonProperty("display_phone_number")
    val displayPhoneNumber: String,

    @JsonProperty("phone_number_id")
    val phoneNumberId: String
): DefaultParent()

data class OfficialWhatsappContact(
    val profile: OfficialWhatsappProfile,

    @JsonProperty("wa_id")
    val waId: String
): DefaultParent()

data class OfficialWhatsappProfile(
    val name: String
): DefaultParent()

data class OfficialWhatsappMessage(
    val from: String,
    val id: String,
    val timestamp: String,
    val text: OfficialWhatsappTextContent? = null,
    val type: String
): DefaultParent()

data class OfficialWhatsappTextContent(
    val body: String
): DefaultParent()

data class OfficialWhatsappStatus(
    val id: String,
    val status: OfficialWhatsappStatusEnum,
    val timestamp: String,

    @JsonProperty("recipient_id")
    val recipientId: String,

    val errors: List<OfficialWhatsappError>? = null
): DefaultParent()

data class OfficialWhatsappError(
    val code: Int,
    val title: String,
    val message: String? = null
): DefaultParent()