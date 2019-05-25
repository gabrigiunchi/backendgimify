package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Avatar
import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface AvatarDAO : PagingAndSortingRepository<Avatar, String> {
    fun findByUser(user: User): Optional<Avatar>
}