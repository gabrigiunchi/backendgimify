package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockObjectStorage
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
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

    @Value("\${application.objectstorage.gymphotosbucket}")
    private var bucketName = ""

    private lateinit var objectStorageService: ObjectStorageService
    private lateinit var amazonS3: AmazonS3
    private lateinit var avatarService: AvatarService
    private val mockObjectStorage = MockObjectStorage()

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var userFactory: UserFactory

    @Before
    fun init() {
        this.userDAO.deleteAll()
        this.mockObjectStorage.clear()
        this.amazonS3 = Mockito.mock(AmazonS3::class.java)
        this.objectStorageService = Mockito.mock(ObjectStorageService::class.java)
        this.avatarService = AvatarService(this.userDAO, this.objectStorageService, this.bucketName)
        Mockito.`when`(this.objectStorageService.createClient()).thenReturn(this.amazonS3)
    }

    @Test
    fun `Should get the avatar metadata of a user`() {
        val now = Date().time
        val user = this.mockUser()
        val imageId = user.username
        val content = "content"
        this.createMockImage(imageId, content)
        val result = this.avatarService.getAvatarMetadataOfUser(user.username)
        Assertions.assertThat(result.id).isEqualTo(imageId)
        Assertions.assertThat(result.lastModified).isGreaterThanOrEqualTo(now)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not get the avatar metadata of a user if the user does not exist`() {
        this.avatarService.getAvatarMetadataOfUser("ddasjdada")
    }

    @Test
    fun `Should return the avatar of a user`() {
        val user = this.mockUser()
        val content = "content"
        this.createMockImage(user.username, content)
        val result = this.avatarService.getAvatarOfUser(user.username)
        Assertions.assertThat(result).isEqualTo(content.toByteArray())
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when requesting the avatar of a user if the user does not exist`() {
        this.avatarService.getAvatarOfUser("djasnjdnajd")
    }

    @Test
    fun `Should upload an avatar for a user`() {
        val now = Date().time
        val user = this.mockUser()
        val imageId = user.username
        val content = "djnsajda"
        val image = this.createMockImage(imageId, content)
        val result = this.avatarService.setAvatarOfUser(user.username, image)
        Assertions.assertThat(result.id).isEqualTo(imageId)
        Assertions.assertThat(result.lastModified).isGreaterThanOrEqualTo(now)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when setting the avatar of a user if the user does not exist`() {
        this.avatarService.setAvatarOfUser("jndsajnda", this.createMockImage("name", "content"))
    }

    @Test
    fun `Should delete an avatar of a user`() {
        val user = this.mockUser()
        val imageId = user.username
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

    private fun mockUser(username: String = "gabrigiunci"): User {
        return this.userDAO.save(this.userFactory.createAdminUser(username, "aaaa", "", ""))
    }
}