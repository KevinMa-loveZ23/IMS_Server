@file:UseSerializers(LongAsStringSerializer::class)
package xyz.keinthema.serverims.model.dto.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.keinthema.serverims.constant.ChatId
import xyz.keinthema.serverims.constant.MessageId
import xyz.keinthema.serverims.constant.ServerId

@Serializable
data class ResponseMessage(
    val messageId: MessageId,
    val serverId: ServerId,
    val chatId: ChatId,
//    val userId: AccountId,
    val content: String,
    val localId: Int,
//    val type: Int
) {
    companion object {
        fun void(): ResponseMessage {
            return ResponseMessage(
                messageId = -1L,
                serverId = -1L,
                chatId = -1,
                content = "",
                localId = -1
            )
        }
        fun fromStringContent(content: String): ResponseMessage {
            return ResponseMessage(
                messageId = -1L,
                serverId = -1L,
                chatId = -1,
                content = content,
                localId = -1
            )
        }
        fun fromMetaMessage(metaMessage: MetaMessage): ResponseMessage {
            return ResponseMessage(
                messageId = metaMessage.id,
                serverId = metaMessage.serverId,
                chatId = metaMessage.chatId,
                content = "",
                localId = metaMessage.localId
            )
        }
    }
    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}