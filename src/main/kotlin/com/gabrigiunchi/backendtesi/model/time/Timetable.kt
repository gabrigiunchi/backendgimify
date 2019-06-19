package com.gabrigiunchi.backendtesi.model.time

import com.gabrigiunchi.backendtesi.model.dto.input.TimetableDTO
import com.gabrigiunchi.backendtesi.model.entities.Gym
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
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
        val openings: Collection<RepeatedZonedInterval>,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val closingDays: Collection<RepeatedZonedInterval>
) {

    constructor(id: Int, gym: Gym, timetableDTO: TimetableDTO) :
            this(id = id,
                    gym = gym,
                    openings = timetableDTO.openings.map { it.toRepeatedZonedInterval(gym.city.zoneId) },
                    closingDays = timetableDTO.closingDays.map { it.toRepeatedZonedInterval(gym.city.zoneId) })


    constructor(gym: Gym) : this(-1, gym, emptySet(), emptySet())

    constructor(gym: Gym, openings: Collection<RepeatedLocalInterval>) :
            this(-1, gym, openings.map { it.toRepeatedZonedInterval(gym.city.zoneId) }, emptySet())

    constructor(gym: Gym, openings: Set<RepeatedLocalInterval>, closingDays: Set<RepeatedLocalInterval>) :
            this(-1, gym, openings.map { it.toRepeatedZonedInterval(gym.city.zoneId) }, closingDays.map { it.toRepeatedZonedInterval(gym.city.zoneId) })


    fun isClosedAt(interval: ZonedInterval): Boolean = this.closingDays.any { it.overlaps(interval) }
    fun isClosedAt(date: OffsetDateTime): Boolean = this.closingDays.any { it.contains(date) }
    fun isOpenAt(interval: ZonedInterval): Boolean = this.closingDays.none { it.overlaps(interval) } && this.openings.any { it.contains(interval) }
    fun isOpenAt(date: OffsetDateTime): Boolean = this.closingDays.none { it.contains(date) } && this.openings.any { it.contains(date) }

}