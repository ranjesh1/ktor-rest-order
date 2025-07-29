package com.demo.app.routes

import com.demo.app.models.PartialUserUpdate
import com.demo.app.models.User
import com.demo.app.services.UserService
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
import kotlin.test.assertTrue

class UserRoutesTest {

    private val json = Json {
        prettyPrint = true
        isLenient = true
    }
    val mockUserService = mockk<UserService>(relaxed = true)


    private fun Application.testModule(mockUserService: UserService) {
        if (pluginOrNull(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) == null) {
            install(ContentNegotiation) {
                json(Json { prettyPrint = true; isLenient = true })
            }
        }

        install(Koin) {
            modules(
                module {
                    single { mockUserService }
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
            userRoutes()
        }
    }

    private inline fun <reified T> assertJsonEquals(expectedObj: T, actual: String) {
        val expected = json.encodeToString(expectedObj)
        val expectedElement = json.parseToJsonElement(expected)
        val actualElement = json.parseToJsonElement(actual)
        assertEquals(expectedElement, actualElement)
    }

    @Test
    fun `Get all users should return list`() = testApplication {
        application { testModule(mockUserService) }
        val testUsers = listOf(
            User(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                email = "john@example.com",
                firstLineOfAddress = "123 Main St",
                secondLineOfAddress = null,
                town = "Townsville",
                postCode = "12345"
            )
        )
        every { mockUserService.getAllUsers() } returns testUsers

        val response = client.get("/api/users")
        assertEquals(HttpStatusCode.OK, response.status)
        assertJsonEquals(testUsers, response.bodyAsText())
    }

    @Test
    fun `Get user by ID should return user`() = testApplication {
        application { testModule(mockUserService) }

        val testUser = User(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            firstLineOfAddress = "123 Main St",
            secondLineOfAddress = null,
            town = "Townsville",
            postCode = "12345"
        )
        every { mockUserService.getUser(1) } returns testUser

        val response = client.get("/api/users/1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertJsonEquals(testUser, response.bodyAsText())

    }


    @Test
    fun `Get user by ID should return 404 if not found`() = testApplication {
        application { testModule(mockUserService) }

        every { mockUserService.getUser(999) } returns null

        val response = client.get("/api/users/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `Post user should return created user`() = testApplication {
        application { testModule(mockUserService) }

        val newUser = User(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            firstLineOfAddress = "123 Main St",
            secondLineOfAddress = null,
            town = "Townsville",
            postCode = "12345"
        )
        val savedUser = newUser.copy(id = 10)

        every { mockUserService.createUser(any()) } returns savedUser

        val jsonBody = Json.encodeToString(User.serializer(), newUser)

        val response = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.Created, response.status)

        assertJsonEquals(savedUser, response.bodyAsText())
    }


    @Test
    fun `Put user should return updated user`() = testApplication {
        application { testModule(mockUserService) }

        val updatedUser = User(
            firstName = "Updated",
            lastName = "Doe",
            email = "updated@example.com",
            firstLineOfAddress = "123 Main St",
            secondLineOfAddress = null,
            town = "Townsville",
            postCode = "12345"
        )
        every { mockUserService.updateUser(1, any()) } returns updatedUser

        val jsonBody = Json.encodeToString(User.serializer(), updatedUser)

        val response = client.put("/api/users/1") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        assertJsonEquals(updatedUser, response.bodyAsText())

    }

    @Test
    fun `Put user should return 404 if not found`() = testApplication {
        application { testModule(mockUserService) }

        every { mockUserService.updateUser(1, any()) } returns null

        val updatedUser = User(
            firstName = "Updated",
            lastName = "Doe",
            email = "updated@example.com",
            firstLineOfAddress = "123 Main St",
            secondLineOfAddress = null,
            town = "Townsville",
            postCode = "12345"
        )

        val jsonBody = Json.encodeToString(User.serializer(), updatedUser)
        val response = client.put("/api/users/1") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `Patch user should return patched user`() = testApplication {
        application { testModule(mockUserService) }

        val partial = PartialUserUpdate(firstName = "Patched", email = null)
        val patchedUser = User(
            id = 1,
            firstName = "Patched",
            lastName = "Doe",
            email = "patched@example.com",
            firstLineOfAddress = "123 Main St",
            secondLineOfAddress = null,
            town = "Townsville",
            postCode = "12345"
        )
        every { mockUserService.patchUser(1, partial) } returns patchedUser

        val response = client.patch("/api/users/1") {
            contentType(ContentType.Application.Json)
            setBody("""{"firstName":"Patched"}""")
        }

        assertJsonEquals(patchedUser, response.bodyAsText())

    }

    @Test
    fun `Patch user should return 404 if not found`() = testApplication {
        application { testModule(mockUserService) }


        val partial = PartialUserUpdate(firstName = "Nonexistent", email = null)
        every { mockUserService.patchUser(1, partial) } returns null

        val response = client.patch("/api/users/1") {
            contentType(ContentType.Application.Json)
            setBody("""{"firstName":"Nonexistent"}""")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `Delete user should return success`() = testApplication {
        application { testModule(mockUserService) }

        every { mockUserService.deleteUser(1) } returns true

        val response = client.delete("/api/users/1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("User deleted"))
    }

    @Test
    fun `Delete user should return 404 if not found`() = testApplication {
        application { testModule(mockUserService) }

        every { mockUserService.deleteUser(1) } returns false

        val response = client.delete("/api/users/1")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
