package xyz.keinthema.serverims.handler

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Curve
import io.jsonwebtoken.security.Jwks
import org.springframework.stereotype.Component
import xyz.keinthema.serverims.constant.JwtConst
import xyz.keinthema.serverims.constant.JwtConst.Companion.ISSUER
import xyz.keinthema.serverims.constant.JwtConst.Companion.TOKEN_TYPE
import java.security.KeyPair
import java.security.PublicKey
import java.util.*


@Component
class JwtProvider {

    companion object {
        private val curve: Curve = Jwks.CRV.Ed25519

//        const val publicKeyPath = "classpath:publicKey.pub"
//        private const val privateKeyPath = "classpath:privateKey.pem"

        private fun generateKeyPair(): KeyPair {
            return curve
                .keyPair()
                .build()
        }

        fun readKeyPairOrGenerate(): KeyPair {
            return generateKeyPair()
        }
    }

    private val keyPairEd25519 = readKeyPairOrGenerate()

    val publicKeyEd25519: PublicKey = keyPairEd25519.public
    private val privateKeyEd25519 = keyPairEd25519.private

    fun createAccessToken(id: Long): String {
        return createJws(id, JwtConst.Companion.TokenType.ACCESS)
    }

    fun createRefreshToken(id: Long): String {
        return createJws(id, JwtConst.Companion.TokenType.REFRESH)
    }

    fun createJws(id: Long, tokenType: JwtConst.Companion.TokenType): String {
        val now = Date()
        val validity = Date(now.time.plus(tokenType.validityMsec))
        val claims = Jwts.claims()
            .issuer(ISSUER)
            .issuedAt(now)
            .expiration(validity)
            .subject(id.toString())
            .id(UUID.randomUUID().toString())
            .add(TOKEN_TYPE, tokenType.str)
            .build()
        return Jwts.builder()
            .claims(claims)
            .signWith(privateKeyEd25519, Jwts.SIG.EdDSA)
            .compact()
    }

//    fun getAuthentication(jwtToken: String): Authentication {
//
//    }

    fun validateToken(jwsToken: String): Jws<Claims>? {
        return try {
//            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
            Jwts.parser()
                .requireIssuer(ISSUER)
                .verifyWith(publicKeyEd25519).build()
                .parseSignedClaims(jwsToken)
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}