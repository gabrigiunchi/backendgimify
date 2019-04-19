package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.Region
import org.springframework.data.repository.CrudRepository

interface GymDAO: CrudRepository<Gym, Int> {
    fun findByName(name: String): List<Gym>
    fun findByRegion(region: Region): List<Gym>
}