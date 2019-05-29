package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.MockObjectStorage
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.GymImageDAO
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.GymImage
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.gabrigiunchi.backendtesi.service.ObjectStorageService
import com.ibm.cloud.objectstorage.services.s3.AmazonS3
import com.ibm.cloud.objectstorage.services.s3.model.ListObjectsV2Result
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

class GymImageControllerTest : AbstractControllerTest() {

    @Value("\${application.objectstorage.gymphotosbucket}")
    private var bucketName = ""

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @MockBean
    private lateinit var objectStorageService: ObjectStorageService

    @MockBean
    private lateinit var amazonS3: AmazonS3

    private val mockObjectStorage = MockObjectStorage()

    @Autowired
    private lateinit var context: ApplicationContext

    @Autowired
    private lateinit var gymImageDAO: GymImageDAO

    @Before
    fun init() {
        this.gymDAO.deleteAll()
        this.gymImageDAO.deleteAll()
        this.mockObjectStorage.clear()
        this.amazonS3 = this.context.getBean(AmazonS3::class)
        this.objectStorageService = this.context.getBean(ObjectStorageService::class)
        Mockito.`when`(this.objectStorageService.createClient()).thenReturn(this.amazonS3)
    }

    @Test
    fun `Should get all images metadata`() {
        (1..2).map { this.mockImage("$it.png", "jdnsajdas") }
        val objectListing = ListObjectsV2Result()
        objectListing.objectSummaries.addAll(this.mockObjectStorage.getAllMetadata()
                .map { metadata ->
                    val summary = S3ObjectSummary()
                    summary.key = metadata.id
                    summary.lastModified = Date(metadata.lastModified)
                    summary
                })

        Assertions.assertThat(this.mockObjectStorage.getAllMetadata().size).isEqualTo(2)
        Mockito.`when`(this.amazonS3.listObjectsV2(this.bucketName)).thenReturn(objectListing)

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/photos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`("1.png")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`("2.png")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the metadata of an image`() {
        val name = "a.png"
        this.mockImage(name, "dsada")
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/photos/$name/metadata")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a photo`() {
        val name = "a.png"
        val content = "fdsda"
        this.mockImage(name, content)
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/photos/$name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(content)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the photos of a gym by its id`() {
        val gym = this.mockGym()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/${gym.id}/photos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(0)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the photos of a gym if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/-1/photos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
    }

    @Test
    fun `Should add the photo of a gym`() {
        val gym = this.mockGym()
        val name = "photo1.jpg"

        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.GYMS}/${gym.id}/photos/$name")
                .file(this.mockImage(name, "content")))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(name)))

        Assertions.assertThat(this.gymImageDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should not add the photo of a gym if the gym does not exit`() {
        val name = "photo1.jpg"
        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.GYMS}/-1/photos/$name")
                .file(this.mockImage(name, "content")))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
    }

    @Test
    fun `Should delete a photo`() {
        val imageId = "photo1.jpg"
        this.gymImageDAO.save(GymImage(imageId, ImageType.profile, this.mockGym()))
        this.mockImage(imageId, "content")
        Assertions.assertThat(this.gymImageDAO.findById(imageId).isPresent).isTrue()
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.GYMS}/photos/$imageId"))
                .andExpect(MockMvcResultMatchers.status().isNoContent)

        Assertions.assertThat(this.gymImageDAO.count()).isEqualTo(0)
    }

    @Test
    fun `Should not delete a photo if it does not exit`() {
        val name = "photo1.jpg"
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.GYMS}/photos/$name"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("image $name does not exist")))
    }

    private fun mockGym(): Gym {
        val city = this.cityDAO.save(MockEntities.mockCities[0])
        return this.gymDAO.save(Gym("gym1", "address1", city))
    }

    private fun mockImage(name: String, content: String): MockMultipartFile {
        val image = MockMultipartFile("image", name, "text/plain", content.toByteArray())
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

        return image
    }
}