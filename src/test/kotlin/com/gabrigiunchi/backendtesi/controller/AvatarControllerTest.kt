package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.entities.User
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.gabrigiunchi.backendtesi.service.ImageService
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AvatarControllerTest : BaseTest() {

    @Value("\${application.objectstorage.avatarsbucket}")
    private var bucketName = ""

    @SpyBean
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var avatarDAO: ImageDAO

    private lateinit var mockUser: User

    @Before
    fun init() {
        this.userDAO.deleteAll()
        this.mockUser = this.mockUser("gabrigiunchi")
    }

    @Test
    fun `Should get all avatars metadata`() {
        val images = (1..2).map { Image("avatar$it", ImageType.avatar, this.bucketName, 0) }
        Mockito.doReturn(PageImpl(images)).`when`(this.imageService)
                .getAllMetadata(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.`is`("avatar1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id", Matchers.`is`("avatar2")))
                .andDo(MockMvcResultHandlers.print())

        Mockito.verify(this.imageService).getAllMetadata(this.bucketName, 0, 10)
    }

    @Test
    fun `Should get the avatar metadata of a user`() {
        val user = this.mockUser
        val image = this.avatarDAO.save(Image("avatar1", ImageType.avatar, user, this.bucketName))
        Mockito.doReturn(image).`when`(this.imageService)
                .getAvatarMetadata(Mockito.anyString(), Mockito.anyInt())

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/metadata/user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("avatar1")))
                .andDo(MockMvcResultHandlers.print())

        Mockito.verify(this.imageService)
                .getAvatarMetadata(this.bucketName, user.id)
    }

    @Test
    fun `Should get the default avatar metadata if the user does not have an avatar`() {
        val user = this.mockUser
        Mockito.doCallRealMethod().`when`(this.imageService)
                .getAvatarMetadata(Mockito.anyString(), Mockito.anyInt())
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/metadata/user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("default")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastModified", Matchers.`is`(0)))
                .andDo(MockMvcResultHandlers.print())

        Mockito.verify(this.imageService)
                .getAvatarMetadata(this.bucketName, user.id)
    }

    @Test
    fun `Should not get the avatar metadata of a user if the user does not exist`() {
        val user = this.mockUser
        this.avatarDAO.save(Image("avatar1", ImageType.avatar, user, this.bucketName))

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/metadata/user/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("entity -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the avatar of a user if the user does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/user/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("entity -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get all presets avatars metadata`() {
        val prefix = "preset"
        val images = (1..2).map { Image("preset$it", ImageType.avatar, this.bucketName, 0) }
        Mockito.doReturn(images).`when`(this.imageService)
                .getAllMetadataWithPrefix(Mockito.anyString(), Mockito.anyString())

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/presets")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.startsWith(prefix)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.startsWith(prefix)))
                .andDo(MockMvcResultHandlers.print())

        Mockito.verify(this.imageService).getAllMetadataWithPrefix(this.bucketName, AvatarController.PRESET_PREFIX)
    }

    @Test
    fun `Should get an avatar`() {
        val name = "a.png"
        val content = "fdsda"
        Mockito.doReturn(content.toByteArray()).`when`(this.imageService)
                .download(Mockito.anyString(), Mockito.anyString())

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/$name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(content)))
                .andDo(MockMvcResultHandlers.print())

        Mockito.verify(this.imageService).download(this.bucketName, name)
    }

    @Test
    fun `Should add an avatar`() {
        val image = this.mockImage("1", "dhajshdas")
        val user = this.mockUser
        Mockito.doReturn(Image("1", ImageType.avatar, this.bucketName, 0))
                .`when`(this.imageService)
                .setAvatar(this.bucketName, user.id, image)
        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.AVATARS}/${user.id}")
                .file(image))
                .andExpect(MockMvcResultMatchers.status().isCreated)

        Mockito.verify(this.imageService)
                .setAvatar(this.bucketName, user.id, image)
    }

    @Test
    fun `Should delete an avatar`() {
        val name = "photo1.jpg"
        val image = Image.create(name, ImageType.avatar, this.bucketName, this.mockUser)
        Mockito.doReturn(image)
                .`when`(this.imageService)
                .deleteImage(Mockito.anyString(), Mockito.anyString())
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.AVATARS}/$name"))
                .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(this.imageService).deleteImage(this.bucketName, name)
    }

    @Test
    fun `Should not delete an avatar if it does not exit`() {
        val name = "photo1.jpg"
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.AVATARS}/$name"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("Image #$name not found")))
    }

    /************************** MY AVATAR *********************************************************/

    @Test
    fun `Should get my avatar`() {
        val content = "dajndjsa"
        Mockito.doReturn(content.toByteArray())
                .`when`(this.imageService)
                .getAvatar(Mockito.anyString(), Mockito.anyInt())

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(content)))
                .andDo(MockMvcResultHandlers.print())

        Mockito.verify(this.imageService)
                .getAvatar(this.bucketName, this.mockUser.id)
    }

    @Test
    fun `Should get my avatar metadata`() {
        val avatar = Image("dasdasda", ImageType.avatar, this.bucketName, 1)
        Mockito.doReturn(avatar)
                .`when`(this.imageService)
                .getAvatarMetadata(Mockito.anyString(), Mockito.anyInt())
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.AVATARS}/me/metadata")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(avatar.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastModified", Matchers.`is`(avatar.lastModified.toInt())))
                .andDo(MockMvcResultHandlers.print())

        Mockito.verify(this.imageService)
                .getAvatarMetadata(this.bucketName, this.mockUser.id)
    }

    @Test
    fun `Should change my avatar`() {
        val imageMetadata = Image("dasdasda", ImageType.avatar, this.bucketName, 1)
        val image = this.mockImage(imageMetadata.id, "dasdasjkd")
        Mockito.doReturn(imageMetadata)
                .`when`(this.imageService)
                .setAvatar(this.bucketName, this.mockUser.id, image)

        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.AVATARS}/me")
                .file(image))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(imageMetadata.id)))

        Mockito.verify(this.imageService)
                .setAvatar(this.bucketName, this.mockUser.id, image)
    }

    @Test
    fun `Should change my avatar using a default one`() {
        val imageMetadata = Image("dasdasda", ImageType.avatar, this.bucketName, 1)
        Mockito.doReturn(imageMetadata)
                .`when`(this.imageService)
                .associateExistingImageToEntity(this.bucketName, this.mockUser.id, ImageType.avatar, imageMetadata.id)

        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.AVATARS}/me/use/${imageMetadata.id}"))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(imageMetadata.id)))

        Mockito.verify(this.imageService)
                .associateExistingImageToEntity(this.bucketName, this.mockUser.id, ImageType.avatar, imageMetadata.id)
    }

    @Test
    fun `Should change my avatar using a default one if the image does not exist`() {
        val imageMetadata = Image("dasdasda", ImageType.avatar, this.bucketName, 1)
        Mockito.doCallRealMethod()
                .`when`(this.imageService)
                .associateExistingImageToEntity(this.bucketName, this.mockUser.id, ImageType.avatar, imageMetadata.id)

        mockMvc.perform(MockMvcRequestBuilders.multipart("${ApiUrls.AVATARS}/me/use/${imageMetadata.id}"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message",
                        Matchers.`is`("Image #dasdasda not found")))
    }

    @Test
    fun `Should delete my avatar`() {
        Mockito.doReturn(Image("dasdasda", ImageType.avatar, this.bucketName, 0, this.mockUser))
                .`when`(this.imageService)
                .deleteAvatar(Mockito.anyString(), Mockito.anyInt())

        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.AVATARS}/me"))
                .andExpect(MockMvcResultMatchers.status().isNoContent)

        Mockito.verify(this.imageService)
                .deleteAvatar(this.bucketName, this.mockUser.id)
    }

    private fun mockImage(name: String, content: String): MockMultipartFile =
            MockMultipartFile("avatar", name, "text/plain", content.toByteArray())
}