package com.gabrigiunchi.backendtesi.model.dto.input

class CommentDTOInput(
        val userId: Int,
        val gymId: Int,
        val title: String,
        val message: String,
        val rating: Int
)