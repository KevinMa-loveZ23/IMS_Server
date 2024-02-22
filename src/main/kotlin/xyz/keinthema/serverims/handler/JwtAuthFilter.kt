package xyz.keinthema.serverims.handler

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.JwtConst.Companion.JWT_ATTR_NAME

@Component
class JwtAuthFilter(private val jwtProvider: JwtProvider): WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (preventCheck(exchange.request)) return chain.filter(exchange)

        val jwtToken = extractTokenFromRequest(exchange.request)
        if (jwtToken != null) {
            val jwtClaims = jwtProvider.validateToken(jwtToken)
            if (jwtClaims != null) {
                exchange.attributes[JWT_ATTR_NAME] = jwtClaims
                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder
                        .withAuthentication(UsernamePasswordAuthenticationToken(
                            jwtClaims.payload.subject,
                            jwtClaims.payload.id
                        )))
            }
        }
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        return exchange.response
            .writeWith(Mono.just(exchange.response.bufferFactory()
                .wrap("Unauthorized".toByteArray())
            ))
    }

    private fun extractTokenFromRequest(request: ServerHttpRequest): String? {
        val bearerToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return null
    }

    private fun preventCheck(request: ServerHttpRequest): Boolean {
        return request.method == HttpMethod.POST &&
                ((request.path.value() == "/account") || (request.path.value() == "/login"))
    }
}