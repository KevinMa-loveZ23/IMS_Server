package xyz.keinthema.serverims.handler

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Curve
import io.jsonwebtoken.security.Jwks
import org.springframework.stereotype.Component
import xyz.keinthema.serverims.constant.JwtConst.Companion.ISSUER
import xyz.keinthema.serverims.constant.JwtConst.Companion.VALIDITY_IN_MILLISECOND
import java.security.KeyPair
import java.security.PublicKey
import java.util.*


@Component
class JwtProvider {

    companion object {
        private val curve: Curve = Jwks.CRV.Ed25519

        val publicKeyPath = "classpath:publicKey.pub"
        private val privateKeyPath = "classpath:privateKey.pem"

//        fun readKeyOrGenerate(): KeyPair {
//
//        }
    }

    private val keyPairEd25519 = generateKeyPair()

    val publicKeyEd25519: PublicKey = keyPairEd25519.public
    private val privateKeyEd25519 = keyPairEd25519.private

    private fun generateKeyPair(): KeyPair {
        return curve
            .keyPair()
            .build()
    }

    fun createJwtToken(id: Long): String {
        val now = Date()
        val validity = Date(now.time.plus(VALIDITY_IN_MILLISECOND))
        val claims = Jwts.claims()
            .issuer(ISSUER)
            .issuedAt(now)
            .expiration(validity)
            .subject(id.toString())
            .id(UUID.randomUUID().toString())
            .build()
        return Jwts.builder()
            .claims(claims)
            .signWith(privateKeyEd25519, Jwts.SIG.EdDSA)
            .compact()
    }

//    fun getAuthentication(jwtToken: String): Authentication {
//
//    }

    fun validateToken(jwtToken: String): Jws<Claims>? {
        return try {
//            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
            Jwts.parser()
                .verifyWith(publicKeyEd25519).build()
                .parseSignedClaims(jwtToken)
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}