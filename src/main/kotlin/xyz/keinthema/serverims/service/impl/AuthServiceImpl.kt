package xyz.keinthema.serverims.service.impl

import io.jsonwebtoken.Claims
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.JwtConst
import xyz.keinthema.serverims.constant.JwtConst.Companion.TOKEN_TYPE
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

    override fun getTokenType(claims: Claims): JwtConst.Companion.TokenType? {
        return JwtConst.Companion.TokenType.entries.find { it.str == claims[TOKEN_TYPE] }
    }

    override fun renewRefreshToken(claims: Claims): Mono<String> {
        val id: Long = claims.subject.toLong()
        val newJws = jwsService.getRefreshJws(id)
        return if (isNecessaryToDeactivateToken(claims)) {
            jwsService.deactivateJws(UUID.fromString(claims.id), claims.expiration)
                .thenReturn(newJws)
        } else {
            Mono.just(newJws)
        }
    }

    override fun isLegalToRenewToken(claims: Claims): Boolean {
        return claims.expiration.time - Date().time < JwtConst.Companion.TokenType.REFRESH.validityMsec / 2
    }

    override fun isNecessaryToDeactivateToken(claims: Claims): Boolean {
        return claims.expiration.time - Date().time < JwtConst.Companion.TimeInMsec.DAY.msecTime
    }
}