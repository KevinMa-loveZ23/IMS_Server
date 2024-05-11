@file:UseSerializers(LongAsStringSerializer::class)
package xyz.keinthema.serverims.model.dto.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import xyz.keinthema.serverims.constant.*

@Serializable
data class MetaMessage(
    val id: MessageId,
    val localId: Int,
    val serverId: ServerId,
    val chatId: ChatId,
    val userId: AccountId,
    val content: String,
    val type: Int
) {
    fun isTextMessage(): Boolean = type == EntityConst.Companion.MessageType.TextMessage.ordinal
//    fun isSubscribeMessage(): Boolean = type == EntityConst.Companion.MessageType.SubscribeMessage.ordinal
//    fun getSubscribeServer(): ServerId {
//        return if (isSubscribeMessage()) content.toLong() else -1L
//    }
}