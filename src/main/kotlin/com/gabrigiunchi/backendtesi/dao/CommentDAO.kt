package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.Comment
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.entities.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface CommentDAO : PagingAndSortingRepository<Comment, Int> {
    override fun findAll(pageable: Pageable): Page<Comment>
    fun findByIdAndUser(id: Int, user: User): Optional<Comment>
    fun findByUser(user: User, pageable: Pageable): Page<Comment>
    fun findByGym(gym: Gym, pageable: Pageable): Page<Comment>
    fun findByUserAndGym(user: User, gym: Gym): Collection<Comment>
}