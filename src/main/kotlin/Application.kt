package com.test

import com.test.db.Database
import com.test.plugins.configureSerialization
import com.test.routes.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    Database.init(environment)
    configureSerialization()
    configureRouting()
}
