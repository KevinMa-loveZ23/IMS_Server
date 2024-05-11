package xyz.keinthema.serverims.service.intf

import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.ServerId

interface ServerMemberService {
    fun processJoinRequestToServer(serverId: ServerId, userId: AccountId, code: Int, message: String): Mono<Boolean>
    fun getJoinRequestList(serverId: ServerId): Mono<MutableMap<AccountId, String>?>
    fun allowJoinRequestToServer(serverId: ServerId, userId: AccountId, operator: AccountId): Mono<Boolean>
    fun getServerMembersFromServer(serverId: ServerId): Mono<MutableSet<AccountId>?>
    fun deleteMembersFromServer(serverId: ServerId, userIdList: List<AccountId>, operator: AccountId): Mono<Boolean>

    fun getEntryMessageList(serverId: ServerId): Mono<Set<String>>
    fun addEntryMessage(serverId: ServerId, entryMessage: String): Mono<Boolean>
    fun deleteEntryMessage(serverId: ServerId, entryMessage: String): Mono<Boolean>

    fun isLegalToGetWaitingList(operator: AccountId, serverId: ServerId): Mono<Boolean>
    fun isLegalToAllowJoinRequest(operator: AccountId, serverId: ServerId): Mono<Boolean>
    fun isLegalToGetMembers(operator: AccountId, serverId: ServerId): Mono<Boolean>
    fun isLegalToDeleteMembers(operator: AccountId, serverId: ServerId, userId: AccountId): Mono<Boolean>
    fun isLegalToDeleteMuitiMembers(operator: AccountId, serverId: ServerId, userIdList: List<AccountId>): Mono<Boolean>
}