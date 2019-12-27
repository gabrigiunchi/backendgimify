package com.gabrigiunchi.backendtesi.service


import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.MockObjectStorage
import com.gabrigiunchi.backendtesi.dao.DrawableDAO
import com.gabrigiunchi.backendtesi.dao.ImageDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.ibm.cloud.objectstorage.services.s3.AmazonS3
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile
import java.util.*

class ImageServiceTest : BaseTest() {
    private val bucketName = "bucket"

    @Autowired
    private lateinit var drawableDAO: DrawableDAO

    @Autowired
    private lateinit var imageDAO: ImageDAO

    private lateinit var mockGym: Gym

    @Autowired
    private lateinit var objectStorageService: ObjectStorageService
    private lateinit var amazonS3: AmazonS3
    private lateinit var imageService: ImageService
    private val mockObjectStorage = MockObjectStorage()

    @Before
    fun init() {
        this.mockGym = this.mockGym()
        this.mockObjectStorage.clear()
        this.amazonS3 = Mockito.mock(AmazonS3::class.java)
        this.objectStorageService = Mockito.spy(this.objectStorageService)
        `when`(this.objectStorageService.createClient()).thenReturn(this.amazonS3)
        this.imageService = ImageService(drawableDAO, imageDAO, this.objectStorageService, this.bucketName)
    }

    @Test
    fun `Should return all images metadata`() {
        val gym = this.mockGym
        this.imageDAO.saveAll((1..10).map { Image("image$it", ImageType.profile, gym, this.bucketName) })
        val result = this.imageService.getAllMetadata(0, 100).content
        Assertions.assertThat(result.size).isEqualTo(10)
    }

    @Test
    fun `Should upload an image`() {
        val name = "kjdnajs.dasda"
        this.createMockImage(name, "dnansda")
        Assertions.assertThat(this.objectStorageService.createClient().doesObjectExist(this.bucketName, name))
                .isTrue()
    }

    @Test
    fun `Should download an image by name`() {
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

    /************************** AVATAR **************************************************/

    @Test
    fun `Should set an avatar`() {
        val gym = this.mockGym
        val content = "ndjansa"
        this.createMockImage("fasda", content)
        this.imageService.setAvatar(gym.id, this.createMockImage("djandjan", content))
        Assertions.assertThat(this.imageDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should get an avatar`() {
        val gym = this.mockGym
        val name = "njdajsnd.aaa"
        val content = "ndjansa"
        this.createMockImage(name, content)
        this.imageDAO.save(Image(name, ImageType.avatar, gym, this.bucketName))
        val result = this.imageService.getAvatar(gym.id)
        Assertions.assertThat(result.size).isGreaterThan(0)
        Assertions.assertThat(result).isEqualTo(content.toByteArray())
    }

    @Test
    fun `Should delete an avatar`() {
        val gym = this.mockGym
        val content = "ndjansa"
        val name = "image1"
        this.createMockImage(name, content)
        this.imageDAO.save(Image(name, ImageType.avatar, gym, this.bucketName))
        Assertions.assertThat(this.imageDAO.count()).isEqualTo(1)
        this.imageService.deleteAvatar(gym.id)
        Assertions.assertThat(this.imageDAO.count()).isEqualTo(0)
    }

    @Test
    fun `Should get an avatar metadata`() {
        val gym = this.mockGym
        val image = Image("image1", ImageType.avatar, gym, this.bucketName)
        this.imageDAO.save(image)
        Assertions.assertThat(this.imageDAO.count()).isEqualTo(1)
        val result = this.imageService.getAvatarMetadata(gym.id)
        Assertions.assertThat(result.id).isEqualTo(image.id)
        Assertions.assertThat(result.lastModified).isEqualTo(image.lastModified)
    }

    @Test
    fun `Should get the default avatar metadata`() {
        val gym = this.mockGym
        val result = this.imageService.getAvatarMetadata(gym.id)
        Assertions.assertThat(result.id).isEqualTo(ImageService.DEFAULT_AVATAR_METADATA.id)
        Assertions.assertThat(result.lastModified).isEqualTo(ImageService.DEFAULT_AVATAR_METADATA.lastModified)
    }

    @Test
    fun `Should never create two avatars for the same entity`() {
        val gym = this.mockGym
        this.imageService.setAvatar(gym.id, this.createMockImage("das", "jjnj"))
        this.imageService.setAvatar(gym.id, this.createMockImage("sss", "aa"))
        Assertions.assertThat(this.imageDAO.count()).isEqualTo(1)
    }


    /****************************** IMAGES *********************************************/

    @Test
    fun `Should return the photos of an entity`() {
        val now = Date().time
        val gym = this.mockGym
        val saved = this.imageDAO.saveAll((1..4).map { Image("photo$it", ImageType.profile, gym, this.bucketName) }).toList()

        val result = this.imageService.getImagesOfEntity(gym.id)
        Assertions.assertThat(result.size).isEqualTo(4)
        Assertions.assertThat(result[0].id).isEqualTo(saved[0].id)
        Assertions.assertThat(result.all { it.lastModified >= now }).isTrue()
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when requesting the photos of an entity if it does not exist`() {
        this.imageService.getImagesOfEntity(-1)
    }

    @Test
    fun `Should upload an image for an entity`() {
        val now = Date().time
        val name = "photo1"
        val gym = this.mockGym
        val image = this.createMockImage(name, "dnansda")
        this.imageService.updateImage(gym.id, image, name, ImageType.profile)
        val saved = this.imageDAO.findByDrawableAndBucket(gym, this.bucketName)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isTrue()
        Assertions.assertThat(saved.size).isEqualTo(1)
        Assertions.assertThat(saved[0].id).isEqualTo(name)
        Assertions.assertThat(saved[0].drawable.id).isEqualTo(gym.id)
        Assertions.assertThat(saved[0].lastModified).isGreaterThanOrEqualTo(now)
        Assertions.assertThat(saved[0].type).isEqualTo(ImageType.profile)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when adding the photo of an entity if it does not exist`() {
        this.imageService.updateImage(-1, this.createMockImage("name", "content"), "photo1", ImageType.profile)
    }

    @Test
    fun `Should add an image`() {
        val gym = this.mockGym
        val name = "name"
        this.imageService.addImage(gym.id, this.createMockImage(name, "content"))
        Assertions.assertThat(this.objectStorageService.contains(name, this.bucketName)).isTrue()
    }

    @Test
    fun `Should override the photo of an entity`() {
        val gym = this.mockGym
        val name = "name"
        this.imageService.updateImage(gym.id, this.createMockImage(name, "content"), name, ImageType.profile)
        Assertions.assertThat(this.objectStorageService.contains(name, this.bucketName)).isTrue()
        this.imageService.updateImage(gym.id, this.createMockImage(name, "content"), name, ImageType.profile)
        Assertions.assertThat(this.objectStorageService.contains(name, this.bucketName)).isTrue()
    }

    @Test(expected = ResourceAlreadyExistsException::class)
    fun `Should not be possible to add two photos with the same name for different entities`() {
        val gym1 = this.mockGym
        val gym2 = this.gymDAO.save(Gym("gym2", "address2", gym1.city))
        val name = "name"
        this.imageService.updateImage(gym1.id, this.createMockImage(name, "content"), name, ImageType.profile)
        Assertions.assertThat(this.mockObjectStorage.contains(name)).isTrue()
        this.imageService.updateImage(gym2.id, this.createMockImage(name, "content"), name, ImageType.profile)
    }

    @Test
    fun `Should associate an existing image to an entity`() {
        val imageId = "asdasda"
        val user = this.mockUser("gabrigiunchi")
        val image = this.imageDAO.save(Image(imageId, ImageType.avatar, user, bucketName))
        val result = this.imageService.associateExistingImageToEntity(user.id, ImageType.avatar, imageId)
        Assertions.assertThat(result.bucketName).isEqualTo(bucketName)
        Assertions.assertThat(result.id).isEqualTo(imageId)
        Assertions.assertThat(result.type).isEqualTo(ImageType.avatar)
        Assertions.assertThat(this.imageDAO.findByDrawableAndBucket(user, bucketName).toList()).isEqualTo(listOf(image))
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not associate an existing image to an entity if the entity does not exist`() {
        val imageId = "asdasda"
        val user = this.mockUser("gabrigiunchi")
        this.imageDAO.save(Image(imageId, ImageType.avatar, user, bucketName))
        this.imageService.associateExistingImageToEntity(-1, ImageType.avatar, imageId)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not associate an existing image to an entity if the image does not exist`() {
        val user = this.mockUser("gabrigiunchi")
        this.imageService.associateExistingImageToEntity(user.id, ImageType.avatar, "asas")
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not associate an existing image to an entity if the image does not exist in the bucket`() {
        val imageId = "asdasda"
        val user = this.mockUser("gabrigiunchi")
        this.imageDAO.save(Image(imageId, ImageType.avatar, user, "dasdahdsj"))
        this.imageService.associateExistingImageToEntity(user.id, ImageType.avatar, imageId)
    }

    private fun createMockImage(name: String, content: String): MockMultipartFile {
        val image = MockMultipartFile(name, content.toByteArray())
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size
        metadata.lastModified = Date()

        val putObjectResult = this.mockObjectStorage.add(image, name)

        `when`(this.amazonS3.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(putObjectResult)

        `when`(this.amazonS3.getObjectMetadata(this.bucketName, name)).thenReturn(metadata)

        `when`(this.amazonS3.doesObjectExist(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.contains(name))

        `when`(this.amazonS3.getObject(this.bucketName, name))
                .thenReturn(this.mockObjectStorage.getImage(name))

        `when`(this.amazonS3.deleteObject(this.bucketName, name))
                .then { this.mockObjectStorage.delete(name) }

        this.objectStorageService.upload(image, name, this.bucketName)
        return image
    }
}
