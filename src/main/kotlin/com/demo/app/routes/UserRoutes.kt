package com.demo.app.routes

import com.demo.app.models.PartialUserUpdate
import com.demo.app.models.User
import com.demo.app.db.UserDAO
import com.demo.app.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val userService by inject<UserService>()

    route("/api/users") {

        // GET /users
        get {
            call.respond(userService.getAllUsers())
        }

        // GET /users/{id}
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid or missing ID")
                return@get
            }
            val user = userService.getUser(id)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user)
            }
        }

        // POST /users
        post {
            val user = call.receive<User>()
            val savedUser = userService.createUser(user)
            call.respond(HttpStatusCode.Created, savedUser)
        }

        // PUT /users/{id}
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid or missing ID")
                return@put
            }
            val user = call.receive<User>()
            val updatedUser = userService.updateUser(id, user)
            if (updatedUser == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(updatedUser)
            }
        }

        // PATCH /users/{id}
        patch("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@patch
            }

            val updates = call.receive<PartialUserUpdate>()
            val updated = userService.patchUser(id, updates)

            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(updated)
            }
        }

        // DELETE /users/{id}
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid or missing ID")
                return@delete
            }
            val deleted = userService.deleteUser(id)
            if (deleted) {
                call.respondText("User deleted successfully.")
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
    }
}