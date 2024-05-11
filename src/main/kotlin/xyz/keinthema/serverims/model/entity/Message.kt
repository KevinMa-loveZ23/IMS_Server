package xyz.keinthema.serverims.model.entity

import kotlinx.serialization.Serializable
import org.springframework.data.annotation.Id
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.ChatId
import xyz.keinthema.serverims.constant.EntityConst
import xyz.keinthema.serverims.constant.MessageId
import xyz.keinthema.serverims.model.dto.message.MetaMessage

@Serializable
data class Message(
    @Id val id: MessageId,
    val chatId: ChatId,
    val userId: AccountId,
    val content: String,
    val type: Int
) {
    constructor(id: MessageId, userId: AccountId, chatId: ChatId, content: String): this(
        id = id,
        chatId = chatId,
        userId = userId,
        content = content,
        type = EntityConst.Companion.MessageType.TextMessage.ordinal
    )

    constructor(metaMessage: MetaMessage): this(
        id = metaMessage.id,
        chatId = metaMessage.chatId,
        userId = metaMessage.userId,
        content = metaMessage.content,
        type = metaMessage.type
    )
}
