package com.gabrigiunchi.backendtesi.model.dto.output

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.Reservation
import java.util.*

data class ReservationDTOOutput(
        val id: Int,
        val user: UserDTO,
        val asset: Asset,
        val start: Date,
        val end: Date) {

    constructor(reservation: Reservation) :
            this(reservation.id, UserDTO(reservation.user), reservation.asset, reservation.start, reservation.end)

}