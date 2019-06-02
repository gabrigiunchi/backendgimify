package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.GymImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.gabrigiunchi.backendtesi.service.GymImageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/gyms")
class GymImageController(private val gymImageService: GymImageService, private val gymImageDAO: GymImageDAO) {

    private val logger = LoggerFactory.getLogger(GymImageController::class.java)

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping("/photos")
    fun getAllMetadata(): ResponseEntity<Collection<Image>> {
        this.logger.info("GET all images")
        return ResponseEntity(this.gymImageService.getAllMetadata(), HttpStatus.OK)
    }

    @GetMapping("/{id}/photos")
    fun getPhotoMetadataOfGym(@PathVariable id: Int): ResponseEntity<Collection<Image>> {
        this.logger.info("GET photos of gym $id")
        return ResponseEntity(this.gymImageService.getPhotosOfGym(id), HttpStatus.OK)
    }

    @GetMapping("/{id}/avatar")
    fun getAvatarOfGym(@PathVariable id: Int): ResponseEntity<ByteArray> {
        this.logger.info("GET avatar of gym $id")
        return ResponseEntity(this.gymImageService.getAvatarOfGym(id), HttpStatus.OK)
    }

    @GetMapping("/{id}/avatar/metadata")
    fun getAvatarMetadataOfGym(@PathVariable id: Int): ResponseEntity<Image> {
        this.logger.info("GET avatar metadata of gym $id")
        return ResponseEntity(this.gymImageService.getAvatarMetadataOfGym(id), HttpStatus.OK)
    }

    @GetMapping("/photos/{imageId}")
    fun getPhoto(@PathVariable imageId: String): ResponseEntity<ByteArray> {
        this.logger.info("GET image $imageId")
        return ResponseEntity(this.gymImageService.download(imageId), HttpStatus.OK)
    }

    @GetMapping("/photos/{imageId}/metadata")
    fun getMetadataOfPhoto(@PathVariable imageId: String): ResponseEntity<Image> {
        this.logger.info("GET metadata of image $imageId")
        return ResponseEntity(
                this.gymImageDAO.findById(imageId).orElseThrow { ResourceNotFoundException("image $imageId does not exist") },
                HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @RequestMapping("/{id}/photos/{name}", method = [RequestMethod.POST, RequestMethod.PUT])
    fun addPhoto(@PathVariable id: Int,
                 @PathVariable name: String,
                 @RequestBody image: MultipartFile): ResponseEntity<Image> {
        this.logger.info("POST photo of gym $id")
        return ResponseEntity(this.gymImageService.setImage(id, image, name, ImageType.profile), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @RequestMapping("/{id}/avatar/{name}", method = [RequestMethod.POST, RequestMethod.PUT])
    fun setAvatar(@PathVariable id: Int,
                  @PathVariable name: String,
                  @RequestBody image: MultipartFile): ResponseEntity<Image> {
        this.logger.info("POST avatar of gym $id")
        return ResponseEntity(this.gymImageService.setAvatar(id, image, name), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/photos/{image}")
    fun deletePhoto(@PathVariable image: String): ResponseEntity<Void> {
        this.logger.info("DELETE photo $image")
        this.gymImageService.deleteImage(image)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}