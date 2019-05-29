package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.GymImage
import com.gabrigiunchi.backendtesi.model.type.ImageType
import org.springframework.data.repository.CrudRepository

interface GymImageDAO : CrudRepository<GymImage, String> {
    fun findByGym(gym: Gym): List<GymImage>
    fun findByGymAndType(gym: Gym, type: ImageType): List<GymImage>
}