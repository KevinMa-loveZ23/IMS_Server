package xyz.keinthema.serverims.service.intf

import io.jsonwebtoken.Claims
import xyz.keinthema.serverims.constant.JwtConst

interface AuthService {
    fun getNewRefreshToken(id: Long): String
    fun getNewAccessToken(id: Long): String
    fun getTokenType(claims: Claims): JwtConst.Companion.TokenType
    fun renewRefreshToken(claims: Claims): String
    fun isLegalToRenewToken(claims: Claims): Boolean
    fun isNecessaryToRenewToken(claims: Claims): Boolean
}