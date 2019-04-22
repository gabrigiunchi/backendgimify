package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.ScheduleDTO
import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.time.DayOfWeek
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

    private fun isSameDay(date: Date): Boolean = DateDecorator.of(date).dayOfWeek == this.dayOfWeek.value

    fun contains(date: Date): Boolean {
        return if (this.isSameDay(date)) {
            this.timeIntervals.any { it.contains(date) }
        } else false
    }

    fun contains(dateInterval: DateInterval): Boolean {
        return if (dateInterval.isWithinSameDay() && this.isSameDay(dateInterval.start)) {
            this.timeIntervals.any { it.contains(dateInterval.start) && it.contains(dateInterval.end) }
        } else false
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
                Pair("id", this.id.toString()),
                Pair("dayOfWeek", this.dayOfWeek.toString()),
                Pair("timeIntervals", this.timeIntervals.map { it.toMap() }))
    }
}