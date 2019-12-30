package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.type.ImageType
import com.ibm.cloud.objectstorage.ClientConfiguration
import com.ibm.cloud.objectstorage.SDKGlobalConfiguration
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials
import com.ibm.cloud.objectstorage.services.s3.AmazonS3
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.util.*

@Service
class ObjectStorageService {

    @Value("\${application.objectstorage.cos.host}")
    private lateinit var cosHost: String

    @Value("\${application.objectstorage.cos.apikeyid}")
    private lateinit var cosApiKey: String

    @Value("\${application.objectstorage.cos.authendpoint}")
    private lateinit var cosAuthEndpoint: String

    @Value("\${application.objectstorage.cos.crn}")
    private lateinit var cosServiceCrn: String

    @Value("\${application.objectstorage.cos.location}")
    private lateinit var cosLocation: String

    private val logger = LoggerFactory.getLogger(ObjectStorageService::class.java)
    private var lastUpdate = 0L
    private var client: AmazonS3? = null

    fun getAllMetadataWithPrefix(prefix: String, bucket: String): List<Image> =
            this.createClient()
                    .listObjectsV2(bucket, prefix)
                    .objectSummaries
                    .map { summary -> Image(summary.key, ImageType.unknown, bucket, summary.lastModified.time) }

    fun contains(image: String, bucket: String): Boolean = this.createClient().doesObjectExist(bucket, image)

    fun download(id: String, bucket: String): ByteArray {
        val client = this.createClient()
        if (!client.doesObjectExist(bucket, id)) {
            throw ResourceNotFoundException(Image::class.java, id)
        }

        return client.getObject(bucket, id).objectContent.readAllBytes()
    }

    fun upload(image: MultipartFile, id: String, bucket: String): Image {
        val metadata = ObjectMetadata()
        metadata.contentLength = image.size
        this.createClient().putObject(bucket, id, image.inputStream, metadata).metadata
        return Image(id, ImageType.avatar, bucket, Date().time)
    }

    fun delete(id: String, bucket: String) {
        val client = this.createClient()
        if (!client.doesObjectExist(bucket, id)) {
            throw ResourceNotFoundException(Image::class.java, id)
        }

        client.deleteObject(bucket, id)
    }

    fun createClient(): AmazonS3 {
        this.logger.info("Creating ObjectStorage client")
        val now = Date().time

        if ((now - this.lastUpdate) > Duration.ofMinutes(5).toMillis()) {
            this.logger.info("ObjectStorage client needs refresh, creating a new instance")
            SDKGlobalConfiguration.IAM_ENDPOINT = this.cosAuthEndpoint
            val credentials = BasicIBMOAuthCredentials(this.cosApiKey, this.cosServiceCrn)
            val clientConfig = ClientConfiguration().withRequestTimeout(5000)
            clientConfig.setUseTcpKeepAlive(true)

            this.client = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(credentials))
                    .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(
                            this.cosHost, this.cosLocation))
                    .withPathStyleAccessEnabled(true)
                    .withClientConfiguration(clientConfig).build()

            this.lastUpdate = now
        } else {
            this.logger.info("ObjectStorage client does not need refresh, returning cached instance")
        }

        return this.client!!
    }

}