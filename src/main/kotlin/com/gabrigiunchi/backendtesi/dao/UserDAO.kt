package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface UserDAO : PagingAndSortingRepository<User, Int> {
    override fun findAll(pageable: Pageable): Page<User>
    fun findByUsername(username: String): Optional<User>
}