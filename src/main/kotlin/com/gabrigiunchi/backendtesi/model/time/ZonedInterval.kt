package com.gabrigiunchi.backendtesi.model.time

import java.time.OffsetDateTime
import java.time.ZoneId
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
open class ZonedInterval(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        override val start: OffsetDateTime,
        override val end: OffsetDateTime) : Interval<OffsetDateTime> {

    constructor(start: OffsetDateTime, end: OffsetDateTime) : this(-1, start, end)
    constructor(start: String, end: String) : this(-1, OffsetDateTime.parse(start), OffsetDateTime.parse(end))

    init {
        if (this.start >= this.end) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    override fun contains(instant: OffsetDateTime): Boolean = instant.toInstant() in this.start.toInstant()..this.end.toInstant()
    override fun contains(interval: Interval<OffsetDateTime>): Boolean = this.contains(interval.start) && this.contains(interval.end)
    override fun overlaps(interval: Interval<OffsetDateTime>): Boolean =
            !(this.end.toInstant() <= interval.start.toInstant() || interval.end.toInstant() <= this.start.toInstant())

    fun toLocalInterval(zoneId: ZoneId): LocalInterval =
            LocalInterval(this.start.atZoneSameInstant(zoneId).toLocalDateTime(),
                    this.end.atZoneSameInstant(zoneId).toLocalDateTime())

}