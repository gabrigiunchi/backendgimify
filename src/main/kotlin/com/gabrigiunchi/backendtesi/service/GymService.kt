package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.CommentDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.GymDTOInput
import com.gabrigiunchi.backendtesi.model.entities.City
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.google.maps.model.LatLng
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class GymService(
        private val timetableDAO: TimetableDAO,
        private val gymDAO: GymDAO,
        private val cityDAO: CityDAO,
        private val mapsService: MapsService,
        private val commentDAO: CommentDAO) {

    fun getGymById(gymId: Int): Gym = this.gymDAO.findById(gymId).orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }

    fun getGymsByCity(cityId: Int): List<Gym> =
            this.cityDAO.findById(cityId)
                    .map { this.gymDAO.findByCity(it) }
                    .orElseThrow { ResourceNotFoundException("city $cityId does not exist") }

    fun saveGym(gym: GymDTOInput): Gym {
        if (this.gymDAO.findByName(gym.name).isPresent) {
            throw ResourceAlreadyExistsException("gym with name ${gym.name} already exists")
        }

        val city = this.cityDAO.findById(gym.cityId)
                .orElseThrow { ResourceNotFoundException("city ${gym.cityId} does not exist") }

        val location = this.getCoordinatedOfGym(gym.address, city)
        return this.gymDAO.save(Gym(gym.name, gym.address, city, location.lat, location.lng))
    }

    fun updateGym(gymDTO: GymDTOInput, id: Int): Gym {
        val city = this.cityDAO.findById(gymDTO.cityId)
                .orElseThrow { ResourceNotFoundException("city ${gymDTO.cityId} does not exist") }

        val savedGym = this.gymDAO.findById(id)
                .orElseThrow { ResourceNotFoundException("gym $id does not exist") }

        if (savedGym.address != gymDTO.address) {
            savedGym.address = gymDTO.address
            val coordinated = this.getCoordinatedOfGym(gymDTO.address, city)
            savedGym.latitude = coordinated.lat
            savedGym.longitude = coordinated.lng
        }

        savedGym.name = gymDTO.name
        savedGym.city = city

        return this.gymDAO.save(savedGym)
    }

    fun deleteGym(gymId: Int) {
        val gym = this.gymDAO.findById(gymId).orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
        val timetable = this.timetableDAO.findByGym(gym)

        if (timetable.isPresent) {
            this.timetableDAO.delete(timetable.get())
        }

        this.gymDAO.delete(gym)
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

    private fun getCoordinatedOfGym(address: String, city: City): LatLng =
            this.mapsService.geocode("$address ${city.name}")
                    ?: throw IllegalArgumentException("cannot geocode address $address")
}