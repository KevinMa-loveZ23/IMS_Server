package xyz.keinthema.serverims.controller.account

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.ControllerConst.Companion.LOG_IN_PATH
import xyz.keinthema.serverims.constant.MonoResponse
import xyz.keinthema.serverims.handler.JwtProvider
import xyz.keinthema.serverims.model.dto.request.RequestLogIn
import xyz.keinthema.serverims.model.dto.response.LogInBody
import xyz.keinthema.serverims.model.dto.response.StdResponse

@RestController
class AuthController(private val authenticationManager: ReactiveAuthenticationManager,
                     private val jwtProvider: JwtProvider) {

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
                val jwtToken = jwtProvider.createJwtToken(requestLogIn.id)
                val responseHeaders = HttpHeaders()
                responseHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
                Mono.just(StdResponse
                    .makeResponseEntity(HttpStatus.OK,
                        responseHeaders,
                        "Logged In",
                        LogInBody(jwtToken)
                    ))
            }.onErrorResume {
                Mono.just(StdResponse
                    .makeResponseEntity(HttpStatus.UNAUTHORIZED,
                        "Log In Failed",
                        LogInBody.void()
                    ))
            }
    }
}
