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

    fun getAllCities(): Iterable<City> = this.cityDAO.findAll()

    fun getCityByName(name: String): City = this.cityDAO.findByName(name)
            .orElseThrow { ResourceNotFoundException(City::class.java, name) }

    fun getCityById(cityId: Int): City = this.cityDAO.findById(cityId)
            .orElseThrow { ResourceNotFoundException(City::class.java, cityId) }

    fun saveCity(city: CityDTOInput): City {
        if (this.cityDAO.findByName(city.name).isPresent) {
            throw ResourceAlreadyExistsException("city already exists")
        }
        return this.cityDAO.save(City(city.name, this.getTimezone(city.name)))
    }

    fun modifyCity(cityDTO: CityDTOInput, id: Int): City {
        val city = this.cityDAO.findById(id).orElseThrow { ResourceNotFoundException(City::class.java, id) }
        city.name = cityDTO.name
        city.zoneId = this.getTimezone(cityDTO.name)
        return this.cityDAO.save(city)
    }


    fun deleteCity(cityId: Int) {
        val city = this.cityDAO.findById(cityId).orElseThrow { ResourceNotFoundException(City::class.java, cityId) }
        this.cityDAO.delete(city)
    }

    private fun getTimezone(cityName: String): ZoneId {
        val latLng = this.mapsService.geocode(cityName)
                ?: throw ResourceNotFoundException(City::class.java, cityName)
        return this.mapsService.getTimezone(latLng)
    }
}