package com.gabrigiunchi.backendtesi.model.rules

import com.gabrigiunchi.backendtesi.exceptions.ReservationDurationException
import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.stereotype.Service

@Service
class ReservationDurationRule : Rule<Pair<Asset, ZonedInterval>> {
    override fun validate(element: Pair<Asset, ZonedInterval>) {
        if (!this.test(element)) {
            throw ReservationDurationException(element.first.kind.maxReservationTime)
        }
    }

    override fun test(element: Pair<Asset, ZonedInterval>): Boolean =
            element.second.start.plusMinutes(element.first.kind.maxReservationTime.toLong()) >= element.second.end


}