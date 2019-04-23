package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Comment
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.repository.CrudRepository

interface CommentDAO : CrudRepository<Comment, Int> {
    fun findByUser(user: User): Collection<Comment>
    fun findByGym(gym: Gym): Collection<Comment>
    fun findByUserAndGym(user: User, gym: Gym): Collection<Comment>
}