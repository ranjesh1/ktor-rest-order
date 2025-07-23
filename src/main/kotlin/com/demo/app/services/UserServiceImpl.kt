package com.demo.app.services

import com.demo.app.db.UserDAO
import com.demo.app.models.PartialUserUpdate
import com.demo.app.models.User

class UserServiceImpl(private val userDAO: UserDAO) : UserService {
    override fun getUser(id: Long) = userDAO.getById(id)
    override fun getAllUsers() = userDAO.getAll()
    override fun createUser(user: User) = userDAO.add(user)

    override fun updateUser(id: Long, user: User) = userDAO.update(id, user)
    override fun patchUser(id: Long, patch: PartialUserUpdate) = userDAO.patch(id, patch)

    override fun deleteUser(id: Long) = userDAO.delete(id)
}