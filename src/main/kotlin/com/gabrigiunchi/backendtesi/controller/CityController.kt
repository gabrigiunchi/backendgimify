package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.model.dto.input.CityDTOInput
import com.gabrigiunchi.backendtesi.model.entities.City
import com.gabrigiunchi.backendtesi.service.CityService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/cities")
class CityController(private val cityService: CityService) {

    private val logger = LoggerFactory.getLogger(CityController::class.java)

    @GetMapping
    fun getAllCities(): ResponseEntity<Iterable<City>> {
        this.logger.info("GET all cities")
        return ResponseEntity(this.cityService.getAllCities(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getCityById(@PathVariable id: Int): ResponseEntity<City> {
        this.logger.info("GET city #$id")
        return ResponseEntity(this.cityService.getCityById(id), HttpStatus.OK)
    }

    @GetMapping("/by_name/{name}")
    fun getCityByName(@PathVariable name: String): ResponseEntity<City> {
        this.logger.info("GET city $name")
        return ResponseEntity(this.cityService.getCityByName(name), HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun createCity(@Valid @RequestBody city: CityDTOInput): ResponseEntity<City> {
        this.logger.info("CREATE city")
        return ResponseEntity(this.cityService.saveCity(city), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping("/{id}")
    fun modifyCity(@Valid @RequestBody city: CityDTOInput, @PathVariable id: Int): ResponseEntity<City> {
        this.logger.info("Modify city $id")
        return ResponseEntity(this.cityService.modifyCity(city, id), HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteCityById(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE city #$id")
        this.cityService.deleteCity(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}