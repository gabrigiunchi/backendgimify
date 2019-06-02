package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.model.dto.input.CityDTOInput
import com.gabrigiunchi.backendtesi.model.entities.City
import org.springframework.stereotype.Service

@Service
class CityService(private val cityDAO: CityDAO, private val mapsService: MapsService) {

    fun saveCity(city: CityDTOInput): City {
        if (this.cityDAO.findByName(city.name).isPresent) {
            throw ResourceAlreadyExistsException("city already exists")
        }

        val latLng = this.mapsService.geocode(city.name)
                ?: throw IllegalArgumentException("city ${city.name} not found")
        val zoneId = this.mapsService.getTimezone(latLng)
        return this.cityDAO.save(City(city.name, zoneId))
    }
}