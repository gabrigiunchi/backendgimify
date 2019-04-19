package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class AssetKind(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        val name: String,
        val maxReservationTime: Int // minutes
) {
        constructor(name: String, maxReservationTime: Int): this(-1, name, maxReservationTime)
        constructor(kind: AssetKindEnum, maxReservationTime: Int): this(-1, kind, maxReservationTime)
        constructor(id: Int,kind: AssetKindEnum, maxReservationTime: Int): this(id, kind.name, maxReservationTime)
}