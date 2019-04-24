package com.gabrigiunchi.backendtesi.model.dto.output

import com.gabrigiunchi.backendtesi.model.Comment
import com.gabrigiunchi.backendtesi.model.Gym
import java.util.*

data class CommentDTOOutput(
        val id: Int,
        val user: UserDTO,
        val gym: Gym,
        val title: String,
        val message: String,
        val rating: Int,
        val date: Date) {

    constructor(comment: Comment) :
            this(comment.id, UserDTO(comment.user), comment.gym, comment.title, comment.message, comment.rating, comment.date)
}