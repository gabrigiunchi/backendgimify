package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.DrawableDAO
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.model.entities.ImageMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AvatarService(
        drawableDAO: DrawableDAO,
        imageDAO: ImageDAO,
        objectStorageService: ObjectStorageService,

        @Value("\${application.objectstorage.avatarsbucket}")
        bucketName: String) : ImageService(drawableDAO, imageDAO, objectStorageService, bucketName) {


    companion object {
        val DEFAULT_AVATAR_METADATA = ImageMetadata("default", 0)
        const val PRESET_PREFIX = "preset"
    }

    val presetAvatars: List<ImageMetadata>
        get() = this.getAllMetadataWithPrefix(PRESET_PREFIX)

}