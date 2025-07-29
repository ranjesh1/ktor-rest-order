package com.demo.app.db

import com.demo.app.models.Order
import com.demo.app.models.PartialOrderUpdate
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import kotlin.test.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderDAOTest {

    private val dao = OrderDAO()

    @BeforeAll
    fun setupDatabase() {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
            user = "root",
            password = ""
        )
        transaction {
            create(Users, Orders)
        }
    }

    @AfterEach
    fun cleanUp() {
        transaction {
            Orders.deleteAll()
            Users.deleteAll()
        }
    }

    @AfterAll
    fun tearDown() {
        transaction {
            drop(Orders, Users)
        }
    }

    private fun createTestOrder(
        id: Long? = null,
        description: String = "Test Order",
        priceInPence: Long = 100L,
        completedStatus: Boolean = false,
        userId: Long = 1L
    ) = Order(
        id,
        description,
        priceInPence,
        completedStatus,
        userId,
    )


    object Users : LongIdTable("users") {
        val firstName = varchar("first_name", length = 50)
        val lastName = varchar("last_name", length = 50)
        val email = varchar("email", length = 120)
        val firstLineOfAddress = varchar("first_line_of_address", length = 50)
        val secondLineOfAddress = varchar("second_line_of_address", length = 50).nullable()
        val town = varchar("town", length = 50)
        val postCode = varchar("post_code", length = 10)

    }

    @Test
    fun `add and get order`() {

        val userId = transaction {
            Users.insertAndGetId {
                it[firstName] = "testuser"
                it[lastName] = "testuserLastName"
                it[email] = "test@example.com"
                it[firstLineOfAddress] = "1st Street"
                it[secondLineOfAddress] = "1st Block"
                it[town] = "Town"
                it[postCode] = "AB 123"
            }.value
        }

        val saved = dao.add(userId, createTestOrder(userId = userId))
        assertNotNull(saved.id)

        val loaded = dao.getbyIdAndUserId(userId, saved.id!!)
        assertEquals(saved, loaded)
    }

    @Test
    fun `update order`() {
        val userId = transaction {
            Users.insertAndGetId {
                it[firstName] = "testuser"
                it[lastName] = "testuserLastName"
                it[email] = "test@example.com"
                it[firstLineOfAddress] = "1st Street"
                it[secondLineOfAddress] = "1st Block"
                it[town] = "Town"
                it[postCode] = "AB 123"
            }.value
        }

        val saved = dao.add(userId, createTestOrder(userId = userId))

        val updateOrder = createTestOrder(
            saved.id!!,
            description = "Updated Test Order",
            priceInPence = 101L,
            completedStatus = true,
            userId = userId
        )

        val updated = dao.update(userId, saved.id!!, updateOrder)
        assertEquals("Updated Test Order", updated?.description)
        assertEquals(101L, updated?.priceInPence)

    }

    @Test
    fun `patch order`() {

        val userId = transaction {
            Users.insertAndGetId {
                it[firstName] = "testuser"
                it[lastName] = "testuserLastName"
                it[email] = "test@example.com"
                it[firstLineOfAddress] = "1st Street"
                it[secondLineOfAddress] = "1st Block"
                it[town] = "Town"
                it[postCode] = "AB 123"
            }.value
        }

        val saved = dao.add(userId, createTestOrder(userId = userId))

        val patch = PartialOrderUpdate(description = "Patched", completedStatus = true)
        val patched = dao.patchUpdate(userId, saved.id!!, patch)

        assertEquals("Patched", patched?.description)
        assertEquals(true, patched?.completedStatus)

    }

    @Test
    fun `delete order`() {
        val userId = transaction {
            Users.insertAndGetId {
                it[firstName] = "testuser"
                it[lastName] = "testuserLastName"
                it[email] = "test@example.com"
                it[firstLineOfAddress] = "1st Street"
                it[secondLineOfAddress] = "1st Block"
                it[town] = "Town"
                it[postCode] = "AB 123"
            }.value
        }

        val saved = dao.add(userId, createTestOrder(userId = userId))

        val deleted = dao.deleteByIdAndUserId(saved.id!!, userId)
        assertTrue(deleted)

        val shouldBeNull = dao.getbyIdAndUserId(userId, saved.id!!)
        assertNull(shouldBeNull)
    }
}