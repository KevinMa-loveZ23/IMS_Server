package xyz.keinthema.serverims.service.intf

import reactor.core.publisher.Mono

interface ServerAdminService {
    fun addAdminToServer(serverId: Long, adminId: Long): Mono<MutableSet<Long>?>
    fun getAdminsFromServer(serverId: Long): Mono<Map<Long, String>>
    fun deleteAdminFromServer(serverId: Long, adminId: Long): Mono<MutableSet<Long>?>

    fun isLegalToModifyAdmin(operator: Long, variedId: Long, serverId: Long): Mono<Boolean>
    fun isLegalToGetAdmin(operator: Long, serverId: Long): Mono<Boolean>
}