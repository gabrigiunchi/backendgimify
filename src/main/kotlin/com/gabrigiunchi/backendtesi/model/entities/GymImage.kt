package com.gabrigiunchi.backendtesi.model.entities

import com.gabrigiunchi.backendtesi.model.type.ImageType
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class GymImage(
        id: String,

        type: ImageType,

        @ManyToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        val gym: Gym,

        lastModified: Long
) : Image(id, type, lastModified) {

    constructor(id: String, type: ImageType, gym: Gym) : this(id, type, gym, Date().time)
}