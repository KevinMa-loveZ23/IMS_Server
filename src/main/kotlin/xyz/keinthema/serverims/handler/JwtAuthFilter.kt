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
import xyz.keinthema.serverims.constant.ControllerConst.Companion.ACCOUNT_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.AUTH_PATH
import xyz.keinthema.serverims.constant.JwtConst.Companion.JWT_CLAIMS_ATTR_NAME
import xyz.keinthema.serverims.model.dto.response.RequireLegalTokenBody
import xyz.keinthema.serverims.model.dto.response.StdResponse
import xyz.keinthema.serverims.service.intf.JwsService


@Component
class JwtAuthFilter(
//    private val jwtProvider: JwtProvider,
    private val jwsService: JwsService
): WebFilter {

//    private val illegalTokenResponseBodyByteArray = Json.encodeToString(StdResponse(
//        HttpStatus.UNAUTHORIZED,
//        "Unauthorized",
//        RequireLegalTokenBody()
//    )).toByteArray()


    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
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
//                val responseBody = unauthorizedMonoResponse(RequireLegalTokenBody())
//                ServerResponse.status(HttpStatus.UNAUTHORIZED)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(BodyInserters.fromValue(responseBody))
//                    .flatMap {
//                        exchange.response.writeWith(Mono.just(responseBody).map { resData ->
//                            exchange.response.bufferFactory().wrap(
//                                resData.toString().toByteArray()
//
//                            )
//                        })
//                    }
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                val objectMapper = ObjectMapper()
                val unauthJsonStr: String =
                    Json.encodeToString(StdResponse(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        RequireLegalTokenBody()
                    ))
//                    try {
//                        val json = objectMapper.writeValueAsString(StdResponse(
//                            HttpStatus.UNAUTHORIZED,
//                            "Unauthorized",
//                            RequireLegalTokenBody()
//                        ))
//                        json
//                    } catch (e: JsonProcessingException) {
//                        e.printStackTrace()
//                        "{" +
//                                "\"httpStatusCode\":401,\"info\":\"Unauthorized\"," +
//                                "\"responseBody\":{\"illegalOrExpiredToken\": true,\"void\": false}" +
//                                "}"
//                    }
                exchange.response
                    .writeWith(
                        Mono.just(
                            exchange.response.bufferFactory()
                                .wrap(
//                                    Json.encodeToString(StdResponse(
//                                        HttpStatus.UNAUTHORIZED,
//                                        "Unauthorized",
//                                        RequireLegalTokenBody()
//                                    )).toByteArray()
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

    private fun preventCheck(request: ServerHttpRequest): Boolean {
        return request.method == HttpMethod.POST
                && ((request.path.value() == ACCOUNT_PATH)
                        || (request.path.value().startsWith(AUTH_PATH)))
    }
}