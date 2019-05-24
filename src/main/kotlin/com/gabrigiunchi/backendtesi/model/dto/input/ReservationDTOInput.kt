package com.gabrigiunchi.backendtesi.model.dto.input

import java.time.OffsetDateTime

class ReservationDTOInput(
        val userID: Int,
        val assetID: Int,
        val start: OffsetDateTime,
        val end: OffsetDateTime)