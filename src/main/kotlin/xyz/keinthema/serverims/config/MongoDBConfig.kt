package xyz.keinthema.serverims.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.bson.UuidRepresentation
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

@Configuration
//@PropertySource("classpath:mongodb.properties")
@EnableConfigurationProperties(MongoProperties::class)
class MongoDBConfig(val prop: MongoProperties): AbstractReactiveMongoConfiguration() {
    override fun getDatabaseName(): String {
        return prop.database
    }

    override fun reactiveMongoClient(): MongoClient {
        val clientSettings = MongoClientSettings.builder()
            .applyConnectionString(
                ConnectionString("mongodb://${prop.host}:${prop.port}/${prop.database}")
            )
            .uuidRepresentation(prop.uuidRepresentation)
//            .uuidRepresentation(UuidRepresentation.STANDARD)
            .build()
        return MongoClients.create(clientSettings)
    }

    @Bean
    @Primary
    fun reactiveMongoTemplate(): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(reactiveMongoClient(), databaseName)
    }

    @Bean
    fun chatMongoTemplate(): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(
            MongoClients.create(
                ConnectionString("mongodb://${prop.chat.host}" +
                        ":${prop.chat.port}/${prop.chat.database}")
            ),
            prop.chat.database
        )
    }
}

@ConfigurationProperties(prefix = "spring.data.mongodb")
data class MongoProperties(
    val host: String,
    val port: String,
    val database: String,
    val uuidRepresentation: UuidRepresentation,
    val chat: ChatDBProperties
) {
    companion object {
        data class ChatDBProperties(
            val host: String,
            val port: String,
            val database: String
        )
    }
}