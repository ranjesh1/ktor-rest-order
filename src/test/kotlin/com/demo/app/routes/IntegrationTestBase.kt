package com.demo.app.routes

import com.demo.app.db.OrderDAO
import com.demo.app.db.Orders
import com.demo.app.db.UserDAO
import com.demo.app.db.Users
import com.demo.app.models.Order
import com.demo.app.models.User
import com.demo.app.services.OrderService
import com.demo.app.services.OrderServiceImpl
import com.demo.app.services.UserService
import com.demo.app.services.UserServiceImpl
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

abstract class IntegrationTestBase {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            Database.connect(
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver",
                user = "root",
                password = ""
            )
            transaction {
                SchemaUtils.createMissingTablesAndColumns(Users, Orders)
            }
        }
    }

    @AfterEach
    fun cleanup() {
        transaction {
            Orders.deleteAll()
            Users.deleteAll()
        }
    }

    /*    protected fun createTestUser(): Long {
            return transaction {
                Users.insertAndGetId {
                    it[firstName] = "Test"
                    it[lastName] = "User"
                    it[email] = "test@example.com"
                    it[firstLineOfAddress] = "123 Lane"
                    it[secondLineOfAddress] = null
                    it[town] = "City"
                    it[postCode] = "00000"
                }.value
            }
        }*/

    protected fun createTestUser(): User {
        return UserDAO().add(
            User(
                firstName = "Jane",
                lastName = "Doe",
                email = "jane@example.com",
                firstLineOfAddress = "123 Lane",
                secondLineOfAddress = null,
                town = "Townsville",
                postCode = "12345"
            )
        )
    }

    protected fun createUser(): User = User(
        firstName = "Jane",
        lastName = "Doe",
        email = "jane@example.com",
        firstLineOfAddress = "123 Lane",
        secondLineOfAddress = null,
        town = "Townsville",
        postCode = "12345"
    )

    protected fun createTestOrder(userId: Long): Order {
        return OrderDAO().add(
            userId = userId,
            order = Order(
                description = "Test order",
                priceInPence = 9999,
                completedStatus = false,
                userId = userId
            )
        )
    }

    protected fun createOrder(userId: Long): Order = Order(
        description = "Sample order",
        priceInPence = 5000,
        completedStatus = false,
        userId = userId
    )

    protected fun Application.defaultTestModule(withRoutes: Application.() -> Unit, includeOrder: Boolean = false) {
        install(Koin) {
            modules(
                module {
                    single { UserDAO() }
                    single<UserService> { UserServiceImpl(get()) }

                    if (includeOrder) {
                        single { OrderDAO() }
                        single<OrderService> { OrderServiceImpl(get()) }
                    }
                }
            )
        }
        install(ContentNegotiation) {
            json()
        }
        withRoutes()
    }

    protected fun Application.orderTestModule() {
        install(Koin) {
            modules(
                module {
                    single { UserDAO() }
                    single { OrderDAO() }
                    single<UserService> { UserServiceImpl(get()) }
                    single<OrderService> { OrderServiceImpl(get()) }
                }
            )
        }
        install(ContentNegotiation) { json() }
    }
}
