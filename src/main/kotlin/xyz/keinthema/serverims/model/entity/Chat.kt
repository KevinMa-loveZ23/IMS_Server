package xyz.keinthema.serverims.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

//@Document(collection = "")
data class Chat(
    @Id val id: Int,
    val name: String
)
