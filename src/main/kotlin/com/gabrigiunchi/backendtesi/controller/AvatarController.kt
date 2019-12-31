package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.UserDAO
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
@RequestMapping("/api/v1/avatars")
class AvatarController(
        userDAO: UserDAO,
        @Value("\${application.objectstorage.avatarsbucket}")
        private val bucketName: String,
        private val avatarService: ImageService) : BaseController(userDAO) {

    private val logger = LoggerFactory.getLogger(AvatarController::class.java)

    companion object {
        const val PRESET_PREFIX = "preset"
    }


    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping("/page/{page}/size/{size}")
    fun getAllAvatarsMetadata(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<Image>> {
        this.logger.info("GET all avatars metadata")
        return ResponseEntity.ok(this.avatarService.getAllMetadata(this.bucketName, page, size))
    }

    @GetMapping("/{id}")
    fun getAvatar(@PathVariable id: String): ResponseEntity<ByteArray> {
        this.logger.info("GET avatar #$id")
        return ResponseEntity.ok(this.avatarService.download(this.bucketName, id))
    }

    @GetMapping("/{id}/metadata")
    fun getMetadataOfAvatar(@PathVariable id: String): ResponseEntity<Image> {
        this.logger.info("GET metadata of avatar #$id")
        return ResponseEntity.ok(this.avatarService.getMetadataOfImage(this.bucketName, id))
    }

    @GetMapping("/metadata/user/{id}")
    fun getAvatarMetadataOfuser(@PathVariable id: Int): ResponseEntity<Image> {
        this.logger.info("GET avatar metadata of user #$id")
        return ResponseEntity.ok(this.avatarService.getAvatarMetadata(this.bucketName, id))
    }

    @GetMapping("/user/{id}")
    fun getAvatarOfuser(@PathVariable id: Int): ResponseEntity<ByteArray> {
        this.logger.info("GET avatar of user #$id")
        return ResponseEntity.ok(this.avatarService.getAvatar(this.bucketName, id))
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @RequestMapping("/{id}", method = [RequestMethod.POST, RequestMethod.PUT])
    fun setAvatar(@PathVariable id: Int, @RequestBody avatar: MultipartFile): ResponseEntity<Image> {
        this.logger.info("PUT avatar #$id")
        return ResponseEntity(this.avatarService.setAvatar(this.bucketName, id, avatar), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteAvatar(@PathVariable id: String): ResponseEntity<Void> {
        this.logger.info("DELETE avatar $id")
        this.avatarService.deleteImage(this.bucketName, id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /***************************** PRESETS ****************************************************/

    @GetMapping("/presets")
    fun getAllPresetsAvatar(): ResponseEntity<Collection<Image>> {
        this.logger.info("GET all presets avatar")
        return ResponseEntity.ok(this.avatarService.getAllMetadataWithPrefix(this.bucketName, PRESET_PREFIX))
    }

    /***************************** MY AVATAR **************************************************/

    @GetMapping("/me")
    fun getMyAvatar(): ResponseEntity<ByteArray> {
        val user = this.getLoggedUser()
        this.logger.info("GET avatar for logged user (#${user.id})")
        return ResponseEntity.ok(this.avatarService.getAvatar(this.bucketName, user.id))
    }

    @GetMapping("/me/metadata")
    fun getMyAvatarMetadata(): ResponseEntity<Image> {
        val user = this.getLoggedUser()
        this.logger.info("GET avatar metadata for logged user (#${user.id})")
        return ResponseEntity.ok(this.avatarService.getAvatarMetadata(this.bucketName, user.id))
    }

    @RequestMapping("/me", method = [RequestMethod.POST, RequestMethod.PUT])
    fun changeMyAvatar(@RequestBody avatar: MultipartFile): ResponseEntity<Image> {
        val user = this.getLoggedUser()
        this.logger.info("PUT avatar for logged user (#${user.id})")
        return ResponseEntity(this.avatarService.setAvatar(this.bucketName, user.id, avatar), HttpStatus.CREATED)
    }


    @RequestMapping("/me/use/{imageId}", method = [RequestMethod.POST, RequestMethod.PUT])
    fun changeMyAvatarWithADefaultOne(@PathVariable imageId: String): ResponseEntity<Image> {
        val user = this.getLoggedUser()
        this.logger.info("PUT avatar for logged user (#${user.id}), use image $imageId")
        return ResponseEntity(
                this.avatarService.associateExistingImageToEntity(this.bucketName, user.id, ImageType.avatar, imageId),
                HttpStatus.CREATED)
    }

    @DeleteMapping("/me")
    fun deleteMyAvatar(): ResponseEntity<Void> {
        val user = this.getLoggedUser()
        this.logger.info("DELETE avatar for logged user (#${user.id})")
        this.avatarService.deleteAvatar(this.bucketName, user.id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}