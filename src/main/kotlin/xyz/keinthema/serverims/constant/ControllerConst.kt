package xyz.keinthema.serverims.constant

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.model.dto.response.ResponseDataBody
import xyz.keinthema.serverims.model.dto.response.StdResponse

typealias MonoResponse<T> = Mono<ResponseEntity<StdResponse<T>>>

class ControllerConst {
    companion object {

        const val LOG_IN_PATH = "/login"

        const val ACCOUNT_PATH = "/account"
        const val ACCOUNT_ID_STR = "accountId"
        const val ACCOUNT_ID_PATH = "/{$ACCOUNT_ID_STR}"

        const val SERVER_PATH = "/server"
        const val SERVER_ID_STR = "serverId"
        const val SERVER_ID_PATH = "/{$SERVER_ID_STR}"

        const val SERVER_ADMIN_SERVER_ID_STR = SERVER_ID_STR
        const val SERVER_ADMIN_PATH = "/server/{$SERVER_ADMIN_SERVER_ID_STR}/admin"
        const val SERVER_ADMIN_ACCOUNT_ID_STR = ACCOUNT_ID_STR
        const val SERVER_ADMIN_ACCOUNT_ID_PATH = "/{$SERVER_ADMIN_ACCOUNT_ID_STR}"

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

        fun <T: ResponseDataBody<T>> notFoundMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.NOT_FOUND,
                "Not Found",
                responseDataBody
            ))
        }

        fun <T: ResponseDataBody<T>> internalServerErrorMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                responseDataBody
            ))
        }
    }
}