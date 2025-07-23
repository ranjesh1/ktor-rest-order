package com.demo.app.db

import com.demo.app.models.PartialUserUpdate
import com.demo.app.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserDAO {
    fun getAll(): List<User> = transaction {
        Users.selectAll().map { toUser(it) }
    }

    fun getById(id: Long): User? = transaction {
        Users.select { Users.id eq id }.map { toUser(it) }.singleOrNull()
    }

    fun delete(id: Long): Boolean = transaction {
        Users.deleteWhere { Users.id eq id } > 0
    }

    fun update(id: Long, user: User): User? = transaction {
        val updatedRows = Users.update({ Users.id eq id }) {
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[email] = user.email
            it[firstLineOfAddress] = user.firstLineOfAddress
            it[secondLineOfAddress] = user.secondLineOfAddress
            it[town] = user.town
            it[postCode] = user.postCode
        }
        if (updatedRows > 0) getById(id) else null
    }

    fun add(user: User): User = transaction {
        val id = Users.insert {
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[email] = user.email
            it[firstLineOfAddress] = user.firstLineOfAddress
            it[secondLineOfAddress] = user.secondLineOfAddress
            it[town] = user.town
            it[postCode] = user.postCode
        } get Users.id
        user.copy(id = id.value)
    }

    fun patch(id: Long, updates: PartialUserUpdate): User? = transaction {
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
    }

    private fun toUser(row: ResultRow) = User(
        id = row[Users.id].value,
        firstName = row[Users.firstName],
        lastName = row[Users.lastName],
        email = row[Users.email],
        firstLineOfAddress = row[Users.firstLineOfAddress],
        secondLineOfAddress = row[Users.secondLineOfAddress],
        town = row[Users.town],
        postCode = row[Users.postCode]
    )
}