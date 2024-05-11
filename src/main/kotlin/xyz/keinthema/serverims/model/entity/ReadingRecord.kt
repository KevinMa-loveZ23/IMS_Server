package xyz.keinthema.serverims.model.entity

import org.springframework.data.annotation.Id
import xyz.keinthema.serverims.constant.ChatId


//typealias ChatId = Int
data class ReadingRecord(
    @Id val userId: Long,
    val record: Map<ChatId, Long>
)
