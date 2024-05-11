package xyz.keinthema.serverims.config

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(MinioProperties::class)
class MinioConfig(val prop: MinioProperties) {
    @Bean
    fun minioClient(): MinioClient {
        val client = MinioClient.builder()
            .endpoint(prop.endpoint)
            .credentials(prop.accessKey, prop.secretKey)
            .build()
//        if (!client.bucketExists(
//                BucketExistsArgs.builder().bucket(prop.bucketName).build()
//        )) {
//            client.makeBucket(MakeBucketArgs.builder().bucket(prop.bucketName).build())
//        }
        return client
    }

}

@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
//    val host: String,
//    val port: String,
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
//    val bucketName: String
)