package xyz.keinthema.serverims.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import xyz.keinthema.serverims.model.entity.Server

@Repository
interface ServerRepository: ReactiveMongoRepository<Server, Long>

//@Document(collection = "servers")
//data class Server(
//    @Id val id: Long,
//    var name: String,
//    var owner: Long,
//    var usersId: MutableSet<Long>
//)