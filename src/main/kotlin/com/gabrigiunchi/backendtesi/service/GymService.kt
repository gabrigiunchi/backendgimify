package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.CommentDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.dto.input.GymDTOInput
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class GymService(
        private val gymDAO: GymDAO,
        private val cityDAO: CityDAO,
        private val mapsService: MapsService,
        private val commentDAO: CommentDAO) {

    fun saveGym(gym: GymDTOInput): Gym {
        if (this.gymDAO.findByName(gym.name).isPresent) {
            throw ResourceAlreadyExistsException("gym with name ${gym.name} already exists")
        }

        val city = this.cityDAO.findById(gym.cityId)
                .orElseThrow { ResourceNotFoundException("city ${gym.cityId} does not exist") }

        val location = this.mapsService.geocode("${gym.address} ${city.name}")
                ?: throw IllegalArgumentException("address ${gym.address} does not exist")
        return this.gymDAO.save(Gym(gym.name, gym.address, city, location.lat, location.lng))
    }

    fun calculateRatingOfGym(gymId: Int): Double {
        return this.gymDAO.findById(gymId)
                .map {
                    val ratings = this.commentDAO.findByGym(it, Pageable.unpaged())
                            .map { comment -> comment.rating }
                            .toList()

                    val sum = ratings.sum()
                    if (ratings.isEmpty()) -1.0 else sum / ratings.size.toDouble()
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }
}