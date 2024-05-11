package xyz.keinthema.serverims.service.impl

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.config.MongoDBServersSemaphore
import xyz.keinthema.serverims.constant.EntityConst
import xyz.keinthema.serverims.constant.ServerId
import xyz.keinthema.serverims.constant.ServiceConst.Companion.SERVER_COLL_NAME
import xyz.keinthema.serverims.model.entity.Message
import xyz.keinthema.serverims.model.entity.Server
import xyz.keinthema.serverims.repository.ServerRepository
import xyz.keinthema.serverims.service.intf.AccountService
import xyz.keinthema.serverims.service.intf.ServerService
import xyz.keinthema.serverims.service.intf.ServerService.Companion.getServerRecordCollName

@Service
class ServerServiceImpl(
    private val serverRepository: ServerRepository,
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val serverSemaphore: MongoDBServersSemaphore,
    private val accountService: AccountService,
    @Qualifier("chatMongoTemplate") private val chatMongoTemplate: ReactiveMongoTemplate
): ServerService {
    override fun createServer(accountId: Long, name: String, description: String): Mono<Server?> {
        return mono { coroutineScope {
            val newCreateTimes = accountService.modifyAccountServerCreateTimes(accountId, -1)
                .awaitFirstOrNull() ?: -1
            if (newCreateTimes == -1) {
                return@coroutineScope Server.void()
            }
            serverSemaphore.acquire()
            val newServerId = (getLastServer().awaitFirstOrNull()?.id?.plus(1L)) ?: 0L
            val newServerCreating = serverRepository.save(Server(
                id = newServerId,
                name = name,
                creatorId = accountId,
                description = description
            )).awaitFirstOrNull() ?: Server.void()
            val newServer = accountService.addServerToAccount(accountId, newServerId)
                .flatMap {
                    val newServerIdStr = newServerId.toString()
                    val newRecordCollName = getServerRecordCollName(newServerId)
                    chatMongoTemplate.collectionExists(newServerIdStr)
                        .flatMap { isExist ->
                            if (isExist) {
                                Mono.just(false)
                            } else {
                                Mono.zip(chatMongoTemplate
                                    .createCollection(newServerIdStr),
                                    chatMongoTemplate
                                        .createCollection(newRecordCollName)
                                    )
                            }
                        }
                }
//                .flatMap {
//                    newServer
//                }
                .thenReturn(newServerCreating)
                .awaitFirstOrNull()
            serverSemaphore.release()
            newServer
        } }
    }

    override fun getLastServer(): Mono<Server?> {
        return reactiveMongoTemplate.findOne(
            Query().limit(1).with(Sort.by(Sort.Direction.DESC, "_id")),
            Server::class.java, SERVER_COLL_NAME
        )
    }

    override fun getServerById(id: Long): Mono<Server?> {
        return serverRepository.findById(id)
    }

    override fun modifyServerInfo(
        id: Long,
        operator: Long,
        serverModifiablePart: Server.Companion.ServerModifiablePart
    ): Mono<Server?> {
        return getServerById(id)
            .flatMap { server ->
                if (server == null) {
                    Mono.just(Server.void())
                } else {
                    val update = serverModifiablePart.getUpdateObj()
                    if ( serverModifiablePart.owner != null
                        && (!server.admins.contains(serverModifiablePart.owner))) {
                        update.push("admins", serverModifiablePart.owner)
                    }
                    reactiveMongoTemplate.findAndModify(
                        Query(Criteria.where("id").`is`(id)),
                        update,
                        FindAndModifyOptions.options().returnNew(true),
                        Server::class.java,
                        SERVER_COLL_NAME
                    )
                }
            }
    }

    override fun deleteServer(id: Long): Mono<Boolean> {
        return getServerById(id)
            .flatMap { server ->
                if (server == null) {
                    Mono.just(false)
                } else {
//                    val userList: List<Long> = server.usersRecord.map { it.key }
                    val userList = server.userList.toList()
                    accountService
                        .deleteServerFromMultiAccount(userList, server.id)
                        .flatMap {
                            mono { coroutineScope {
                                serverSemaphore.acquire()
                                serverRepository.deleteById(id)
                                    .flatMap {
                                        val serverIdStr = server.id.toString()
                                        val serverRecordCollName = getServerRecordCollName(server.id)
//                                        chatMongoTemplate.find<Message>(
//                                            Query(Criteria.where("type")
//                                                .`is`(EntityConst.Companion.MessageType.MediaMessage.ordinal)),
//                                            serverIdStr
//                                        ).map { msg ->
//                                            msg.content
//                                        }
                                        Mono.zip(
                                            chatMongoTemplate
                                                .dropCollection(serverIdStr),
                                            chatMongoTemplate
                                                .dropCollection(serverRecordCollName),
                                            reactiveMongoTemplate.findAndRemove(
                                                Query(Criteria.where("id").`is`(server.id)),
                                                Server::class.java,
                                                SERVER_COLL_NAME
                                            )
                                        )
                                    }
                                serverSemaphore.release()
                            } }
                        }
                        .thenReturn(true)
                }
            }
    }

//    override fun getServerRecordCollName(id: ServerId): String {
//        return id.toString() + "record"
//    }

    override fun isLegalToModifyServerInfo(
        operator: Long,
        serverId: Long,
        serverModifiablePart: Server.Companion.ServerModifiablePart
    ): Mono<Boolean> {
        return getServerById(serverId).flatMap { server ->
            if (server != null) {
                if (server.owner == operator) {
                    if (serverModifiablePart.owner != null) {
                        Mono.just(server.admins.contains(serverModifiablePart.owner)
                                && server.userList.contains(serverModifiablePart.owner))
                    } else {
                        Mono.just(true)
                    }
                } else if (server.admins.contains(operator)) {
                    Mono.just(serverModifiablePart.owner == null)
                } else {
                    Mono.just(false)
                }
            } else {
                Mono.just(false)
            }
//            if (server?.owner?.equals(operator) == true) {
//                if (serverModifiablePart.owner != null) {
//                    accountService.getAccountById(serverModifiablePart.owner).map { account ->
//                        account?.servers?.contains(serverId) ?: false
//                    }
//                } else {
//                    Mono.just(true)
//                }
//            } else if (server?.admins?.contains(operator) == true && serverModifiablePart.owner == null) {
//                Mono.just(true)
//            } else {
//                Mono.just(false)
//            }
        }
    }

    override fun isLegalToDeleteServer(operator: Long, serverId: Long): Mono<Boolean> {
        return getServerById(serverId).map { server ->
            server?.owner?.equals(operator) ?: false
        }
    }
}