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
        val end: LocalTime,
        val zoneId: ZoneId) {

    companion object {
        const val DEFAULT_ZONE_ID = "UTC"
    }

    constructor(start: String, end: String, zoneId: String = DEFAULT_ZONE_ID) :
            this(-1, LocalTime.parse(start), LocalTime.parse(end), ZoneId.of(zoneId))

    constructor(timeIntervalDTO: TimeIntervalDTO) :
            this(timeIntervalDTO.start, timeIntervalDTO.end, timeIntervalDTO.zoneId)

    constructor(start: Date, end: Date, zoneId: ZoneId = ZoneId.of(DEFAULT_ZONE_ID)) :
            this(-1, DateDecorator.of(start).toLocalTime(zoneId),
                    DateDecorator.of(end).toLocalTime(zoneId), zoneId)

    init {
        if (this.start > this.end) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    fun contains(date: Date): Boolean = DateDecorator.of(date).toLocalTime(this.zoneId) in this.start..this.end

    fun contains(dateInterval: DateInterval) =
            dateInterval.isWithinSameDay(this.zoneId) &&
                    this.contains(dateInterval.start) &&
                    this.contains(dateInterval.end)

    fun overlaps(dateInterval: DateInterval): Boolean {
        return !dateInterval.isWithinSameDay(this.zoneId) ||
                TimeInterval(dateInterval.start, dateInterval.end, this.zoneId).overlaps(this)
    }

    fun overlaps(timeInterval: TimeInterval): Boolean {
        return !((this.start <= timeInterval.end && this.end <= timeInterval.start) ||
                (timeInterval.start <= this.end && timeInterval.end <= this.start))
    }

    fun toMap(): Map<String, String> {
        return mapOf(
                Pair("id", this.id.toString()),
                Pair("zoneId", this.zoneId.id),
                Pair("start", this.start.toString()),
                Pair("end", this.end.toString())
        )
    }
}