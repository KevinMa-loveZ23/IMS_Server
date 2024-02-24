package xyz.keinthema.serverims.service.intf

import io.jsonwebtoken.Claims
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.JwtConst

interface AuthService {
    fun getNewRefreshToken(id: Long): String
    fun getNewAccessToken(id: Long): String
    fun getTokenType(claims: Claims): JwtConst.Companion.TokenType?
    fun renewRefreshToken(claims: Claims): Mono<String>
    fun isLegalToRenewToken(claims: Claims): Boolean
    fun isNecessaryToDeactivateToken(claims: Claims): Boolean
}