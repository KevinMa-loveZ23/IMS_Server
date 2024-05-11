package xyz.keinthema.serverims.service.intf

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import reactor.core.publisher.Mono
import java.util.Date
import java.util.UUID

interface JwsService {
    fun getAccessJws(id: Long): String
    fun getRefreshJws(id: Long): String
    fun legalClaimsOrNull(jws: String?): Mono<Pair<Boolean, Jws<Claims>?>>
    fun isRevoked(jti: UUID): Mono<Boolean>
    fun revokeJws(jti: UUID, expireAt: Date): Mono<Boolean>

}