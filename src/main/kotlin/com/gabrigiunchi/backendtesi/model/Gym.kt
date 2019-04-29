package com.gabrigiunchi.backendtesi.model

import com.fasterxml.jackson.databind.ObjectMapper
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

        val zoneId: ZoneId
) {

    constructor(name: String, address: String, city: City) :
            this(name, address, city, "UTC")

    constructor(name: String, address: String, city: City, zoneId: String) :
            this(-1, name, address, city, ZoneId.of(zoneId))

    fun toJson(): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("id", this.id.toString()),
                Pair("name", this.name),
                Pair("address", this.address),
                Pair("city", this.city),
                Pair("zoneId", this.zoneId.toString())))
    }
}