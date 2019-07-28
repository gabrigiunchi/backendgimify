package com.gabrigiunchi.backendtesi.model.dto.output

import com.gabrigiunchi.backendtesi.model.entities.User

data class UserDTOOutput(
        val id: Int,
        val username: String,
        val name: String,
        val surname: String,
        val email: String,
        val notificationsEnabled: Boolean) {

    constructor(user: User) : this(user.id, user.username, user.name, user.surname, user.email, user.notificationsEnabled)
}