package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.model.dto.input.GymDTOInput
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.service.GymService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/gyms")
class GymController(private val gymDAO: GymDAO,
                    private val gymService: GymService) {

    private val logger = LoggerFactory.getLogger(GymController::class.java)

    @GetMapping
    fun getAllGyms(): ResponseEntity<Iterable<Gym>> {
        this.logger.info("GET all gyms")
        return ResponseEntity(this.gymDAO.findAll().sortedBy(Gym::name), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getGymById(@PathVariable id: Int): ResponseEntity<Gym> {
        this.logger.info("GET gym #$id")
        return ResponseEntity(this.gymService.getGymById(id), HttpStatus.OK)
    }

    @GetMapping("/by_city/{cityId}")
    fun getGymByCity(@PathVariable cityId: Int): ResponseEntity<List<Gym>> {
        this.logger.info("GET gym by city #$cityId")
        return ResponseEntity(this.gymService.getGymsByCity(cityId), HttpStatus.OK)
    }

    @GetMapping("/{id}/rating")
    fun getRatingOfGym(@PathVariable id: Int): ResponseEntity<Double> {
        this.logger.info("GET rating of gym $id")
        return ResponseEntity(this.gymService.calculateRatingOfGym(id), HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun createGym(@Valid @RequestBody gym: GymDTOInput): ResponseEntity<Gym> {
        this.logger.info("CREATE gym")
        return ResponseEntity(this.gymService.saveGym(gym), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping("/{id}")
    fun updateGym(@Valid @RequestBody gym: GymDTOInput, @PathVariable id: Int): ResponseEntity<Gym> {
        this.logger.info("PUT gym #$id")
        return ResponseEntity(this.gymService.updateGym(gym, id), HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteGym(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE gym #$id")
        this.gymService.deleteGym(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}