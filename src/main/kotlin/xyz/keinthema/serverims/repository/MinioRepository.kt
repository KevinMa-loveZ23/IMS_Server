package xyz.keinthema.serverims.repository

import io.minio.*
import org.apache.commons.io.IOUtils
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import xyz.keinthema.serverims.config.MinioConfig
import java.io.FileInputStream
import java.io.InputStream


@Component
class MinioRepository(
//    private val minioConfig: MinioConfig,
    private val minioClient: MinioClient,
) {
    fun MinioClient.setBucket(bucketName: String) {
        if (!this.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        )) {
            this.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        }
    }

    fun putObject(filePart: FilePart, bucketName: String, fileName: String): Mono<String> {
//        val osPipe = PipedOutputStream()
//        DataBufferUtils.write(filePart.content(), osPipe)
        val contentType = filePart.headers().contentType
        val mediaType = if (contentType != null && contentType.isConcrete) {
            contentType.toString()
        } else {
            MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE
        }
        return DataBufferUtils.join(filePart.content()).map {
            it.asInputStream(true).use { stream ->
                minioClient.setBucket(bucketName = bucketName)
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .`object`(fileName)
                        .stream(stream, -1, 10485760)
                        .contentType(mediaType)
                        .build()
                )
            }
        }.map { it.`object`() }
    }

    fun getObject(bucketName: String, fileName: String): Mono<Pair<ByteArray, MediaType?>> {
        return Mono.fromCallable {
            if (!minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            )) {
                return@fromCallable Pair(byteArrayOf(),null)
            }
            val stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .build()
            )
            val contentType = stream.headers()["Content-Type"] ?: MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE
            val mediaType: MediaType = MediaType.asMediaType(MimeTypeUtils.parseMimeType(contentType))
            val bArray = IOUtils.toByteArray(stream)
            stream.close()
//            Pair(stream.use { it.readBytes() }, mediaType)
            Pair(bArray, mediaType)
        }
    }

    fun removeBucket(bucketName: String) {
        if (!minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        )) {
            return
        }
        val objResults = minioClient.listObjects(
            ListObjectsArgs.builder().bucket(bucketName).build()
        ).iterator()
        while (objResults.hasNext()) {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objResults.next().get().objectName())
                    .build()
            )
        }
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build())
    }

    fun getBucketNameFromLong(long: Long): String {
        return "long-bid-${long}"
    }
}