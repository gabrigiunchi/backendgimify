package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.ScheduleDTO
import java.time.DayOfWeek
import javax.persistence.*

@Entity
class Schedule(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,

        val dayOfWeek: DayOfWeek,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val intervals: Set<Interval>,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val exceptions: Set<DateInterval>
) {
        constructor(scheduleDTO: ScheduleDTO): this(-1, scheduleDTO.dayOfWeek, scheduleDTO.intervals, scheduleDTO.exceptions)
        constructor(dayOfWeek: DayOfWeek): this(-1, dayOfWeek, emptySet(), emptySet())
        constructor(dayOfWeek: DayOfWeek, intervals: Set<Interval>): this(-1, dayOfWeek, intervals, emptySet())
        constructor(dayOfWeek: DayOfWeek, intervals: Set<Interval>, exceptions: Set<DateInterval>): this(-1, dayOfWeek, intervals, exceptions)

        fun toMap(): Map<String, Any> {
                return mapOf(
                        Pair("id", this.id.toString()),
                        Pair("dayOfWeek", this.dayOfWeek.toString()),
                        Pair("intervals", this.intervals.map { it.toMap() }),
                        Pair("exceptions", this.exceptions))
        }
}