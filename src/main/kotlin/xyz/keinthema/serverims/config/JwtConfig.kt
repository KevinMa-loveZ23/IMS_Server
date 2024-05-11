package xyz.keinthema.serverims.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig(val prop: JwtProperties) {
    //
}

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val issuer: String,
)