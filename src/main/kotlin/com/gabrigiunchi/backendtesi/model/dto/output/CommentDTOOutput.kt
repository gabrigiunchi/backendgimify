package com.gabrigiunchi.backendtesi.model.dto.output

import com.gabrigiunchi.backendtesi.model.Comment
import java.util.*

data class CommentDTOOutput(
        val id: Int,
        val user: UserDTO,
        val gymId: Int,
        val title: String,
        val message: String,
        val rating: Int,
        val date: Date) {

    constructor(comment: Comment) :
            this(comment.id, UserDTO(comment.user), comment.gym.id, comment.title, comment.message, comment.rating, comment.date)
}