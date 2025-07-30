package com.demo.app.routes

import com.demo.app.db.OrderDAO
import com.demo.app.db.Orders
import com.demo.app.db.Users
import com.demo.app.models.Order
import com.demo.app.models.PartialOrderUpdate
import com.demo.app.services.OrderService
import com.demo.app.services.OrderServiceImpl
import com.demo.app.services.UserService
import com.demo.app.services.UserServiceImpl
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderRouteIntegrationTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
            transaction {
                SchemaUtils.create(Users, Orders)
            }
        }
    }

    @AfterEach
    fun cleanup() {
        transaction {
            Orders.deleteAll()
            Users.deleteAll()
        }
    }

    private fun Application.testModule() {
        install(Koin) {
            modules(
                module {
                    single { OrderDAO() }
                    single { UserServiceImpl(get()) as UserService }
                    single { OrderServiceImpl(get()) as OrderService }
                }
            )
        }
        install(ContentNegotiation) { json() }
        routing { orderRoutes() }
    }

    private fun createTestUser(): Long {
        return transaction {
            Users.insertAndGetId {
                it[firstName] = "Test"
                it[lastName] = "User"
                it[email] = "test@example.com"
                it[firstLineOfAddress] = "123 Street"
                it[secondLineOfAddress] = null
                it[town] = "City"
                it[postCode] = "00000"
            }.value
        }
    }

    private fun createTestOrder(userId: Long): Order {
        val order = Order(
            description = "Sample order",
            priceInPence = 9999,
            completedStatus = false,
            userId = userId
        )
        return OrderDAO().add(userId, order)
    }

    @Test
    fun `Post an Order and Get it`() = testApplication {
        application { testModule() }

        val userId = createTestUser()
        val newOrder = Order(description = "New Order", priceInPence = 5000, completedStatus = false, userId = userId)

        val postResponse = client.post("/api/users/$userId/orders") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(newOrder))
        }

        assertEquals(HttpStatusCode.Created, postResponse.status)
        val createdOrder = Json.decodeFromString<Order>(postResponse.bodyAsText())
        assertEquals("New Order", createdOrder.description)

        val getResponse = client.get("/api/users/$userId/orders/${createdOrder.id}")
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val fetchedOrder = Json.decodeFromString<Order>(getResponse.bodyAsText())
        assertEquals(createdOrder, fetchedOrder)
    }

    @Test
    fun `Patch an Order should update fields`() = testApplication {
        application { testModule() }

        val userId = createTestUser()
        val order = createTestOrder(userId)

        val patch = PartialOrderUpdate(description = "Updated", priceInPence = 7000)

        val patchResponse = client.patch("/api/users/$userId/orders/${order.id}") {
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
        application { testModule() }

        val userId = createTestUser()
        val originalOrder = createTestOrder(userId)

        val updatedOrder = originalOrder.copy(
            description = "Fully updated",
            priceInPence = 10000,
            completedStatus = true
        )

        val response = client.put("/api/users/$userId/orders/${originalOrder.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updatedOrder))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Fully updated"))
    }

    @Test
    fun `Delete Order should remove order`() = testApplication {
        application { testModule() }

        val userId = createTestUser()
        val order = createTestOrder(userId)

        val deleteResponse = client.delete("/api/users/$userId/orders/${order.id}")
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val getResponse = client.get("/api/users/$userId/orders/${order.id}")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}
