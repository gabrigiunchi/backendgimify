package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.CityDTOInput
import com.gabrigiunchi.backendtesi.model.entities.City
import com.gabrigiunchi.backendtesi.service.MapsService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/cities")
class CityController(private val cityDAO: CityDAO, private val mapsService: MapsService) {

    private val logger = LoggerFactory.getLogger(CityController::class.java)

    @GetMapping
    fun getAllCities(): ResponseEntity<Iterable<City>> {
        this.logger.info("GET all cities")
        return ResponseEntity(this.cityDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getCityById(@PathVariable id: Int): ResponseEntity<City> {
        this.logger.info("GET city #$id")
        return this.cityDAO.findById(id)
                .map { ResponseEntity(it, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("city $id does not exist") }
    }

    @GetMapping("/by_name/{name}")
    fun getCityByName(@PathVariable name: String): ResponseEntity<City> {
        this.logger.info("GET city $name")
        return this.cityDAO.findByName(name)
                .map { ResponseEntity(it, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("city $name does not exist") }
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun createCity(@Valid @RequestBody city: CityDTOInput): ResponseEntity<City> {
        this.logger.info("CREATE city")

        if (this.cityDAO.findByName(city.name).isPresent) {
            throw ResourceAlreadyExistsException("city already exists")
        }

        val latLng = this.mapsService.geocode(city.name)
                ?: throw IllegalArgumentException("city ${city.name} not found")
        val zoneId = this.mapsService.getTimezone(latLng)
        return ResponseEntity(this.cityDAO.save(City(city.name, zoneId)), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteCityById(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE city #$id")
        val city = this.cityDAO.findById(id).orElseThrow { ResourceNotFoundException("city $id does not exist") }
        this.cityDAO.delete(city)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}