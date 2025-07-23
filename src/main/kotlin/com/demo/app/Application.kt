package com.demo.app

import com.demo.app.routes.userRoutes
import com.demo.app.db.DatabaseFactory
import com.demo.app.di.appModule
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(Koin) {
        modules(appModule)
    }

    DatabaseFactory.init()
    install(ContentNegotiation) {
        json(Json { prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            // Note: DO NOT enable polymorphism
        })
    }

    routing {
        userRoutes()
        /*route("/api/users") {
            get {
                call.respond(users.toList())
            }

            get("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                val user = users.find { it.id == id }
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respondText("User not found", status = io.ktor.http.HttpStatusCode.NotFound)
                }
            }

            post {
                val user = call.receive<User>()
                users.add(user)
                call.respondText("User added", status = io.ktor.http.HttpStatusCode.Created)
            }
        }*/
    }
}




//val users = mutableListOf(
//    User(1, "Alice"),
//    User(2, "Bob")
//)