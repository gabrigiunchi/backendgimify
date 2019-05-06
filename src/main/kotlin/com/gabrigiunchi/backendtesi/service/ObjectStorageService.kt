package com.gabrigiunchi.backendtesi.service

import com.ibm.cloud.objectstorage.ClientConfiguration
import com.ibm.cloud.objectstorage.SDKGlobalConfiguration
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials
import com.ibm.cloud.objectstorage.services.s3.AmazonS3
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

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

    fun createClient(): AmazonS3 {
        SDKGlobalConfiguration.IAM_ENDPOINT = this.cosAuthEndpoint
        val credentials = BasicIBMOAuthCredentials(this.cosApiKey, this.cosServiceCrn)
        val clientConfig = ClientConfiguration().withRequestTimeout(5000)
        clientConfig.setUseTcpKeepAlive(true)

        return AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(
                        this.cosHost, this.cosLocation))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfig).build()
    }

}