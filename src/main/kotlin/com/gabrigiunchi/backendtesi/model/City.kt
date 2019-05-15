package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.type.CityEnum
import java.time.ZoneId
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class City(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        val name: String,
        val zoneId: ZoneId
) {
    constructor(city: CityEnum) : this(-1, city.name, ZoneId.of("UTC"))
    constructor(city: CityEnum, zoneId: ZoneId) : this(-1, city.name, zoneId)
    constructor(city: CityEnum, zoneId: String) : this(-1, city.name, ZoneId.of(zoneId))
}