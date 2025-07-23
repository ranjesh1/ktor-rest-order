package com.demo.app.di

import com.demo.app.db.UserDAO
import com.demo.app.services.UserService
import com.demo.app.services.UserServiceImpl
import org.koin.dsl.module

val appModule = module {
    single { UserDAO() }
    single<UserService> { UserServiceImpl(get()) }
}