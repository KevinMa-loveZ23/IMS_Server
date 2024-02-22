package xyz.keinthema.serverims.service.intf

import reactor.core.publisher.Mono
import xyz.keinthema.serverims.model.entity.Server

interface ServerService {
    fun createServer(accountId: Long, name: String, description: String): Mono<Server?>
    fun getLastServer(): Mono<Server?>
    fun getServerById(id: Long): Mono<Server?>
    fun modifyServerInfo(id: Long, operator: Long, serverModifiablePart: Server.Companion.ServerModifiablePart): Mono<Server?>
    fun deleteServer(id: Long): Mono<Void>

    fun isLegalToModifyServerInfo(operator: Long, server: Server): Boolean
    fun isLegalToDeleteServer(operator: Long, server: Server): Boolean
}