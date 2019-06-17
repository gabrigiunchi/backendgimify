package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.DrawableDAO
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GymImageService(
        imageDAO: ImageDAO,
        drawableDAO: DrawableDAO,
        objectStorageService: ObjectStorageService,

        @Value("\${application.objectstorage.gymphotosbucket}")
        bucketName: String) : ImageService(drawableDAO, imageDAO, objectStorageService, bucketName) {
}