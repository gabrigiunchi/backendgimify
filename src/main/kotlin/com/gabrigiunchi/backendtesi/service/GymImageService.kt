package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.GymImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.GymImage
import com.gabrigiunchi.backendtesi.model.ImageMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class GymImageService(
        private val gymImageDAO: GymImageDAO,
        private val gymDAO: GymDAO,
        objectStorageService: ObjectStorageService,

        @Value("\${application.objectstorage.gymphotosbucket}")
        bucketName: String) : ImageService(objectStorageService, bucketName) {

    fun getPhotosOfGym(gymId: Int): List<ImageMetadata> {
        return this.gymDAO.findById(gymId)
                .map {
                    this.gymImageDAO.findByGym(it).map { image ->
                        ImageMetadata(image.id, image.lastModified)
                    }
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    fun setImage(gymId: Int, image: MultipartFile, imageId: String): ImageMetadata {
        return this.gymDAO.findById(gymId)
                .map {
                    this.gymImageDAO.findById(imageId)
                            .ifPresent { i ->
                                if (i.gym.id == gymId) {
                                    this.gymImageDAO.delete(i)
                                } else {
                                    throw ResourceAlreadyExistsException("image ${image.name} already exists for another gym")
                                }
                            }

                    val metadata = this.upload(image, imageId)
                    this.gymImageDAO.save(GymImage(imageId, it))
                    metadata
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    override fun deleteImage(id: String) {
        this.gymImageDAO.findById(id)
                .map {
                    super.deleteImage(id)
                    this.gymImageDAO.delete(it)
                }
                .orElseThrow { ResourceNotFoundException("image $id does not exist") }
    }
}