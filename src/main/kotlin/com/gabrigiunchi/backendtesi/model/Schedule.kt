package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.input.ScheduleDTO
import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.time.DayOfWeek
import java.time.ZoneId
import java.util.*
import javax.persistence.*

@Entity
class Schedule(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,

        val dayOfWeek: DayOfWeek,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val timeIntervals: Set<TimeInterval>
) {
    constructor(scheduleDTO: ScheduleDTO) : this(-1, scheduleDTO.dayOfWeek, scheduleDTO.timeIntervals)
    constructor(dayOfWeek: DayOfWeek, timeIntervals: Set<TimeInterval>) : this(-1, dayOfWeek, timeIntervals)

    private fun isSameDayOfWeek(date: Date, zoneId: ZoneId): Boolean {
        return DateDecorator.of(date).isDayOfWeek(this.dayOfWeek, zoneId)
    }

    fun contains(date: Date, zoneId: ZoneId): Boolean =
            this.isSameDayOfWeek(date, zoneId) && this.timeIntervals.any { it.contains(date, zoneId) }

    fun contains(dateInterval: DateInterval, zoneId: ZoneId): Boolean {
        return this.isSameDayOfWeek(dateInterval.start, zoneId) && this.timeIntervals.any { it.contains(dateInterval, zoneId) }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
                Pair("id", this.id.toString()),
                Pair("dayOfWeek", this.dayOfWeek.toString()),
                Pair("timeIntervals", this.timeIntervals.map { it.toMap() }))
    }
}