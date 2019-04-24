package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.ReservationLog
import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ReservationLogDAO : CrudRepository<ReservationLog, Int> {
    fun findByUser(user: User): Collection<ReservationLog>
    fun findByUserAndDateBetween(user: User, start: Date, end: Date): Collection<ReservationLog>
    fun findByReservationId(reservationId: Int): Optional<ReservationLog>
}