package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockObjectStorage
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.ibm.cloud.objectstorage.services.s3.AmazonS3
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile
import java.util.*

class ObjectStorageServiceTest : AbstractControllerTest() {

    private val bucketName = "bucket"

    @Autowired
    private lateinit var objectStorageService: ObjectStorageService
    private lateinit var amazonS3: AmazonS3
    private val mockObjectStorage = MockObjectStorage()

    @Before
    fun init() {
        this.mockObjectStorage.clear()
        this.amazonS3 = Mockito.mock(AmazonS3::class.java)
        this.objectStorageService = Mockito.spy(this.objectStorageService)
        Mockito.`when`(this.objectStorageService.createClient()).thenReturn(this.amazonS3)
    }

    @Test
    fun `Should upload an image`() {
        val name = "kjdnajs.dasda"
        this.createMockImage(name, "dnansda")
        Assertions.assertThat(this.objectStorageService.createClient().doesObjectExist(this.bucketName, name))
                .isTrue()
    }

    @Test
    fun `Should download an object`() {
        val name = "njdajsnd.aaa"
        val content = "ndjansa"
        this.createMockImage(name, content)
        val result = this.objectStorageService.download(name, this.bucketName)
        Assertions.assertThat(result).isEqualTo(content.toByteArray())
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when downloading an object if it does not exist`() {
        val name = "nonexistingimage.fake"
        Mockito.`when`(this.amazonS3.doesObjectExist(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.contains(name))
        this.objectStorageService.download(name, this.bucketName)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when deleting an image that does not exist`() {
        val name = "milano1.jpg"
        Mockito.`when`(this.amazonS3.doesObjectExist(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.contains(name))
        this.objectStorageService.delete(name, this.bucketName)
    }

    @Test
    fun `Should delete an object`() {
        val name = "njdajsnd.aaa"
        val content = "ndjansa"
        this.createMockImage(name, content)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isTrue()
        this.objectStorageService.delete(name, this.bucketName)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isFalse()
    }

    private fun createMockImage(name: String, content: String): MockMultipartFile {
        val image = MockMultipartFile(name, content.toByteArray())
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size
        metadata.lastModified = Date()

        val putObjectResult = this.mockObjectStorage.add(image, name)

        Mockito.`when`(this.amazonS3.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(putObjectResult)

        Mockito.`when`(this.amazonS3.getObjectMetadata(this.bucketName, name)).thenReturn(metadata)

        Mockito.`when`(this.amazonS3.doesObjectExist(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.contains(name))

        Mockito.`when`(this.amazonS3.getObject(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.getImage(name))

        Mockito.`when`(this.amazonS3.deleteObject(this.bucketName, name))
                .then { this.mockObjectStorage.delete(name) }

        this.objectStorageService.upload(image, name, this.bucketName)
        return image
    }
}