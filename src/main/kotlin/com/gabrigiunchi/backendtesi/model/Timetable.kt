package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.TimetableDTO
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
        val openingExceptions: Set<DateInterval>
) {
    constructor(gym: Gym) : this(-1, gym, emptySet(), emptySet(), emptySet())
    constructor(gym: Gym, openings: Set<Schedule>, closingDays: Set<DateInterval>) :
            this(-1, gym, openings, closingDays, emptySet())

    constructor(gym: Gym, openings: Set<Schedule>, closingDays: Set<DateInterval>, openingExceptions: Set<DateInterval>) :
            this(-1, gym, openings, closingDays, openingExceptions)


    fun contains(date: Date): Boolean {
        return this.closingDays.none { it.contains(date) } &&
                (this.openingExceptions.any { it.contains(date) } || this.openings.any { it.contains(date) })
    }

    fun contains(dateInterval: DateInterval): Boolean {
        return this.closingDays.none { it.overlaps(dateInterval) } &&
                (this.openingExceptions.any { it.contains(dateInterval) } || this.openings.any { it.contains(dateInterval) })
    }

    constructor(id: Int, timetableDTO: TimetableDTO, gym: Gym) :
            this(
                    id = id,
                    gym = gym,
                    openings = timetableDTO.openings,
                    openingExceptions = timetableDTO.openingExceptions,
                    closingDays = timetableDTO.closingDays
            )
}