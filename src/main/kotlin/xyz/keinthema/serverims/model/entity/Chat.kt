package xyz.keinthema.serverims.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import xyz.keinthema.serverims.constant.ChatId

//@Document(collection = "")
data class Chat(
    @Id val id: ChatId,
    val name: String
)
