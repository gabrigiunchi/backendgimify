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
        val name: String
) {
        constructor(name: String): this(-1, name)
        constructor(kind: AssetKindEnum): this(-1, kind)
        constructor(id: Int,kind: AssetKindEnum): this(id, kind.name)
}