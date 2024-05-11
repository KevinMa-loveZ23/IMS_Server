package xyz.keinthema.serverims.handler

import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.EntityConst
import xyz.keinthema.serverims.constant.ServerId
import xyz.keinthema.serverims.constant.WebSocketConst
import xyz.keinthema.serverims.constant.WebSocketConst.Companion.getIdAndToken
import xyz.keinthema.serverims.model.dto.message.ClientMessage
import xyz.keinthema.serverims.model.dto.message.ForwardMessage
import xyz.keinthema.serverims.model.dto.message.MetaMessage
import xyz.keinthema.serverims.model.dto.message.ResponseMessage
import xyz.keinthema.serverims.service.intf.MessageService
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebSocketHandler(
    private val webSocketConst: WebSocketConst,
    private val messageService: MessageService
): WebSocketHandler {

//    private val userSessions: ConcurrentHashMap<AccountId, SessionList> = ConcurrentHashMap()

//    private val serverSessions: ConcurrentHashMap<ServerId, SessionList> = ConcurrentHashMap()

//    data class SessionContext(
//        val session: WebSocketSession,
//        val sinks: Sinks.Many<String>,
//        val userIdOfSession: AccountId,
//        var serverIdOfSession: ServerId
//    ) {}

//    data class SessionList(
//        val sessionList: MutableMap<WebSocketSession, Sinks.Many<String>>
//    ) {
//        constructor(): this(
//            sessionList = mutableMapOf()
//        )
//        fun addNewSession(session: WebSocketSession): Flux<String> {
//            val tempSinks = Sinks.many().multicast().directBestEffort<String>()
//            sessionList[session] = tempSinks
//            return tempSinks.asFlux()
//        }
//        fun removeSession(session: WebSocketSession) {
//            sessionList.remove(session)
//        }
//        fun isEmpty(): Boolean {
//            return sessionList.isEmpty()
//        }
//    }

    override fun handle(session: WebSocketSession): Mono<Void> {
//        val uuid = session.handshakeInfo.headers.getFirst(HttpHeaders.AUTHORIZATION)
//        println("create handler: ${session.handshakeInfo.headers["Sec-WebSocket-Protocol"]?.get(0)}")

//        val claims: Jws<Claims> =
//            (session.handshakeInfo.attributes[JWT_CLAIMS_ATTR_NAME] as? Jws<Claims>) ?: return session.close()
//        val userIdFromClaims = claims.payload.subject.toLong()
        val userIdFromHandshake = getIdAndToken(session.handshakeInfo.headers["Sec-WebSocket-Protocol"]?.get(0))
            ?.first ?: return session.close()
//        println("id from claims is $userIdFromClaims")
        try {
//            val uriTemplateMatcher = UriTemplate(webSocketConst.websocketPathWithUserId).match(session.handshakeInfo.uri.path)
//            val uriTemplateMatcher = UriTemplate(webSocketConst.websocketPath).match(session.handshakeInfo.uri.path)

//            val userIdStr = uriTemplateMatcher[webSocketConst.userIdStr] ?: return session.close()
//            val userId: Long = userIdStr.toLong()
            val userId: Long = userIdFromHandshake
//            userSessions.computeIfAbsent(userId) {
//                SessionList()
//            }
//            val userFluxToSub = userSessions[userId]?.addNewSession(session) ?: return session.close()

//            val sessionContext = SessionContext(
//                session = session,
//                sinks = Sinks.many().multicast().directBestEffort<String>(),
//                userIdOfSession = userId,
//                serverIdOfSession = -1L
//            )

            val sessionSinks = Sinks.many().multicast().directBestEffort<String>()
            val sessionFlux = sessionSinks.asFlux()

            messageService
                .sessionOnline(userId = userId, session = session, sinks = sessionSinks)
                .subscribe()

            return session.receive()
                .doOnNext { rawMessage ->
                    val message = rawMessage.payloadAsText
                    val msg = Json.decodeFromString<ClientMessage>(message)
//                    val metaMessage = MetaMessage(
//                        id = snowflakeHandler.getSnowflakeId(),
//                        serverId = msg.serverId,
//                        chatId = msg.chatId,
//                        userId = userId,
//                        content = msg.content,
//                        type = msg.type
//                    )
//                    println("message from $userId: $message")

                    val newMetaMessageMono = messageService.processNewMessage(
                        localId = msg.localId,
                        serverId = msg.serverId,
                        chatId = msg.chatId,
                        userId = userId,
                        content = msg.content,
                        type = msg.type
                    )
                    val actions = newMetaMessageMono.flatMap { newMeta ->
//                        messageService.isLegalToMessaging(
//                            serverId = newMeta.serverId,
//                            userId = userId
//                        ).flatMap { isLegal ->
//                            if (isLegal) {
//                                processMessage(newMeta, sessionSinks)
//                            } else {
//                                Mono.just(false).then()
//                            }
//                        }
                        processMessage(newMeta, sessionSinks)
                    }//.subscribe()
                    actions.subscribe()
//                    newMetaMessage.flatMap {
//                        synMessage(),
//                        forwardMessage(),
//                        confirmMessage()
//                    }

//                    val actions = Flux.concat(
//                        synMessage(userId, message, session),
//                        forwardMessage(userId, message, userId + 1),
//                        confirmMessage(userId, session)
//                    )
//                    actions.subscribe()
//                    synMessage(userId, message, session)
//                    forwardMessage(userId, message, userId + 1)
//                    confirmMessage(userId, session)
                }
//                .zipWith(session.send(userFluxToSub.map { session.textMessage(it) }))
                .zipWith(session.send(sessionFlux.map { session.textMessage(it) }))
                .then()
                .doFinally {
//                    if ( !session.isOpen) {
//                        userSessions[userId]?.removeSession(session) ?: println("$userId is not exist!")
//                        if (userSessions[userId]?.isEmpty() != false) {
//                            userSessions.remove(userId)
//                        }
//                    }
                    if ( !session.isOpen) {
                        messageService.sessionOffline(
                            userId = userId, session = session
                        )
                    }
                }
        } catch (e: NumberFormatException) {
            return session.close()
        } finally {
            //
        }
    }

    private fun processMessage(metaMessage: MetaMessage, sessionSinks: Sinks.Many<String>): Mono<Void> { //Flux<Sinks.EmitResult?>
        //, session: WebSocketSession
        return messageService.isLegalToMessaging(serverId = metaMessage.serverId, userId = metaMessage.userId)
            .flatMap { isLegal ->
                if (isLegal) {
                    val actions = when (metaMessage.type) { //: Flux<Sinks.EmitResult?>
                        EntityConst.Companion.MessageType.TextMessage.ordinal -> {
                            responseMessage(metaMessage = metaMessage, sessionSinks = sessionSinks)
                            Flux.concat(
                                storeMessageInServer(metaMessage = metaMessage),
                                forwardMessageInServer(metaMessage = metaMessage, excludedSessionSinks = sessionSinks),
                            )
                        }
                        EntityConst.Companion.MessageType.MediaMessage.ordinal -> {
                            responseMessage(metaMessage = metaMessage, sessionSinks = sessionSinks)
                            Flux.concat(
                                storeMessageInServer(metaMessage = metaMessage),
                                forwardMessageInServer(metaMessage = metaMessage, excludedSessionSinks = sessionSinks),
                            )
                        }

                        else -> {
                            responseMessage(content = "", sessionSinks = sessionSinks)
                            Flux.just()
                        }
                    }
                    actions.then()
                } else {
                    responseMessage(content = "FORBIDDEN", sessionSinks = sessionSinks)
                    Mono.just(false).then()
                }
            }
//        val actions = when (metaMessage.type) { //: Flux<Sinks.EmitResult?>
//            EntityConst.Companion.MessageType.TextMessage.ordinal -> {
//                responseMessage(metaMessage = metaMessage, sessionSinks = sessionSinks)
//                Flux.concat(
//                    storeMessageInServer(metaMessage = metaMessage),
//                    forwardMessageInServer(metaMessage = metaMessage),
////                    responseMessage(metaMessage = metaMessage, session = session)
//                )
//            }
////            EntityConst.Companion.MessageType.SubscribeMessage.ordinal -> {
////                subscribeServer(metaMessage = metaMessage)
////                Flux.concat(
////                    storeMessageInServer(metaMessage = metaMessage),
////                    responseMessage(metaMessage = metaMessage, session = session)
////                )
////            }
//            EntityConst.Companion.MessageType.MediaMessage.ordinal -> {
//                responseMessage(metaMessage = metaMessage, sessionSinks = sessionSinks)
//                Flux.concat(
//                    storeMessageInServer(metaMessage = metaMessage),
//                    forwardMessageInServer(metaMessage = metaMessage),
////                    responseMessage(metaMessage = metaMessage, session = session)
//                )
//            }
//
//            else -> {
//                responseMessage(content = "", sessionSinks = sessionSinks)
//                Flux.just()
////                responseMessage(content = "", userId = metaMessage.userId, session = session).flux()
//            }
//        }
//        return actions.then()
    }

    private fun forwardMessageInServer(metaMessage: MetaMessage, excludedSessionSinks: Sinks.Many<String>): Mono<Void> { //Flux<Sinks.EmitResult>
//        if (metaMessage.isSubscribeMessage()) {
//            throw RuntimeException(
//                "Subscribe Message Should Not Get Forwarded."
//            )
//        }
        return messageService.sendMessageByServer(metaMessage = metaMessage, excludedSessionSinks = excludedSessionSinks)
//        val serverSessionList = serverSessions[metaMessage.serverId]
//        return Flux.fromIterable(serverSessionList
//            ?.sessionList
//            ?.filterKeys { it != session }
//            ?.values ?: emptyList()
//        ).map { it.tryEmitNext(ForwardMessage(metaMessage).toJsonString()) }
    }

    fun storeMessageInServer(metaMessage: MetaMessage): Mono<Void> {
        return messageService.storeMessage(metaMessage = metaMessage)
    }

//    private fun subscribeServer(metaMessage: MetaMessage) {
//        serverSessions[metaMessage.serverId]
//    }

    private fun responseMessage(metaMessage: MetaMessage, sessionSinks: Sinks.Many<String>) {
        sessionSinks.tryEmitNext(ResponseMessage.fromMetaMessage(metaMessage).toJsonString())
    }

    private fun responseMessage(content: String, sessionSinks: Sinks.Many<String>) {
        sessionSinks.tryEmitNext(content)
    }

//    private fun responseMessage(content: String, userId: AccountId, session: WebSocketSession): Mono<Void> { //Flux<Sinks.EmitResult?>
//        messageService
//        return Mono.fromCallable {
//            userSessions[userId]?.sessionList?.get(session)
//                ?.tryEmitNext(ResponseMessage.fromStringContent(content).toJsonString())
//        }.then()
//
//    }
//    private fun responseMessage(metaMessage: MetaMessage, session: WebSocketSession): Mono<Void> { //Flux<Sinks.EmitResult?>
//        return Mono.fromCallable {
//            userSessions[metaMessage.userId]?.sessionList?.get(session)
//                ?.tryEmitNext(ResponseMessage.fromMetaMessage(metaMessage).toJsonString())
//        }.then()
//    }

//    private fun forwardMessage(userId: Long, message: String, destId: Long): Flux<Sinks.EmitResult> {
//        val nextUsersSessions = userSessions[destId]
//        return Flux.fromIterable(nextUsersSessions?.sessionList?.values ?: emptyList())
//            .map { session ->
//                session.tryEmitNext(message)
//            }
////        nextUsersSessions?.sessionList?.forEach {
////            it.value.tryEmitNext(message)
////        }
//    }
//
//    private fun synMessage(userId: Long, message: String, session: WebSocketSession): Flux<Sinks.EmitResult> {
//        val userSessions = userSessions[userId]
//        return Flux.fromIterable(userSessions
//            ?.sessionList
//            ?.filterKeys { it != session }
//            ?.values ?: emptyList()
//        ).map { it.tryEmitNext(message) }
////        userSessions
////            ?.sessionList
////            ?.filterKeys { it != session }
////            ?.forEach { it.value.tryEmitNext(message) }
//    }
//
//    private fun confirmMessage(userId: Long, session: WebSocketSession): Flux<Sinks.EmitResult?> {
//        return Mono.fromCallable {
//            val mid = 1L
//            val cid = 1
//            val tmpid = 1L
////            val receipt = Message(id = tmpid, chatId = cid, content = mid.toString())
//            userSessions[userId]
//                ?.sessionList
//                ?.get(session)
//                ?.tryEmitNext("confirm message")
//        }.flux()
////        userSessions[userId]
////            ?.sessionList
////            ?.get(session)
////            ?.tryEmitNext("confirm message")
//    }
}