package xyz.keinthema.serverims.service.intf

import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import xyz.keinthema.serverims.constant.*
import xyz.keinthema.serverims.model.dto.message.ForwardMessage
import xyz.keinthema.serverims.model.dto.message.MetaMessage
import xyz.keinthema.serverims.model.dto.message.SimpleChatMessage
import xyz.keinthema.serverims.model.entity.Chat


interface MessageService {

//    val userSessionList: ConcurrentHashMap<AccountId, Sinks.Many<String>>
//    val serverSessionList: ConcurrentHashMap<ServerId, Sinks.Many<String>>

    fun sessionOnline(userId: AccountId, session: WebSocketSession, sinks: Sinks.Many<String>): Mono<Unit>
    fun sessionOffline(userId: AccountId, session: WebSocketSession)
    fun subscribeServer(userId: AccountId, serverId: ServerId)
    fun unsubscribeServer(userId: AccountId, serverId: ServerId)

//    fun processNewSession(session: WebSocketSession, userId: AccountId)
    fun processNewMessage(
        localId: Int,
        serverId: ServerId,
        chatId: ChatId,
        userId: AccountId,
        content: String,
        type: Int
    ): Mono<MetaMessage>

    fun newMediaMessage(
        messageId: MessageId,
        serverId: ServerId,
        chatId: ChatId,
        userId: AccountId,
        fileName: String
    ): MetaMessage

    //    fun processNewTextMessage(
//        serverId: ServerId,
//        chatId: ChatId,
//        userId: AccountId,
//        content: String
//    ): Mono<MetaMessage> = processNewMessage(
//        serverId = serverId,
//        chatId = chatId,
//        userId = userId,
//        content = content,
//        type = EntityConst.Companion.MessageType.TextMessage.ordinal
//    )
    fun storeMessage(
        metaMessage: MetaMessage
    ): Mono<Void>
    fun sendMessageByServer(
        metaMessage: MetaMessage,
        excludedSessionSinks: Sinks.Many<String>?
    ): Mono<Void>
//    fun sendMessageByUser(
//        metaMessage: MetaMessage,
//        accountId: AccountId
//    ): Mono<Void>

    fun createChatInServer(
        serverId: ServerId,
        chatName: String
    ): Mono<ChatId>
    fun getChatListOfServer(
        serverId: ServerId
    ): Mono<List<Chat>>
    fun modifyChatInfoInServer(
        serverId: ServerId,
        chatId: ChatId,
        chatName: String
    ): Mono<Boolean>
    fun deleteChatInServer(
        serverId: ServerId,
        chatId: ChatId
    ): Mono<Boolean>

    fun getMessageByServerChat(
        serverId: ServerId,
        chatId: ChatId,
        beforeTimestampInMsec: Long,
        number: Int
    ): Mono<List<SimpleChatMessage>>

    fun saveFile(serverId: ServerId, filePart: FilePart): Mono<Pair<MessageId, String>>
    fun getFile(serverId: ServerId, fileName: String): Mono<Pair<ByteArray, MediaType?>>
    fun deleteAllFiles(serverId: ServerId): Mono<Void>

    fun isLegalToMessaging(serverId: ServerId, userId: AccountId): Mono<Boolean>
    fun isLegalToChangeChat(
        serverId: ServerId,
        userId: AccountId
    ): Mono<Boolean>
}