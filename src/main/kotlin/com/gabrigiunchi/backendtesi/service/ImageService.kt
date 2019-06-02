package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import org.springframework.web.multipart.MultipartFile
import java.util.*

open class ImageService(
        private val objectStorageService: ObjectStorageService,
        private val bucketName: String) {

    fun contains(image: String): Boolean = this.objectStorageService.createClient().doesObjectExist(this.bucketName, image)

    fun getAllMetadata(): List<Image> =
            this.objectStorageService.createClient()
                    .listObjectsV2(this.bucketName)
                    .objectSummaries
                    .map { summary -> Image(summary.key, summary.lastModified.time) }

    fun getAllMetadataWithPrefix(prefix: String): List<Image> =
            this.objectStorageService.createClient()
                    .listObjectsV2(this.bucketName, prefix)
                    .objectSummaries
                    .map { summary -> Image(summary.key, summary.lastModified.time) }


    fun download(id: String): ByteArray {
        val client = this.objectStorageService.createClient()

        if (!client.doesObjectExist(this.bucketName, id)) {
            throw ResourceNotFoundException("image $id does not exist")
        }

        return client.getObject(this.bucketName, id)
                .objectContent
                .readAllBytes()
    }

    fun upload(image: MultipartFile, id: String): Image {
        val client = this.objectStorageService.createClient()
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size
        client.putObject(this.bucketName, id, image.inputStream, metadata).metadata
        return Image(id, Date().time)
    }

    open fun delete(id: String) {
        val client = this.objectStorageService.createClient()

        if (!client.doesObjectExist(this.bucketName, id)) {
            throw ResourceNotFoundException("image $id does not exist")
        }

        client.deleteObject(this.bucketName, id)
    }
}