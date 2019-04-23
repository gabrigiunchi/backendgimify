package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Comment
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface CommentDAO : CrudRepository<Comment, Int> {
    fun findByIdAndUser(id: Int, user: User): Optional<Comment>
    fun findByUser(user: User): Collection<Comment>
    fun findByGym(gym: Gym): Collection<Comment>
    fun findByUserAndGym(user: User, gym: Gym): Collection<Comment>
}