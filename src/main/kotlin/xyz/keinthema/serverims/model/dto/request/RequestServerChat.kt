package xyz.keinthema.serverims.model.dto.request

import kotlinx.serialization.Serializable
import xyz.keinthema.serverims.constant.RequestConst.Companion.CHAT_NAME_LENGTH_LIMIT

@Serializable
sealed interface RequestServerChat {
    fun isLegal(): Boolean
}

data class RequestCreateChat(
    val name: String
): RequestServerChat {
    override fun isLegal(): Boolean {
        return name.isNotEmpty() && name.length < CHAT_NAME_LENGTH_LIMIT
    }

}

data class RequestModifyChatInfo(
    val name: String
): RequestServerChat {
    override fun isLegal(): Boolean {
        return name.isNotEmpty() && name.length < CHAT_NAME_LENGTH_LIMIT
    }

}