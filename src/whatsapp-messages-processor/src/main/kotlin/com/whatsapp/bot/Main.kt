package com.whatsapp.bot

import org.http4k.server.Undertow
import org.http4k.server.asServer

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    App.handler.asServer(Undertow(port)).start().block()
}
