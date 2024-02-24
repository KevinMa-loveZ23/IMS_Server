package xyz.keinthema.serverims.service.impl

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.ServiceConst.Companion.DEACTIVATED_TOKEN_COLL_NAME
import xyz.keinthema.serverims.handler.JwtProvider
import xyz.keinthema.serverims.model.entity.DeactivatedToken
import xyz.keinthema.serverims.repository.DeactivatedTokenRepository
import xyz.keinthema.serverims.service.intf.JwsService
import java.util.*

@Service
class JwsServiceImpl(
    private val jwtProvider: JwtProvider,
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val deactivatedTokenRepository: DeactivatedTokenRepository
): JwsService {

    override fun getAccessJws(id: Long): String {
        return jwtProvider.createAccessToken(id = id)
    }

    override fun getRefreshJws(id: Long): String {
        return jwtProvider.createRefreshToken(id = id)
    }

    override fun legalClaimsOrNull(jws: String?): Mono<Pair<Boolean, Jws<Claims>?>> {
        if (jws == null) return Mono.just(Pair<Boolean, Jws<Claims>?>(false, null))
        val claims = jwtProvider.validateToken(jwsToken = jws)
        val monoClaims = if (claims != null) {
            isDeactivated(UUID.fromString(claims.payload.id))
                .map { exist -> if (exist) Pair<Boolean, Jws<Claims>?>(false, null) else Pair(true, claims) }
        } else {
            Mono.just(Pair<Boolean, Jws<Claims>?>(false, null))
        }
        return monoClaims
    }

    override fun isDeactivated(jti: UUID): Mono<Boolean> {
        return reactiveMongoTemplate.exists(
            Query.query(Criteria.where("id").`is`(jti)),
            DeactivatedToken::class.java,
            DEACTIVATED_TOKEN_COLL_NAME
        )
    }

    override fun deactivateJws(jti: UUID, expireAt: Date): Mono<DeactivatedToken> {
        return deactivatedTokenRepository.save(
            DeactivatedToken(id = jti, expireAt = expireAt)
        )
//        return reactiveMongoTemplate.save(
//            DeactivatedToken(id = jti, expireAt = expireAt),
//            DEACTIVATED_TOKEN_COLL_NAME
//        )
    }
}