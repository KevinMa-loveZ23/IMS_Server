package xyz.keinthema.serverims.constant

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.model.dto.response.ResponseDataBody
import xyz.keinthema.serverims.model.dto.response.StdResponse

typealias MonoResponse<T> = Mono<ResponseEntity<StdResponse<T>>>

class ControllerConst {
    companion object {
        const val ACCOUNT_ID_STR = "accountId"
        const val ACCOUNT_ID_PATH = "/{$ACCOUNT_ID_STR}"

        const val SERVER_ID_STR = "serverId"
        const val SERVER_ID_PATH = "/{$SERVER_ID_STR}"

        fun <T: ResponseDataBody<T>> badRequestMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.BAD_REQUEST,
                "Illegal Parameters",
                responseDataBody
            ))
        }
        fun <T: ResponseDataBody<T>> unauthorizedMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                responseDataBody
            ))
        }
    }
}