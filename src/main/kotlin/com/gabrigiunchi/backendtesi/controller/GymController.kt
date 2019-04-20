package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.RegionDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Gym
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/gyms")
class GymController(private val gymDAO: GymDAO,
                    private val regionDAO: RegionDAO) {

    private val logger = LoggerFactory.getLogger(GymController::class.java)

    @GetMapping
    fun getGyms(): ResponseEntity<Iterable<Gym>> {
        this.logger.info("GET all gyms")
        return ResponseEntity(this.gymDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getGymById(@PathVariable id: Int): ResponseEntity<Gym> {
        this.logger.info("GET gym #$id")
        return this.gymDAO.findById(id)
                .map { kind -> ResponseEntity(kind, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException(id) }
    }

    @GetMapping("/by_region/{regionId}")
    fun getGymByRegion(@PathVariable regionId: Int): ResponseEntity<List<Gym>> {
        this.logger.info("GET gym by region #$regionId")
        return this.regionDAO.findById(regionId)
                .map { ResponseEntity(this.gymDAO.findByRegion(it), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("region #$regionId does not exist") }
    }

    @PostMapping
    fun createGym(@Valid @RequestBody gym: Gym): ResponseEntity<Gym> {
        this.logger.info("CREATE gym")

        if (this.gymDAO.findById(gym.id).isPresent || this.gymDAO.findByName(gym.name).isPresent) {
            throw ResourceAlreadyExistsException(gym.id)
        }

        if (this.regionDAO.findById(gym.region.id).isEmpty) {
            throw ResourceNotFoundException("region #${gym.region.id} does not exists")
        }

        return ResponseEntity(this.gymDAO.save(gym), HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateGym(@Valid @RequestBody gym: Gym, @PathVariable id: Int): ResponseEntity<Gym> {
        this.logger.info("PUT gym #${gym.id}")

        if (this.gymDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException(gym.id)
        }

        return ResponseEntity(this.gymDAO.save(gym), HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteGym(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE gym #$id")

        if (this.gymDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException(id)
        }

        this.gymDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}