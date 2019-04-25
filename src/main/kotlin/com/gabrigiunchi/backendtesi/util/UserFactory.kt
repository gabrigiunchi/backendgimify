package com.gabrigiunchi.backendtesi.util

import com.gabrigiunchi.backendtesi.dao.UserRoleDAO
import com.gabrigiunchi.backendtesi.model.User
import com.gabrigiunchi.backendtesi.model.type.UserRoleEnum
import org.springframework.stereotype.Service

@Service
class UserFactory(private val userRoleDAO: UserRoleDAO) {

    fun createAdminUser(username: String, password: String, name: String, surname: String, email: String = "prova@server.com"): User {
        val roles = this.userRoleDAO.findByName(UserRoleEnum.ADMINISTRATOR.toString())
        return User(username, password, name, surname, email, roles.toMutableList())
    }

    fun createRegularUser(username: String, password: String, name: String, surname: String, email: String = "prova@server.com"): User {
        val roles = this.userRoleDAO.findByName(UserRoleEnum.USER.toString())
        return User(username, password, name, surname, email, roles.toMutableList())
    }

}