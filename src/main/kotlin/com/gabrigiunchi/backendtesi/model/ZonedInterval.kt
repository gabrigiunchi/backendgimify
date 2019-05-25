package com.gabrigiunchi.backendtesi.model

import java.time.OffsetDateTime
import java.time.ZoneId

class ZonedInterval(val start: OffsetDateTime, val end: OffsetDateTime) {

    constructor(start: String, end: String) : this(OffsetDateTime.parse(start), OffsetDateTime.parse(end))

    init {
        if (this.start > this.end) {
            throw IllegalArgumentException("start is before the end")
        }
    }

    fun contains(date: OffsetDateTime): Boolean = date in this.start..this.end

    fun overlaps(interval: ZonedInterval): Boolean =
            !((this.start <= interval.end && this.end <= interval.start) ||
                    (interval.start <= this.end && interval.end <= this.start))

    fun toInterval(zoneId: ZoneId): Interval =
            Interval(this.start.atZoneSameInstant(zoneId).toLocalDateTime(),
                    this.end.atZoneSameInstant(zoneId).toLocalDateTime())

}