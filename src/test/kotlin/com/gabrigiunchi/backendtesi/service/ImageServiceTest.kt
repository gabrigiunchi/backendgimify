package com.gabrigiunchi.backendtesi.service


import com.gabrigiunchi.backendtesi.MockObjectStorage
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.ibm.cloud.objectstorage.services.s3.AmazonS3
import com.ibm.cloud.objectstorage.services.s3.model.ObjectListing
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.mock.web.MockMultipartFile
import java.util.*

class ImageServiceTest {
    @Value("\${application.objectstorage.gymphotosbucket}")
    private var bucketName = ""

    private lateinit var objectStorageService: ObjectStorageService
    private lateinit var amazonS3: AmazonS3
    private lateinit var imageService: ImageService
    private val mockObjectStorage = MockObjectStorage()

    @Before
    fun init() {
        this.mockObjectStorage.clear()
        this.amazonS3 = Mockito.mock(AmazonS3::class.java)
        this.objectStorageService = Mockito.mock(ObjectStorageService::class.java)
        this.imageService = ImageService(this.objectStorageService, this.bucketName)
        `when`(this.objectStorageService.createClient()).thenReturn(this.amazonS3)
    }

    @Test
    fun `Should return all images metadata`() {
        val name = "name1"
        this.createMockImage(name, "jndkjanskjdnsa")
        val objectListing = ObjectListing()
        objectListing.objectSummaries.addAll(this.mockObjectStorage.getAllMetadata().map { metadata ->
            val summary = S3ObjectSummary()
            summary.key = metadata.id
            summary.lastModified = Date(metadata.lastModified)
            summary
        })

        `when`(this.amazonS3.listObjects(this.bucketName)).thenReturn(objectListing)
        val result = this.imageService.getAllMetadata()
        Assertions.assertThat(result.size).isEqualTo(1)
        Assertions.assertThat(result[0].id).isEqualTo(name)
    }


    @Test
    fun `Should upload an image`() {
        val name = "kjdnajs.dasda"
        this.createMockImage(name, "dnansda")
        Assertions.assertThat(this.objectStorageService.createClient().doesObjectExist(this.bucketName, name))
                .isTrue()
    }

    @Test
    fun `Should get the metadata of a specific image`() {
        val name = "jdnsajndksa.dnsajda"
        this.createMockImage(name, "dnansda")

        val result = this.imageService.getImageMetadata(name)
        Assertions.assertThat(result.id).isEqualTo(name)
        Assertions.assertThat(result.lastModified).isGreaterThan(0)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when requesting the metadata of an image if it does not exist`() {
        val name = "nonexistingimage.fake"
        `when`(this.amazonS3.doesObjectExist(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.contains(name))
        this.imageService.getImageMetadata(name)
    }

    @Test
    fun `Should get an image by name`() {
        val name = "njdajsnd.aaa"
        val content = "ndjansa"
        this.createMockImage(name, content)
        val result = this.imageService.download(name)
        Assertions.assertThat(result.size).isGreaterThan(0)
        Assertions.assertThat(result).isEqualTo(content.toByteArray())
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when requesting an image if it does not exist`() {
        this.imageService.download("nonexistingimage.fake")
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when deleting an image that does not exist`() {
        val name = "milano1.jpg"
        `when`(this.amazonS3.doesObjectExist(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.contains(name))
        this.imageService.deleteImage(name)
    }

    @Test
    fun `Should delete an object`() {
        val name = "njdajsnd.aaa"
        val content = "ndjansa"
        this.createMockImage(name, content)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isTrue()
        this.imageService.deleteImage(name)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isFalse()
    }

    private fun createMockImage(name: String, content: String) {
        val image = MockMultipartFile(name, content.toByteArray())
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size

        `when`(this.amazonS3.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(this.mockObjectStorage.add(image, name))

        this.imageService.upload(image, name)

        `when`(this.amazonS3.doesObjectExist(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.contains(name))

        `when`(this.amazonS3.getObject(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.getImage(name))

        `when`(this.amazonS3.deleteObject(this.bucketName, name))
                .then { this.mockObjectStorage.delete(name) }

    }
}
