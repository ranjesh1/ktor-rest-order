package com.demo.app.routes

import com.demo.app.models.Order
import com.demo.app.models.PartialOrderUpdate
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderRouteIntegrationTest : IntegrationTestBase() {


    @Test
    fun `Post an Order and Get it`() = testApplication {
        application {
            orderTestModule()
            routing { orderRoutes() }
        }

        val user = createTestUser()
        val newOrder = createOrder(user.id!!)

        val postResponse = client.post("/api/users/${user.id}/orders") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(newOrder))
        }

        assertEquals(HttpStatusCode.Created, postResponse.status)
        val createdOrder = Json.decodeFromString<Order>(postResponse.bodyAsText())
        assertEquals(newOrder.description, createdOrder.description)

        val getResponse = client.get("/api/users/${user.id}/orders/${createdOrder.id}")
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val fetchedOrder = Json.decodeFromString<Order>(getResponse.bodyAsText())
        assertEquals(createdOrder, fetchedOrder)
    }

    @Test
    fun `Patch an Order should update fields`() = testApplication {
        application {
            orderTestModule()
            routing { orderRoutes() }
        }

        val user = createTestUser()
        val order = createTestOrder(user.id!!)

        val patch = PartialOrderUpdate(description = "Updated", priceInPence = 7000)

        val patchResponse = client.patch("/api/users/${user.id}/orders/${order.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(patch))
        }

        assertEquals(HttpStatusCode.OK, patchResponse.status)
        val updatedOrder = Json.decodeFromString<Order>(patchResponse.bodyAsText())
        assertEquals("Updated", updatedOrder.description)
        assertEquals(7000, updatedOrder.priceInPence)
    }

    @Test
    fun `Put an Order should replace all fields`() = testApplication {
        application {
            orderTestModule()
            routing { orderRoutes() }
        }

        val user = createTestUser()
        val originalOrder = createTestOrder(user.id!!)

        val updatedOrder = originalOrder.copy(
            description = "Fully updated",
            priceInPence = 10000,
            completedStatus = true
        )

        val response = client.put("/api/users/${user.id}/orders/${originalOrder.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updatedOrder))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Fully updated"))
    }

    @Test
    fun `Delete Order should remove order`() = testApplication {
        application {
            orderTestModule()
            routing { orderRoutes() }
        }

        val user = createTestUser()
        val order = createTestOrder(user.id!!)

        val deleteResponse = client.delete("/api/users/${user.id}/orders/${order.id}")
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val getResponse = client.get("/api/users/${user.id}/orders/${order.id}")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}
