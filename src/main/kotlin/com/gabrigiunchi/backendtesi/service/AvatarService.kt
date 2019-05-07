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
                    val userAvatarId = this.imageNameOfUser(userId)
                    val avatar = if (this.contains(userAvatarId)) userAvatarId else DEFAULT_AVATAR_METADATA.id
                    this.getImageMetadata(avatar)
                }
                .orElseThrow { ResourceNotFoundException("user $userId does not exist") }
    }

    fun setAvatarOfUser(userId: Int, image: MultipartFile): ImageMetadata {
        return this.userDAO.findById(userId)
                .map { this.upload(image, this.imageNameOfUser(userId)) }
                .orElseThrow { ResourceNotFoundException("user $userId does not exist") }
    }

    fun deleteAvatarOfUser(userId: Int) {
        return this.userDAO.findById(userId)
                .map { super.deleteImage(this.imageNameOfUser(userId)) }
                .orElseThrow { ResourceNotFoundException("user $userId does not exist") }
    }

    fun getAvatarOfUser(userId: Int): ByteArray {
        return this.download(this.getAvatarMetadataOfUser(userId).id)
    }

    fun setDefaultAvatar(image: MultipartFile) = super.upload(image, DEFAULT_AVATAR_METADATA.id)

    val defaultAvatar: ByteArray
        get() = super.download(DEFAULT_AVATAR_METADATA.id)

    private fun imageNameOfUser(userId: Int): String = "user$userId"
}