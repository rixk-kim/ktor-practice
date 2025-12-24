package com.test.routes

import com.test.db.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class User(val id: Int? = null, val name: String)
fun Application.configureRouting() {
    routing {
        get("/users") {
            val users = transaction {
                Users.selectAll().map { User(it[Users.id], it[Users.name]) }
            }
            call.respond(users)
        }

        post("/users") {
            val user = call.receive<User>()
            val id = transaction {
                Users.insert {
                    it[name] = user.name
                } get Users.id
            }
            call.respond(HttpStatusCode.Created, User(id, user.name))
        }
    }
}
