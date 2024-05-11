package xyz.keinthema.serverims.service.impl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.collectMap
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.ServerId
import xyz.keinthema.serverims.constant.ServiceConst
import xyz.keinthema.serverims.constant.ServiceConst.Companion.SERVER_COLL_NAME
import xyz.keinthema.serverims.constant.ServiceConst.Companion.getKeyForServerEntryWaiting
import xyz.keinthema.serverims.constant.ServiceConst.Companion.getKeyPatternForServerEntryWaiting
import xyz.keinthema.serverims.model.entity.Account
import xyz.keinthema.serverims.model.entity.ReadingRecord
import xyz.keinthema.serverims.model.entity.Server
import xyz.keinthema.serverims.model.entity.ServerEntryStrategy
import xyz.keinthema.serverims.service.intf.*
import xyz.keinthema.serverims.service.intf.ServerService.Companion.getServerRecordCollName
import java.time.Duration

@Service
class ServerMemberServiceImpl(
    private val serverService: ServerService,
    private val accountService: AccountService,
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    @Qualifier("chatMongoTemplate") private val chatMongoTemplate: ReactiveMongoTemplate,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    private val messageService: MessageService
): ServerMemberService {

    fun addServerToUser(userId: AccountId, serverId: ServerId): Mono<Boolean> {
        return accountService.getAccountById(userId)
            .flatMap { account ->
                if (account != null) {
                    reactiveMongoTemplate.findAndModify(
                        Query(Criteria.where("id").`is`(userId)),
                        Update().push("servers", serverId),
                        FindAndModifyOptions.options().returnNew(true),
                        Account::class.java,
                        ServiceConst.ACCOUNT_COLL_NAME
                    ).map { it != null }
                } else {
                    Mono.just(false)
                }
            }
    }
    override fun processJoinRequestToServer(serverId: ServerId, userId: AccountId, code: Int, message: String): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .flatMap { server ->
                if (server == null) {
                    Mono.just(false)
                } else {
                    when (code) {
                        ServerEntryStrategy.StrategyType.CodeCheck.ordinal -> {
                            if (server.entryMessageList.contains(message)) {
                                messageService.subscribeServer(userId = userId, serverId = serverId)
                                Mono.zip(
                                    reactiveMongoTemplate.findAndModify(
                                        Query(Criteria.where("id").`is`(serverId)),
                                        Update().push("userList", userId),
                                        Server::class.java,
                                        SERVER_COLL_NAME
                                    ),
                                    addServerToUser(userId = userId, serverId = serverId)
                                ).map { it.t2 }
//                                messageService.subscribeServer(userId = userId, serverId = serverId)
//                                addServerToUser(userId = userId, serverId = serverId)
                            } else {
                                Mono.just(false)
                            }
                        }
                        ServerEntryStrategy.StrategyType.AdminCheck.ordinal -> {
                            reactiveRedisTemplate.opsForValue()
                                .set(getKeyForServerEntryWaiting(serverId, userId), message,
                                    Duration.ofMillis(server.entryWaitingTimeInMsec))
                        }
                        else -> {
                            Mono.just(false)
                        }
                    }
                }
            }
    }

    override fun getJoinRequestList(serverId: ServerId): Mono<MutableMap<AccountId, String>?> {
//        reactiveRedisTemplate.keys(getKeyPatternForServerEntryWaiting(serverId))
        return reactiveRedisTemplate.scan(
            ScanOptions.scanOptions()
                .match(getKeyPatternForServerEntryWaiting(serverId))
                .build()
        ).flatMap {
            val userId = it.split(":").last().toLong()
            reactiveRedisTemplate.opsForValue()[it].map { value ->
                Pair(userId, value)
            }
        }.collectMap().map { it.toMutableMap() }
    }

    override fun allowJoinRequestToServer(serverId: ServerId, userId: AccountId, operator: AccountId): Mono<Boolean> {
        return Mono.zip(
            reactiveRedisTemplate.delete(getKeyForServerEntryWaiting(serverId, userId)),
            serverService.getServerById(serverId).flatMap { server ->
                if (server !== null) {
                    messageService.subscribeServer(userId = userId, serverId = serverId)
                    Mono.zip(
                        reactiveMongoTemplate.findAndModify(
                            Query(Criteria.where("id").`is`(serverId)),
                            Update().push("userList", userId),
//                        FindAndModifyOptions.options().returnNew(true),
                            Server::class.java,
                            SERVER_COLL_NAME
                        ),
                        addServerToUser(userId = userId, serverId = serverId)
                    ).map { it.t2 }
                } else {
                    Mono.just(false)
                }
            }
        ).map { it.t2 }
    }

    override fun getServerMembersFromServer(serverId: ServerId): Mono<MutableSet<AccountId>?> {
        return serverService.getServerById(serverId).map { it?.userList }
    }

    override fun deleteMembersFromServer(
        serverId: ServerId,
        userIdList: List<AccountId>,
        operator: AccountId
    ): Mono<Boolean> {
        userIdList.forEach { messageService.unsubscribeServer(userId = it, serverId = serverId) }
        return Mono.zip(
            reactiveMongoTemplate.findAndModify(
                Query(Criteria.where("id").`is`(serverId)),
                Update().pullAll("userList", userIdList.toTypedArray()),
//                FindAndModifyOptions.options().returnNew(true),
                Server::class.java,
                SERVER_COLL_NAME
            ),
            chatMongoTemplate.findAndRemove(
                Query(Criteria.where("id").`in`(userIdList)),
                ReadingRecord::class.java,
                getServerRecordCollName(serverId)
            ),
            accountService.deleteServerFromMultiAccount(userIdList, serverId)
        ).map { true }
    }

    override fun getEntryMessageList(serverId: ServerId): Mono<Set<String>> {
        return serverService.getServerById(serverId)
            .map { server -> server?.entryMessageList ?: setOf() }
    }

    override fun addEntryMessage(serverId: ServerId, entryMessage: String): Mono<Boolean> {
        return reactiveMongoTemplate.findAndModify(
            Query(Criteria.where("id").`is`(serverId)),
            Update().push("entryMessageList", entryMessage),
            Server::class.java,
            SERVER_COLL_NAME
        ).map { true }
    }

    override fun deleteEntryMessage(serverId: ServerId, entryMessage: String): Mono<Boolean> {
        return reactiveMongoTemplate.findAndModify(
            Query(Criteria.where("id").`is`(serverId)),
            Update().pull("entryMessageList", entryMessage),
            Server::class.java,
            SERVER_COLL_NAME
        ).map { true }
    }

    override fun isLegalToGetWaitingList(operator: AccountId, serverId: ServerId): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .map { it?.admins?.contains(operator) ?: false }
    }

    override fun isLegalToAllowJoinRequest(operator: AccountId, serverId: ServerId): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .map { it?.admins?.contains(operator) ?: false }
    }

    override fun isLegalToGetMembers(operator: AccountId, serverId: ServerId): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .map { it?.userList?.contains(operator) ?: false }
    }

    override fun isLegalToDeleteMembers(operator: AccountId, serverId: ServerId, userId: AccountId): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .map {
//                it?.admins?.contains(operator) ?: false
                if (it != null) {
                    if (it.admins.contains(userId)) {
                        false
                    } else if (it.admins.contains(operator)) {
                        it.userList.contains(userId)
                    } else {
                        operator == userId
                    }
//                    (it.admins.contains(operator) && it.owner != operator)
                } else {
                    false
                }
            }
    }

    override fun isLegalToDeleteMuitiMembers(operator: AccountId, serverId: ServerId, userIdList: List<AccountId>): Mono<Boolean> {
        return serverService.getServerById(serverId)
            .map {
                it?.admins?.contains(operator) ?: false
                if (it != null) {
                    if (userIdList.any { uid -> uid in it.admins }) {
                        false
                    } else if (it.admins.contains(operator)) {
                        it.userList.containsAll(userIdList)
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
    }
}