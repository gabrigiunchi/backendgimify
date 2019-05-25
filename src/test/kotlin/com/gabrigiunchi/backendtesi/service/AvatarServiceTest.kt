package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockObjectStorage
import com.gabrigiunchi.backendtesi.dao.AvatarDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Avatar
import com.gabrigiunchi.backendtesi.model.ImageMetadata
import com.gabrigiunchi.backendtesi.model.User
import com.gabrigiunchi.backendtesi.util.UserFactory
import com.ibm.cloud.objectstorage.services.s3.AmazonS3
import com.ibm.cloud.objectstorage.services.s3.model.ListObjectsV2Result
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.util.*

class AvatarServiceTest : AbstractControllerTest() {

    @Value("\${application.objectstorage.avatarsbucket}")
    private var bucketName = ""

    private lateinit var objectStorageService: ObjectStorageService
    private lateinit var amazonS3: AmazonS3
    private lateinit var avatarService: AvatarService
    private val mockObjectStorage = MockObjectStorage()

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var avatarDAO: AvatarDAO

    @Autowired
    private lateinit var userFactory: UserFactory

    @Before
    fun init() {
        this.userDAO.deleteAll()
        this.mockObjectStorage.clear()
        this.amazonS3 = Mockito.mock(AmazonS3::class.java)
        this.objectStorageService = Mockito.mock(ObjectStorageService::class.java)
        this.avatarService = AvatarService(this.userDAO, this.avatarDAO, this.objectStorageService, this.bucketName)
        Mockito.`when`(this.objectStorageService.createClient()).thenReturn(this.amazonS3)
    }

    @Test
    fun `Should get the avatar metadata of a user`() {
        val user = this.mockUser()
        val metadata = this.avatarDAO.save(Avatar("name", user))
        val result = this.avatarService.getAvatarMetadataOfUser(user.username)
        Assertions.assertThat(metadata.user.id).isEqualTo(user.id)
        Assertions.assertThat(result.id).isEqualTo(metadata.id)
        Assertions.assertThat(result.lastModified).isEqualTo(metadata.lastModified)
    }

    @Test
    fun `Should get the default avatar metadata of a user if the user has not an avatar`() {
        val user = this.mockUser()
        val result = this.avatarService.getAvatarMetadataOfUser(user.username)
        Assertions.assertThat(result.id).isEqualTo(AvatarService.DEFAULT_AVATAR_METADATA.id)
        Assertions.assertThat(result.lastModified).isEqualTo(AvatarService.DEFAULT_AVATAR_METADATA.lastModified)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not get the avatar metadata of a user if the user does not exist`() {
        this.avatarService.getAvatarMetadataOfUser("ddasjdada")
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when requesting the avatar of a user if the user does not exist`() {
        this.avatarService.getAvatarOfUser("djasnjdnajd")
    }

    @Test
    fun `Should upload an avatar for a user`() {
        val image = MockMultipartFile("name", "content".toByteArray())
        val putObjectResult = this.mockObjectStorage.add(image, "name")

        Mockito.`when`(this.amazonS3.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(putObjectResult)

        val user = this.mockUser()
        val putResult = this.avatarService.setAvatarOfUser(user.username, image)

        val optionalSavedAvatar = this.avatarDAO.findByUser(user)
        Assertions.assertThat(optionalSavedAvatar.isPresent).isTrue()
        val savedMetadata = optionalSavedAvatar.get()
        Assertions.assertThat(savedMetadata.id).isEqualTo(putResult.id)
        Assertions.assertThat(savedMetadata.lastModified).isEqualTo(putResult.lastModified)
    }

    @Test
    fun `Should change the avatar of a user`() {
        val user = this.mockUser()
        val metadata = this.avatarDAO.save(Avatar("avatar", user))

        val image = MockMultipartFile("name", "content".toByteArray())
        val putObjectResult = this.mockObjectStorage.add(image, "name")

        Mockito.`when`(this.amazonS3.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(putObjectResult)

        val result = this.avatarService.setAvatarOfUser(user.username, image)
        Assertions.assertThat(metadata.id).isEqualTo(result.id)
        Assertions.assertThat(metadata.lastModified).isEqualTo(result.lastModified)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when setting the avatar of a user if the user does not exist`() {
        this.avatarService.setAvatarOfUser("jndsajnda", this.createMockImage("name", "content"))
    }

    @Test
    fun `Should delete an avatar of a user`() {
        val user = this.mockUser()
        val imageId = user.username
        this.avatarDAO.save(Avatar(imageId, user))
        this.avatarService.setAvatarOfUser(user.username, this.createMockImage(imageId, "dsadas"))
        Assertions.assertThat(this.mockObjectStorage.contains(imageId)).isTrue()
        this.avatarService.deleteAvatarOfUser(user.username)
        Assertions.assertThat(this.mockObjectStorage.contains(imageId)).isFalse()
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not delete an avatar of a user if the user does not exist`() {
        this.avatarService.deleteAvatarOfUser("jndnsa")
    }

    @Test
    fun `Should get the default avatar metadata`() {
        val content = "jdnajsd"
        this.createMockImage(AvatarService.DEFAULT_AVATAR_METADATA.id, content)
        val result = this.avatarService.defaultAvatar
        Assertions.assertThat(result).isEqualTo(content.toByteArray())
    }

    @Test
    fun `Should get the preset avatars metadata`() {
        val prefix = "preset"
        (1..4).map { this.createMockImage("$prefix$it", "jdnsajdas") }.forEach { this.avatarService.upload(it, it.name) }
        val objectListing = ListObjectsV2Result()
        objectListing.objectSummaries.addAll(this.mockObjectStorage.getAllMetadata()
                .filter { it.id.startsWith(prefix) }
                .map { metadata ->
                    val summary = S3ObjectSummary()
                    summary.key = metadata.id
                    summary.lastModified = Date(metadata.lastModified)
                    summary
                })

        Mockito.`when`(this.amazonS3.listObjectsV2(this.bucketName, prefix)).thenReturn(objectListing)
        val result = this.avatarService.presetAvatars
        Assertions.assertThat(result.size).isEqualTo(4)
        Assertions.assertThat(result.all { it.id.startsWith(prefix) })
    }

    @Test
    fun `Should set the default avatar`() {
        val content = "a"
        this.avatarService.setDefaultAvatar(this.createMockImage(AvatarService.DEFAULT_AVATAR_METADATA.id, content))
        val result = this.avatarService.defaultAvatar
        Assertions.assertThat(result).isEqualTo(content.toByteArray())
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

        this.avatarService.upload(image, name)
        return image
    }

    private fun mockUser(username: String = "gabrigiunchi"): User {
        return this.userDAO.save(this.userFactory.createAdminUser(username, "aaaa", "", ""))
    }
}