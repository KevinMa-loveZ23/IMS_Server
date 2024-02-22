package xyz.keinthema.serverims.model.dto.request

import kotlinx.serialization.Serializable
import xyz.keinthema.serverims.constant.RequestConst.Companion.DESCRIPTION_LENGTH_LIMIT
import xyz.keinthema.serverims.constant.RequestConst.Companion.HASHED_PASSWORD_LENGTH
import xyz.keinthema.serverims.constant.RequestConst.Companion.NAME_LENGTH_LIMIT
import xyz.keinthema.serverims.model.entity.Server

@Serializable
sealed interface RequestServer {
    fun isLegal(): Boolean
}

data class RequestCreateServer(
    val name: String,
    val description: String
): RequestServer {
    override fun isLegal(): Boolean {
        return name.length < NAME_LENGTH_LIMIT
                && description.length < DESCRIPTION_LENGTH_LIMIT
    }
}

data class RequestModifyServer(
    val serverModifiablePart: Server.Companion.ServerModifiablePart
): RequestServer {
    override fun isLegal(): Boolean {
        return serverModifiablePart.isLegal() ?: true
    }
    private fun Server.Companion.ServerModifiablePart.isLegal(): Boolean =
        (if (this.name != null) this.name.length < NAME_LENGTH_LIMIT else true)
                && (if (this.description != null) this.description.length < DESCRIPTION_LENGTH_LIMIT else true)
}

data class RequestDeleteServer(
    val hashedPw: String?
): RequestServer {
    override fun isLegal(): Boolean {
        return if (hashedPw != null) hashedPw.length == HASHED_PASSWORD_LENGTH else true
    }
}