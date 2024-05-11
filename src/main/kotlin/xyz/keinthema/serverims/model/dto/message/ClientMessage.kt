@file:UseSerializers(LongAsStringSerializer::class)
package xyz.keinthema.serverims.model.dto.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.LongAsStringSerializer
import xyz.keinthema.serverims.constant.ChatId
import xyz.keinthema.serverims.constant.ServerId

@Serializable
data class ClientMessage(
    val serverId: ServerId,
    val chatId: ChatId,
    val localId: Int,
    val content: String,
    val type: Int
)