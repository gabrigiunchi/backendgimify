package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.model.type.UserRoleEnum
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

open class BaseController(private val userDAO: UserDAO) {

    protected fun getLoggedUser(): com.gabrigiunchi.backendtesi.model.User {
        return this.userDAO.findByUsername((SecurityContextHolder.getContext().authentication.principal as User).username).get()
    }


    protected fun isAdmin(): Boolean {
        return this.getLoggedUser().roles.any { it.name == UserRoleEnum.ADMINISTRATOR.name }
    }
}