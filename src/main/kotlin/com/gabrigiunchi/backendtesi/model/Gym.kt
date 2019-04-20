package com.gabrigiunchi.backendtesi.model

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
        val region: Region
) {
    constructor(name: String, address: String, region: Region) : this(-1, name, address, region)
}