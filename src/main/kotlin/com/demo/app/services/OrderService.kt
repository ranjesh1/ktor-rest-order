package com.demo.app.services

import com.demo.app.models.Order
import com.demo.app.models.PartialOrderUpdate

interface OrderService {
    fun createOrder(userId: Long, order: Order): Order

    fun updateOrder(userId: Long, orderId: Long, order: Order): Order?

    fun patchUpdateOrder(userId: Long, orderId: Long, partialOrderUpdate: PartialOrderUpdate): Order?

    fun getOrder(userId: Long, orderId: Long): Order?

    fun getAllOrdersByUserId(userId: Long): List<Order>

    fun deleteOrder(userId: Long, orderId: Long): Boolean
}






