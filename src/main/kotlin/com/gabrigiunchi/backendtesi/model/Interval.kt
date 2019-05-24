package com.gabrigiunchi.backendtesi.model

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
open class Interval(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        val start: LocalDateTime,
        val end: LocalDateTime
) {

    constructor(start: String, end: String) : this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end))

    init {
        if (this.start > this.end) {
            throw IllegalArgumentException("start is before the end")
        }
    }

    open fun contains(date: OffsetDateTime, zoneId: ZoneId): Boolean =
            date in this.start.atZone(zoneId).toOffsetDateTime()..this.end.atZone(zoneId).toOffsetDateTime()

    open fun contains(zonedInterval: ZonedInterval, zoneId: ZoneId): Boolean =
            this.contains(zonedInterval.start, zoneId) && this.contains(zonedInterval.end, zoneId)

    open fun contains(date: LocalDateTime): Boolean = date in this.start..this.end
    open fun contains(interval: Interval): Boolean =
            this.contains(interval.start) && this.contains(interval.end)

    open fun contains(date: String): Boolean = this.contains(LocalDateTime.parse(date))

    open fun overlaps(interval: Interval): Boolean =
            !((this.start <= interval.end && this.end <= interval.start) ||
                    (interval.start <= this.end && interval.end <= this.start))
}