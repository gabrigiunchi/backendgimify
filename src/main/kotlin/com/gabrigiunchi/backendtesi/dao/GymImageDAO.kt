package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.GymImage
import org.springframework.data.repository.CrudRepository

interface GymImageDAO : CrudRepository<GymImage, String> {
    fun findByGym(gym: Gym): List<GymImage>
}