package xyz.keinthema.serverims.handler

import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest
import xyz.keinthema.serverims.model.dto.response.AccountInfoBody
import xyz.keinthema.serverims.model.entity.Account

@SpringBootTest
class ResponseBodyJsonMessageWriterTest {

    @Test
    fun write() {
        println(
//            Json {
//        serializersModule = SerializersModule {
//            contextual(Long::class, LongAsStringSerializer)
//        }
////            serializersModule = serializersModuleOf(Long::class, ResponseBodyJsonMessageWriter.LongAsStringSerializer)
//        }
            Json.encodeToString(
                AccountInfoBody(
                    0,
                    "aaa"
                )
            )
        )
    }
}