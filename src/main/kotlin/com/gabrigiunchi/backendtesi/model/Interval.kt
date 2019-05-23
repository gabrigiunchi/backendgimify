package com.gabrigiunchi.backendtesi.model

import java.time.LocalDate
import java.time.LocalDateTime
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

    constructor(start: LocalDateTime, end: LocalDateTime): this(-1, start, end)
    constructor(start: String, end: String) : this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end))

    init {
        if (this.start > this.end) {
            throw IllegalArgumentException("start is before the end")
        }
    }

    open fun contains(date: LocalDateTime): Boolean = date in this.start..this.end
    open fun contains(date: String): Boolean = this.contains(LocalDateTime.parse(date))
    open fun overlaps(interval: Interval): Boolean =
            !((this.start <= interval.end && this.end <= interval.start) ||
                    (interval.start <= this.end && interval.end <= this.start))
}