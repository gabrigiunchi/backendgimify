package com.gabrigiunchi.backendtesi.model

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
        val user: User,

        val start: Date,

        val end: Date

) {
    constructor(asset: Asset, user: User, start: Date, end: Date) : this(-1, asset, user, start, end)
}