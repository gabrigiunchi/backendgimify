package com.gabrigiunchi.backendtesi.model

import java.time.OffsetDateTime

class ZonedInterval(val start: OffsetDateTime, val end: OffsetDateTime) {

    constructor(start: String, end: String): this(OffsetDateTime.parse(start), OffsetDateTime.parse(end))

    fun contains(date: OffsetDateTime): Boolean = date in this.start..this.end
    fun contains(interval: ZonedInterval): Boolean =
            this.contains(interval.start) && this.contains(interval.end)

    fun overlaps(interval: ZonedInterval): Boolean =
            !((this.start <= interval.end && this.end <= interval.start) ||
                    (interval.start <= this.end && interval.end <= this.start))

}