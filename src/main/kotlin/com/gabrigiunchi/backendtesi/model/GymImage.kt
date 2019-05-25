package com.gabrigiunchi.backendtesi.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class GymImage(
        @Id
        val id: String,

        @ManyToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        val gym: Gym,

        val lastModified: Long
) {
    constructor(id: String, gym: Gym) : this(id, gym, Date().time)
}