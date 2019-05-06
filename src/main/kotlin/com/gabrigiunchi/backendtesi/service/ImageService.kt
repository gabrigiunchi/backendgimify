package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.ImageMetadata
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import org.springframework.web.multipart.MultipartFile

open class ImageService(
        private val objectStorageService: ObjectStorageService,
        private val bucketName: String
) {

    fun contains(image: String): Boolean {
        return this.objectStorageService.createClient().doesObjectExist(this.bucketName, image)
    }

    fun getAllMetadata(): List<ImageMetadata> {
        return this.objectStorageService.createClient()
                .listObjects(this.bucketName)
                .objectSummaries
                .map { summary -> ImageMetadata(summary.key, summary.lastModified.time) }
    }

    fun getAllMetadataWithPrefix(prefix: String): List<ImageMetadata> {
        val a = this.objectStorageService.createClient().listObjects(this.bucketName, prefix)
        return this.objectStorageService.createClient()
                .listObjects(this.bucketName, prefix)
                .objectSummaries
                .map { summary -> ImageMetadata(summary.key, summary.lastModified.time) }
    }

    fun getImageMetadata(id: String): ImageMetadata {
        val client = this.objectStorageService.createClient()

        if (!client.doesObjectExist(this.bucketName, id)) {
            throw ResourceNotFoundException("image $id does not exist")
        }

        val image = client.getObject(this.bucketName, id)
        return ImageMetadata(image.key, image.objectMetadata.lastModified.time)
    }

    fun download(id: String): ByteArray {
        val client = this.objectStorageService.createClient()

        if (!client.doesObjectExist(this.bucketName, id)) {
            throw ResourceNotFoundException("image $id does not exist")
        }

        return client.getObject(this.bucketName, id)
                .objectContent
                .readAllBytes()
    }

    fun upload(image: MultipartFile, name: String): ImageMetadata {
        val client = this.objectStorageService.createClient()
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size
        val putResult = client.putObject(this.bucketName, name, image.inputStream, metadata)
        return ImageMetadata(name, putResult.metadata.lastModified.time)
    }

    open fun deleteImage(id: String) {
        val client = this.objectStorageService.createClient()

        if (!client.doesObjectExist(this.bucketName, id)) {
            throw ResourceNotFoundException("image $id does not exist")
        }

        client.deleteObject(this.bucketName, id)
    }
}