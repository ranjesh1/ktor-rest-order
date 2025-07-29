package com.demo.app.db

import com.demo.app.models.Order
import com.demo.app.models.PartialOrderUpdate
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class OrderDAO {
    fun add(userId: Long, order: Order): Order = transaction {
        val id = Orders.insert {
            it[description] = order.description
            it[priceInPence] = order.priceInPence
            it[completedStatus] = order.completedStatus
            it[user] = EntityID(userId, Users) // ✅ new way
        } get Orders.id

        order.copy(id = id.value, userId = userId)
    }

    fun getbyIdAndUserId(userId: Long, orderId: Long): Order? = transaction {
        Orders
            .select { (Orders.id eq orderId) and (Orders.user eq EntityID(userId, Users)) }
            .map(::toOrder)
            .singleOrNull()
    }


    fun getAllByUserId(userId: Long): List<Order> = transaction {
        Orders
            .select { Orders.user eq EntityID(userId, Users) }
            .map(::toOrder)
    }

    fun update(userId: Long, orderId: Long, order: Order): Order? = transaction {
        val updated = Orders.update({
            (Orders.id eq orderId) and (Orders.user eq EntityID(userId, Users))
        }) {
            it[description] = order.description
            it[priceInPence] = order.priceInPence
            it[completedStatus] = order.completedStatus
        }

        if (updated > 0) getbyIdAndUserId(userId, orderId) else null
    }

    fun patchUpdate(userId: Long, orderId: Long, partialOrderUpdate: PartialOrderUpdate): Order? = transaction {
        val updated = Orders.update({
            (Orders.id eq orderId) and (Orders.user eq EntityID(userId, Users))
        }) {
            partialOrderUpdate.description?.let { desc -> it[description] = desc }
            partialOrderUpdate.priceInPence?.let { price -> it[priceInPence] = price }
            partialOrderUpdate.completedStatus?.let { status -> it[completedStatus] = status }
//            partialOrderUpdate.userId?.let { id -> it[userId] = id }
//            order.description?.let { desc -> it[description] = desc }
//            order.description?.let { desc -> it[description] = desc }
//            if (order.priceInPence != 0L) it[priceInPence] = order.priceInPence
//            it[completedStatus] = order.completedStatus
        }

        if (updated > 0) getbyIdAndUserId(userId, orderId) else null
    }


    /*fun patch(id: Long, updates: PartialUserUpdate): User? = transaction {
        val user = Users.select { Users.id eq id }.singleOrNull() ?: return@transaction null

        Users.update({ Users.id eq id }) {
            updates.firstName?.let { f -> it[firstName] = f }
            updates.lastName?.let { l -> it[lastName] = l }
            updates.email?.let { e -> it[email] = e }
            updates.firstLineOfAddress?.let { f -> it[firstLineOfAddress] = f }
            updates.secondLineOfAddress?.let { s -> it[secondLineOfAddress] = s }
            updates.town?.let { t -> it[town] = t }
            updates.postCode?.let { p -> it[postCode] = p }
        }

        getById(id)
    }*/

    fun deleteByIdAndUserId(orderId: Long, userId: Long): Boolean = transaction {
        Orders.deleteWhere {
            (Orders.id eq orderId) and (Orders.user eq EntityID(userId, Users))
        } > 0
    }

    private fun toOrder(row: ResultRow): Order = Order(
        id = row[Orders.id].value,
        description = row[Orders.description],
        priceInPence = row[Orders.priceInPence],
        completedStatus = row[Orders.completedStatus],
        userId = row[Orders.user].value // ✅ unwrap EntityID
    )
}