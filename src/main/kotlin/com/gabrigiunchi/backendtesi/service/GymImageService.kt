package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.DrawableDAO
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.model.entities.ImageMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GymImageService(
        imageDAO: ImageDAO,
        drawableDAO: DrawableDAO,
        objectStorageService: ObjectStorageService,

        @Value("\${application.objectstorage.gymphotosbucket}")
        bucketName: String) : ImageService(drawableDAO, imageDAO, objectStorageService, bucketName) {


    companion object {
        val DEFAULT_GYM_AVATAR = ImageMetadata("gymdefault.jpg", 0)
    }
}