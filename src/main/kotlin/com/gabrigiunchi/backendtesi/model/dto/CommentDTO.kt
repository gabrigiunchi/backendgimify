package com.gabrigiunchi.backendtesi.model.dto

import com.gabrigiunchi.backendtesi.model.Comment

class CommentDTO(
        val userId: Int,
        val gymId: Int,
        val title: String,
        val message: String,
        val rating: Int
)