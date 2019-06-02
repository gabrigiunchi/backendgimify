package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.time.LocalInterval
import com.gabrigiunchi.backendtesi.model.time.RepeatedLocalInterval
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
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

    fun contains(localInterval: LocalInterval): Boolean =
            this.closingDays.none { it.overlaps(localInterval) } && this.openings.any { it.contains(localInterval) }
}