package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.util.*
import javax.persistence.*

@Entity
class User(
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,

        val username: String,
        val password: String,
        val name: String,
        val surname: String,
        val email: String,

        @ManyToMany(fetch = FetchType.EAGER)
        val roles: MutableCollection<UserRole>,

        val validFrom: Date,
        val expireDate: Date) {

    var isActive = true

    constructor(username: String, password: String, name: String, surname: String, email: String, roles: MutableCollection<UserRole>) :
            this(-1, username, password, name, surname, email, roles, Date(), DateDecorator.max().date)


    constructor(username: String, password: String, name: String, surname: String, email: String) :
            this(username, password, name, surname, email, mutableListOf())


    override fun toString(): String {
        return "{id:$id, username:$username, password:$password, name:$name, surname:$surname, email:$email}"
    }
}