package com.gabrigiunchi.backendtesi.model.rules

import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.exceptions.ReservationConflictException
import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.model.time.Interval
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class ReservationOverlapRule(private val reservationDAO: ReservationDAO) : Rule<Pair<Asset, Interval<OffsetDateTime>>> {

    override fun validate(element: Pair<Asset, Interval<OffsetDateTime>>) {
        if (!this.test(element)) {
            throw ReservationConflictException()
        }
    }

    override fun test(element: Pair<Asset, Interval<OffsetDateTime>>): Boolean =
            this.reservationDAO
                    .findByAssetAndEndAfter(element.first, element.second.start)
                    .none { ZonedInterval(it.start, it.end).overlaps(ZonedInterval(element.second.start, element.second.end)) }

}