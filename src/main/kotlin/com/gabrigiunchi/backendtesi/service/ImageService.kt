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
        val DEFAULT_AVATAR_METADATA = ImageMetadata("default", ImageType.unknown, "", 0)
    }

    private val logger = LoggerFactory.getLogger(ImageService::class.java)

    /******************************* AVATARS *********************************************/

    fun getAvatarMetadata(bucket: String, entityId: Int): ImageMetadata {
        this.logger.info("Get metadata of avatar $entityId in bucket $bucket")
        val images = this.imageDAO.findByDrawableAndBucket(this.getEntity(entityId), bucket)
                .filter { it.type == ImageType.avatar }

        if (images.isEmpty()) {
            this.logger.info("Avatar $entityId not found in bucket $bucket, returning default avatar")
            return DEFAULT_AVATAR_METADATA
        }
        return ImageMetadata(images.first())
    }

    fun getAvatar(bucket: String, entityId: Int): ByteArray {
        this.logger.info("Get avatar $entityId in bucket $bucket")
        return this.objectStorageService.download(this.getAvatarMetadata(bucket, entityId).id, bucket)
    }

    fun setAvatar(bucket: String, entityId: Int, image: MultipartFile): ImageMetadata {
        this.logger.info("Uploading avatar of entity #$entityId in bucket $bucket")
        val entity = this.getEntity(entityId)
        val images = this.imageDAO.findByDrawableAndTypeAndBucket(entity, ImageType.avatar, bucket)
        val avatarId = if (images.isEmpty()) this.randomName() else images.first().id
        this.logger.info("Avatar id: $avatarId")
        val metadata = this.objectStorageService.upload(image, avatarId, bucket)
        this.imageDAO.save(Image(metadata.id, ImageType.avatar, entity, metadata.lastModified, bucket))
        this.logger.info("Successfully uploaded avatar of entity #$entity in bucket $bucket")
        return metadata
    }

    fun associateExistingImageToEntity(bucket: String, entityId: Int, type: ImageType, imageId: String): ImageMetadata {
        this.logger.info("Associate image $imageId to entity $entityId as type $type")
        val result = this.imageDAO.save(
                Image(
                        this.imageDAO.findByIdAndBucket(imageId, bucket)
                                .orElseThrow { ResourceNotFoundException(Image::class.java, imageId) },
                        type,
                        this.getEntity(entityId)
                )
        )
        this.logger.info("Successfully associated image $imageId to entity $entityId as type $type")
        return ImageMetadata(result)
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

    fun getImagesOfEntity(bucket: String, entityId: Int): List<ImageMetadata> {
        this.logger.info("Get metadata of entity #$entityId in bucket $bucket")
        return this.drawableDAO.findById(entityId)
                .map {
                    this.imageDAO.findByDrawableAndBucket(it, bucket)
                            .map { image -> ImageMetadata(image) }
                }
                .orElseThrow { ResourceNotFoundException("entity $entityId does not exist") }
    }


    fun addImage(bucket: String, entityId: Int, image: MultipartFile): ImageMetadata {
        this.logger.info("Uploading image for entity #$entityId in bucket $bucket")
        val entity = this.getEntity(entityId)
        val imageId = this.randomName()
        val metadata = this.objectStorageService.upload(image, imageId, bucket)
        this.imageDAO.save(Image(metadata.id, ImageType.profile, entity, metadata.lastModified, bucket))
        this.logger.info("Successfully uploaded image for entity $entity in bucket $bucket")
        return metadata
    }

    fun updateImage(bucket: String, entityId: Int, image: MultipartFile, imageId: String, type: ImageType): ImageMetadata {
        this.logger.info("Uploading image $imageId for entity $entityId in bucket $bucket")
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

                    val metadata = this.objectStorageService.upload(image, imageId, bucket)
                    this.imageDAO.save(Image(imageId, type, it, bucket))
                    this.logger.info("Successfully updated image $imageId for entity #$entityId in bucket $bucket")
                    metadata
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
                }.orElseThrow { ResourceNotFoundException("image $imageId does not exist") }
    }

    /******************************** UTILS *******************************************/

    fun getAllMetadata(bucket: String, page: Int, size: Int): Page<ImageMetadata> =
            this.imageDAO.findByBucket(bucket, PageRequest.of(page, size, Sort.by("id")))
                    .map { ImageMetadata(it) }

    fun getAllMetadataWithPrefix(bucket: String, prefix: String): List<ImageMetadata> = this.objectStorageService.getAllMetadataWithPrefix(prefix, bucket)

    fun download(bucket: String, id: String): ByteArray = this.objectStorageService.download(id, bucket)

    private fun randomName(): String = UUID.randomUUID().toString()
    private fun getEntity(id: Int) = this.drawableDAO.findById(id).orElseThrow { ResourceNotFoundException("entity $id does not exist") }
}