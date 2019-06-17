package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.model.entities.ImageMetadata
import com.gabrigiunchi.backendtesi.service.AvatarService
import org.slf4j.LoggerFactory
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
        private val avatarService: AvatarService) : BaseController(userDAO) {

    private val logger = LoggerFactory.getLogger(AvatarController::class.java)

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping("/page/{page}/size/{size}")
    fun getAllAvatarsMetadata(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<ImageMetadata>> {
        this.logger.info("GET all avatars metadata")
        return ResponseEntity(this.avatarService.getAllMetadata(page, size), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getAvatar(@PathVariable id: String): ResponseEntity<ByteArray> {
        this.logger.info("GET avatar #$id")
        return ResponseEntity(this.avatarService.download(id), HttpStatus.OK)
    }

    @GetMapping("/of_user/{id}")
    fun getAvatarOfuser(@PathVariable id: Int): ResponseEntity<ByteArray> {
        this.logger.info("GET avatar of user #$id")
        return ResponseEntity(this.avatarService.getAvatar(id), HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @RequestMapping("/{id}", method = [RequestMethod.POST, RequestMethod.PUT])
    fun setAvatar(@PathVariable id: Int, @RequestBody avatar: MultipartFile): ResponseEntity<ImageMetadata> {
        this.logger.info("PUT avatar #$id")
        return ResponseEntity(this.avatarService.setAvatar(id, avatar), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteAvatar(@PathVariable id: String): ResponseEntity<Void> {
        this.logger.info("DELETE avatar $id")
        this.avatarService.deleteImage(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /***************************** PRESETS ****************************************************/

    @GetMapping("/presets")
    fun getAllPresetsAvatar(): ResponseEntity<Collection<ImageMetadata>> {
        this.logger.info("GET all presets avatar")
        return ResponseEntity(this.avatarService.presetAvatars, HttpStatus.OK)
    }

    /***************************** MY AVATAR **************************************************/

    @GetMapping("/me")
    fun getMyAvatar(): ResponseEntity<ByteArray> {
        val user = this.getLoggedUser()
        this.logger.info("GET avatar for logged user (#${user.id})")
        return ResponseEntity(this.avatarService.getAvatar(user.id), HttpStatus.OK)
    }

    @GetMapping("/me/metadata")
    fun getMyAvatarMetadata(): ResponseEntity<ImageMetadata> {
        val user = this.getLoggedUser()
        this.logger.info("GET avatar metadata for logged user (#${user.id})")
        return ResponseEntity(this.avatarService.getAvatarMetadata(user.id), HttpStatus.OK)
    }

    @RequestMapping("/me", method = [RequestMethod.POST, RequestMethod.PUT])
    fun changeMyAvatar(@RequestBody avatar: MultipartFile): ResponseEntity<ImageMetadata> {
        val user = this.getLoggedUser()
        this.logger.info("PUT avatar for logged user (#${user.id})")
        return ResponseEntity(this.avatarService.setAvatar(user.id, avatar), HttpStatus.CREATED)
    }

    @DeleteMapping("/me")
    fun deleteMyAvatar(): ResponseEntity<Void> {
        val user = this.getLoggedUser()
        this.logger.info("DELETE avatar for logged user (#${user.id})")
        this.avatarService.deleteAvatar(user.id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}