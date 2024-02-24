package xyz.keinthema.serverims.service.impl

import io.jsonwebtoken.Claims
import org.springframework.stereotype.Service
import xyz.keinthema.serverims.constant.JwtConst
import xyz.keinthema.serverims.service.intf.AuthService
import xyz.keinthema.serverims.service.intf.JwsService

@Service
class AuthServiceImpl(
    private val jwsService: JwsService
): AuthService {
    override fun getNewRefreshToken(id: Long): String {
        return jwsService.getRefreshJws(id = id)
    }

    override fun getNewAccessToken(id: Long): String {
        TODO("Not yet implemented")
    }

    override fun getTokenType(claims: Claims): JwtConst.Companion.TokenType {
        TODO("Not yet implemented")
    }

    override fun renewRefreshToken(claims: Claims): String {
        TODO("Not yet implemented")
    }

    override fun isLegalToRenewToken(claims: Claims): Boolean {
        TODO("Not yet implemented")
    }

    override fun isNecessaryToRenewToken(claims: Claims): Boolean {
        TODO("Not yet implemented")
    }
}