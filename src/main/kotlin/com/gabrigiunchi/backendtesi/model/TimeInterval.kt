package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.input.TimeIntervalDTO
import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class TimeInterval(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        val start: LocalTime,
        val end: LocalTime) {

    constructor(start: String, end: String) :
            this(-1, LocalTime.parse(start), LocalTime.parse(end))

    constructor(timeIntervalDTO: TimeIntervalDTO) :
            this(timeIntervalDTO.start, timeIntervalDTO.end)

    constructor(start: Date, end: Date, zoneId: ZoneId) :
            this(-1, DateDecorator.of(start).toLocalTime(zoneId),
                    DateDecorator.of(end).toLocalTime(zoneId))

    init {
        if (this.start > this.end) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    fun contains(date: Date, zoneId: ZoneId): Boolean =
            DateDecorator.of(date).toLocalTime(zoneId) in this.start..this.end

    fun contains(dateInterval: DateInterval, zoneId: ZoneId) =
            dateInterval.isWithinSameDay(zoneId) &&
                    this.contains(dateInterval.start, zoneId) &&
                    this.contains(dateInterval.end, zoneId)

    fun overlaps(dateInterval: DateInterval, zoneId: ZoneId): Boolean {
        return !dateInterval.isWithinSameDay(zoneId) ||
                TimeInterval(dateInterval.start, dateInterval.end, zoneId).overlaps(this)
    }

    fun overlaps(timeInterval: TimeInterval): Boolean {
        return !((this.start <= timeInterval.end && this.end <= timeInterval.start) ||
                (timeInterval.start <= this.end && timeInterval.end <= this.start))
    }

    fun toMap(): Map<String, String> {
        return mapOf(
                Pair("id", this.id.toString()),
                Pair("start", this.start.toString()),
                Pair("end", this.end.toString())
        )
    }
}