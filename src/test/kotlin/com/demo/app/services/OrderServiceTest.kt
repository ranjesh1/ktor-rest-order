package com.demo.app.services

import com.demo.app.db.OrderDAO
import com.demo.app.models.Order
import com.demo.app.models.PartialOrderUpdate
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrderServiceTest {
    private val orderDAO: OrderDAO = mockk()
    private lateinit var service: OrderService

    @BeforeEach
    fun setup() {
        service = OrderServiceImpl(orderDAO)
    }

    @Test
    fun `createOrder creates Order successfully`() {
        val order = Order(null, "Test Order", 5000L, false, 1L)
        val savedOrder = order.copy(id = 1L)
        every { orderDAO.add(1L, order) } returns savedOrder

        val result = service.createOrder(1L, order)

        assertEquals(savedOrder, result)
        verify { orderDAO.add(1L, order) }
    }

    @Test
    fun `getOrder should return null if order not found`() {
        every { orderDAO.getbyIdAndUserId(99L, 99L) } returns null

        val result = service.getOrder(99L, 99L)

        assertNull(result)
        verify { orderDAO.getbyIdAndUserId(99L, 99L) }
    }

    @Test
    fun `getOrder returns order successfully`() {
        val expected = Order(2L, "Item", 1000, true, 1L)
        every { orderDAO.getbyIdAndUserId(1L, 2L) } returns expected

        val result = service.getOrder(1L, 2L)

        assertEquals(expected, result)
        verify { orderDAO.getbyIdAndUserId(1L, 2L) }

    }

    @Test
    fun `getAllOrdersByUserId returns list of orders successfully`() {
        val orders = listOf(
            Order(1L, "Item A", 1000, false, 1L),
            Order(2L, "Item B", 2000, true, 1L)
        )
        every { orderDAO.getAllByUserId(1L) } returns orders

        val result = service.getAllOrdersByUserId(1L)

        assertEquals(orders, result)
        verify { orderDAO.getAllByUserId(1L) }
    }

    @Test
    fun `deleteOrder deletes Order successfully`() {
        every { orderDAO.deleteByIdAndUserId(5L, 1L) } returns true

        val result = service.deleteOrder(1L, 5L)

        assertTrue(result)
        verify { orderDAO.deleteByIdAndUserId(5L, 1L) }
    }

    @Test
    fun `updateOrder updates and returns order successfully`() {
        val orderId = 10L
        val userId = 1L
        val updatedOrder = Order(orderId, "Updated Order", 7500L, true, userId)

        every { orderDAO.update(userId, orderId, updatedOrder) } returns updatedOrder

        val result = service.updateOrder(userId, orderId, updatedOrder)

        assertEquals(updatedOrder, result)
        verify { orderDAO.update(userId, orderId, updatedOrder) }
    }

    @Test
    fun `updateOrder should return null when DAO returns null`() {
        val orderId = 11L
        val userId = 1L
        val inputOrder = Order(orderId, "Will Fail", 5000L, false, userId)

        every { orderDAO.update(userId, orderId, inputOrder) } returns null

        val result = service.updateOrder(userId, orderId, inputOrder)

        assertNull(result)
        verify { orderDAO.update(userId, orderId, inputOrder) }
    }


    @Test
    fun `patchUpdateOrder updates and returns order successfully`() {
        val orderId = 12L
        val userId = 1L
        val patch = PartialOrderUpdate(
            description = "Partially Updated",
            priceInPence = 6500L,
            completedStatus = true
        )
        val patchedOrder = Order(orderId, "Partially Updated", 6500L, true, userId)

        every { orderDAO.patchUpdate(userId, orderId, patch) } returns patchedOrder

        val result = service.patchUpdateOrder(userId, orderId, patch)

        assertEquals(patchedOrder, result)
        verify { orderDAO.patchUpdate(userId, orderId, patch) }
    }

    @Test
    fun `patchUpdateOrder should return null when DAO returns null`() {
        val orderId = 13L
        val userId = 1L
        val patch = PartialOrderUpdate(description = null, priceInPence = 0, completedStatus = false)

        every { orderDAO.patchUpdate(userId, orderId, patch) } returns null

        val result = service.patchUpdateOrder(userId, orderId, patch)

        assertNull(result)
        verify { orderDAO.patchUpdate(userId, orderId, patch) }
    }
}