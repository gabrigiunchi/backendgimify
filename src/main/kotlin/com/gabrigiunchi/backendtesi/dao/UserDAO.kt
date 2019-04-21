package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserDAO : CrudRepository<User, Int> {

    override fun findById(id: Int): Optional<User>
    fun findByUsername(username: String): Optional<User>
}