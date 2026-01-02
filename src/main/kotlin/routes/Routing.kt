package com.test.routes

import com.test.db.Users
import com.test.db.Users.age
import com.test.db.Users.email
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
data class User(
    val id: Int? = null,
    val name: String,
    val email: String? = null,
    val age: Int? = null
)

@Serializable
data class UserPatch(
    val name: String? = null,
    val email: String? = null,
    val age: Int? = null
)
fun Application.configureRouting() {
    routing {
        get("/users") {
            val users = transaction {
                Users.selectAll().map { User(
                    id = it[Users.id],
                    name = it[Users.name],
                    email = it[Users.email],
                    age = it[Users.age]
                    )
                }
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
                    .map {
                        User(
                            it[Users.id],
                            it[Users.name],
                            email = it[Users.email],
                            age = it[Users.age]
                        )
                    }
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
                    it[email] = user.email
                    it[age] = user.age
                } get Users.id
            }
            call.respond(
                HttpStatusCode.Created,
                User(id = id, name = user.name, email = user.email, age = user.age)
            )
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
                    it[email] = user.email
                    it[age] = user.age
                }
            }

            if (updated == 0) {
                call.respond(HttpStatusCode.NotFound, "user not found")
            } else {
                call.respond(User(id = id, name = user.name, email = user.email, age = user.age))
            }
        }

        patch("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Id")
                return@patch
            }

            val patch = call.receive<User>()
            val updated = transaction {
                Users.update({ Users.id eq id }) {
                    patch.name?.let { name -> it[Users.name] = name }
                    patch.email?.let { email -> it[Users.email] = email }
                    patch.age?.let { age -> it[Users.age] = age }
                }
            }

            if (updated == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                val updatedUser = transaction {
                    Users.select { Users.id eq id }
                        .map { User(
                            id = it[Users.id],
                            name = it[Users.name],
                            email = it[Users.email],
                            age = it[Users.age]
                        )
                        }
                        .single()
                }
                call.respond(updatedUser)
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

        options("/users/{id?}") {
            call.response.header("Allow", "GET, POST, PATCH, DELETE, OPTIONS")
            call.respond(HttpStatusCode.OK)
        }
    }
}
