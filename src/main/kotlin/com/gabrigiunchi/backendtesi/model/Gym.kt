package com.gabrigiunchi.backendtesi.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.ZoneId
import javax.persistence.*

@Entity
class Gym(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        var name: String,
        val address: String,

        @ManyToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        val city: City,

        val zoneId: ZoneId,
        val latitude: Double,
        val longitude: Double
) {

    constructor(name: String, address: String, city: City) :
            this(-1, name, address, city, ZoneId.of("UTC"), 45.467066, 9.233418)

    constructor(name: String, address: String, city: City, zoneId: String) :
            this(-1, name, address, city, ZoneId.of(zoneId), 45.467066, 9.233418)

    constructor(name: String, address: String, city: City, zoneId: String, latitude: Double, longitude: Double) :
            this(-1, name, address, city, ZoneId.of(zoneId), latitude, longitude)
}