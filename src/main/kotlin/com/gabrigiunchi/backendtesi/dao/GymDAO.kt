package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.City
import com.gabrigiunchi.backendtesi.model.entities.Gym
import org.springframework.data.repository.CrudRepository
import java.util.*

interface GymDAO : CrudRepository<Gym, Int> {
    fun findByName(name: String): Optional<Gym>
    fun findByCity(city: City): List<Gym>
}