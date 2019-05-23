package com.gabrigiunchi.backendtesi.model.dto.output

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.Reservation
import java.time.LocalDateTime

data class ReservationDTOOutput(
        val id: Int,
        val user: UserDTO,
        val asset: Asset,
        val start: LocalDateTime,
        val end: LocalDateTime) {

    constructor(reservation: Reservation) :
            this(reservation.id, UserDTO(reservation.user), reservation.asset, reservation.start, reservation.end)

}