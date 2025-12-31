package com.test.routes

import com.test.db.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

        get("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadGateway, "Invalid Id")
                return@get
            }

            val user = transaction {
                Users.select { Users.id eq id }
                    .map { User(it[Users.id], it[Users.name])}
                    .singleOrNull()
            }

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "user not found")
            } else {
                call.respond(user)
            }
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

        put("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Id")
                return@put
            }

            val user = call.receive<User>()
            val updated = transaction {
                Users.update({ Users.id eq id }) {
                    it[name] = user.name
                }
            }

            if (updated == 0) {
                call.respond(HttpStatusCode.NotFound, "user not found")
            } else {
                call.respond(User(id, user.name))
            }
        }

        delete("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Id")
                return@delete
            }

            val deleted = transaction {
                Users.deleteWhere { Users.id eq id }
            }

            if (deleted == 0) {
                call.respond(HttpStatusCode.NotFound, "user not found")
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        patch("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Id")
                return@patch
            }

            val user = call.receive<User>()
            val updated = transaction {
                Users.update({ Users.id eq id }) {
                    it[name] = user.name
                }
            }

            if (updated == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                val updatedUser = transaction {
                    Users.select { Users.id eq id }
                    .map { User(it[Users.id], it[Users.name]) }
                        .single()
                }
                call.respond(updatedUser)
            }
        }
    }
}
