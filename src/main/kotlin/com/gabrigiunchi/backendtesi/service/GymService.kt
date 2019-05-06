package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.CommentDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class GymService(
        private val gymDAO: GymDAO,
        private val commentDAO: CommentDAO) {

    fun calculateRatingOfGym(gymId: Int): Double {
        return this.gymDAO.findById(gymId)
                .map {
                    val ratings = this.commentDAO.findByGym(it, Pageable.unpaged())
                            .map { comment -> comment.rating }
                            .toList()

                    val sum = ratings.sum()
                    if (ratings.isEmpty()) -1.0 else sum / ratings.size.toDouble()
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }
}