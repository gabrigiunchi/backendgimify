package com.gabrigiunchi.backendtesi.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
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
        val region: Region,

        @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
        val openings: List<Schedule>
) {
    constructor(name: String, address: String, region: Region, openings: List<Schedule>) : this(-1, name, address, region, openings)
    constructor(name: String, address: String, region: Region) : this(-1, name, address, region, emptyList<Schedule>())


    fun toJson(): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("id", this.id.toString()),
                Pair("name", this.name),
                Pair("address", this.address),
                Pair("region", this.region),
                Pair("openings", this.openings.map { it.toMap() })))
    }
}