package com.demo.app.di

import com.demo.app.db.OrderDAO
import com.demo.app.db.UserDAO
import com.demo.app.services.OrderService
import com.demo.app.services.OrderServiceImpl
import com.demo.app.services.UserService
import com.demo.app.services.UserServiceImpl
import org.koin.dsl.module

val appModule = module {
    single { UserDAO() }
    single { OrderDAO() }
    single<UserService> { UserServiceImpl(get()) }
    single<OrderService> { OrderServiceImpl(get()) }
}