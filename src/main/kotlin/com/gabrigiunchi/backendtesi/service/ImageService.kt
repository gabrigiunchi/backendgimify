package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.ImageMetadata
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import org.springframework.web.multipart.MultipartFile
import java.util.*

open class ImageService(
        private val objectStorageService: ObjectStorageService,
        private val bucketName: String) {

    fun contains(image: String): Boolean = this.objectStorageService.createClient().doesObjectExist(this.bucketName, image)

    fun getAllMetadata(): List<ImageMetadata> =
            this.objectStorageService.createClient()
                    .listObjectsV2(this.bucketName)
                    .objectSummaries
                    .map { summary -> ImageMetadata(summary.key, summary.lastModified.time) }

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
        val client = this.objectStorageService.createClient()
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size
        client.putObject(this.bucketName, id, image.inputStream, metadata).metadata
        return ImageMetadata(id, Date().time)
    }

    open fun delete(id: String) {
        val client = this.objectStorageService.createClient()

        if (!client.doesObjectExist(this.bucketName, id)) {
            throw ResourceNotFoundException("image $id does not exist")
        }

        client.deleteObject(this.bucketName, id)
    }

    protected fun randomName(): String = UUID.randomUUID().toString()

}