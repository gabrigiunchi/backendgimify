package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.model.dto.input.GymDTOInput
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.service.GymService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/gyms")
class GymController(private val gymService: GymService) {

    private val logger = LoggerFactory.getLogger(GymController::class.java)

    @GetMapping("/page/{page}/size/{size}")
    fun getAllGyms(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<Gym>> {
        this.logger.info("GET all gyms")
        return ResponseEntity.ok(this.gymService.getAllGyms(page, size))
    }

    @GetMapping("/{id}")
    fun getGymById(@PathVariable id: Int): ResponseEntity<Gym> {
        this.logger.info("GET gym #$id")
        return ResponseEntity.ok(this.gymService.getGymById(id))
    }

    @GetMapping("/by_city/{cityId}")
    fun getGymByCity(@PathVariable cityId: Int): ResponseEntity<List<Gym>> {
        this.logger.info("GET gym by city #$cityId")
        return ResponseEntity.ok(this.gymService.getGymsByCity(cityId))
    }

    @GetMapping("/{id}/rating")
    fun getRatingOfGym(@PathVariable id: Int): ResponseEntity<Double> {
        this.logger.info("GET rating of gym $id")
        return ResponseEntity.ok(this.gymService.calculateRatingOfGym(id))
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
        return ResponseEntity.ok(this.gymService.updateGym(gym, id))
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteGym(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE gym #$id")
        this.gymService.deleteGym(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}