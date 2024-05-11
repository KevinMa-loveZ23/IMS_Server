package xyz.keinthema.serverims.service.impl

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.ServiceConst.Companion.getKeyForRevokedUUID
import xyz.keinthema.serverims.handler.JwtProvider
import xyz.keinthema.serverims.service.intf.JwsService
import java.time.Duration
import java.util.*

@Service
class JwsServiceImpl(
    private val jwtProvider: JwtProvider,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
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
            isRevoked(UUID.fromString(claims.payload.id))
                .map { exist -> if (exist) Pair<Boolean, Jws<Claims>?>(false, null) else Pair(true, claims) }
        } else {
            Mono.just(Pair<Boolean, Jws<Claims>?>(false, null))
        }
        return monoClaims
    }

    override fun isRevoked(jti: UUID): Mono<Boolean> {
        return reactiveRedisTemplate.hasKey(getKeyForRevokedUUID(jti))
//        return reactiveMongoTemplate.exists(
//            Query.query(Criteria.where("id").`is`(jti)),
//            RevokedToken::class.java,
//            REVOKED_TOKEN_COLL_NAME
//        )
    }

    override fun revokeJws(jti: UUID, expireAt: Date): Mono<Boolean> {
        return reactiveRedisTemplate.opsForValue()
            .set(getKeyForRevokedUUID(jti), jti.toString(),
                Duration.ofMillis(expireAt.time - Date().time))
//        return revokedTokenRepository.save(
//            RevokedToken(id = jti, expireAt = expireAt)
//        )
//        return reactiveMongoTemplate.save(
//            DeactivatedToken(id = jti, expireAt = expireAt),
//            DEACTIVATED_TOKEN_COLL_NAME
//        )
    }
}