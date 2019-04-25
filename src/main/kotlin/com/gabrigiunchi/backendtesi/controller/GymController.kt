package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.service.GymService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/gyms")
class GymController(private val gymDAO: GymDAO,
                    private val gymService: GymService,
                    private val cityDAO: CityDAO) {

    private val logger = LoggerFactory.getLogger(GymController::class.java)

    @GetMapping
    fun getAllGyms(): ResponseEntity<Iterable<Gym>> {
        this.logger.info("GET all gyms")
        return ResponseEntity(this.gymDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getGymById(@PathVariable id: Int): ResponseEntity<Gym> {
        this.logger.info("GET gym #$id")
        return this.gymDAO.findById(id)
                .map { kind -> ResponseEntity(kind, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("gym $id does not exist") }
    }

    @GetMapping("/by_city/{cityId}")
    fun getGymByCity(@PathVariable cityId: Int): ResponseEntity<List<Gym>> {
        this.logger.info("GET gym by city #$cityId")
        return this.cityDAO.findById(cityId)
                .map { ResponseEntity(this.gymDAO.findByCity(it), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("city $cityId does not exist") }
    }

    @GetMapping("/{id}/rating")
    fun getRatingOfGym(@PathVariable id: Int): ResponseEntity<Double> {
        this.logger.info("GET rating of gym $id")
        return ResponseEntity(this.gymService.calculateRatingOfGym(id), HttpStatus.OK)
    }

    @PostMapping
    fun createGym(@Valid @RequestBody gym: Gym): ResponseEntity<Gym> {
        this.logger.info("CREATE gym")

        if (this.gymDAO.findById(gym.id).isPresent || this.gymDAO.findByName(gym.name).isPresent) {
            throw ResourceAlreadyExistsException("gym ${gym.id} with name ${gym.name} already exists")
        }

        if (this.cityDAO.findById(gym.city.id).isEmpty) {
            throw ResourceNotFoundException("city ${gym.city.id} does not exist")
        }

        return ResponseEntity(this.gymDAO.save(gym), HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateGym(@Valid @RequestBody gym: Gym, @PathVariable id: Int): ResponseEntity<Gym> {
        this.logger.info("PUT gym #${gym.id}")

        if (this.gymDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("gym $id does not exist")
        }

        return ResponseEntity(this.gymDAO.save(gym), HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteGym(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE gym #$id")
        this.gymDAO.delete(this.gymDAO.findById(id).orElseThrow { ResourceNotFoundException("gym $id does not exist") })
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}