@file:UseSerializers(LongAsStringSerializer::class)
package xyz.keinthema.serverims.model.dto.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.LongAsStringSerializer
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.MessageId
import xyz.keinthema.serverims.model.entity.Message

@Serializable
data class SimpleChatMessage(
    val id: MessageId,
    val userId: AccountId,
    val content: String,
    val type: Int
) {
    constructor(messageInDB: Message): this(
        id = messageInDB.id,
        userId = messageInDB.userId,
        content = messageInDB.content,
        type = messageInDB.type
    )
}
