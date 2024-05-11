package xyz.keinthema.serverims.service.intf

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.JwtConst

interface AuthService {
    fun getNewRefreshToken(id: Long): String
    fun getNewAccessToken(id: Long): String
    fun getTokenType(claims: Jws<Claims>): JwtConst.Companion.TokenType?
    fun renewRefreshToken(claims: Jws<Claims>): Mono<String>
    fun revokeRefreshToken(claims: Jws<Claims>): Mono<Boolean>
    fun isLegalToRenewToken(claims: Jws<Claims>): Boolean
    fun isNecessaryToDeactivateToken(claims: Jws<Claims>): Boolean
}