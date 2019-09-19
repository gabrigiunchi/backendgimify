package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.DrawableDAO
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.entities.ImageMetadata
import com.gabrigiunchi.backendtesi.model.type.ImageType
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.web.multipart.MultipartFile
import java.util.*

open class ImageService(
        private val drawableDAO: DrawableDAO,
        private val imageDAO: ImageDAO,
        private val objectStorageService: ObjectStorageService,
        private val bucketName: String) {

    companion object {
        val DEFAULT_AVATAR_METADATA = ImageMetadata("default", 0)
    }

    private val logger = LoggerFactory.getLogger(ImageService::class.java)

    /******************************* AVATARS *********************************************/

    fun getAvatarMetadata(entityId: Int): ImageMetadata {
        this.logger.info("Get metadata of avatar $entityId in bucket $bucketName")
        val images = this.imageDAO.findByDrawableAndBucket(this.getEntity(entityId), this.bucketName)
                .filter { it.type == ImageType.avatar }

        if (images.isEmpty()) {
            this.logger.info("Avatar $entityId not found in bucket $bucketName, returning default avatar")
            return DEFAULT_AVATAR_METADATA
        }
        return ImageMetadata(images.first().id, images.first().lastModified)
    }

    fun getAvatar(entityId: Int): ByteArray {
        this.logger.info("Get avatar $entityId in bucket $bucketName")
        return this.objectStorageService.download(this.getAvatarMetadata(entityId).id, this.bucketName)
    }

    fun setAvatar(entityId: Int, image: MultipartFile): ImageMetadata {
        this.logger.info("Uploading avatar of entity #$entityId in bucket $bucketName")
        val entity = this.getEntity(entityId)
        val images = this.imageDAO.findByDrawableAndTypeAndBucket(entity, ImageType.avatar, this.bucketName)
        val avatarId = if (images.isEmpty()) this.randomName() else images.first().id
        this.logger.info("Avatar id: $avatarId")
        val metadata = this.objectStorageService.upload(image, avatarId, this.bucketName)
        this.imageDAO.save(Image(metadata.id, ImageType.avatar, entity, metadata.lastModified, this.bucketName))
        this.logger.info("Successfully uploaded avatar of entity #$entity in bucket $bucketName")
        return metadata
    }

    fun deleteAvatar(entityId: Int) {
        this.logger.info("Deleting avatar of entity $entityId in bucket $bucketName")
        val entity = this.getEntity(entityId)
        val images = this.imageDAO.findByDrawableAndBucket(entity, this.bucketName).filter { it.type == ImageType.avatar }

        if (images.isNotEmpty()) {
            this.deleteImage(images.first().id)
            this.logger.info("Successfully deleted avatar of entity $entity in bucket $bucketName")
        } else {
            this.logger.info("Avatar of entity #$entity not found in bucket $bucketName")
        }
    }

    /**************************** IMAGES *******************************************/

    fun getImagesOfEntity(entityId: Int): List<ImageMetadata> {
        this.logger.info("Get metadata of entity #$entityId in bucket $bucketName")
        return this.drawableDAO.findById(entityId)
                .map {
                    this.imageDAO.findByDrawableAndBucket(it, this.bucketName).map { image ->
                        ImageMetadata(image.id, image.lastModified)
                    }
                }
                .orElseThrow { ResourceNotFoundException("entity $entityId does not exist") }
    }


    fun addImage(entityId: Int, image: MultipartFile): ImageMetadata {
        this.logger.info("Uploading image for entity #$entityId in bucket $bucketName")
        val entity = this.getEntity(entityId)
        val imageId = this.randomName()
        val metadata = this.objectStorageService.upload(image, imageId, this.bucketName)
        this.imageDAO.save(Image(metadata.id, ImageType.profile, entity, metadata.lastModified, this.bucketName))
        this.logger.info("Successfully uploaded image for entity $entity in bucket $bucketName")
        return metadata
    }

    fun updateImage(entityId: Int, image: MultipartFile, imageId: String, type: ImageType): ImageMetadata {
        this.logger.info("Uploading image $imageId for entity $entityId in bucket $bucketName")
        return this.drawableDAO.findById(entityId)
                .map {
                    this.imageDAO.findById(imageId)
                            .ifPresent { i ->
                                if (i.drawable.id == entityId) {
                                    this.logger.info("Previous image found, deleting image $i")
                                    this.imageDAO.delete(i)
                                } else {
                                    this.logger.info("image ${image.name} already exists for another entity")
                                    throw ResourceAlreadyExistsException("image ${image.name} already exists for another entity")
                                }
                            }

                    val metadata = this.objectStorageService.upload(image, imageId, this.bucketName)
                    this.imageDAO.save(Image(imageId, type, it, this.bucketName))
                    this.logger.info("Successfully updated image $imageId for entity #$entityId in bucket $bucketName")
                    metadata
                }
                .orElseThrow { ResourceNotFoundException("entity $entityId does not exist") }
    }

    fun deleteImage(imageId: String) {
        this.logger.info("Deleting image $imageId in bucket $bucketName")
        this.imageDAO.findById(imageId)
                .map {
                    this.objectStorageService.delete(it.id, this.bucketName)
                    this.imageDAO.delete(it)
                    this.logger.info("Successfully deleted image $imageId in bucket $bucketName")
                }.orElseThrow { ResourceNotFoundException("image $imageId does not exist") }
    }

    /******************************** UTILS *******************************************/

    fun getAllMetadata(page: Int, size: Int): Page<ImageMetadata> =
            this.imageDAO.findByBucket(this.bucketName, PageRequest.of(page, size)).map { ImageMetadata(it.id, it.lastModified) }

    fun getAllMetadataWithPrefix(prefix: String): List<ImageMetadata> = this.objectStorageService.getAllMetadataWithPrefix(prefix, this.bucketName)

    fun download(id: String): ByteArray = this.objectStorageService.download(id, this.bucketName)

    private fun randomName(): String = UUID.randomUUID().toString()
    private fun getEntity(id: Int) = this.drawableDAO.findById(id).orElseThrow { ResourceNotFoundException("entity $id does not exist") }
}