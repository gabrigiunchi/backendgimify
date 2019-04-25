package com.gabrigiunchi.backendtesi.model

import com.fasterxml.jackson.databind.ObjectMapper
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
        val start: OffsetTime,
        val end: OffsetTime
) {

    companion object {
        private const val format = "HH:mm+00:00"
    }

    constructor(start: String, end: String) : this(OffsetTime.parse(start), OffsetTime.parse(end))
    private constructor(start: OffsetTime, end: OffsetTime) : this(-1, start, end)
    constructor(timeIntervalDTO: TimeIntervalDTO) : this(-1, timeIntervalDTO.start, timeIntervalDTO.end)

    constructor(start: Date, end: Date) :
            this(DateDecorator.of(start).format(format), DateDecorator.of(end).format(format))

    init {
        if (this.start > this.end) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    fun contains(date: Date): Boolean = OffsetTime.parse(DateDecorator.of(date).format(format)) in this.start..this.end

    fun contains(dateInterval: DateInterval) =
            dateInterval.isWithinSameDay() && this.contains(dateInterval.start) && this.contains(dateInterval.end)

    fun overlaps(dateInterval: DateInterval): Boolean {
        return !dateInterval.isWithinSameDay() || TimeInterval(dateInterval.start, dateInterval.end).overlaps(this)
    }

    fun overlaps(timeInterval: TimeInterval): Boolean {
        return !((this.start <= timeInterval.end && this.end <= timeInterval.start) ||
                (timeInterval.start <= this.end && timeInterval.end <= this.start))
    }

    override fun toString() = this.toMap().toString()

    fun toMap(): Map<String, String> {
        return mapOf(
                Pair("id", this.id.toString()),
                Pair("start", this.start.format(DateTimeFormatter.ofPattern(format))),
                Pair("end", this.end.format(DateTimeFormatter.ofPattern(format)))
        )
    }
}