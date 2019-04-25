package com.gabrigiunchi.backendtesi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.gabrigiunchi.backendtesi.model.dto.input.TimeIntervalDTO
import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.time.OffsetTime
import java.time.format.DateTimeFormatter
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
        val start: String,
        val end: String) {

    companion object {
        private const val format = "HH:mm+00:00"
    }

    @JsonIgnore
    private val startOffsetTime: OffsetTime = OffsetTime.parse(start)

    @JsonIgnore
    private val endOffsetTime: OffsetTime = OffsetTime.parse(end)

    constructor(start: String, end: String) : this(-1, start, end)
    constructor(timeIntervalDTO: TimeIntervalDTO) : this(-1, timeIntervalDTO.start, timeIntervalDTO.end)

    constructor(start: Date, end: Date) :
            this(-1, DateDecorator.of(start).format(format), DateDecorator.of(end).format(format))

    init {
        if (this.startOffsetTime > this.endOffsetTime) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    fun contains(date: Date): Boolean = OffsetTime.parse(DateDecorator.of(date).format(format)) in this.startOffsetTime..this.endOffsetTime

    fun contains(dateInterval: DateInterval) =
            dateInterval.isWithinSameDay() && this.contains(dateInterval.start) && this.contains(dateInterval.end)

    fun overlaps(dateInterval: DateInterval): Boolean {
        return !dateInterval.isWithinSameDay() || TimeInterval(dateInterval.start, dateInterval.end).overlaps(this)
    }

    fun overlaps(timeInterval: TimeInterval): Boolean {
        return !((this.start <= timeInterval.end && this.end <= timeInterval.start) ||
                (timeInterval.start <= this.end && timeInterval.end <= this.start))
    }

    fun toMap(): Map<String, String> {
        return mapOf(
                Pair("id", this.id.toString()),
                Pair("start", this.start.format(DateTimeFormatter.ofPattern(format))),
                Pair("end", this.end.format(DateTimeFormatter.ofPattern(format)))
        )
    }
}