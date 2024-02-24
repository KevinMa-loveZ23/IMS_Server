package xyz.keinthema.serverims.handler

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.codec.EncodingException
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
class ResponseBodyJsonMessageWriter: HttpMessageWriter<ResponseEntity<*>> {
    override fun getWritableMediaTypes(): MutableList<MediaType> {
        return mutableListOf(MediaType.APPLICATION_JSON)
    }

    override fun canWrite(elementType: ResolvableType, mediaType: MediaType?): Boolean {
        return elementType.rawClass?.let { ResponseEntity::class.java.isAssignableFrom(it) } ?: false
    }

    override fun write(
        inputStream: Publisher<out ResponseEntity<*>>,
        elementType: ResolvableType,
        mediaType: MediaType?,
        message: ReactiveHttpOutputMessage,
        hints: MutableMap<String, Any>
    ): Mono<Void> {
        return inputStream.toMono().flatMap { entity ->
            val body = entity.body
            if (body != null) {
                val jsonBody = Json.encodeToString(body)
                val buffer = message.bufferFactory().wrap(jsonBody.toByteArray())
                message.headers.contentType = MediaType.APPLICATION_JSON
                message.writeWith(Mono.just(buffer))
            } else {
                Mono.error(EncodingException("Response body is null"))
            }
        }
    }
}