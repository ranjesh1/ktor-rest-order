package com.demo.app.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object DatabaseFactory {
    fun init() {
        Database.connect(
            "jdbc:h2:./data/db",
            driver = "org.h2.Driver",
            user = "root",
            password = ""
        )

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Orders)
        }

        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }
}