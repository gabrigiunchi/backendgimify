package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.Avatar
import com.gabrigiunchi.backendtesi.model.entities.User
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface AvatarDAO : PagingAndSortingRepository<Avatar, String> {
    fun findByUser(user: User): Optional<Avatar>
}