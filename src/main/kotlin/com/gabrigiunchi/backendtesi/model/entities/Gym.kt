package com.gabrigiunchi.backendtesi.model.entities

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*

@Entity
class Gym(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        var name: String,
        var address: String,

        @ManyToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        var city: City,

        var latitude: Double,
        var longitude: Double
) {

    constructor(name: String, address: String, city: City) :
            this(-1, name, address, city, 40.0, 40.0)

    constructor(name: String, address: String, city: City, latitude: Double, longitude: Double) :
            this(-1, name, address, city, latitude, longitude)
}