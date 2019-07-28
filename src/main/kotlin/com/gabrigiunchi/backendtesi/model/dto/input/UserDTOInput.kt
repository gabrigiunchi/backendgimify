package com.gabrigiunchi.backendtesi.model.dto.input

import com.gabrigiunchi.backendtesi.model.entities.User

class UserDTOInput(
        val username: String,
        val password: String,
        val name: String,
        val surname: String,
        val email: String,
        val roles: Collection<String>,
        val isActive: Boolean,
        val notificationsEnabled: Boolean

) {

    constructor(username: String, password: String, name: String, surname: String, email: String, roles: Collection<String>) :
            this(username, password, name, surname, email, roles, true, true)

    constructor(user: User) :
            this(user.username, user.password, user.name, user.surname, user.email,
                    user.roles.map { it.name }, user.isActive, user.notificationsEnabled)
}