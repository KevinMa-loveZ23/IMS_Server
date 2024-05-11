package xyz.keinthema.serverims.service.impl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.kotlin.core.publisher.toMono
import xyz.keinthema.serverims.constant.*
import xyz.keinthema.serverims.constant.ServiceConst.Companion.SERVER_COLL_NAME
import xyz.keinthema.serverims.handler.SnowflakeHandler
import xyz.keinthema.serverims.model.dto.message.ForwardMessage
import xyz.keinthema.serverims.model.dto.message.MetaMessage
import xyz.keinthema.serverims.model.dto.message.SimpleChatMessage
import xyz.keinthema.serverims.model.entity.Chat
import xyz.keinthema.serverims.model.entity.Message
import xyz.keinthema.serverims.model.entity.Server
import xyz.keinthema.serverims.repository.MinioRepository
import xyz.keinthema.serverims.service.intf.AccountService
import xyz.keinthema.serverims.service.intf.MessageService
import xyz.keinthema.serverims.service.intf.ServerService
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.util.concurrent.ConcurrentHashMap

@Service
class MessageServiceImpl(
    private val snowflakeHandler: SnowflakeHandler,
//    private val redisTemplate: ReactiveRedisTemplate<String,String>,
    private val accountService: AccountService,
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    @Qualifier("chatMongoTemplate") private val chatMongoTemplate: ReactiveMongoTemplate,
    private val serverService: ServerService,
    private val minioRepository: MinioRepository
): MessageService {
    private val userSessionList: ConcurrentHashMap<AccountId, Sinks.Many<String>> = ConcurrentHashMap()
    private val serverSessionList: ConcurrentHashMap<ServerId, Sinks.Many<String>> = ConcurrentHashMap()

    private val sessionContextList = SessionContextList()

    class SessionContextList {

        data class UserContext(
            val serverSet: MutableSet<ServerId>,
            val sessionMap: MutableMap<WebSocketSession, Sinks.Many<String>>
        )

        private val listOfSessionsByUser:
                ConcurrentHashMap<AccountId, UserContext> =
            ConcurrentHashMap()
        private val listOfUserIdByServer:
                ConcurrentHashMap<ServerId, MutableSet<AccountId>> =
            ConcurrentHashMap()
//        fun isUserOnline(userId: AccountId): Boolean = !(listOfSessionsByUser[userId].isNullOrEmpty())
        fun getServerSetByUser(userId: AccountId): Set<ServerId>? {
            return listOfSessionsByUser[userId]?.serverSet?.toSet()
        }
        fun addNewSession(
            userId: AccountId,
            session: WebSocketSession,
            sinks: Sinks.Many<String>,
            serverSet: Set<ServerId>
        ) {
//            val newSinks = Sinks.many().multicast().directBestEffort<String>()
            val sessionsByUser = listOfSessionsByUser[userId]
            if (sessionsByUser != null) {
                sessionsByUser.sessionMap[session] = sinks
            } else {
                listOfSessionsByUser[userId] =
                    UserContext(serverSet.toMutableSet(), mutableMapOf(Pair(session, sinks)))
            }
            serverSet.forEach {
                val serverInfo = listOfUserIdByServer[it]
                if (serverInfo == null) {
                    listOfUserIdByServer[it] = mutableSetOf(userId)
                } else {
                    serverInfo.add(userId)
                }
            }
//            return sinks.asFlux()
        }
        fun removeSession(
            userId: AccountId,
            session: WebSocketSession
        ) {
            listOfSessionsByUser[userId]?.sessionMap?.remove(session)
            if (listOfSessionsByUser[userId]?.sessionMap.isNullOrEmpty()) {
                val serverSet = listOfSessionsByUser[userId]?.serverSet ?: mutableSetOf()
                serverSet.forEach {
                    listOfUserIdByServer[it]?.remove(userId)
                }
                listOfSessionsByUser.remove(userId)
            }
        }
        fun addNewServerToUser(userId: AccountId, serverId: ServerId) {
            listOfSessionsByUser[userId]?.serverSet?.add(serverId)
            listOfUserIdByServer[serverId]?.add(userId)
        }
        fun removeServerFromUser(userId: AccountId, serverId: ServerId) {
            listOfSessionsByUser[userId]?.serverSet?.remove(serverId)
            listOfUserIdByServer[serverId]?.remove(userId)
        }

        fun sendMessageByServer(metaMessage: MetaMessage, excludedSessionSinks: Sinks.Many<String>?): Mono<Void> {
            val forwardMessageJson = ForwardMessage(metaMessage =  metaMessage).toJsonString()
            return Flux.fromIterable(
                listOfUserIdByServer[metaMessage.serverId] ?: emptySet()
            ).map { accountId ->
                if (accountId == metaMessage.userId) {
                    listOfSessionsByUser[accountId]?.sessionMap?.values
                        ?.forEach {
                            if (it != excludedSessionSinks) {
                                it.tryEmitNext((forwardMessageJson))
                            }
                        }
                } else {
                    listOfSessionsByUser[accountId]?.sessionMap?.values
                        ?.forEach { it.tryEmitNext(forwardMessageJson) }
                }
            }.then()

        }
    }

    override fun sessionOnline(userId: AccountId, session: WebSocketSession, sinks: Sinks.Many<String>): Mono<Unit> {
        val serverSetFromContext = sessionContextList.getServerSetByUser(userId)
        val serverSetMono: Mono<Set<ServerId>> =
            if (serverSetFromContext == null) {
                accountService.getAccountById(userId).map { it?.servers ?: setOf() }
            } else {
                Mono.just(serverSetFromContext)
            }
        return serverSetMono.map { set ->
            sessionContextList.addNewSession(
                userId = userId, session = session, sinks = sinks, serverSet = set
            )
        }
    }

    override fun sessionOffline(userId: AccountId, session: WebSocketSession) {
        sessionContextList.removeSession(userId = userId, session = session)
    }


    override fun subscribeServer(userId: AccountId, serverId: ServerId) {
        sessionContextList.addNewServerToUser(userId = userId, serverId = serverId)
    }

    override fun unsubscribeServer(userId: AccountId, serverId: ServerId) {
        sessionContextList.removeServerFromUser(userId = userId, serverId = serverId)
    }

//    override fun processNewSession(session: WebSocketSession, userId: AccountId) {
//        TODO("Not yet implemented")
//    }

    override fun processNewMessage(
        localId: Int,
        serverId: ServerId,
        chatId: ChatId,
        userId: AccountId,
        content: String,
        type: Int
    ): Mono<MetaMessage> {
        return snowflakeHandler.getSnowflakeId()
            .map {
                val meta = MetaMessage(
                    id = it,
                    localId = localId,
                    serverId = serverId,
                    chatId = chatId,
                    userId = userId,
                    content = content,
                    type = type
                )
//                redisTemplate.opsForValue()
//                    .set("", content, Duration.ofMinutes(30L))
                meta
            }
    }

    override fun newMediaMessage(
        messageId: MessageId,
        serverId: ServerId,
        chatId: ChatId,
        userId: AccountId,
        fileName: String
    ): MetaMessage {
        return MetaMessage(
            id = messageId,
            localId = 0,
            serverId = serverId,
            chatId = chatId,
            userId = userId,
            content = fileName,
            type = EntityConst.Companion.MessageType.MediaMessage.ordinal
        )
    }

    override fun storeMessage(metaMessage: MetaMessage): Mono<Void> {
        return chatMongoTemplate.save(
            Message(metaMessage = metaMessage),
            metaMessage.serverId.toString()
        ).then()
    }

    override fun sendMessageByServer(metaMessage: MetaMessage, excludedSessionSinks: Sinks.Many<String>?): Mono<Void> {
        return sessionContextList.sendMessageByServer(metaMessage = metaMessage, excludedSessionSinks = excludedSessionSinks)
    }

    override fun createChatInServer(serverId: ServerId, chatName: String): Mono<ChatId> {
        return serverService.getServerById(serverId)
            .flatMap { server ->
                if (server == null) {
                    Mono.just(-1)
                } else {
                    val newChatId = server.chatList.last().id + 1
                    reactiveMongoTemplate.findAndModify(
                        Query(Criteria.where("id").`is`(serverId)),
                        Update().push("chatList", Chat(newChatId, chatName)),
                        FindAndModifyOptions.options().returnNew(true),
                        Server::class.java,
                        SERVER_COLL_NAME
                    ).map { newChatId }
                }
            }
    }

    override fun getChatListOfServer(serverId: ServerId): Mono<List<Chat>> {
        return serverService.getServerById(serverId)
            .map { server ->
                server?.chatList?.toList() ?: listOf()
            }
    }

    override fun modifyChatInfoInServer(serverId: ServerId, chatId: ChatId, chatName: String): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .flatMap { server ->
                if (server == null) {
                    Mono.just(false)
                } else {
                    val chats = server.chatList.filter { it.id == chatId }
                    if (chats.isEmpty() || chats.size > 1) {
                        Mono.just(false)
                    } else {
                        val chat = chats[0]
                        reactiveMongoTemplate.findAndModify(
                            Query(Criteria.where("id").`is`(serverId).and("chatList.id").`is`(chat.id)),
//                            Update().pull("chatList", chat).push("chatList", Chat(chat.id, chatName)),
                            Update().set("chatList.$.name", chatName),
                            FindAndModifyOptions.options().returnNew(true),
                            Server::class.java,
                            SERVER_COLL_NAME
                        ).map { it !== null }
                    }

                }
            }
    }

    override fun deleteChatInServer(serverId: ServerId, chatId: ChatId): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .flatMap { server ->
                if (server == null) {
                    Mono.just(false)
                } else {
                    val chats = server.chatList.filter { it.id == chatId }
                    val chatsArray = chats.toTypedArray()
                    reactiveMongoTemplate.findAndModify(
                        Query(Criteria.where("id").`is`(serverId)),
                        Update().pullAll("chatList", chatsArray),
                        FindAndModifyOptions.options().returnNew(true),
                        Server::class.java,
                        SERVER_COLL_NAME
                    ).map { it != null }
                }
            }
    }

//    override fun sendMessageByUser(metaMessage: MetaMessage, accountId: AccountId): Mono<Void> {
//        TODO("Not yet implemented")
//    }

    override fun getMessageByServerChat(
        serverId: ServerId,
        chatId: ChatId,
        beforeTimestampInMsec: Long,
        number: Int
    ): Mono<List<SimpleChatMessage>> {
        val correspondSnowflake = SnowflakeHandler.getMinSnowflake(beforeTimestampInMsec)
        val query = Query()
        query
            .addCriteria(Criteria.where("chatId").`is`(chatId))
            .addCriteria(Criteria.where("id").lt(correspondSnowflake))
            .with(Sort.by(Sort.Direction.DESC, "id"))
            .limit(number)
        return chatMongoTemplate.find(
            query,
            Message::class.java,
            serverId.toString()
        )
            .map { SimpleChatMessage(it) }
            .collectList()
    }
    private val basePath = Paths.get("").toAbsolutePath()
    override fun saveFile(serverId: ServerId, filePart: FilePart): Mono<Pair<MessageId, String>> {
        return snowflakeHandler.getSnowflakeId()
            .flatMap { id ->
//                val mimeType = MediaType.parseMediaType(filePart.headers().contentType.toString())
//                val extension = mimeType.subtype
//                val fileName = "$id.$extension"
                val fileName = "$id-${filePart.filename()}"
                /**
                 * old below
                 */
//                val filePath = "$IMAGE_DIR$fileName"
//                val filePath = Paths.get("$serverId/$fileName")
//                //
//                val serverPath = basePath.resolve(serverId.toString())
//                Files.createDirectories(serverPath)
//                val filePath = serverPath.resolve(fileName)
//                filePart.transferTo(filePath)
//                    .thenReturn(Pair(id, fileName))
                /**
                 * new below
                 */
                val fileBucketName = minioRepository.getBucketNameFromLong(serverId)
                minioRepository.putObject(filePart = filePart, bucketName = fileBucketName, fileName = fileName)
                    .thenReturn(Pair(id, fileName))
            }
    }

    override fun getFile(serverId: ServerId, fileName: String): Mono<Pair<ByteArray, MediaType?>> {
        /**
         * old below
         */
//        val filePath = "$IMAGE_DIR$fileName"
//        val filePathStr = "$serverId/$fileName"
//        val filePath = Paths.get(filePathStr)
//        //
//        val serverPath = basePath.resolve(serverId.toString())
//        val filePath = serverPath.resolve(fileName)
//        return Mono.fromCallable {
//            if (Files.exists(filePath)) {
//                val mediaType = MediaType.parseMediaType(Files.probeContentType(filePath))
//                val bytes = Files.readAllBytes(filePath)
//                Pair(bytes, mediaType)
//            } else {
//                Pair(byteArrayOf(), null)
//            }
//        }
        /**
         * new below
         */
        val fileBucketName = minioRepository.getBucketNameFromLong(serverId)
        return minioRepository.getObject(bucketName = fileBucketName, fileName = fileName)
    }

//    /**
//     * Object Storage with server ID partition?
//     * Not Designed and Not Finished
//     */
    override fun deleteAllFiles(serverId: ServerId): Mono<Void> {
        /**
         * old below
         */
//        val serverPath = basePath.resolve(serverId.toString())
//        return Files.list(serverPath)
//            .forEach { path -> Files.delete(path) }
//            .toMono().then()
        /**
         * new below
         */
        return Mono.fromCallable {
            minioRepository.removeBucket(minioRepository.getBucketNameFromLong(serverId))
        }.then()
    }

    override fun isLegalToMessaging(serverId: ServerId, userId: AccountId): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .map { server ->
                server?.userList?.contains(userId) ?: false
            }
    }

    override fun isLegalToChangeChat(serverId: ServerId, userId: AccountId): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .map { server ->
                server?.admins?.contains(userId) ?: false
            }
    }
}