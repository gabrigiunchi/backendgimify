package com.gabrigiunchi.backendtesi.model.dto.output

import com.gabrigiunchi.backendtesi.model.entities.Comment
import java.time.OffsetDateTime

data class CommentDTOOutput(
        val id: Int,
        val user: UserDTOOutput,
        val gymId: Int,
        val title: String,
        val message: String,
        val rating: Int,
        val date: OffsetDateTime) {

    constructor(comment: Comment) :
            this(comment.id, UserDTOOutput(comment.user), comment.gym.id, comment.title, comment.message, comment.rating, comment.date)
}