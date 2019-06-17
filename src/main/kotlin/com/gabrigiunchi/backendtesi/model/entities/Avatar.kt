package com.gabrigiunchi.backendtesi.model.entities

import com.gabrigiunchi.backendtesi.model.type.ImageType
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.Entity
import javax.persistence.OneToOne

@Entity
class Avatar(
        id: String,

        @OneToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        val user: User,

        lastModified: Long) : Image(id, ImageType.avatar, lastModified) {

    constructor(id: String, user: User) : this(id, user, Date().time)
}