package xyz.keinthema.serverims.service.impl

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.config.MongoDBAccountsSemaphore
import xyz.keinthema.serverims.config.MongoDBServersSemaphore
import xyz.keinthema.serverims.constant.ServiceConst.Companion.SERVER_COLL_NAME
import xyz.keinthema.serverims.model.entity.Server
import xyz.keinthema.serverims.repository.ServerRepository
import xyz.keinthema.serverims.service.intf.AccountService
import xyz.keinthema.serverims.service.intf.ServerService

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
                return@coroutineScope null
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
                    chatMongoTemplate.collectionExists(newServerId.toString())
                        .flatMap { isExist ->
                            if (isExist) {
                                Mono.just(false)
                            } else {
                                chatMongoTemplate
                                    .createCollection(newServerId.toString())
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
                    val userList: List<Long> = server.usersRecord.map { record -> record.userId }
                    accountService
                        .deleteServerFromMultiAccount(userList, server.id)
                        .flatMap {
                            serverRepository.deleteById(id)
                                .flatMap {
                                    chatMongoTemplate
                                        .dropCollection(server.id.toString())
                                }
                        }
                        .thenReturn(true)
                }
            }
    }

    override fun isLegalToModifyServerInfo(
        operator: Long,
        serverId: Long,
        serverModifiablePart: Server.Companion.ServerModifiablePart
    ): Mono<Boolean> {
        return getServerById(serverId).flatMap { server ->
            if (server?.owner?.equals(operator) == true) {
                if (serverModifiablePart.owner != null) {
                    accountService.getAccountById(serverModifiablePart.owner).map { account ->
                        account?.servers?.contains(serverId) ?: false
                    }
                } else {
                    Mono.just(true)
                }
            } else if (server?.admins?.contains(operator) == true && serverModifiablePart.owner == null) {
                Mono.just(true)
            } else {
                Mono.just(false)
            }
        }
    }

    override fun isLegalToDeleteServer(operator: Long, serverId: Long): Mono<Boolean> {
        return getServerById(serverId).map { server ->
            server?.owner?.equals(operator) ?: false
        }
    }
}