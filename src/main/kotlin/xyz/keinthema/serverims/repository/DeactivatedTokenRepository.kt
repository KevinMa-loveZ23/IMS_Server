package xyz.keinthema.serverims.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import xyz.keinthema.serverims.model.entity.DeactivatedToken
import java.util.UUID

@Repository
interface DeactivatedTokenRepository: ReactiveMongoRepository<DeactivatedToken, UUID>