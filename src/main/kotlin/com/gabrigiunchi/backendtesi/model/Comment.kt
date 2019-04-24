package com.gabrigiunchi.backendtesi.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.*

@Entity
class Comment(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,

        @ManyToOne(fetch = FetchType.EAGER)
        @OnDelete(action = OnDeleteAction.CASCADE)
        val user: User,

        @ManyToOne(fetch = FetchType.EAGER)
        @OnDelete(action = OnDeleteAction.CASCADE)
        val gym: Gym,

        val title: String,
        val message: String,
        val rating: Int,
        val date: Date
) {
    constructor(user: User, gym: Gym, title: String, message: String, rating: Int) :
            this(-1, user, gym, title, message, rating, Date())

    constructor(user: User, gym: Gym, title: String, message: String, rating: Int, date: Date) :
            this(-1, user, gym, title, message, rating, date)

    init {
        if (this.rating < 1 || this.rating > 5) {
            throw IllegalArgumentException("rating must be between 1 and 5")
        }
    }
}