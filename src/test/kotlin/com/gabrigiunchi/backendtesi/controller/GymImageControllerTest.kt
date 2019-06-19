package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.MockObjectStorage
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.gabrigiunchi.backendtesi.service.ImageService
import com.gabrigiunchi.backendtesi.service.ObjectStorageService
import com.ibm.cloud.objectstorage.services.s3.AmazonS3
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
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

    @SpyBean
    private lateinit var objectStorageService: ObjectStorageService

    @MockBean
    private lateinit var amazonS3: AmazonS3

    private val mockObjectStorage = MockObjectStorage()

    @Autowired
    private lateinit var context: ApplicationContext

    @Autowired
    private lateinit var gymImageDAO: ImageDAO

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
        val gym = this.mockGym()
        (1..2).map { this.gymImageDAO.save(Image("$it.png", ImageType.profile, gym, this.bucketName)) }
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/photos/page/0/size/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.`is`("1.png")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id", Matchers.`is`("2.png")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the metadata of an image`() {
        val id = "a.png"
        this.gymImageDAO.save(Image(id, ImageType.profile, this.mockGym(), this.bucketName))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/photos/$id/metadata")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the metadata of an image if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/photos/-1/metadata")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("image -1 does not exist")))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("entity -1 does not exist")))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("entity -1 does not exist")))
    }

    @Test
    fun `Should delete a photo`() {
        val imageId = "photo1.jpg"
        this.gymImageDAO.save(Image(imageId, ImageType.profile, this.mockGym(), this.bucketName))
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

    @Test
    fun `Should get the avatar of a gym`() {
        val gym = this.mockGym()
        val image = this.gymImageDAO.save(Image("avatar1.png", ImageType.avatar, gym, this.bucketName))
        val content = "dnasjndjk"
        this.mockImage(image.id, content)
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/${gym.id}/avatar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(content)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the avatar metadata of a gym`() {
        val gym = this.mockGym()
        val metadata = this.gymImageDAO.save(Image("avatar1", ImageType.avatar, gym, this.bucketName))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/${gym.id}/avatar/metadata")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(metadata.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the default avatar metadata of a gym`() {
        val gym = this.mockGym()
        val metadata = ImageService.DEFAULT_AVATAR_METADATA
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/${gym.id}/avatar/metadata")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(metadata.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should set the avatar of a gym`() {
        val gym = this.mockGym()
        val name = "photo1.jpg"
        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.GYMS}/${gym.id}/avatar")
                .file(this.mockImage(name, "content")))
                .andExpect(MockMvcResultMatchers.status().isCreated)

        Assertions.assertThat(this.gymImageDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should not set the avatar of a gym if the gym does not exit`() {
        val name = "photo1.jpg"
        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.GYMS}/-1/avatar")
                .file(this.mockImage(name, "content")))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("entity -1 does not exist")))
    }

    @Test
    fun `Should not get the avatar metadata of a gym if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/-1/avatar/metadata")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("entity -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
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