package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockObjectStorage
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.entities.User
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.gabrigiunchi.backendtesi.service.ImageService
import com.gabrigiunchi.backendtesi.service.ObjectStorageService
import com.gabrigiunchi.backendtesi.service.UserService
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
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

class AvatarControllerTest : AbstractControllerTest() {

    @Value("\${application.objectstorage.avatarsbucket}")
    private var bucketName = ""

    @SpyBean
    private lateinit var objectStorageService: ObjectStorageService

    @MockBean
    private lateinit var amazonS3: AmazonS3

    private val mockObjectStorage = MockObjectStorage()

    @Autowired
    private lateinit var context: ApplicationContext

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var avatarDAO: ImageDAO

    @Before
    fun init() {
        this.mockObjectStorage.clear()
        this.amazonS3 = this.context.getBean(AmazonS3::class)
        this.objectStorageService = this.context.getBean(ObjectStorageService::class)
        Mockito.`when`(this.objectStorageService.createClient()).thenReturn(this.amazonS3)
    }

    @Test
    fun `Should get all avatars metadata`() {
        val user = this.mockUser
        (1..2).map { this.avatarDAO.save(Image("avatar$it", ImageType.avatar, user, this.bucketName)) }

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.`is`("avatar1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id", Matchers.`is`("avatar2")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the avatar metadata of a user`() {
        val user = this.mockUser
        this.avatarDAO.save(Image("avatar1", ImageType.avatar, user, this.bucketName))

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/metadata/of_user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("avatar1")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the default avatar metadata if the user does not have an avatar`() {
        val user = this.mockUser
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/metadata/of_user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("default")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastModified", Matchers.`is`(0)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the avatar metadata of a user if the user does not exist`() {
        val user = this.mockUser
        this.avatarDAO.save(Image("avatar1", ImageType.avatar, user, this.bucketName))

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/metadata/of_user/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("entity -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the avatar of a user if the user does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/of_user/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("entity -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get all presets avatars metadata`() {
        val prefix = "preset"
        (1..2).map { this.mockImage("$it.png", "jdnsajdas") }
        (1..2).map { this.mockImage("$prefix$it.png", "jdnsajdas") }
        val objectListing = ListObjectsV2Result()
        objectListing.objectSummaries.addAll(this.mockObjectStorage.getAllMetadata()
                .filter { it.id.startsWith(prefix) }
                .map { metadata ->
                    val summary = S3ObjectSummary()
                    summary.key = metadata.id
                    summary.lastModified = Date(metadata.lastModified)
                    summary
                })

        Assertions.assertThat(this.mockObjectStorage.getAllMetadata().size).isEqualTo(4)
        Mockito.`when`(this.amazonS3.listObjectsV2(this.bucketName, prefix)).thenReturn(objectListing)

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/presets")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.startsWith(prefix)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.startsWith(prefix)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an avatar`() {
        val name = "a.png"
        val content = "fdsda"
        this.mockImage(name, content)
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/$name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(content)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should add an avatar`() {
        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.AVATARS}/${this.mockUser.id}")
                .file(this.mockImage("adsada", "content")))
                .andExpect(MockMvcResultMatchers.status().isCreated)
    }

    @Test
    fun `Should delete an avatar`() {
        val name = "photo1.jpg"
        this.mockImage(name, "dsjdas")
        this.avatarDAO.save(Image(name, ImageType.avatar, this.mockUser, this.bucketName))
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.AVATARS}/$name"))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    fun `Should not delete an avatar if it does not exit`() {
        val name = "photo1.jpg"
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.AVATARS}/$name"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("image $name does not exist")))
    }

    /************************** MY AVATAR *********************************************************/

    @Test
    fun `Should get my avatar`() {
        val avatar = ImageService.DEFAULT_AVATAR_METADATA
        val name = avatar.id
        val content = "dajndjsa"
        this.mockImage(name, content)
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(content)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get my avatar metadata`() {
        val avatar = ImageService.DEFAULT_AVATAR_METADATA
        val name = avatar.id
        val content = "fdsda"
        this.mockImage(name, content)
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/me/metadata")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(avatar.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastModified", Matchers.`is`(avatar.lastModified.toInt())))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should change my avatar`() {
        val now = Date().time
        this.userDAO.deleteAll()
        val user = this.mockUser
        val image = this.mockImage("name", "content")
        val putObjectResult = this.mockObjectStorage.add(image, "name")

        Mockito.`when`(this.amazonS3.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(putObjectResult)

        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.AVATARS}/me")
                .file(image))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastModified", Matchers.greaterThanOrEqualTo(now)))

        val optionalSavedAvatar = this.avatarDAO.findByDrawableAndTypeAndBucket(user, ImageType.avatar, this.bucketName)
        Assertions.assertThat(optionalSavedAvatar.isNotEmpty()).isTrue()
        val savedMetadata = optionalSavedAvatar.first()
        Assertions.assertThat(savedMetadata.drawable.id).isEqualTo(user.id)
        Assertions.assertThat(savedMetadata.lastModified).isGreaterThanOrEqualTo(now)
    }

    @Test
    fun `Should delete my avatar`() {
        val name = mockUser.username
        this.avatarDAO.save(Image(name, ImageType.avatar, this.mockUser, this.bucketName))
        this.mockImage(name, "dsjdas")
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.AVATARS}/me"))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    private fun mockImage(name: String, content: String): MockMultipartFile {
        val image = MockMultipartFile("avatar", name, "text/plain", content.toByteArray())
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size
        metadata.lastModified = Date()

        val putObjectResult = this.mockObjectStorage.add(image, name)

        Mockito.`when`(this.amazonS3.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(putObjectResult)

        Mockito.`when`(this.amazonS3.getObjectMetadata(Mockito.anyString(), Mockito.anyString())).thenReturn(metadata)

        Mockito.`when`(this.amazonS3.doesObjectExist(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.contains(name))

        Mockito.`when`(this.amazonS3.getObject(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.getImage(name))

        Mockito.`when`(this.amazonS3.deleteObject(this.bucketName, name))
                .then { this.mockObjectStorage.delete(name) }

        return image
    }

    val mockUser: User
        get() {
            this.userDAO.deleteAll()
            return this.userDAO.save(this.userService.createAdminUser("gabrigiunchi", "aaaa", "", ""))
        }
}