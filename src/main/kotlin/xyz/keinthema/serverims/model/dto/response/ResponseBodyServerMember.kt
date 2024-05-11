package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable
import xyz.keinthema.serverims.constant.AccountId

@Serializable
sealed interface ResponseBodyServerMember<T>: ResponseDataBody<T> {
}

data class ServerJoinRequestBody(
    val success: Boolean
): ResponseBodyServerMember<ServerJoinRequestBody> {
    companion object {
        fun void(): ServerJoinRequestBody {
            return ServerJoinRequestBody(false)
        }
    }
    override fun isVoid(): Boolean = !success
}

data class ListWaitingGetBody(
    val waitingList: MutableMap<AccountId, String>?
): ResponseBodyServerMember<ListWaitingGetBody> {
    companion object {
        fun void(): ListWaitingGetBody {
            return ListWaitingGetBody(null)
        }
    }

    override fun isVoid(): Boolean = waitingList == null
}

data class ServerJoinAllowBody(
    val success: Boolean
): ResponseBodyServerMember<ServerJoinAllowBody> {
    companion object {
        fun void(): ServerJoinAllowBody {
            return ServerJoinAllowBody(false)
        }
    }

    override fun isVoid(): Boolean = !success
}

data class MembersGetBody(
    val userIdList: List<AccountId>
): ResponseBodyServerMember<MembersGetBody> {
    companion object {
        fun void(): MembersGetBody {
            return MembersGetBody(listOf())
        }
    }

    override fun isVoid(): Boolean = userIdList.isEmpty()
}

data class MemberDeleteBody(
    val success: Boolean
): ResponseBodyServerMember<MemberDeleteBody> {
    companion object {
        fun void(): MemberDeleteBody {
            return MemberDeleteBody(false)
        }
    }

    override fun isVoid(): Boolean = !success
}

data class MemberMultiDeleteBody(
    val success: Boolean
): ResponseBodyServerMember<MemberMultiDeleteBody> {
    companion object {
        fun void(): MemberMultiDeleteBody {
            return MemberMultiDeleteBody(false)
        }
    }

    override fun isVoid(): Boolean = !success
}

data class ListMessageEntryGetBody(
    val entryMessageList: List<String>
): ResponseBodyServerMember<ListMessageEntryGetBody> {
    companion object {
        fun void(): ListMessageEntryGetBody {
            return ListMessageEntryGetBody(listOf())
        }
    }

    override fun isVoid(): Boolean = entryMessageList.isEmpty()
}

data class MessageEntryAddBody(
    val success: Boolean
): ResponseBodyServerMember<MessageEntryAddBody> {
    companion object {
        fun void(): MessageEntryAddBody {
            return MessageEntryAddBody(false)
        }
    }

    override fun isVoid(): Boolean = !success
}

data class MessageEntryDeleteBody(
    val success: Boolean
): ResponseBodyServerMember<MessageEntryDeleteBody> {
    companion object {
        fun void(): MessageEntryDeleteBody {
            return MessageEntryDeleteBody(false)
        }
    }

    override fun isVoid(): Boolean = !success
}