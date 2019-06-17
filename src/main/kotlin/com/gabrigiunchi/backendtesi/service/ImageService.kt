package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.DrawableDAO
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.entities.ImageMetadata
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
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

    /*************************** AVATARS **************************************************/

    fun getAvatarMetadata(entityId: Int): ImageMetadata {
        val images = this.imageDAO.findByDrawableAndBucket(this.getEntity(entityId), this.bucketName)
                .filter { it.type == ImageType.avatar }

        return if (images.isEmpty()) DEFAULT_AVATAR_METADATA else ImageMetadata(images.first().id, images.first().lastModified)
    }

    fun getAvatar(entityId: Int): ByteArray = this.download(this.getAvatarMetadata(entityId).id)

    fun setAvatar(entityId: Int, image: MultipartFile): ImageMetadata {
        val entity = this.getEntity(entityId)
        val images = this.imageDAO.findByDrawableAndTypeAndBucket(entity, ImageType.avatar, this.bucketName)
        val avatarId = if (images.isEmpty()) this.randomName() else images.first().id
        val metadata = this.upload(image, avatarId)
        this.imageDAO.save(Image(metadata.id, ImageType.avatar, entity, metadata.lastModified, this.bucketName))
        return metadata
    }

    fun deleteAvatar(entityId: Int) {
        val entity = this.getEntity(entityId)
        val images = this.imageDAO.findByDrawableAndBucket(entity, this.bucketName).filter { it.type == ImageType.avatar }

        if (images.isNotEmpty()) {
            this.deleteImage(images.first().id)
        }
    }

    /**************************** IMAGES *******************************************/

    fun getImagesOfEntity(entityId: Int): List<ImageMetadata> =
            this.drawableDAO.findById(entityId)
                    .map {
                        this.imageDAO.findByDrawableAndBucket(it, this.bucketName).map { image ->
                            ImageMetadata(image.id, image.lastModified)
                        }
                    }
                    .orElseThrow { ResourceNotFoundException("entity $entityId does not exist") }


    fun addImage(entityId: Int, image: MultipartFile): ImageMetadata {
        val entity = this.getEntity(entityId)
        val imageId = this.randomName()
        val metadata = this.upload(image, imageId)
        this.imageDAO.save(Image(metadata.id, ImageType.profile, entity, metadata.lastModified, this.bucketName))
        return metadata
    }

    fun updateImage(entityId: Int, image: MultipartFile, imageId: String, type: ImageType): ImageMetadata {
        return this.drawableDAO.findById(entityId)
                .map {
                    this.imageDAO.findById(imageId)
                            .ifPresent { i ->
                                if (i.drawable.id == entityId) {
                                    this.imageDAO.delete(i)
                                } else {
                                    throw ResourceAlreadyExistsException("image ${image.name} already exists for another entity")
                                }
                            }

                    val metadata = this.upload(image, imageId)
                    this.imageDAO.save(Image(imageId, type, it, this.bucketName))
                    metadata
                }
                .orElseThrow { ResourceNotFoundException("entity $entityId does not exist") }
    }

    fun deleteImage(imageId: String) {
        this.imageDAO.findById(imageId)
                .map {
                    this.delete(it.id)
                    this.imageDAO.delete(it)
                }.orElseThrow { ResourceNotFoundException("image $imageId does not exist") }
    }

    /******************************** UTILS *******************************************/

    fun contains(image: String): Boolean = this.objectStorageService.createClient().doesObjectExist(this.bucketName, image)

    fun getAllMetadata(page: Int, size: Int): Page<ImageMetadata> =
            this.imageDAO.findByBucket(this.bucketName, PageRequest.of(page, size)).map { ImageMetadata(it.id, it.lastModified) }

    fun getAllMetadataWithPrefix(prefix: String): List<ImageMetadata> =
            this.objectStorageService.createClient()
                    .listObjectsV2(this.bucketName, prefix)
                    .objectSummaries
                    .map { summary -> ImageMetadata(summary.key, summary.lastModified.time) }

    fun download(id: String): ByteArray {
        val client = this.objectStorageService.createClient()

        if (!client.doesObjectExist(this.bucketName, id)) {
            throw ResourceNotFoundException("image $id does not exist")
        }

        return client.getObject(this.bucketName, id)
                .objectContent
                .readAllBytes()
    }

    fun upload(image: MultipartFile, id: String): ImageMetadata {
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size
        this.objectStorageService.createClient().putObject(this.bucketName, id, image.inputStream, metadata).metadata
        return ImageMetadata(id, Date().time)
    }

    fun delete(id: String) {
        val client = this.objectStorageService.createClient()

        if (!client.doesObjectExist(this.bucketName, id)) {
            throw ResourceNotFoundException("image $id does not exist")
        }

        client.deleteObject(this.bucketName, id)
    }

    private fun randomName(): String = UUID.randomUUID().toString()
    private fun getEntity(id: Int) = this.drawableDAO.findById(id).orElseThrow { ResourceNotFoundException("entity $id does not exist") }
}