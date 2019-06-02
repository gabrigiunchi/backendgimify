package com.gabrigiunchi.backendtesi.model.time

import java.time.OffsetDateTime
import java.time.ZoneId

class ZonedInterval(override val start: OffsetDateTime, override val end: OffsetDateTime) : Interval<OffsetDateTime> {

    constructor(start: String, end: String) : this(OffsetDateTime.parse(start), OffsetDateTime.parse(end))

    init {
        if (this.start >= this.end) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    override fun contains(instant: OffsetDateTime): Boolean = instant in this.start..this.end
    override fun contains(interval: Interval<OffsetDateTime>): Boolean = this.contains(interval.start) && this.contains(interval.end)
    override fun overlaps(interval: Interval<OffsetDateTime>): Boolean = !(this.end <= interval.start || interval.end <= this.start)

    fun toLocalInterval(zoneId: ZoneId): LocalInterval =
            LocalInterval(this.start.atZoneSameInstant(zoneId).toLocalDateTime(),
                    this.end.atZoneSameInstant(zoneId).toLocalDateTime())

}