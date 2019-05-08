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

    fun getAvatarMetadataOfUser(userId: Int): ImageMetadata {
        return this.userDAO.findById(userId)
                .map {
                    val userAvatarId = this.userAvatarId(userId)
                    if (this.contains(userAvatarId)) this.getImageMetadata(userAvatarId)
                    else DEFAULT_AVATAR_METADATA
                }
                .orElseThrow { ResourceNotFoundException("user $userId does not exist") }
    }

    fun setAvatarOfUser(userId: Int, image: MultipartFile): ImageMetadata {
        return this.userDAO.findById(userId)
                .map { this.upload(image, this.userAvatarId(userId)) }
                .orElseThrow { ResourceNotFoundException("user $userId does not exist") }
    }

    fun deleteAvatarOfUser(userId: Int) {
        return this.userDAO.findById(userId)
                .map { this.deleteImage(this.userAvatarId(userId)) }
                .orElseThrow { ResourceNotFoundException("user $userId does not exist") }
    }

    fun getAvatarOfUser(userId: Int): ByteArray {
        return this.userDAO.findById(userId)
                .map { this.download(this.getAvatarMetadataOfUser(userId).id) }
                .orElseThrow { ResourceNotFoundException("user $userId does not exist") }
    }

    fun setDefaultAvatar(image: MultipartFile) = this.upload(image, DEFAULT_AVATAR_METADATA.id)

    val defaultAvatar: ByteArray
        get() = super.download(DEFAULT_AVATAR_METADATA.id)

    private fun userAvatarId(userId: Int): String = "user$userId"
}