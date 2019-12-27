package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.ImageMetadata
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import com.ibm.cloud.objectstorage.services.s3.model.PutObjectResult
import com.ibm.cloud.objectstorage.services.s3.model.S3Object
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectInputStream
import org.springframework.mock.web.MockMultipartFile
import java.util.*

class MockObjectStorage {
    private val objects = HashMap<String, Pair<ImageMetadata, MockMultipartFile>>()

    fun getAllMetadata(): List<ImageMetadata> {
        return this.objects.values.map(Pair<ImageMetadata, MockMultipartFile>::first).toList()
    }

    fun contains(image: String): Boolean {
        return this.objects.contains(image)
    }

    fun add(image: MockMultipartFile, name: String): PutObjectResult {
        this.objects[name] = Pair(ImageMetadata(name, ImageType.unknown, "", Date().time), image)
        val putObjectResult = PutObjectResult()
        putObjectResult.metadata = ObjectMetadata()
        putObjectResult.metadata.lastModified = Date()
        return putObjectResult
    }

    fun delete(image: String) {
        if (!this.contains(image)) {
            throw ResourceNotFoundException(image)
        }

        this.objects.remove(image)
    }

    fun getImage(name: String): S3Object {
        if (!this.contains(name)) {
            throw ResourceNotFoundException(name)
        }

        val pair = this.objects[name]!!
        val image = pair.second
        val metadata = pair.first
        val obj = S3Object()
        obj.key = metadata.id
        obj.objectContent = S3ObjectInputStream(image.inputStream, null)
        obj.objectMetadata.lastModified = Date(metadata.lastModified)
        return obj
    }

    fun clear() {
        this.objects.clear()
    }
}