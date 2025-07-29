package com.demo.app.routes

import com.demo.app.models.Order
import com.demo.app.models.PartialOrderUpdate
import com.demo.app.services.OrderService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals

class OrderRoutesTest {

    private val json = Json {
        prettyPrint = true
        isLenient = true
    }
    val mockOrderService = mockk<OrderService>(relaxed = true)


    private fun Application.testModule(mockOrderService: OrderService) {
        if (pluginOrNull(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) == null) {
            install(ContentNegotiation) {
                json(Json { prettyPrint = true; isLenient = true })
            }
        }

        install(Koin) {
            modules(
                module {
                    single { mockOrderService }
                }
            )
        }
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                cause.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
            }
        }
        routing {
            orderRoutes()
        }
    }

    private inline fun <reified T> assertJsonEquals(expectedObj: T, actual: String) {
        val expected = json.encodeToString(expectedObj)
        val expectedElement = json.parseToJsonElement(expected)
        val actualElement = json.parseToJsonElement(actual)
        assertEquals(expectedElement, actualElement)
    }


    @Test
    fun `Get order by ID should return order`() = testApplication {
        application { testModule(mockOrderService) }

        val order = Order(10L, "A book", 1500L, false, userId = 1L)
        every { mockOrderService.getOrder(1L, 10L) } returns order

        val response = client.get("/api/users/1/orders/10")
        assertEquals(HttpStatusCode.OK, response.status)
        assertJsonEquals(order, response.bodyAsText())

    }

    @Test
    fun `Get all orders should return list of orders`() = testApplication {
        application { testModule(mockOrderService) }
        val orders = listOf(
            Order(10L, "A book", 1500L, false, userId = 1L)
        )
        every { mockOrderService.getAllOrdersByUserId(1L) } returns orders

        val response = client.get("/api/users/1/orders")
        assertEquals(HttpStatusCode.OK, response.status)
        assertJsonEquals(orders, response.bodyAsText())
    }

    @Test
    fun `Post order should return created order`() = testApplication {
        application { testModule(mockOrderService) }

        val newOrder = Order(description = "A book", priceInPence = 1500L, completedStatus = false, userId = 1L)
        val savedOrder = newOrder.copy(id = 10)

        every { mockOrderService.createOrder(1L, newOrder) } returns savedOrder

        val jsonBody = Json.encodeToString(Order.serializer(), newOrder)

        val response = client.post("/api/users/1/orders") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.Created, response.status)

        assertJsonEquals(savedOrder, response.bodyAsText())
    }


    @Test
    fun `Put order should return updated order`() = testApplication {
        application { testModule(mockOrderService) }

        val updateOrder = Order(description = "A book", priceInPence = 1500L, completedStatus = false, userId = 1L)
        val updatedOrder = updateOrder.copy(id = 10)
        every { mockOrderService.updateOrder(1L, 10L, updateOrder) } returns updatedOrder

        val jsonBody = Json.encodeToString(Order.serializer(), updateOrder)

        val response = client.put("/api/users/1/orders/10") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        assertJsonEquals(updatedOrder, response.bodyAsText())

    }


    @Test
    fun `Patch order should return patched order`() = testApplication {
        application { testModule(mockOrderService) }

        val partial = PartialOrderUpdate(description = "Patched", completedStatus = true)
        val patchedOrder = Order(10L, "Patched", 1500L, true, userId = 1L)
        every { mockOrderService.patchUpdateOrder(1L, 10L, partial) } returns patchedOrder

        val response = client.patch("/api/users/1/orders/10") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(PartialOrderUpdate.serializer(), partial))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertJsonEquals(patchedOrder, response.bodyAsText())

    }

    @Test
    fun `Delete order should return success`() = testApplication {
        application { testModule(mockOrderService) }

        every { mockOrderService.deleteOrder(1L, 10L) } returns true

        val response = client.delete("/api/users/1/orders/10")
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `Get order by ID should return 404 if not found`() = testApplication {
        application { testModule(mockOrderService) }

        every { mockOrderService.getOrder(1L, 999L) } returns null

        val response = client.get("/api/users/1/orders/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }


    @Test
    fun `Put order should return 404 if not found`() = testApplication {
        application { testModule(mockOrderService) }

        every { mockOrderService.updateOrder(1, 99L, any()) } returns null

        val updateOrder = Order(description = "A book", priceInPence = 1500L, completedStatus = false, userId = 1L)


        val jsonBody = Json.encodeToString(Order.serializer(), updateOrder)
        val response = client.put("/api/users/1/orders/99") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `Patch order should return 404 if not found`() = testApplication {
        application { testModule(mockOrderService) }


        val partial = PartialOrderUpdate(description = "Patched", completedStatus = true)
        every { mockOrderService.patchUpdateOrder(1, 99L, partial) } returns null

        val response = client.patch("/api/users/1/orders/99") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(PartialOrderUpdate.serializer(), partial))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `Delete order should return 404 if not found`() = testApplication {
        application { testModule(mockOrderService) }

        every { mockOrderService.deleteOrder(1L, 99L) } returns false

        val response = client.delete("/api/users/1/orders/99")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }


}
