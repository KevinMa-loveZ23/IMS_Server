package xyz.keinthema.serverims.model.entity

import kotlinx.serialization.Serializable
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "servers")
data class Server(
    @Id val id: Long,
    val name: String,
    val owner: Long,
    val admins: MutableSet<Long>,
    val usersId: MutableSet<Long>,
    val description: String,
    val chatList: MutableSet<Chat>
) {
    constructor(id: Long, name: String, creatorId: Long, description: String): this(
        id = id,
        name = name,
        owner = creatorId,
        admins = mutableSetOf(creatorId),
        usersId = mutableSetOf(creatorId),
        description = description,
        chatList = mutableSetOf()
    )
    companion object {
        fun void() = Server(
            id = -1L,
            name = "VOID",
            creatorId = -1L,
            description = ""
        )
        @Serializable
        data class ServerModifiablePart(
            val name: String?,
            val owner: Long?,
            val addAdmin: MutableSet<Long>?,
            val minusAdmin: MutableSet<Long>?,
            val description: String?
        ) {
            constructor(server: Server): this(
                name = server.name,
                owner = server.owner,
                addAdmin = null,
                minusAdmin = null,
                description = server.description
            )
        }
    }
    fun isVoid() = this.id == -1L
}