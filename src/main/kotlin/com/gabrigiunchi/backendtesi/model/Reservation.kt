package com.gabrigiunchi.backendtesi.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class Reservation(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,

        @ManyToOne(fetch = FetchType.EAGER)
        val asset: Asset,

        @ManyToOne(fetch = FetchType.EAGER)
        @OnDelete(action = OnDeleteAction.CASCADE)
        val user: User,

        val start: LocalDateTime,

        val end: LocalDateTime

) {
    constructor(asset: Asset, user: User, start: LocalDateTime, end: LocalDateTime) : this(-1, asset, user, start, end)
}