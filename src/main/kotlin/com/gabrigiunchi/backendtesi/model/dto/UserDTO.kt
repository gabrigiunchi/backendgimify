package com.gabrigiunchi.backendtesi.model.dto

import com.gabrigiunchi.backendtesi.model.User

data class UserDTO(
        val id: Int,
        val username: String,
        val name: String,
        val surname: String)
{
    constructor(user: User): this(user.id, user.username, user.name, user.surname)
}