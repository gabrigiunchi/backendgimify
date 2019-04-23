package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.City
import com.gabrigiunchi.backendtesi.model.Gym
import org.springframework.data.repository.CrudRepository
import java.util.*

interface GymDAO : CrudRepository<Gym, Int> {
    fun findByName(name: String): Optional<Gym>
    fun findByCity(city: City): List<Gym>
}