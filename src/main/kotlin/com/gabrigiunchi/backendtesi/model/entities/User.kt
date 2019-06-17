package com.gabrigiunchi.backendtesi.model.entities

import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.util.*
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToMany

@Entity
class User(
        id: Int,
        val username: String,
        val password: String,
        name: String,
        val surname: String,
        val email: String,

        @ManyToMany(fetch = FetchType.EAGER)
        val roles: MutableCollection<UserRole>,

        val validFrom: Date,
        val expireDate: Date) : Drawable(id, name) {

    var notificationsEnabled = true
    var isActive = true

    constructor(username: String, password: String, name: String, surname: String, email: String, roles: MutableCollection<UserRole>) :
            this(-1, username, password, name, surname, email, roles, Date(),
                    DateDecorator.of("9999", "yyyy").date)


    constructor(username: String, password: String, name: String, surname: String, email: String) :
            this(username, password, name, surname, email, mutableListOf())


    override fun toString(): String {
        return "{id:$id, username:$username, password:$password, name:$name, surname:$surname, email:$email}"
    }
}