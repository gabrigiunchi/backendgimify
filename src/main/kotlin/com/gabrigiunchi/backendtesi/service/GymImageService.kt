package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.GymImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.GymImage
import com.gabrigiunchi.backendtesi.model.entities.Image
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
        val DEFAULT_GYM_AVATAR = Image("gymdefault.jpg", 0)
    }

    fun getPhotosOfGym(gymId: Int): List<Image> {
        return this.gymDAO.findById(gymId)
                .map {
                    this.gymImageDAO.findByGym(it).map { image ->
                        Image(image.id, image.lastModified)
                    }
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    fun getAvatarMetadataOfGym(gymId: Int): Image {
        return this.gymDAO.findById(gymId)
                .map {
                    val avatar = this.gymImageDAO.findByGymAndType(it, ImageType.avatar).map { image ->
                        Image(image.id, image.lastModified)
                    }

                    if (avatar.isNotEmpty()) avatar.first() else DEFAULT_GYM_AVATAR
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    fun getAvatarOfGym(gymId: Int): ByteArray {
        return this.download(this.getAvatarMetadataOfGym(gymId).id)
    }

    fun setAvatar(gymId: Int, image: MultipartFile, imageId: String): Image {
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

    fun setImage(gymId: Int, image: MultipartFile, imageId: String, type: ImageType): Image {
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

    override fun delete(id: String) {
        this.gymImageDAO.findById(id)
                .map {
                    super.delete(id)
                    this.gymImageDAO.delete(it)
                }
                .orElseThrow { ResourceNotFoundException("image $id does not exist") }
    }
}