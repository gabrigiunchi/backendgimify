package com.gabrigiunchi.backendtesi.model.dto

import java.util.*

class ReservationDTO(
        val userID: Int,
        val assetID: Int,
        val start: Date,
        val end: Date)