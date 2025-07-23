package com.demo.app.services

import com.demo.app.db.UserDAO
import com.demo.app.models.PartialUserUpdate
import com.demo.app.models.User
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserServiceTest {
    private val userDAO: UserDAO = mockk()
    private lateinit var userService: UserService

    private val dummyUser = User(
        id = 1L,
        firstName = "John",
        lastName = "Doe",
        email = "john@example.com",
        firstLineOfAddress = "123 Main St",
        secondLineOfAddress = null,
        town = "Townsville",
        postCode = "12345"
    )

    @BeforeEach
    fun setUp() {
        userService = UserServiceImpl(userDAO)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getUser returns user successfully`() {
        every { userDAO.getById(1L) } returns dummyUser

        val result = userService.getUser(1L)
        assertNotNull(result)
        assertEquals("John", result.firstName)
    }

    @Test
    fun `getAllUsers returns list of users successfully`() {
        val users = listOf(
            User(1, "John", "Doe", "john@example.com", "123 Street", "Apt 1", "City", "12345"),
            User(2, "Jane", "Smith", "jane@example.com", "456 Avenue", "Apt 2", "Town", "67890")
        )
        every { userDAO.getAll() } returns users

        val result = userService.getAllUsers()

        assertEquals(2, result.size)
        assertEquals("John", result[0].firstName)
        verify { userDAO.getAll() }
    }

    @Test
    fun `createUser creates User successfully`() {
        every { userDAO.add(any()) } returns dummyUser

        val created = userService.createUser(dummyUser)
        assertEquals(dummyUser, created)
    }

    @Test
    fun `deleteUser deletes User successfully`() {
        every { userDAO.delete(1L) } returns true

        val result = userService.deleteUser(1L)
        assertTrue(result)
    }


    @Test
    fun `patchUser updates partial fields successfully`() {
        val patch = PartialUserUpdate(email = "patched@example.com")
        val patchedUser = User(1, "Patched", "User", "patched@example.com", "Address", "", "City", "22222")

        every { userDAO.patch(1, patch) } returns patchedUser

        val result = userService.patchUser(1, patch)

        assertEquals(patchedUser, result)
        verify { userDAO.patch(1, patch) }
    }

    @Test
    fun `updateUser updates and returns user successfully`() {
        val user = User(1, "Updated", "Name", "updated@example.com", "New Address", "", "City", "00000")
        every { userDAO.update(1, user) } returns user

        val result = userService.updateUser(1, user)

        assertEquals(user, result)
        verify { userDAO.update(1, user) }
    }
}