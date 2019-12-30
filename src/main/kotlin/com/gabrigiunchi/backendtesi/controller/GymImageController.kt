package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.gabrigiunchi.backendtesi.service.ImageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/gyms")
class GymImageController(
        @Value("\${application.objectstorage.gymphotosbucket}")
        private val bucketName: String,
        private val gymImageService: ImageService,
        private val gymImageDAO: ImageDAO) {

    private val logger = LoggerFactory.getLogger(GymImageController::class.java)

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping("/photos/page/{page}/size/{size}")
    fun getAllMetadata(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<Image>> {
        this.logger.info("GET all images")
        return ResponseEntity.ok(this.gymImageService.getAllMetadata(this.bucketName, page, size))
    }

    @GetMapping("/{id}/photos")
    fun getPhotoMetadataOfGym(@PathVariable id: Int): ResponseEntity<Collection<Image>> {
        this.logger.info("GET photos of gym $id")
        return ResponseEntity.ok(this.gymImageService.getImagesOfEntity(this.bucketName, id))
    }

    @GetMapping("/{id}/avatar")
    fun getAvatarOfGym(@PathVariable id: Int): ResponseEntity<ByteArray> {
        this.logger.info("GET avatar of gym $id")
        return ResponseEntity.ok(this.gymImageService.getAvatar(this.bucketName, id))
    }

    @GetMapping("/{id}/avatar/metadata")
    fun getAvatarMetadataOfGym(@PathVariable id: Int): ResponseEntity<Image> {
        this.logger.info("GET avatar metadata of gym $id")
        return ResponseEntity.ok(this.gymImageService.getAvatarMetadata(this.bucketName, id))
    }

    @GetMapping("/photos/{imageId}")
    fun getPhoto(@PathVariable imageId: String): ResponseEntity<ByteArray> {
        this.logger.info("GET image $imageId")
        return ResponseEntity.ok(this.gymImageService.download(this.bucketName, imageId))
    }

    @GetMapping("/photos/{imageId}/metadata")
    fun getMetadataOfPhoto(@PathVariable imageId: String): ResponseEntity<Image> {
        this.logger.info("GET metadata of image $imageId")
        return ResponseEntity.ok(this.gymImageDAO.findById(imageId)
                .orElseThrow { ResourceNotFoundException(Image::class.java, imageId) })
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @RequestMapping("/{id}/photos/{name}", method = [RequestMethod.POST, RequestMethod.PUT])
    fun addPhoto(@PathVariable id: Int,
                 @PathVariable name: String,
                 @RequestBody image: MultipartFile): ResponseEntity<Image> {
        this.logger.info("POST photo of gym $id")
        return ResponseEntity(this.gymImageService.updateImage(this.bucketName, id, image, name, ImageType.profile), HttpStatus.CREATED)
    }


    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @RequestMapping("/{id}/avatar", method = [RequestMethod.POST, RequestMethod.PUT])
    fun setAvatar(@PathVariable id: Int,
                  @RequestBody image: MultipartFile): ResponseEntity<Image> {
        this.logger.info("POST avatar of gym $id")
        return ResponseEntity(this.gymImageService.setAvatar(this.bucketName, id, image), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/photos/{image}")
    fun deletePhoto(@PathVariable image: String): ResponseEntity<Void> {
        this.logger.info("DELETE photo $image")
        this.gymImageService.deleteImage(this.bucketName, image)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}