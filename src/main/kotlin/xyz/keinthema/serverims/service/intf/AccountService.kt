package xyz.keinthema.serverims.service.intf

import reactor.core.publisher.Mono
import xyz.keinthema.serverims.model.entity.Account


interface AccountService {
    fun createNewAccount(name: String,
                         hashedPw: String,
                         email:String,
                         initServers: MutableSet<Long> = mutableSetOf()
    ): Mono<Account?>
    fun getLastAccount(): Mono<Account?>
    fun getAccountById(id: Long): Mono<Account?>
    fun modifyAccountInfo(id:Long, accountModifiablePart: Account.Companion.AccountModifiablePart): Mono<Account?>
    fun modifyAccountPassword(id: Long, previousPw: String, newPw: String): Mono<Boolean>
    fun deleteAccount(id: Long): Mono<Void>

    fun modifyAccountServerCreateTimes(id: Long, variation: Int): Mono<Int>

    fun addServerToAccount(id: Long, serverId: Long): Mono<Account?>
    fun deleteServerFromAccount(id: Long, serverId: Long): Mono<Account?>
    fun deleteServerFromMultiAccount(ids: List<Long>, serverId: Long): Mono<Void>

    fun getNamesFromMultiAccount(ids: List<Long>): Mono<List<Pair<Long, String>>>

    fun isLegalToAccessAllAccountInfo(src: Long, dest: Long): Boolean
    fun isLegalToModifyAccountInfo(src: Long, dest: Long): Boolean
    fun isLegalToModifyAccountPassword(account: Account, previousPw: String): Boolean

    fun isLegalToModifyAccountPassword(id: Long, previousPw: String): Mono<Boolean>
    fun isLegalToDeleteAccount(src: Long, dest: Long, hashedPw: String?): Mono<Boolean>
}