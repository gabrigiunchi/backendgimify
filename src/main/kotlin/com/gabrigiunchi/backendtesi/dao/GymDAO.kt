package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Gym
import org.springframework.data.repository.CrudRepository

interface GymDAO: CrudRepository<Gym, Int> {
    fun findByName(name: String): List<Gym>
}