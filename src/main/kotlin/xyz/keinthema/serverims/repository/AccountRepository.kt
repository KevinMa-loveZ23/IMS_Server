package xyz.keinthema.serverims.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import xyz.keinthema.serverims.model.entity.Account

@Repository
interface AccountRepository: ReactiveMongoRepository<Account, Long> {

}

//@Document(collection = "accounts")
//data class Account(
//    @Id val id: Long,
//    var name: String,
//    var password: String,
//    var salt: String,
//    var servers: MutableSet<Long>
//)