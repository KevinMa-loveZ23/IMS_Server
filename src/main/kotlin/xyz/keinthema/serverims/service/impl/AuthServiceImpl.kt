package xyz.keinthema.serverims.service.impl

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.JwtConst
import xyz.keinthema.serverims.constant.JwtConst.Companion.TOKEN_TYPE
import xyz.keinthema.serverims.constant.TimeConst
import xyz.keinthema.serverims.service.intf.AuthService
import xyz.keinthema.serverims.service.intf.JwsService
import java.util.*

@Service
class AuthServiceImpl(
    private val jwsService: JwsService
): AuthService {
    override fun getNewRefreshToken(id: Long): String {
        return jwsService.getRefreshJws(id = id)
    }

    override fun getNewAccessToken(id: Long): String {
        return jwsService.getAccessJws(id = id)
    }

    override fun getTokenType(claims: Jws<Claims>): JwtConst.Companion.TokenType? {
        return JwtConst.Companion.TokenType.entries.find { it.str == claims.payload[TOKEN_TYPE] }
    }

    override fun renewRefreshToken(claims: Jws<Claims>): Mono<String> {
        val id: Long = claims.payload.subject.toLong()
        val newJws = jwsService.getRefreshJws(id)
        return if (isNecessaryToDeactivateToken(claims)) {
            jwsService.revokeJws(UUID.fromString(claims.payload.id), claims.payload.expiration)
                .thenReturn(newJws)
        } else {
            Mono.just(newJws)
        }
    }

    override fun revokeRefreshToken(claims: Jws<Claims>): Mono<Boolean> {
        return jwsService.revokeJws(UUID.fromString(claims.payload.id), claims.payload.expiration)
    }

    override fun isLegalToRenewToken(claims: Jws<Claims>): Boolean {
        return claims.payload.expiration.time - Date().time < JwtConst.Companion.TokenType.REFRESH.validityMsec / 2
    }

    override fun isNecessaryToDeactivateToken(claims: Jws<Claims>): Boolean {
        return claims.payload.expiration.time - Date().time < TimeConst.Companion.TimeInMsec.DAY.msecTime
    }
}