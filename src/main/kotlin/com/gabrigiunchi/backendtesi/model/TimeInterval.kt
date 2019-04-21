package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.TimeIntervalDTO
import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.time.OffsetTime
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
    constructor(start: String, end: String): this(OffsetTime.parse(start), OffsetTime.parse(end))
    constructor(start: OffsetTime, end: OffsetTime) : this(-1, start, end)
    constructor(timeIntervalDTO: TimeIntervalDTO) : this(-1, timeIntervalDTO.start, timeIntervalDTO.end)

    init {
        if (this.start.isAfter(this.end)) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    fun contains(date: Date): Boolean = OffsetTime.parse(DateDecorator.of(date).format("HH:mm+00:00")) in this.start..this.end
    fun contains(dateInterval: DateInterval) = dateInterval.isWithinSameDay() && this.contains(dateInterval.start) && this.contains(dateInterval.end)

    fun toMap(): Map<String, String> {
        return mapOf(
                Pair("id", this.id.toString()),
                Pair("start", this.start.toString()),
                Pair("end", this.end.toString())
        )
    }
}