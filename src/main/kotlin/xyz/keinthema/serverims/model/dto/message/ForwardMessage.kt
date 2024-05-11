@file:UseSerializers(LongAsStringSerializer::class)
package xyz.keinthema.serverims.model.dto.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.ChatId
import xyz.keinthema.serverims.constant.MessageId
import xyz.keinthema.serverims.constant.ServerId
import xyz.keinthema.serverims.model.entity.Message

@Serializable
data class ForwardMessage(
    val id: MessageId,
    val serverId: ServerId,
    val chatId: ChatId,
    val userId: AccountId,
    val content: String,
    val type: Int
) {
    constructor(metaMessage: MetaMessage): this(
        id = metaMessage.id,
        serverId = metaMessage.serverId,
        chatId = metaMessage.chatId,
        userId = metaMessage.userId,
        content = metaMessage.content,
        type = metaMessage.type
    )
    constructor(messageInDB: Message, serverId: ServerId): this(
        id = messageInDB.id,
        serverId = serverId,
        chatId = messageInDB.chatId,
        userId = messageInDB.userId,
        content = messageInDB.content,
        type = messageInDB.type
    )
    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}
