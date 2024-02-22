package xyz.keinthema.serverims.service.intf

import reactor.core.publisher.Mono
import xyz.keinthema.serverims.model.entity.Server

interface ServerService {
    /**
     * Create Server and Related Collection to DB,
     * Add Related Info to DB.
     *
     * Create server document in Collection servers,
     * add serverId in Set servers of owner's document,
     * create corresponding chat collection (in another DB).
     * */
    fun createServer(accountId: Long, name: String, description: String): Mono<Server?>
    fun getLastServer(): Mono<Server?>
    fun getServerById(id: Long): Mono<Server?>
    /**
     * Modify Modifiable Part of the Server Document.
     *
     * When new owner is not an admin, make it one.
     * */
    fun modifyServerInfo(id: Long, operator: Long, serverModifiablePart: Server.Companion.ServerModifiablePart): Mono<Server?>
    /**
     * Delete Server and Related Collection and Info from DB.
     *
     * Delete server document from Collection servers,
     * delete corresponding chat collection (in another DB),
     * delete serverId from Set servers of document
     * of everyone who is a member of the server.
     * */
    fun deleteServer(id: Long): Mono<Boolean>

    fun isLegalToModifyServerInfo(operator: Long, serverId: Long, serverModifiablePart: Server.Companion.ServerModifiablePart): Mono<Boolean>
    fun isLegalToDeleteServer(operator: Long, serverId: Long): Mono<Boolean>
}