package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.input.TimetableDTO
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
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
        val openings: Set<Schedule>,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val closingDays: Set<DateInterval>,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val exceptionalOpenings: Set<DateInterval>
) {
    constructor(gym: Gym) : this(-1, gym, emptySet(), emptySet(), emptySet())

    constructor(gym: Gym, openings: Set<Schedule>) : this(-1, gym, openings, emptySet(), emptySet())

    constructor(gym: Gym, openings: Set<Schedule>, closingDays: Set<DateInterval>) :
            this(-1, gym, openings, closingDays, emptySet())

    constructor(gym: Gym, openings: Set<Schedule>, closingDays: Set<DateInterval>, exceptionalOpenings: Set<DateInterval>) :
            this(-1, gym, openings, closingDays, exceptionalOpenings)


    fun contains(date: Date): Boolean {
        return this.closingDays.none { it.contains(date) } &&
                (this.exceptionalOpenings.any { it.contains(date) } || this.openings.any { it.contains(date) })
    }

    fun contains(dateInterval: DateInterval): Boolean {
        return this.closingDays.none { it.overlaps(dateInterval) } &&
                (this.exceptionalOpenings.any { it.contains(dateInterval) } || this.openings.any { it.contains(dateInterval) })
    }

    constructor(id: Int, timetableDTO: TimetableDTO, gym: Gym) :
            this(
                    id = id,
                    gym = gym,
                    openings = timetableDTO.openings,
                    exceptionalOpenings = timetableDTO.exceptionalOpenings,
                    closingDays = timetableDTO.closingDays
            )
}