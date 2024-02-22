package xyz.keinthema.serverims.service.impl

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.ServiceConst
import xyz.keinthema.serverims.model.entity.Server
import xyz.keinthema.serverims.repository.ServerRepository
import xyz.keinthema.serverims.service.intf.AccountService
import xyz.keinthema.serverims.service.intf.ServerAdminService

@Service
class ServerAdminServerImpl(
    private val serverRepository: ServerRepository,
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val accountService: AccountService
): ServerAdminService {
    override fun addAdminToServer(serverId: Long, adminId: Long): Mono<MutableSet<Long>?> {
        return reactiveMongoTemplate.findAndModify(
            Query(Criteria.where("id").`is`(serverId)),
            Update().push("admins", adminId),
            FindAndModifyOptions.options().returnNew(true),
            Server::class.java,
            ServiceConst.SERVER_COLL_NAME
        )
            .map { server ->
                server.admins
            }
    }

    override fun getAdminsFromServer(serverId: Long): Mono<MutableSet<Pair<Long, String>>?> {
        return serverRepository.findById(serverId)
            .flatMap { server ->
                accountService.getNamesFromMultiAccount(server.admins.toList())
                    .map { list ->
                        list.toMutableSet()
                    }
            }
    }

    override fun deleteAdminFromServer(serverId: Long, adminId: Long): Mono<MutableSet<Long>?> {
        return reactiveMongoTemplate.findAndModify(
            Query(Criteria.where("id").`is`(serverId)),
            Update().pull("admins", adminId),
            FindAndModifyOptions.options().returnNew(true),
            Server::class.java,
            ServiceConst.SERVER_COLL_NAME
        )
            .map { server ->
                server.admins
            }
    }

    override fun isLegalToModifyAdmin(operator: Long, variedId: Long, serverId: Long): Mono<Boolean> {
        return serverRepository.findById(serverId)
            .flatMap { server ->
                if (server.owner == operator) {
                    accountService.getAccountById(variedId).map { account ->
                        account?.servers?.contains(serverId) ?: false
                    }
                } else if (server.admins.contains(operator)) {
                    Mono.just(operator == variedId)
                } else {
                    Mono.just(false)
                }
//                        && server.usersRecord.map { record -> record.userId }.contains(variedId)
            }
    }

    override fun isLegalToGetAdmin(operator: Long, serverId: Long): Mono<Boolean> {
        return accountService.getAccountById(operator)
            .map { account ->
                account?.servers?.contains(serverId) ?: false
            }
//        return serverRepository.findById(serverId)
//            .map { server ->
//                server.usersRecord.map { record -> record.userId }.contains(operator)
//            }
    }
}