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
        val intervals: Set<Interval>
) {
        constructor(dayOfWeek: DayOfWeek): this(-1, dayOfWeek, emptySet())
        constructor(dayOfWeek: DayOfWeek, intervals: Set<Interval>): this(-1, dayOfWeek, intervals)
        constructor(scheduleDTO: ScheduleDTO): this(-1, scheduleDTO.dayOfWeek, scheduleDTO.intervals)
}