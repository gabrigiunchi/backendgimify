package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.GymImage
import org.springframework.data.repository.CrudRepository
import java.util.*

interface GymImageDAO : CrudRepository<GymImage, Int> {
    fun findByGym(gym: Gym): List<GymImage>
    fun findByName(name: String): Optional<GymImage>
}