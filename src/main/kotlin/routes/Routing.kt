package com.test.routes

import com.test.db.Database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/health/db") {
            Database.dataSource.connection.use { conn ->
                call.respondText("DB OK: ${conn.metaData.databaseProductName}")
            }
        }
    }
}
