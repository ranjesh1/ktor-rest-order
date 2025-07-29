package com.demo.app.routes

import com.demo.app.db.UserDAO
import com.demo.app.db.Users
import com.demo.app.models.PartialUserUpdate
import com.demo.app.models.User
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
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserRouteIntegrationTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            Database.connect(
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
                driver = "org.h2.Driver",
                user = "root",
                password = ""
            )
            transaction { SchemaUtils.create(Users) }
        }
    }

    @AfterEach
    fun cleanup() {
        transaction {
            com.demo.app.db.Users.deleteAll()
        }
    }

    private fun Application.testModule() {
        install(Koin) {
            modules(
                module {
                    single { UserDAO() }
                    single<UserService> { UserServiceImpl(get()) }
                }
            )
        }
        install(ContentNegotiation) {
            json()
        }
        routing { userRoutes() }
    }

    private fun createTestUser(): User {
        return UserDAO().add(
            User(
                firstName = "Jane",
                lastName = "Doe",
                email = "jane@example.com",
                firstLineOfAddress = "123 Lane",
                secondLineOfAddress = null,
                town = "Townsville",
                postCode = "12345"
            )
        )
    }

    private fun createUser(): User {
        return User(
            firstName = "Jane",
            lastName = "Doe",
            email = "jane@example.com",
            firstLineOfAddress = "123 Lane",
            secondLineOfAddress = null,
            town = "Townsville",
            postCode = "12345"
        )
    }

    @Test
    fun `Post a User  and Get same user`() = testApplication {

        application { testModule() }

        val newUser = createUser()

        // POST /api/users
        val postResponse = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(newUser))
        }
        assertEquals(HttpStatusCode.Created, postResponse.status)

        val createdUser = Json.decodeFromString<User>(postResponse.bodyAsText())
        assertEquals("Jane", createdUser.firstName)
        assertEquals("Doe", createdUser.lastName)
        assertEquals("jane@example.com", createdUser.email)

        // GET /api/users/{id}
        val getResponse = client.get("/api/users/${createdUser.id}")
        assertEquals(HttpStatusCode.OK, getResponse.status)

        val fetchedUser = Json.decodeFromString<User>(getResponse.bodyAsText())
        assertEquals(createdUser, fetchedUser)
    }

    @Test
    fun `Patch user should update partial fields`() = testApplication {
        application { testModule() }
        val existingUser = createTestUser()

        val patch = PartialUserUpdate(firstName = "Janet", email = "janet@example.com")

        val response = client.patch("/api/users/${existingUser.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(patch))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Janet"))
        assertTrue(body.contains("janet@example.com"))
    }

    @Test
    fun `Put user should replace user info`() = testApplication {
        application { testModule() }
        val existingUser = createTestUser()

        val updatedUser = existingUser.copy(
            firstName = "Updated",
            lastName = "Person",
            email = "updated@example.com"
        )

        val response = client.put("/api/users/${existingUser.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updatedUser))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Updated"))
        assertTrue(body.contains("updated@example.com"))
    }

    @Test
    fun `Delete user should remove user`() = testApplication {
        application { testModule() }
        val existingUser = createTestUser()

        val response = client.delete("/api/users/${existingUser.id}")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("User deleted successfully."))

        val getResponse = client.get("/api/users/${existingUser.id}")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

}