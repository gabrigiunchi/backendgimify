package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.type.RegionEnum
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Region(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        val name: String
) {
    constructor(region: RegionEnum) : this(-1, region.name)
}