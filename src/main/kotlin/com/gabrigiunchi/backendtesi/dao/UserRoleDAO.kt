package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.UserRole
import org.springframework.data.repository.CrudRepository

interface UserRoleDAO : CrudRepository<UserRole, Int> {
    fun findByName(name: String): Collection<UserRole>
}