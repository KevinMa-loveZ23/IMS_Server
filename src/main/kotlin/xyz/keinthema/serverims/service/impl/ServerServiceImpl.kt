package xyz.keinthema.serverims.service.impl

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
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
    private val accountSemaphore: MongoDBAccountsSemaphore,
    private val serverSemaphore: MongoDBServersSemaphore,
    private val accountService: AccountService
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
            val newServer = serverRepository.save(Server(
                id = newServerId,
                name = name,
                creatorId = accountId,
                description = description
            )).awaitFirstOrNull() ?: Server.void()
            accountService.addServerToAccount(accountId, newServerId)
//                .flatMap {
//                    newServer
//                }
                .thenReturn(newServer).awaitFirstOrNull()
        } }
    }

    override fun getLastServer(): Mono<Server?> {
        return reactiveMongoTemplate.findOne(
            Query().limit(1).with(Sort.by(Sort.Direction.DESC, "_id")),
            Server::class.java, SERVER_COLL_NAME
        )
    }

    override fun getServerById(id: Long): Mono<Server?> {
        TODO("Not yet implemented")
    }

    override fun modifyServerInfo(
        id: Long,
        operator: Long,
        serverModifiablePart: Server.Companion.ServerModifiablePart
    ): Mono<Server?> {
        TODO("Not yet implemented")
    }

    override fun deleteServer(id: Long): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun isLegalToModifyServerInfo(operator: Long, server: Server): Boolean {
        TODO("Not yet implemented")
    }

    override fun isLegalToDeleteServer(operator: Long, server: Server): Boolean {
        TODO("Not yet implemented")
    }
}