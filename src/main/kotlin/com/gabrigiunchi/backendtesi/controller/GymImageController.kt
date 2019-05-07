package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.model.ImageMetadata
import com.gabrigiunchi.backendtesi.service.GymImageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/gyms")
class GymImageController(private val gymImageService: GymImageService) {

    private val logger = LoggerFactory.getLogger(GymImageController::class.java)

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping("/photos")
    fun getAllMetadata(): ResponseEntity<Collection<ImageMetadata>> {
        this.logger.info("GET all images")
        return ResponseEntity(this.gymImageService.getAllMetadata(), HttpStatus.OK)
    }

    @GetMapping("/{id}/photos")
    fun getPhotoMetadataOfGym(@PathVariable id: Int): ResponseEntity<Collection<ImageMetadata>> {
        this.logger.info("GET photos of gym $id")
        return ResponseEntity(this.gymImageService.getPhotosOfGym(id), HttpStatus.OK)
    }

    @GetMapping("/photos/{imageId}")
    fun getPhoto(@PathVariable imageId: String): ResponseEntity<ByteArray> {
        this.logger.info("GET image $imageId")
        return ResponseEntity(this.gymImageService.download(imageId), HttpStatus.OK)
    }

    @GetMapping("/photos/{imageId}/metadata")
    fun getMetadataOfPhoto(@PathVariable imageId: String): ResponseEntity<ImageMetadata> {
        this.logger.info("GET metadata of image $imageId")
        return ResponseEntity(this.gymImageService.getImageMetadata(imageId), HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @RequestMapping("/{id}/photos/{name}", method = [RequestMethod.POST, RequestMethod.PUT])
    fun addPhoto(@PathVariable id: Int,
                 @PathVariable name: String,
                 @RequestBody image: MultipartFile): ResponseEntity<ImageMetadata> {
        this.logger.info("POST photo of gym $id")
        return ResponseEntity(this.gymImageService.setImage(id, image, name), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/photos/{image}")
    fun deletePhoto(@PathVariable image: String): ResponseEntity<Void> {
        this.logger.info("DELETE photo $image")
        this.gymImageService.deleteImage(image)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}