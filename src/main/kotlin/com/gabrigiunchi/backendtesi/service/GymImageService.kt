package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.ImageMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GymImageService(
        private val gymDAO: GymDAO,
        objectStorageService: ObjectStorageService,

        @Value("\${application.objectstorage.gymphotosbucket}")
        bucketName: String) : ImageService(objectStorageService, bucketName) {

    // TODO: fix
    fun getPhotosOfGym(gymId: Int): List<ImageMetadata> {
        return this.gymDAO.findById(gymId)
                .map { emptyList<ImageMetadata>() }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }
}