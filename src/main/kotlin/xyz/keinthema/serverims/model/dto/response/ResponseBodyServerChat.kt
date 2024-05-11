package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable
import xyz.keinthema.serverims.constant.ChatId
import xyz.keinthema.serverims.constant.MessageId
import xyz.keinthema.serverims.model.dto.message.ForwardMessage
import xyz.keinthema.serverims.model.dto.message.SimpleChatMessage
import xyz.keinthema.serverims.model.entity.Chat

@Serializable
sealed interface ResponseBodyServerChat<T>: ResponseDataBody<T> {
}

data class ImageUploadBody(
    val messageId: MessageId?,
    val fileName: String
): ResponseBodyServerChat<ImageUploadBody> {
    companion object {
        fun void(): ImageUploadBody {
            return ImageUploadBody(null, "")
        }
    }
    override fun isVoid(): Boolean = messageId == null
}

data class ChatCreateBody(
    val chatId: ChatId
): ResponseBodyServerChat<ChatCreateBody> {
    companion object {
        fun void(): ChatCreateBody {
            return ChatCreateBody(-1)
        }
    }
    override fun isVoid(): Boolean = chatId == -1
}

data class ListChatGetBody(
    val chatNameList: List<Chat>
): ResponseBodyServerChat<ListChatGetBody> {
    companion object {
        fun void(): ListChatGetBody {
            return ListChatGetBody(listOf())
        }
    }
    override fun isVoid(): Boolean = chatNameList.isEmpty()
}

data class InfoChatModifyBody(
    val chatName: String
): ResponseBodyServerChat<InfoChatModifyBody> {
    companion object {
        fun void(): InfoChatModifyBody {
            return InfoChatModifyBody("")
        }
    }
    override fun isVoid(): Boolean = chatName.isEmpty()
}

data class ChatDeleteBody(
    val success: Boolean
): ResponseBodyServerChat<ChatDeleteBody> {
    companion object {
        fun void(): ChatDeleteBody {
            return ChatDeleteBody(false)
        }
    }
    override fun isVoid(): Boolean = !success
}

data class MessageHistoryGetBody(
    val messageList: List<SimpleChatMessage>
): ResponseBodyServerChat<MessageHistoryGetBody> {
    companion object {
        fun void(): MessageHistoryGetBody {
            return MessageHistoryGetBody(listOf())
        }
    }
    override fun isVoid(): Boolean = messageList.isEmpty()
}