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
        val timeIntervals: Set<TimeInterval>,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val exceptions: Set<DateInterval>
) {
        constructor(scheduleDTO: ScheduleDTO): this(-1, scheduleDTO.dayOfWeek, scheduleDTO.timeIntervals, scheduleDTO.exceptions)
        constructor(dayOfWeek: DayOfWeek): this(-1, dayOfWeek, emptySet(), emptySet())
        constructor(dayOfWeek: DayOfWeek, timeIntervals: Set<TimeInterval>): this(-1, dayOfWeek, timeIntervals, emptySet())
        constructor(dayOfWeek: DayOfWeek, timeIntervals: Set<TimeInterval>, exceptions: Set<DateInterval>): this(-1, dayOfWeek, timeIntervals, exceptions)

        fun toMap(): Map<String, Any> {
                return mapOf(
                        Pair("id", this.id.toString()),
                        Pair("dayOfWeek", this.dayOfWeek.toString()),
                        Pair("timeIntervals", this.timeIntervals.map { it.toMap() }),
                        Pair("exceptions", this.exceptions))
        }
}