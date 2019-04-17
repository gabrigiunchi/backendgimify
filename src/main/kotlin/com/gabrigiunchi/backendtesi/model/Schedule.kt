package com.gabrigiunchi.backendtesi.model

import java.time.DayOfWeek
import javax.persistence.*

@Entity
class Schedule(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,

        val dayOfWeek: DayOfWeek,

        @OneToMany
        val intervals: Set<Interval>
) {
        constructor(dayOfWeek: DayOfWeek): this(-1, dayOfWeek, emptySet())
        constructor(dayOfWeek: DayOfWeek, intervals: Set<Interval>): this(-1, dayOfWeek, intervals)
}