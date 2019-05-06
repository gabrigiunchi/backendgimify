package com.gabrigiunchi.backendtesi.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.*

@Entity
class GymImage(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,

        @ManyToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        val gym: Gym,

        val name: String,
        val lastModified: Long
) {
    constructor(gym: Gym, name: String) : this(-1, gym, name, Date().time)
}