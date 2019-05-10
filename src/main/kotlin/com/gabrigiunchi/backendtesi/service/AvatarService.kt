package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.ImageMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AvatarService(private val userDAO: UserDAO,
                    objectStorageService: ObjectStorageService,

                    @Value("\${application.objectstorage.avatarsbucket}")
                    bucketName: String) : ImageService(objectStorageService, bucketName) {


    companion object {
        val DEFAULT_AVATAR_METADATA = ImageMetadata("default", 0)
        const val PRESET_PREFIX = "preset"
    }

    val presetAvatars: List<ImageMetadata>
        get() = this.getAllMetadataWithPrefix(PRESET_PREFIX)

    fun getAvatarMetadataOfUser(username: String): ImageMetadata {
        return this.userDAO.findByUsername(username)
                .map {
                    if (this.contains(username)) this.getImageMetadata(username)
                    else DEFAULT_AVATAR_METADATA
                }
                .orElseThrow { ResourceNotFoundException("user $username does not exist") }
    }

    fun setAvatarOfUser(username: String, image: MultipartFile): ImageMetadata {
        return this.userDAO.findByUsername(username)
                .map { this.upload(image, username) }
                .orElseThrow { ResourceNotFoundException("user $username does not exist") }
    }

    fun deleteAvatarOfUser(username: String) {
        return this.userDAO.findByUsername(username)
                .map { this.deleteImage(username) }
                .orElseThrow { ResourceNotFoundException("user $username does not exist") }
    }

    fun getAvatarOfUser(username: String): ByteArray {
        return this.userDAO.findByUsername(username)
                .map { this.download(this.getAvatarMetadataOfUser(username).id) }
                .orElseThrow { ResourceNotFoundException("user $username does not exist") }
    }

    fun setDefaultAvatar(image: MultipartFile) = this.upload(image, DEFAULT_AVATAR_METADATA.id)

    val defaultAvatar: ByteArray
        get() = super.download(DEFAULT_AVATAR_METADATA.id)
}