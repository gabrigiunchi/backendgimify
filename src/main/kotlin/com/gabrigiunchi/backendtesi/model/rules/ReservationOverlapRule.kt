package com.gabrigiunchi.backendtesi.model.rules

import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.exceptions.ReservationConflictException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.stereotype.Service

@Service
class ReservationOverlapRule(private val reservationDAO: ReservationDAO) : Rule<Pair<Asset, ZonedInterval>> {

    override fun validate(element: Pair<Asset, ZonedInterval>) {
        if (!this.test(element)) {
            throw ReservationConflictException()
        }
    }

    override fun test(element: Pair<Asset, ZonedInterval>): Boolean =
            this.reservationDAO
                    .findByAssetAndEndAfter(element.first, element.second.start)
                    .none { ZonedInterval(it.start, it.end).overlaps(ZonedInterval(element.second.start, element.second.end)) }

}