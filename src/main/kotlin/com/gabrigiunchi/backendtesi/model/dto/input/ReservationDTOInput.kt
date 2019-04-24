package com.gabrigiunchi.backendtesi.model.dto.input

import java.util.*

class ReservationDTOInput(
        val userID: Int,
        val assetID: Int,
        val start: Date,
        val end: Date)