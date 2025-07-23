package com.demo.app.services

import com.demo.app.models.PartialUserUpdate
import com.demo.app.models.User

interface UserService {
    fun getUser(id: Long): User?
    fun getAllUsers(): List<User>
    fun createUser(user: User): User

    fun updateUser(id: Long, user: User): User?
    fun patchUser(id: Long, patch: PartialUserUpdate): User?

    fun deleteUser(id: Long): Boolean
}