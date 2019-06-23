package com.gabrigiunchi.backendtesi.model.rules

import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.exceptions.TooManyReservationsException
import com.gabrigiunchi.backendtesi.model.entities.Reservation
import com.gabrigiunchi.backendtesi.model.entities.User
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class ReservationValidator(
        private val reservationDAO: ReservationDAO,
        private val gymOpenRule: GymOpenRule,
        private val reservationDurationRule: ReservationDurationRule,
        private val reservationIntervalValidator: ReservationIntervalValidator,
        private val reservationOverlapRule: ReservationOverlapRule) : Rule<Reservation> {

    @Value("\${application.maxReservationsPerDay}")
    private var maxReservationsPerDay: Int = 0

    override fun test(element: Reservation): Boolean {
        val interval = ZonedInterval(element.start, element.end)
        return this.reservationIntervalValidator.test(interval) &&
                this.reservationDurationRule.test(Pair(element.asset.kind, interval)) &&
                this.gymOpenRule.test(Pair(element.asset.gym, interval)) &&
                this.reservationOverlapRule.test(Pair(element.asset, interval))
    }

    override fun validate(element: Reservation) {
        val user = element.user
        val interval = ZonedInterval(element.start, element.end)
        this.reservationIntervalValidator.validate(interval)
        this.reservationDurationRule.validate(Pair(element.asset.kind, interval))
        this.gymOpenRule.validate(Pair(element.asset.gym, interval))
        this.reservationOverlapRule.validate(Pair(element.asset, interval))

        if (this.numberOfReservationsMadeByUserInDate(user, OffsetDateTime.now()) >= this.maxReservationsPerDay) {
            throw TooManyReservationsException()
        }
    }

    private fun numberOfReservationsMadeByUserInDate(user: User, date: OffsetDateTime): Int =
            this.reservationDAO.findByUserAndDateBetween(user, date.minusDays(1), date).count()
}