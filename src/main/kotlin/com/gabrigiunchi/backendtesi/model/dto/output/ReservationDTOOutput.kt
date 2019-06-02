package com.gabrigiunchi.backendtesi.model.dto.output

import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.model.entities.Reservation
import java.time.OffsetDateTime

data class ReservationDTOOutput(
        val id: Int,
        val user: UserDTO,
        val asset: Asset,
        val start: OffsetDateTime,
        val end: OffsetDateTime) {

    constructor(reservation: Reservation) :
            this(reservation.id, UserDTO(reservation.user), reservation.asset, reservation.start, reservation.end)

}