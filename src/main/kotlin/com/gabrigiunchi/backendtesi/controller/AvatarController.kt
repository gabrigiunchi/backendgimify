package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.model.ImageMetadata
import com.gabrigiunchi.backendtesi.service.AvatarService
import org.slf4j.LoggerFactory
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
    @GetMapping
    fun getAllAvatarsMetadata(): ResponseEntity<Collection<ImageMetadata>> {
        this.logger.info("GET all avatars metadata")
        return ResponseEntity(this.avatarService.getAllMetadata(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getAvatar(@PathVariable id: String): ResponseEntity<ByteArray> {
        this.logger.info("GET avatar #$id")
        return ResponseEntity(this.avatarService.download(id), HttpStatus.OK)
    }

    @GetMapping("/of_user/{username}")
    fun getAvatarOfuser(@PathVariable username: String): ResponseEntity<ByteArray> {
        this.logger.info("GET avatar of user $username")
        return ResponseEntity(this.avatarService.getAvatarOfUser(username), HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @RequestMapping("/{id}", method = [RequestMethod.POST, RequestMethod.PUT])
    fun setAvatar(@PathVariable id: String, @RequestBody avatar: MultipartFile): ResponseEntity<ImageMetadata> {
        this.logger.info("PUT avatar #$id")
        return ResponseEntity(this.avatarService.upload(avatar, id), HttpStatus.CREATED)
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
        return ResponseEntity(this.avatarService.getAvatarOfUser(user.username), HttpStatus.OK)
    }

    @GetMapping("/me/metadata")
    fun getMyAvatarMetadata(): ResponseEntity<ImageMetadata> {
        val user = this.getLoggedUser()
        this.logger.info("GET avatar metadata for logged user (#${user.id})")
        return ResponseEntity(this.avatarService.getAvatarMetadataOfUser(user.username), HttpStatus.OK)
    }

    @RequestMapping("/me", method = [RequestMethod.POST, RequestMethod.PUT])
    fun changeMyAvatar(@RequestBody avatar: MultipartFile): ResponseEntity<ImageMetadata> {
        val user = this.getLoggedUser()
        this.logger.info("PUT avatar for logged user (#${user.id})")
        return ResponseEntity(this.avatarService.setAvatarOfUser(user.username, avatar), HttpStatus.CREATED)
    }

    @DeleteMapping("/me")
    fun deleteMyAvatar(): ResponseEntity<Void> {
        val user = this.getLoggedUser()
        this.logger.info("DELETE avatar for logged user (#${user.id})")
        this.avatarService.deleteAvatarOfUser(user.username)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}