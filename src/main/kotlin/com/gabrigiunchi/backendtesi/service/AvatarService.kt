package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.AvatarDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.Avatar
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.entities.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class AvatarService(private val userDAO: UserDAO,
                    private val avatarDAO: AvatarDAO,
                    objectStorageService: ObjectStorageService,

                    @Value("\${application.objectstorage.avatarsbucket}")
                    bucketName: String) : ImageService(objectStorageService, bucketName) {


    companion object {
        val DEFAULT_AVATAR_METADATA = Image("default", 0)
        const val PRESET_PREFIX = "preset"
    }

    val presetAvatars: List<Image>
        get() = this.getAllMetadataWithPrefix(PRESET_PREFIX)

    fun getAvatarMetadataOfUser(username: String): Image {
        val user = this.getUser(username)
        return this.avatarDAO.findByUser(user)
                .map { Image(it.id, it.lastModified) }
                .orElseGet { DEFAULT_AVATAR_METADATA }
    }

    fun getAvatarOfUser(username: String): ByteArray {
        return this.download(this.getAvatarMetadataOfUser(username).id)
    }

    fun setAvatarOfUser(username: String, image: MultipartFile): Image {
        val user = this.getUser(username)
        val avatarId = this.avatarDAO.findByUser(user).map { it.id }.orElseGet { "${randomName()}${user.id}" }
        val metadata = this.upload(image, avatarId)
        this.avatarDAO.save(Avatar(metadata.id, user, metadata.lastModified))
        return metadata
    }

    fun deleteAvatarOfUser(username: String) {
        val user = this.getUser(username)
        this.avatarDAO.findByUser(user)
                .map {
                    this.deleteImage(it.id)
                    this.avatarDAO.delete(it)
                }
    }

    fun setDefaultAvatar(image: MultipartFile) = this.upload(image, DEFAULT_AVATAR_METADATA.id)

    val defaultAvatar: ByteArray
        get() = super.download(DEFAULT_AVATAR_METADATA.id)

    private fun randomName(): String = UUID.randomUUID().toString()

    private fun getUser(username: String): User =
            this.userDAO.findByUsername(username).orElseThrow { ResourceNotFoundException("user $username does not exist") }

}