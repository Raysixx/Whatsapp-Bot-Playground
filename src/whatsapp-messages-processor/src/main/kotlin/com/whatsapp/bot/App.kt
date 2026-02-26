package com.whatsapp.bot

import com.whatsapp.bot.persistence.MemoryPersistence
import com.whatsapp.bot.persistence.Persister
import com.whatsapp.bot.rest.controller.WhatsAppController
import org.http4k.core.HttpHandler
import java.util.*

object App {
    val handler: HttpHandler = WhatsAppController.handler
    val persister: Persister = ServiceLoader.load(Persister::class.java).firstOrNull { it.accepts() } ?: MemoryPersistence()
}