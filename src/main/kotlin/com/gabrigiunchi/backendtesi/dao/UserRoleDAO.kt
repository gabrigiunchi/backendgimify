package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.UserRole
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRoleDAO : CrudRepository<UserRole, Int> {
    fun findByName(name: String): Optional<UserRole>
}