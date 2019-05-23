package com.gabrigiunchi.backendtesi.model.dto.input

import java.time.LocalDateTime

class ReservationDTOInput(
        val userID: Int,
        val assetID: Int,
        val start: LocalDateTime,
        val end: LocalDateTime)