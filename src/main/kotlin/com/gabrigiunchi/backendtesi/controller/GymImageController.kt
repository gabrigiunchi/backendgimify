package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.model.ImageMetadata
import com.gabrigiunchi.backendtesi.service.GymImageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/gyms")
class GymImageController(private val gymImageService: GymImageService) {

    private val logger = LoggerFactory.getLogger(GymImageController::class.java)

    @GetMapping("/{id}/photos")
    fun getPhotosOfGym(@PathVariable id: Int): ResponseEntity<Collection<ImageMetadata>> {
        this.logger.info("GET photos of gym $id")
        return ResponseEntity(this.gymImageService.getPhotosOfGym(id), HttpStatus.OK)
    }
}