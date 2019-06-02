package com.gabrigiunchi.backendtesi.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.OffsetDateTime
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

        val start: OffsetDateTime,

        val end: OffsetDateTime,

        val date: OffsetDateTime,
        var active: Boolean

) {
    constructor(asset: Asset, user: User, start: OffsetDateTime, end: OffsetDateTime) :
            this(-1, asset, user, start, end, OffsetDateTime.now(), true)
}