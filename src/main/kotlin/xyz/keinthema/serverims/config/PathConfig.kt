package xyz.keinthema.serverims.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(PathProperties::class)
class PathConfig(val prop: PathProperties) {
}

@ConfigurationProperties(prefix = "spring.webflux")
data class PathProperties(
    val basePath: String
)