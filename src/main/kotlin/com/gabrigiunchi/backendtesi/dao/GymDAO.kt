package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.Region
import org.springframework.data.repository.CrudRepository
import java.util.*

interface GymDAO: CrudRepository<Gym, Int> {
    fun findByName(name: String): Optional<Gym>
    fun findByRegion(region: Region): List<Gym>
}