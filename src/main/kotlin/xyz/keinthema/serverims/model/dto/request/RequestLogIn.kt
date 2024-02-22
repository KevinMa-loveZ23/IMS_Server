package xyz.keinthema.serverims.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestLogIn(
    val id: Long,
    val hashedPw: String
)
