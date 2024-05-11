package xyz.keinthema.serverims.handler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.config.PathConfig
import xyz.keinthema.serverims.constant.ControllerConst.Companion.ACCOUNT_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.AUTH_PATH
import xyz.keinthema.serverims.constant.JwtConst.Companion.JWT_CLAIMS_ATTR_NAME
import xyz.keinthema.serverims.constant.WebSocketConst
import xyz.keinthema.serverims.constant.WebSocketConst.Companion.getIdAndToken
import xyz.keinthema.serverims.model.dto.response.RequireLegalTokenBody
import xyz.keinthema.serverims.model.dto.response.StdResponse
import xyz.keinthema.serverims.service.intf.JwsService


@Component
class JwtAuthFilter(
//    private val jwtProvider: JwtProvider,
    private val jwsService: JwsService,
    private val webSocketConst: WebSocketConst,
    private val pathConfig: PathConfig
): WebFilter {

//    private val illegalTokenResponseBodyByteArray = Json.encodeToString(StdResponse(
//        HttpStatus.UNAUTHORIZED,
//        "Unauthorized",
//        RequireLegalTokenBody()
//    )).toByteArray()


    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (websocketHandshake(exchange.request)) {
            val idAndToken = getIdTokenPair(exchange.request)
            val id = idAndToken?.first
            val jws = idAndToken?.second
//            val jws = getWebsocketHandshakeToken(exchange.request)
            val monoClaims = jwsService.legalClaimsOrNull(jws)
            return monoClaims.flatMap { pair ->
                val claims = pair.second
                if (pair.first && claims != null
                    && id != null && id == claims.payload.subject.toLong()
                    ) {
//                    exchange.attributes[JWT_CLAIMS_ATTR_NAME] = claims
                    exchange.response.headers["Sec-WebSocket-Protocol"] = id.toString()
                    chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder
                            .withAuthentication(UsernamePasswordAuthenticationToken(
                                claims.payload.subject,
                                claims.payload.id
                            )))
                } else {
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                    val unauthJsonStr: String =
                        Json.encodeToString(StdResponse(
                            HttpStatus.UNAUTHORIZED,
                            "Unauthorized",
                            RequireLegalTokenBody()
                        ))
                    exchange.response
                        .writeWith(
                            Mono.just(
                                exchange.response.bufferFactory()
                                    .wrap(
                                        unauthJsonStr.toByteArray()
                                    )
                            )
                        )
                }
            }
        }

        if (preventCheck(exchange.request)) return chain.filter(exchange)
        val jws = extractTokenFromRequest(exchange.request)

        val monoClaims = jwsService.legalClaimsOrNull(jws)
        return monoClaims.flatMap { pair ->
            val claims = pair.second
            if (pair.first && claims != null) {
                exchange.attributes[JWT_CLAIMS_ATTR_NAME] = claims
                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder
                        .withAuthentication(UsernamePasswordAuthenticationToken(
                            claims.payload.subject,
                            claims.payload.id
                        )))
            } else {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.headers.contentType = MediaType.APPLICATION_JSON
//                val objectMapper = ObjectMapper()
                val unauthJsonStr: String =
                    Json.encodeToString(StdResponse(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        RequireLegalTokenBody()
                    ))
                exchange.response
                    .writeWith(
                        Mono.just(
                            exchange.response.bufferFactory()
                                .wrap(
                                    unauthJsonStr.toByteArray()
                                )
                        )
                    )
            }
        }

//        if (jws != null) {
//            val claims = jwtProvider.validateToken(jws)

//            if (claims != null) {
//                exchange.attributes[JWT_CLAIMS_ATTR_NAME] = claims
//                return chain.filter(exchange)
//                    .contextWrite(ReactiveSecurityContextHolder
//                        .withAuthentication(UsernamePasswordAuthenticationToken(
//                            claims.payload.subject,
//                            claims.payload.id
//                        )))
//            }
//        }
//        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
//        return exchange.response
//            .writeWith(Mono.just(exchange.response.bufferFactory()
////                .wrap(illegalTokenResponseBodyByteArray)
//                .wrap(
//                    Json.encodeToString(StdResponse(
//                        HttpStatus.UNAUTHORIZED,
//                        "Unauthorized",
//                        RequireLegalTokenBody()
//                    )).toByteArray()
//                )
////                .wrap("Unauthorized".toByteArray())
//            ))
    }

    private fun extractTokenFromRequest(request: ServerHttpRequest): String? {
        val bearerToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return null
    }

    val accountPath = pathConfig.prop.basePath + ACCOUNT_PATH
    val authPath = pathConfig.prop.basePath + AUTH_PATH

    private fun preventCheck(request: ServerHttpRequest): Boolean {
        return request.method == HttpMethod.POST
                && ((request.path.value() == accountPath)
                        || (request.path.value().startsWith(authPath)))
    }

    val websocketPath = pathConfig.prop.basePath + webSocketConst.websocketPath

    private fun websocketHandshake(request: ServerHttpRequest): Boolean {
        return request.path.value().startsWith(websocketPath)
    }

//    private fun getWebsocketHandshakeToken(request: ServerHttpRequest): String? {
//        val bearerToken = request.headers.getFirst("Sec-WebSocket-Protocol")
//        val reg = Regex("^Bearer,\\s*(\\S+)$")
//        if (bearerToken != null) {
//            // && bearerToken.startsWith("Bearer,")
//            val regResult = reg.find(bearerToken)
//            if (regResult != null) {
//                return regResult.groupValues[1]
//            }
////            return bearerToken.substring(7)
//        }
//        return null
//    }

    private fun getIdTokenPair(request: ServerHttpRequest): Pair<Long, String>? {
        val bearerToken = request.headers.getFirst("Sec-WebSocket-Protocol")
        return getIdAndToken(bearerToken)
    }
}