package xyz.keinthema.serverims.service.impl

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.config.MongoDBAccountsSemaphore
import xyz.keinthema.serverims.constant.ServiceConst.Companion.ACCOUNT_COLL_NAME
import xyz.keinthema.serverims.model.entity.Account
import xyz.keinthema.serverims.repository.AccountRepository
import xyz.keinthema.serverims.service.intf.AccountService

@Service
class AccountServiceImpl(private val accountRepository: AccountRepository,
                         private val reactiveMongoTemplate: ReactiveMongoTemplate,
                         private val accountsSemaphore: MongoDBAccountsSemaphore,
                         private val passwordEncoder: PasswordEncoder
): AccountService {
    override fun createNewAccount(name: String,
                                  hashedPw: String,
                                  email: String,
                                  initServers: MutableSet<Long>
    ): Mono<Account?> {

//        val resMono = Mono.create<Account> {
//            runBlocking { accountsSemaphore.acquire() }
//        }.map {
//            val account = getLastAccount().blockOptional()
//            val newId = if (account.isPresent) account.get().id else 0L
//            val newAccount = accountRepository.save(Account(
//                id = newId,
//                name = name,
//                password = passwordEncoder.encode(hashedPw),
//                salt = "",
//                servers = initServers
//            ))
//            accountsSemaphore.release()
//            val newAccountOption = newAccount.blockOptional()
//            if (newAccountOption.isPresent) newAccountOption.get()
//            else Account(-1, "NULL", "", "", mutableSetOf())
//        }

//        val resOption = resMono.blockOptional()
////        return if (resOption.isPresent) resOption.get() else Mono.error(RuntimeException("Create account failed"))
//        return if (resOption.isPresent) resOption.get() else Mono.create {  }
//        val resMono = mono { newAccount(name, hashedPw, initServers) }
//
//        return resMono
        return mono { coroutineScope {
            accountsSemaphore.acquire()
            val newId = (getLastAccount().awaitFirstOrNull()?.id?.plus(1L)) ?: 0L
            val newAccount = accountRepository.save(Account(
                id = newId,
                name = name,
                password = passwordEncoder.encode(hashedPw),
                email = email,
                servers = initServers
            )).awaitFirstOrNull()
            accountsSemaphore.release()
            newAccount
        } }
//        return mono { newAccount(name, hashedPw, email, initServers) }
    }

    override fun getLastAccount(): Mono<Account?> {
        return reactiveMongoTemplate.findOne(
            Query().limit(1).with(Sort.by(Sort.Direction.DESC, "_id")),
            Account::class.java, ACCOUNT_COLL_NAME
        )
//            .flatMap { account ->
//            if (account != null) {
//                Mono.just(account)
//            } else {
//                Mono.error(RuntimeException("User not found"))
//            }
//        }.switchIfEmpty { Mono.error(RuntimeException("User not found")) }
    }

    override fun getAccountById(id: Long): Mono<Account?> {
        return accountRepository.findById(id)
    }

    override fun modifyAccountInfo(id:Long, modifiablePart: Account.Companion.AccountModifiablePart): Mono<Account?> {
        val update = Update()
        if (modifiablePart.name != null) update.set("name", modifiablePart.name)
        if (modifiablePart.email != null) update.set("email", modifiablePart.email)
        if (modifiablePart.publishEmail != null) update.set("publishEmail", modifiablePart.publishEmail)
        if (modifiablePart.publishServer != null) update.set("publishServer", modifiablePart.publishServer)
        return reactiveMongoTemplate.findAndModify(
            Query(Criteria.where("id").`is`(id)),
            update,
            FindAndModifyOptions.options().returnNew(true),
            Account::class.java,
            ACCOUNT_COLL_NAME
        )
//        return getAccountById(id)
//            .map { account ->
//                if (account != null) {
//                    if (name != null) account.name = name
//                    if (email != null) account.email = email
//                }
//                account
//            }
//            .flatMap { it?.let { it1 -> accountRepository.save(it1) } }
    }

    override fun modifyAccountPassword(id: Long, previousPw: String, newPw: String): Mono<Boolean> {

        return getAccountById(id)
            .flatMap { account ->
                if (account != null && isLegalToModifyAccountPassword(account, previousPw)) {
//                    account.password = passwordEncoder.encode(newPw)
                    return@flatMap reactiveMongoTemplate.findAndModify(
                        Query(Criteria.where("id").`is`(id)),
                        Update().set("password", passwordEncoder.encode(newPw)),
                        FindAndModifyOptions.options().returnNew(true),
                        Account::class.java,
                        ACCOUNT_COLL_NAME
                    ).thenReturn(true)
                }
                Mono.just(false)
            }
    }

    override fun deleteAccount(id: Long): Mono<Void> {
        return accountRepository.deleteById(id)
    }

    override fun modifyAccountServerCreateTimes(id: Long, variation: Int): Mono<Int> {
        return mono { coroutineScope {
            accountsSemaphore.acquire()
            val newCreateTime = getAccountById(id).flatMap { account ->
                if (account != null) {
                    val newServerCreateTime = account.serverCreateTimes + variation
                    if (newServerCreateTime >= 0) {
                        reactiveMongoTemplate.findAndModify(
                            Query(Criteria.where("id").`is`(id)),
                            Update().set("serverCreateTimes", newServerCreateTime),
                            FindAndModifyOptions.options().returnNew(true),
                            Account::class.java,
                            ACCOUNT_COLL_NAME
                        ).map { account ->
                            account?.serverCreateTimes ?: -1
                        }
                    } else Mono.just(-1)
                } else Mono.just(-1)
            }.awaitFirstOrNull() ?: -1
            accountsSemaphore.release()
            newCreateTime
        } }
    }

    override fun addServerToAccount(id: Long, serverId: Long): Mono<Account?> {
        return reactiveMongoTemplate.findAndModify(
            Query(Criteria.where("id").`is`(id)),
            Update().push("servers", serverId),
            FindAndModifyOptions.options().returnNew(true),
            Account::class.java,
            ACCOUNT_COLL_NAME
        )
    }

    override fun deleteServerFromAccount(id: Long, serverId: Long): Mono<Account?> {
        return reactiveMongoTemplate.findAndModify(
            Query(Criteria.where("id").`is`(id)),
            Update().pull("servers", serverId),
            FindAndModifyOptions.options().returnNew(true),
            Account::class.java,
            ACCOUNT_COLL_NAME
        )
    }

    override fun isLegalToAccessAllAccountInfo(src: Long, dest: Long): Boolean {
        return src == dest
    }

    override fun isLegalToModifyAccountInfo(src: Long, dest: Long): Boolean {
        return src == dest
    }

    override fun isLegalToModifyAccountPassword(account: Account, previousPw: String): Boolean {
        return passwordEncoder.matches(previousPw, account.password)
    }

    override fun isLegalToDeleteAccount(src: Long, dest: Long, hashedPw: String?): Mono<Boolean> {
        return getAccountById(dest).map { account ->
                src == dest && account != null && passwordEncoder.matches(hashedPw, account.password)
            }
    }
}