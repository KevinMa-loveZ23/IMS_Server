package xyz.keinthema.serverims.controller.account

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.ControllerConst.Companion.AUTH_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.LOG_IN_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.LOG_OUT_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.REFRESH_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.badRequestMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.forbiddenMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.internalServerErrorMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.tooManyRequestsMonoResponse
import xyz.keinthema.serverims.constant.JwtConst
import xyz.keinthema.serverims.constant.MonoResponse
import xyz.keinthema.serverims.model.dto.request.RequestLogIn
import xyz.keinthema.serverims.model.dto.request.RequestRenewRefreshToken
import xyz.keinthema.serverims.model.dto.response.LogInBody
import xyz.keinthema.serverims.model.dto.response.LogOutBody
import xyz.keinthema.serverims.model.dto.response.RenewRefreshTokenBody
import xyz.keinthema.serverims.model.dto.response.StdResponse
import xyz.keinthema.serverims.service.intf.AuthService
import xyz.keinthema.serverims.service.intf.JwsService

@RestController
@RequestMapping(AUTH_PATH)
class AuthController(
    private val authenticationManager: ReactiveAuthenticationManager,
//    private val jwtProvider: JwtProvider,
    private val authService: AuthService,
    private val jwsService: JwsService
) {

    @PostMapping(LOG_IN_PATH)
    fun logIn(@RequestBody requestLogIn: RequestLogIn): MonoResponse<LogInBody> {
//        return Mono.fromCallable {
//            authenticationManager
//                .authenticate(UsernamePasswordAuthenticationToken(requestLogIn.id, requestLogIn.hashedPw))
//        }.flatMap { auth ->
//            val jwtToken = jwtProvider.createJwtToken(requestLogIn.id)
//            val responseHeaders = HttpHeaders()
//            responseHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
//            Mono.just(StdResponse
//                .makeResponseEntity(HttpStatus.OK,
//                    responseHeaders,
//                    "Logged In",
//                    LogInBody(jwtToken)
//                ))
//        }.onErrorResume {
//            Mono.just(StdResponse
//                .makeResponseEntity(HttpStatus.UNAUTHORIZED,
//                    "Log In Failed",
//                    LogInBody("")
//                ))
//        }
        return authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(requestLogIn.id, requestLogIn.hashedPw))
            .flatMap { auth ->
                val refreshToken = authService.getNewRefreshToken(requestLogIn.id)
//                val refreshToken = jwtProvider.createRefreshToken(requestLogIn.id)
                val responseHeaders = HttpHeaders()
                responseHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer $refreshToken")
                Mono.just(StdResponse
                    .makeResponseEntity(HttpStatus.OK,
                        responseHeaders,
                        "Logged In",
                        LogInBody(refreshToken)
                    ))
            }.onErrorResume {
                forbiddenMonoResponse(LogInBody.void())
//                Mono.just(StdResponse
//                    .makeResponseEntity(HttpStatus.UNAUTHORIZED,
//                        "Log In Failed",
//                        LogInBody.void()
//                    ))
            }
    }

    @PostMapping(REFRESH_PATH)
    fun renewRefreshToken(
        @RequestBody requestRenewRefreshToken: RequestRenewRefreshToken
    ):  MonoResponse<RenewRefreshTokenBody> {
        if ( !requestRenewRefreshToken.isLegal()) {
            return badRequestMonoResponse(RenewRefreshTokenBody.void())
        }
        return jwsService.legalClaimsOrNull(requestRenewRefreshToken.oldRefreshToken)
            .flatMap { claimsPair ->
                val claims = claimsPair.second
                if ( !claimsPair.first || claims == null) {
                    badRequestMonoResponse(RenewRefreshTokenBody.void())
                } else if ( !authService.isLegalToRenewToken(claims)) {
                    tooManyRequestsMonoResponse(RenewRefreshTokenBody.void())
                } else {
                    authService.renewRefreshToken(claims)
                        .flatMap { newJws ->
                            Mono.just(StdResponse.makeResponseEntity(
                                HttpStatus.OK,
                                "Renewed Refresh Token",
                                RenewRefreshTokenBody(newJws)
                            ))
                        }
                }
            }
    }

    @DeleteMapping(LOG_OUT_PATH)
    fun logOut(
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<LogOutBody> {
        return authService.revokeRefreshToken(claims)
            .flatMap {
                if (it) {
                    Mono.just(StdResponse.makeResponseEntity(
                        HttpStatus.OK,
                        "Logged Out",
                        LogOutBody(true)
                    ))
                } else {
                    internalServerErrorMonoResponse(LogOutBody.void())
                }
            }
    }
}
