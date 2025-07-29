package com.demo.app.services

import com.demo.app.db.OrderDAO
import com.demo.app.models.Order
import com.demo.app.models.PartialOrderUpdate

class OrderServiceImpl(private val orderDAO: OrderDAO): OrderService {
    override fun createOrder(userId: Long, order: Order) = orderDAO.add(userId, order)

    override fun updateOrder(userId: Long, orderId: Long, order: Order) = orderDAO.update(userId, orderId, order)

    override fun patchUpdateOrder(userId: Long, orderId: Long, partialOrderUpdate: PartialOrderUpdate) = orderDAO.patchUpdate(userId, orderId, partialOrderUpdate)
    override fun getOrder(userId: Long, orderId: Long) = orderDAO.getbyIdAndUserId(userId, orderId)

    override fun getAllOrdersByUserId(userId: Long) = orderDAO.getAllByUserId(userId)
    override fun deleteOrder(userId: Long, orderId: Long) = orderDAO.deleteByIdAndUserId(orderId, userId)
}

