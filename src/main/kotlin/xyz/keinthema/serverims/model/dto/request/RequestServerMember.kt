package xyz.keinthema.serverims.model.dto.request

import kotlinx.serialization.Serializable
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.RequestConst.Companion.JOIN_SERVER_MESSAGE_LENGTH_LIMIT

@Serializable
sealed interface RequestServerMember {
    fun isLegal(): Boolean
}

data class RequestJoinServer(
    val message: String
): RequestServerMember {
    override fun isLegal(): Boolean {
        return message.length < JOIN_SERVER_MESSAGE_LENGTH_LIMIT
    }
}

data class RequestDeleteMultiMembers(
    val userIdList: List<AccountId>
): RequestServerMember {
    override fun isLegal(): Boolean {
        return userIdList.isNotEmpty()
    }
}