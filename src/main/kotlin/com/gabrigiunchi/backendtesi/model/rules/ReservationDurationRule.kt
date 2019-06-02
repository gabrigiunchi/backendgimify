package com.gabrigiunchi.backendtesi.model.rules

import com.gabrigiunchi.backendtesi.exceptions.BadRequestException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.stereotype.Service

@Service
class ReservationDurationRule : Rule<Pair<Asset, ZonedInterval>> {
    override fun validate(element: Pair<Asset, ZonedInterval>) {
        if (!this.test(element)) {
            throw BadRequestException("reservation duration exceeds maximum (max=${element.first.kind.maxReservationTime} minutes)")
        }
    }

    override fun test(element: Pair<Asset, ZonedInterval>): Boolean =
            element.second.start.plusMinutes(element.first.kind.maxReservationTime.toLong()) >= element.second.end


}