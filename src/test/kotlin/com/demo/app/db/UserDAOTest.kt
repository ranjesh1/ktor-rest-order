package com.demo.app.db

import com.demo.app.models.PartialUserUpdate
import com.demo.app.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {

    private val dao = UserDAO()

    @BeforeAll
    fun setupDatabase() {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
            user = "root",
            password = ""
        )
        transaction {
            create(Users)
        }
    }

    @AfterEach
    fun cleanUp() {
        transaction {
            Users.deleteAll()
        }
    }

    @AfterAll
    fun tearDown() {
        transaction {
            drop(Users)
        }
    }

    private fun createTestUser(
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john@example.com",
        address: String = "123 Main St",
        secondLine: String? = null,
        town: String = "Springfield",
        postCode: String = "12345"
    ) = User(
        firstName = firstName,
        lastName = lastName,
        email = email,
        firstLineOfAddress = address,
        secondLineOfAddress = secondLine,
        town = town,
        postCode = postCode
    )

    @Test
    fun `add and get user`() {
        val saved = dao.add(createTestUser())
        assertNotNull(saved.id)

        val loaded = dao.getById(saved.id!!)
        assertEquals(saved, loaded)
    }

    @Test
    fun `update user`() {
        val saved = dao.add(createTestUser(firstName = "Old", email = "old@example.com"))
        val updatedUser = saved.copy(firstName = "New", email = "new@example.com")

        val updated = dao.update(saved.id!!, updatedUser)
        assertEquals("New", updated?.firstName)
        assertEquals("new@example.com", updated?.email)

    }

    @Test
    fun `patch user`() {
        val saved = dao.add(createTestUser(firstName = "Jane", email = "jane@example.com"))

        val patch = PartialUserUpdate(firstName = "Janet", email = "janet@example.com")
        val patched = dao.patch(saved.id!!, patch)

        assertEquals("Janet", patched?.firstName)
        assertEquals("janet@example.com", patched?.email)

    }

    @Test
    fun `delete user`() {
        val saved = dao.add(createTestUser(firstName = "ToDelete", email = "del@example.com"))
        assertTrue(dao.delete(saved.id!!))
        assertNull(dao.getById(saved.id!!))
    }
}