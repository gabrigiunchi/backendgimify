package com.gabrigiunchi.backendtesi.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.ZoneId
import javax.persistence.*

@Entity
class Timetable(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,

        @OneToOne(fetch = FetchType.EAGER)
        @OnDelete(action = OnDeleteAction.CASCADE)
        val gym: Gym,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val openings: Set<RepeatedInterval>,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val closingDays: Set<RepeatedInterval>
) {

    constructor(gym: Gym) : this(-1, gym, emptySet(), emptySet())

    constructor(gym: Gym, openings: Set<RepeatedInterval>) :
            this(-1, gym, openings, emptySet())

    constructor(gym: Gym, openings: Set<RepeatedInterval>, closingDays: Set<RepeatedInterval>) :
            this(-1, gym, openings, closingDays)


    fun contains(zonedInterval: ZonedInterval, zoneId: ZoneId): Boolean =
            this.closingDays.none { it.overlaps(zonedInterval.toInterval(zoneId)) } &&
                    this.openings.any { it.contains(zonedInterval.toInterval(zoneId)) }

    fun contains(interval: Interval): Boolean =
            this.closingDays.none { it.overlaps(interval) } && this.openings.any { it.contains(interval) }
}