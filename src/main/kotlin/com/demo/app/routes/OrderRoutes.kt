package com.demo.app.routes

import com.demo.app.models.Order
import com.demo.app.models.PartialOrderUpdate
import com.demo.app.services.OrderService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.orderRoutes() {
    val orderService by inject<OrderService>()



    route("/api/users/{userId}/orders") {

        post {
            val userId = call.parameters["userId"]?.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@post
            }

            val order = call.receive<Order>()
            val created = orderService.createOrder(userId, order)
            call.respond(HttpStatusCode.Created, created)
        }

        get {
            val userId = call.parameters["userId"]?.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }

            val orders = orderService.getAllOrdersByUserId(userId)
            call.respond(orders)
        }

        get("/{orderId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (userId == null || orderId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user or order ID")
                return@get
            }

            val order = orderService.getOrder(userId, orderId)
            if (order == null) {
                call.respond(HttpStatusCode.NotFound, "Order not found")
            } else {
                call.respond(order)
            }
        }

        put("/{orderId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (userId == null || orderId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user or order ID")
                return@put
            }

            val order = call.receive<Order>()
            val updated = orderService.updateOrder(userId, orderId, order)
            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, "Order not found")
            } else {
                call.respond(updated)
            }
        }

        patch("/{orderId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (userId == null || orderId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user or order ID")
                return@patch
            }

            val updates = call.receive<PartialOrderUpdate>() // optionally use a PartialOrderUpdate DTO
            val patched = orderService.patchUpdateOrder(userId, orderId, updates)

            if (patched == null) {
                call.respond(HttpStatusCode.NotFound, "Order not found")
            } else {
                call.respond(patched)
            }
        }

        delete("/{orderId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (userId == null || orderId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user or order ID")
                return@delete
            }

            val deleted = orderService.deleteOrder(userId, orderId)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Order not found")
            }
        }
    }
}