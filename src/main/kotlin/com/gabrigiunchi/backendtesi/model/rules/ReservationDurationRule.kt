package com.gabrigiunchi.backendtesi.model.rules

import com.gabrigiunchi.backendtesi.exceptions.ReservationDurationException
import com.gabrigiunchi.backendtesi.model.entities.AssetKind
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.stereotype.Service

@Service
class ReservationDurationRule : Rule<Pair<AssetKind, ZonedInterval>> {
    override fun validate(element: Pair<AssetKind, ZonedInterval>) {
        if (!this.test(element)) {
            throw ReservationDurationException(element.first.maxReservationTime)
        }
    }

    override fun test(element: Pair<AssetKind, ZonedInterval>): Boolean =
            element.second.start.plusMinutes(element.first.maxReservationTime.toLong()) >= element.second.end


}