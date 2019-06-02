package com.gabrigiunchi.backendtesi.model.entities

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

        lastModified: Long) : Image(id, lastModified) {

    constructor(id: String, user: User) : this(id, user, Date().time)
}