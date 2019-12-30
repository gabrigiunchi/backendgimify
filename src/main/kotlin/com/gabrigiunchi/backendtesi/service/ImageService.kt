package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.DrawableDAO
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.type.ImageType
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class ImageService(
        private val drawableDAO: DrawableDAO,
        private val imageDAO: ImageDAO,
        private val objectStorageService: ObjectStorageService) {

    companion object {
        val DEFAULT_AVATAR_METADATA = Image("default", ImageType.unknown, "", 0)
    }

    private val logger = LoggerFactory.getLogger(ImageService::class.java)

    /******************************* AVATARS *********************************************/

    fun getAvatarMetadata(bucket: String, entityId: Int): Image {
        this.logger.info("Get metadata of avatar $entityId in bucket $bucket")
        val images = this.imageDAO.findByDrawableAndBucket(this.getEntity(entityId), bucket)
                .filter { it.type == ImageType.avatar }

        return if (images.isEmpty()) {
            this.logger.info("Avatar $entityId not found in bucket $bucket, returning default avatar")
            DEFAULT_AVATAR_METADATA
        } else images.first()
    }

    fun getAvatar(bucket: String, entityId: Int): ByteArray {
        this.logger.info("Get avatar $entityId in bucket $bucket")
        return this.objectStorageService.download(this.getAvatarMetadata(bucket, entityId).id, bucket)
    }

    fun setAvatar(bucket: String, entityId: Int, image: MultipartFile): Image {
        this.logger.info("Uploading avatar of entity #$entityId in bucket $bucket")
        val entity = this.getEntity(entityId)
        val images = this.imageDAO.findByDrawableAndTypeAndBucket(entity, ImageType.avatar, bucket)
        val avatarId = if (images.isEmpty()) this.randomName() else images.first().id
        this.logger.info("Avatar id: $avatarId")
        val result = this.imageDAO.save(
                Image.copy(this.objectStorageService.upload(image, avatarId, bucket), entity))
        this.logger.info("Successfully uploaded avatar of entity #$entity in bucket $bucket")
        return result
    }

    fun associateExistingImageToEntity(bucket: String, entityId: Int, type: ImageType, imageId: String): Image {
        this.logger.info("Associate image $imageId to entity $entityId as type $type")
        val result = this.imageDAO.save(
                Image.copy(
                        this.imageDAO.findByIdAndBucket(imageId, bucket)
                                .orElseThrow { ResourceNotFoundException(Image::class.java, imageId) },
                        this.getEntity(entityId),
                        type
                )
        )
        this.logger.info("Successfully associated image $imageId to entity $entityId as type $type")
        return result
    }

    fun deleteAvatar(bucket: String, entityId: Int): Image {
        this.logger.info("Deleting avatar of entity $entityId in bucket $bucket")
        val entity = this.getEntity(entityId)
        val images = this.imageDAO.findByDrawableAndBucket(entity, bucket).filter { it.type == ImageType.avatar }

        return if (images.isNotEmpty()) {
            val deleted = this.deleteImage(bucket, images.first().id)
            this.logger.info("Successfully deleted avatar of entity $entity in bucket $bucket")
            deleted
        } else {
            this.logger.info("Avatar of entity #$entity not found in bucket $bucket")
            throw ResourceNotFoundException("Avatar of entity #$entity not found in bucket $bucket")
        }
    }

    /**************************** IMAGES *******************************************/

    fun getImagesOfEntity(bucket: String, entityId: Int): List<Image> {
        this.logger.info("Get metadata of entity #$entityId in bucket $bucket")
        return this.drawableDAO.findById(entityId)
                .map {
                    this.imageDAO.findByDrawableAndBucket(it, bucket)
                }
                .orElseThrow { ResourceNotFoundException("entity $entityId does not exist") }
    }


    fun addImage(bucket: String, entityId: Int, image: MultipartFile): Image {
        this.logger.info("Uploading image for entity #$entityId in bucket $bucket")
        val entity = this.getEntity(entityId)
        val imageId = this.randomName()
        val uploaded = this.objectStorageService.upload(image, imageId, bucket)
        val result = this.imageDAO.save(Image.copy(uploaded, entity))
        this.logger.info("Successfully uploaded image for entity $entity in bucket $bucket")
        return result
    }

    fun updateImage(bucket: String, entityId: Int, image: MultipartFile, imageId: String, type: ImageType): Image {
        this.logger.info("Uploading image $imageId for entity $entityId in bucket $bucket")
        return this.drawableDAO.findById(entityId)
                .map {
                    this.imageDAO.findById(imageId)
                            .ifPresent { i ->
                                if (i.drawable?.id == entityId) {
                                    this.logger.info("Previous image found, deleting image $i")
                                    this.imageDAO.delete(i)
                                } else {
                                    this.logger.info("image ${image.name} already exists for another entity")
                                    throw ResourceAlreadyExistsException("image ${image.name} already exists for another entity")
                                }
                            }

                    val uploaded = this.objectStorageService.upload(image, imageId, bucket)
                    val result = this.imageDAO.save(Image.copy(uploaded, it, type))
                    this.logger.info("Successfully updated image $imageId for entity #$entityId in bucket $bucket")
                    result
                }
                .orElseThrow { ResourceNotFoundException("entity $entityId does not exist") }
    }

    fun deleteImage(bucket: String, imageId: String): Image {
        this.logger.info("Deleting image $imageId in bucket $bucket")
        return this.imageDAO.findById(imageId)
                .map {
                    this.objectStorageService.delete(it.id, bucket)
                    this.imageDAO.delete(it)
                    this.logger.info("Successfully deleted image $imageId in bucket $bucket")
                    it
                }.orElseThrow { ResourceNotFoundException(Image::class.java, imageId) }
    }

    /******************************** UTILS *******************************************/

    fun getAllMetadata(bucket: String, page: Int, size: Int): Page<Image> =
            this.imageDAO.findByBucket(bucket, PageRequest.of(page, size, Sort.by("id")))

    fun getAllMetadataWithPrefix(bucket: String, prefix: String): List<Image> =
            this.objectStorageService.getAllMetadataWithPrefix(prefix, bucket)

    fun download(bucket: String, id: String): ByteArray = this.objectStorageService.download(id, bucket)

    private fun randomName(): String = UUID.randomUUID().toString()
    private fun getEntity(id: Int) = this.drawableDAO.findById(id).orElseThrow { ResourceNotFoundException("entity $id does not exist") }
}