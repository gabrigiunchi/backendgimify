package com.gabrigiunchi.backendtesi.model.time

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
open class LocalInterval(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        override val start: LocalDateTime,
        override val end: LocalDateTime
) : Interval<LocalDateTime> {

    constructor(start: LocalDateTime, end: LocalDateTime) : this(-1, start, end)
    constructor(start: String, end: String) : this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end))

    init {
        if (this.start >= this.end) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    override fun contains(instant: LocalDateTime): Boolean = instant in this.start..this.end
    override fun contains(interval: Interval<LocalDateTime>): Boolean =
            this.contains(interval.start) && this.contains(interval.end)

    open fun contains(date: String): Boolean = this.contains(LocalDateTime.parse(date))

    override fun overlaps(interval: Interval<LocalDateTime>): Boolean = !(this.end <= interval.start || interval.end <= this.start)

    fun toZonedInterval(zoneId: ZoneId): ZonedInterval =
            ZonedInterval(this.start.atZone(zoneId).toOffsetDateTime(), this.end.atZone(zoneId).toOffsetDateTime())

    @JsonIgnore
    open fun isWithinSameDay(): Boolean = this.start.toLocalDate() == this.end.toLocalDate()
}