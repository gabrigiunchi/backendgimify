package com.gabrigiunchi.backendtesi.model.rules

import com.gabrigiunchi.backendtesi.exceptions.BadRequestException
import com.gabrigiunchi.backendtesi.exceptions.ReservationThresholdExceededException
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime

@Service
class ReservationIntervalValidator : Rule<ZonedInterval> {

    @Value("\${application.reservationThresholdInDays}")
    private var reservationThresholdInDays: Int = 0

    override fun test(element: ZonedInterval): Boolean =
            !this.isBeyondTheThreshold(element.start) && !this.isInThePast(element.start)

    override fun validate(element: ZonedInterval) {
        if (this.isInThePast(element.start)) {
            throw BadRequestException("reservation must be in the future")
        }

        if (this.isBeyondTheThreshold(element.start)) {
            throw ReservationThresholdExceededException()
        }
    }

    private fun isInThePast(date: OffsetDateTime): Boolean = date.toInstant() < Instant.now()
    private fun isBeyondTheThreshold(date: OffsetDateTime) = date.toInstant().isAfter(OffsetDateTime.now().plusDays(this.reservationThresholdInDays.toLong()).toInstant())

}