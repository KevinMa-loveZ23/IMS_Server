package xyz.keinthema.serverims.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {
    @Bean
    fun jacksonMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.serializerByType(Long::class.java, ToStringSerializer.instance)
                .serializerByType(MutableSet::class.java, CustomSetSerializer())
//                .serializerByType(Pair::class.java, CustomPairSerializer())
        }
    }

    class CustomSetSerializer : JsonSerializer<Set<*>>() {
        override fun serialize(value: Set<*>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
            if (value != null) {
//                val newSet = value.mapNotNull { it as? Long }
//                    .map { it.toString() }
//                    .toSet()
//                serializers?.defaultSerializeValue(newSet, gen)
                gen?.writeStartArray()
                for (item in value) {
                    if (item is Long) {
                        gen?.writeString(item.toString())
                    } else if (item is Pair<*, *> && item.first is Long && item.second is String) {
//                        gen?.writeStartArray()
                        gen?.writeString(item.first.toString())
                        gen?.writeString(item.second.toString())
//                        gen?.writeEndArray()
                    }
                }
                gen?.writeEndArray()
            }
        }
    }

//    class CustomPairSerializer : JsonSerializer<Pair<Long, String>>() {
//        override fun serialize(value: Pair<Long, String>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
//            if (value != null) {
//                serializers?.defaultSerializeValue(Pair(value.first.toString(), value.second), gen)
//            }
//        }
//    }

}