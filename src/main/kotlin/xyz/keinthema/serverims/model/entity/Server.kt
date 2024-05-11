package xyz.keinthema.serverims.model.entity

import kotlinx.serialization.Serializable
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Update
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.EntityConst
import xyz.keinthema.serverims.constant.ServerId
import xyz.keinthema.serverims.constant.ServiceConst.Companion.SERVER_COLL_NAME

//typealias AccountId = Long
//typealias ChatId = Int

@Document(collection = SERVER_COLL_NAME)
data class Server(
    @Id val id: ServerId,
    val name: String,
    val owner: AccountId,
    val admins: MutableSet<AccountId>,
//    val usersRecord: MutableSet<UserRecord>,
//    val usersRecord: Map<AccountId, Map<ChatId, Long>>,
    val userList: MutableSet<AccountId>,
    val description: String,
    val chatList: MutableSet<Chat>,
//    val entryStrategies: MutableMap<ServerEntryStrategy.StrategyType, ServerEntryStrategy>,
    val entryMessageList: MutableSet<String>,
    val entryWaitingTimeInMsec: Long
) {
    constructor(id: Long, name: String, creatorId: Long, description: String): this(
        id = id,
        name = name,
        owner = creatorId,
        admins = mutableSetOf(creatorId),
//        usersRecord = mutableSetOf(UserRecord(creatorId)),
//        usersRecord = mapOf(Pair(creatorId, mapOf())),
        userList = mutableSetOf(creatorId),
        description = description,
        chatList = mutableSetOf(
            Chat(id = 0, name = EntityConst.DEFAULT_CHAT_NAME)
        ),
//        entryStrategies = mutableMapOf(
//            Pair(
//            ServerEntryStrategy.StrategyType.AdminCheck, AdminCheckStrategy()
//        )
//        )
        entryMessageList = mutableSetOf(),
        entryWaitingTimeInMsec = EntityConst.WAITING_TIME_IN_MSEC
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
//            val addAdmin: MutableSet<Long>?,
//            val minusAdmin: MutableSet<Long>?,
            val description: String?
        ) {
            constructor(server: Server): this(
                name = server.name,
                owner = server.owner,
//                addAdmin = null,
//                minusAdmin = null,
                description = server.description
            )

            fun getUpdateObj(): Update {
                val update = Update()
                if (name != null) update.set("name", name)
                if (owner != null) update.set("owner", owner)
//                if (addAdmin != null) update.addToSet("admins").each(addAdmin)
//                if (minusAdmin != null) update.pullAll("admins", minusAdmin.toTypedArray())
                if (description != null) update.set("description", description)
                return update
            }
        }

//        @Serializable
//        data class UserRecord(
//            val userId: Long,
//            val browsingHistory: MutableSet<Pair<Int, Long>>
//        ) {
//            constructor(userId: Long): this(
//                userId = userId,
//                browsingHistory = mutableSetOf()
//            )
//        }
    }
    fun isVoid() = this.id == -1L
}