package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.MockObjectStorage
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.GymImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.GymImage
import com.ibm.cloud.objectstorage.services.s3.AmazonS3
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.util.*

class

GymImageServiceTest : AbstractControllerTest() {

    @Value("\${application.objectstorage.gymphotosbucket}")
    private var bucketName = ""

    private lateinit var objectStorageService: ObjectStorageService
    private lateinit var amazonS3: AmazonS3
    private lateinit var imageService: GymImageService
    private val mockObjectStorage = MockObjectStorage()

    @Autowired
    private lateinit var gymImageDAO: GymImageDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Before
    fun init() {
        this.gymDAO.deleteAll()
        this.gymImageDAO.deleteAll()
        this.mockObjectStorage.clear()
        this.amazonS3 = Mockito.mock(AmazonS3::class.java)
        this.objectStorageService = Mockito.mock(ObjectStorageService::class.java)
        this.imageService = GymImageService(this.gymImageDAO, this.gymDAO,
                this.objectStorageService, this.bucketName)

        Mockito.`when`(this.objectStorageService.createClient()).thenReturn(this.amazonS3)
    }

    @Test
    fun `Should return the photos of a gym`() {
        val now = Date().time
        val gym = this.mockGym()
        val saved = this.gymImageDAO.saveAll(listOf(
                GymImage(gym, "photo1"),
                GymImage(gym, "photo2"),
                GymImage(gym, "photo3"),
                GymImage(gym, "photo4")
        )).toList()

        val result = this.imageService.getPhotosOfGym(gym.id)
        Assertions.assertThat(result.size).isEqualTo(4)
        Assertions.assertThat(result[0].id).isEqualTo(saved[0].name)
        Assertions.assertThat(result.all { it.lastModified >= now }).isTrue()
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when requesting the photos of a gym if the gym does not exist`() {
        this.imageService.getPhotosOfGym(-1)
    }

    @Test
    fun `Should upload an image for a gym`() {
        val now = Date().time
        val name = "photo1"
        val gym = this.mockGym()
        val image = this.createMockImage(name, "dnansda")
        this.imageService.setImage(gym.id, image, name)
        val saved = this.gymImageDAO.findByGym(gym)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isTrue()
        Assertions.assertThat(saved.size).isEqualTo(1)
        Assertions.assertThat(saved[0].name).isEqualTo(name)
        Assertions.assertThat(saved[0].gym.id).isEqualTo(gym.id)
        Assertions.assertThat(saved[0].lastModified).isGreaterThanOrEqualTo(now)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when adding the photo of a gym if the gym does not exist`() {
        this.imageService.setImage(-1, this.createMockImage("name", "content"), "photo1")
    }

    @Test
    fun `Should override the photo of a gym`() {
        val gym = this.mockGym()
        val name = "name"
        this.imageService.setImage(gym.id, this.createMockImage(name, "content"), name)
        Assertions.assertThat(this.imageService.contains(name)).isTrue()
        this.imageService.setImage(gym.id, this.createMockImage(name, "content"), name)
        Assertions.assertThat(this.imageService.contains(name)).isTrue()
    }

    @Test(expected = ResourceAlreadyExistsException::class)
    fun `Should not be possible to add two photos with the same name for different gyms`() {
        val gym1 = this.mockGym()
        val gym2 = this.gymDAO.save(Gym("gym2", "address2", gym1.city))
        val name = "name"
        this.imageService.setImage(gym1.id, this.createMockImage(name, "content"), name)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isTrue()
        this.imageService.setImage(gym2.id, this.createMockImage(name, "content"), name)
    }

    @Test
    fun `Should delete an image`() {
        val gym = this.mockGym()
        val name = "name"
        val saved = this.imageService.setImage(gym.id, this.createMockImage(name, "content"), name)
        Assertions.assertThat(this.gymImageDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isTrue()
        this.imageService.deleteImage(saved.id)
        Assertions.assertThat(this.gymImageDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isFalse()
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not delete an image if it does not exist`() {
        this.imageService.deleteImage("jdnsakjnda")
    }

    private fun createMockImage(name: String, content: String): MultipartFile {
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

        this.imageService.upload(image, name)
        return image
    }

    private fun mockGym(): Gym {
        val city = this.cityDAO.save(MockEntities.mockCities[0])
        return this.gymDAO.save(Gym("gym1", "address1", city))
    }


}