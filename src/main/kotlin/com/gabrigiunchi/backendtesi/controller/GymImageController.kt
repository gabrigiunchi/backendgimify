package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.model.ImageMetadata
import com.gabrigiunchi.backendtesi.service.GymImageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/gyms")
class GymImageController(private val gymImageService: GymImageService) {

    private val logger = LoggerFactory.getLogger(GymImageController::class.java)

    @GetMapping("/{id}/photos")
    fun getPhotoMetadataOfGym(@PathVariable id: Int): ResponseEntity<Collection<ImageMetadata>> {
        this.logger.info("GET photos of gym $id")
        return ResponseEntity(this.gymImageService.getPhotosOfGym(id), HttpStatus.OK)
    }

    @GetMapping("/photos/{imageId}")
    fun getPhotoOfGym(@PathVariable imageId: String): ResponseEntity<ByteArray> {
        this.logger.info("GET photo $imageId")
        return ResponseEntity(this.gymImageService.download(imageId), HttpStatus.OK)
    }

    @RequestMapping("/{id}/photos/{name}", method = [RequestMethod.POST, RequestMethod.PUT])
    fun addPhoto(@PathVariable id: Int,
                 @PathVariable name: String,
                 @RequestBody image: MultipartFile): ResponseEntity<ImageMetadata> {
        this.logger.info("POST photo of gym $id")
        return ResponseEntity(this.gymImageService.setImage(id, image, name), HttpStatus.CREATED)
    }

    @DeleteMapping("/photos/{image}")
    fun deletePhoto(@PathVariable image: String): ResponseEntity<Void> {
        this.logger.info("DELETE photo $image")
        this.gymImageService.deleteImage(image)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}