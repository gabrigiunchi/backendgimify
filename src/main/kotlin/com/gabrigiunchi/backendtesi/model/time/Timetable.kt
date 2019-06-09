package com.gabrigiunchi.backendtesi.model.time

import com.gabrigiunchi.backendtesi.model.entities.Gym
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime
import java.time.OffsetDateTime
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
        val openings: Set<RepeatedLocalInterval>,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val closingDays: Set<RepeatedLocalInterval>
) {

    constructor(gym: Gym) : this(-1, gym, emptySet(), emptySet())

    constructor(gym: Gym, openings: Set<RepeatedLocalInterval>) :
            this(-1, gym, openings, emptySet())

    constructor(gym: Gym, openings: Set<RepeatedLocalInterval>, closingDays: Set<RepeatedLocalInterval>) :
            this(-1, gym, openings, closingDays)

    /*********************** LOCAL *********************************************/

    fun isClosedAt(date: LocalDateTime): Boolean = this.closingDays.any { it.contains(date) }

    fun isClosedAt(interval: LocalInterval): Boolean = this.closingDays.any { it.overlaps(interval) }
    fun isOpenAt(localInterval: LocalInterval): Boolean = this.closingDays.none { it.overlaps(localInterval) } && this.openings.any { it.contains(localInterval) }
    fun isOpenAt(date: LocalDateTime): Boolean = this.closingDays.none { it.contains(date) } && this.openings.any { it.contains(date) }

    /************************* ZONED ********************************************/

    fun isClosedAt(date: OffsetDateTime): Boolean = this.isClosedAt(date.atZoneSameInstant(this.gym.city.zoneId).toLocalDateTime())

    fun isClosedAt(interval: ZonedInterval): Boolean = this.isClosedAt(interval.toLocalInterval(this.gym.city.zoneId))
    fun isOpenAt(date: OffsetDateTime): Boolean = this.isOpenAt(date.atZoneSameInstant(this.gym.city.zoneId).toLocalDateTime())
    fun isOpenAt(interval: ZonedInterval): Boolean = this.isOpenAt(interval.toLocalInterval(this.gym.city.zoneId))

}