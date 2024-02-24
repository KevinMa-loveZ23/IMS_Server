package xyz.keinthema.serverims.model.dto.request

import kotlinx.serialization.Serializable
import xyz.keinthema.serverims.constant.RequestConst.Companion.HASHED_PASSWORD_LENGTH

@Serializable
sealed interface RequestAuth {
    fun isLegal(): Boolean
}

data class RequestLogIn(
    val id: Long,
    val hashedPw: String,
    val temporaryLogIn: Boolean
): RequestAuth {
    override fun isLegal(): Boolean {
        return hashedPw.length == HASHED_PASSWORD_LENGTH
    }
}

