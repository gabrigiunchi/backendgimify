package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.CityDTOInput
import com.gabrigiunchi.backendtesi.model.entities.City
import org.springframework.stereotype.Service
import java.time.ZoneId

@Service
class CityService(private val cityDAO: CityDAO, private val mapsService: MapsService) {

    fun saveCity(city: CityDTOInput): City {
        if (this.cityDAO.findByName(city.name).isPresent) {
            throw ResourceAlreadyExistsException("city already exists")
        }
        return this.cityDAO.save(City(city.name, this.getTimezone(city.name)))
    }

    fun modifyCity(cityDTO: CityDTOInput, id: Int): City {
        val city = this.cityDAO.findById(id).orElseThrow { ResourceNotFoundException("city $id does not exist") }
        city.name = cityDTO.name
        city.zoneId = this.getTimezone(cityDTO.name)
        return this.cityDAO.save(city)
    }

    private fun getTimezone(cityName: String): ZoneId {
        val latLng = this.mapsService.geocode(cityName)
                ?: throw IllegalArgumentException("city $cityName not found")
        return this.mapsService.getTimezone(latLng)
    }
}