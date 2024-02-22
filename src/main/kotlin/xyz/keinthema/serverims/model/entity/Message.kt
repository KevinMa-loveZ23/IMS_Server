package xyz.keinthema.serverims.model.entity

import org.springframework.data.annotation.Id

data class Message(
    @Id val id: Long,
    val chatId: Int,
    val content: String,
    val type: Int = 0
)
