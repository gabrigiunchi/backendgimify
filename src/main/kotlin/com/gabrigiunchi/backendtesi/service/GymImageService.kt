package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.GymImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.GymImage
import com.gabrigiunchi.backendtesi.model.ImageMetadata
import com.gabrigiunchi.backendtesi.model.type.ImageType
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


    companion object {
        private val DEFAULT_GYM_AVATAR = ImageMetadata("gymdefault.jpg", 0)
    }

    fun getPhotosOfGym(gymId: Int): List<ImageMetadata> {
        return this.gymDAO.findById(gymId)
                .map {
                    this.gymImageDAO.findByGym(it).map { image ->
                        ImageMetadata(image.id, image.lastModified)
                    }
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    fun getAvatarOfGym(gymId: Int): ByteArray {
        return this.gymDAO.findById(gymId)
                .map {
                    val avatar = this.gymImageDAO.findByGymAndType(it, ImageType.avatar).map { image ->
                        ImageMetadata(image.id, image.lastModified)
                    }

                    val metadata = if (avatar.isNotEmpty()) avatar.first() else DEFAULT_GYM_AVATAR
                    this.download(metadata.id)
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    fun setAvatar(gymId: Int, image: MultipartFile, imageId: String): ImageMetadata {
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
                    this.gymImageDAO.findByGymAndType(it, ImageType.avatar)
                            .forEach { avatar -> this.gymImageDAO.delete(avatar) }
                    this.gymImageDAO.save(GymImage(imageId, ImageType.avatar, it))
                    metadata
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    fun setImage(gymId: Int, image: MultipartFile, imageId: String, type: ImageType): ImageMetadata {
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
                    this.gymImageDAO.save(GymImage(imageId, type, it))
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