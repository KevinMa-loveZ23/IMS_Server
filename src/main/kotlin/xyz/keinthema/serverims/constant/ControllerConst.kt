package xyz.keinthema.serverims.constant

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.model.dto.response.ResponseDataBody
import xyz.keinthema.serverims.model.dto.response.StdResponse

typealias MonoResponse<T> = Mono<ResponseEntity<StdResponse<T>>>

class ControllerConst {
    companion object {

        const val AUTH_PATH = "/auth"
        const val LOG_IN_PATH = "/login"
        const val REFRESH_PATH = "/refresh"
        const val LOG_OUT_PATH = "/logout"

        const val ACCOUNT_PATH = "/account"
        const val ACCOUNT_ID_STR = "accountId"
        const val ACCOUNT_ID_PATH = "/{$ACCOUNT_ID_STR}"
        const val ACCOUNT_NAMES_PATH = "/names"
        const val ACCOUNT_SERVERS_PATH = "$ACCOUNT_ID_PATH/servers"

        const val SERVER_PATH = "/server"
        const val SERVER_ID_STR = "serverId"
        const val SERVER_ID_PATH = "/{$SERVER_ID_STR}"

        const val SERVER_ADMIN_SERVER_ID_STR = SERVER_ID_STR
        const val SERVER_ADMIN_PATH = "/server/{$SERVER_ADMIN_SERVER_ID_STR}/admin"
        const val SERVER_ADMIN_ACCOUNT_ID_STR = ACCOUNT_ID_STR
        const val SERVER_ADMIN_ACCOUNT_ID_PATH = "/{$SERVER_ADMIN_ACCOUNT_ID_STR}"

        const val SERVER_MEMBER_SERVER_ID_STR = SERVER_ID_STR
        const val SERVER_MEMBER_PATH = "/server/{$SERVER_MEMBER_SERVER_ID_STR}/member"
        const val SERVER_MEMBER_WAITING_PATH = "/waiting"
        const val SERVER_MEMBER_ACCOUNT_ID_STR = ACCOUNT_ID_STR
        const val SERVER_MEMBER_ACCOUNT_ID_PATH = "/{$SERVER_MEMBER_ACCOUNT_ID_STR}"
        const val SERVER_MEMBER_JOIN_CODE_STR = "code"
        const val SERVER_MEMBER_JOIN_CODE_DEFAULT = "0"
        const val SERVER_MEMBER_CODE_PATH = "/code"
        const val SERVER_MEMBER_CODE_MESSAGE_STR = "msg"

        const val SERVER_CHAT_SERVER_ID_STR = SERVER_ID_STR
        const val SERVER_CHAT_PATH = "/server/{$SERVER_CHAT_SERVER_ID_STR}/chat"
        const val SERVER_CHAT_CHAT_ID_STR = "chatId"
        const val SERVER_CHAT_ID_PATH = "/{$SERVER_CHAT_CHAT_ID_STR}"
        const val SERVER_CHAT_IMG_PATH = "$SERVER_CHAT_ID_PATH/img"
        const val SERVER_CHAT_IMG_NAME_STR = "imgFullName"
        const val SERVER_CHAT_IMG_NAME_PATH = "/{$SERVER_CHAT_IMG_NAME_STR}"
        const val SERVER_CHAT_IMG_WITH_NAME_PATH = "$SERVER_CHAT_IMG_PATH$SERVER_CHAT_IMG_NAME_PATH"
        const val SERVER_CHAT_MESSAGE_PATH = "$SERVER_CHAT_ID_PATH/message"
        const val SERVER_CHAT_MESSAGE_UNTIL_STR = "until"
        const val SERVER_CHAT_MESSAGE_UNTIL_DEFAULT = "0"
        const val SERVER_CHAT_MESSAGE_NUMBER_STR = "num"
        const val SERVER_CHAT_MESSAGE_NUMBER_DEFAULT = "20"

        /**
         * 400
         * */
        fun <T: ResponseDataBody<T>> badRequestMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.BAD_REQUEST,
                "Illegal Parameters",
                responseDataBody
            ))
        }
        /**
         * 401
         * */
        fun <T: ResponseDataBody<T>> unauthorizedMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                responseDataBody
            ))
        }
        /**
         * 403
         * */
        fun <T: ResponseDataBody<T>> forbiddenMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                responseDataBody
            ))
        }
        /**
         * 404
         * */
        fun <T: ResponseDataBody<T>> notFoundMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.NOT_FOUND,
                "Not Found",
                responseDataBody
            ))
        }
        /**
         * 400
         * */
        fun <T: ResponseDataBody<T>> tooManyRequestsMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                responseDataBody
            ))
        }
        /**
         * 500
         * */
        fun <T: ResponseDataBody<T>> internalServerErrorMonoResponse(responseDataBody: T): MonoResponse<T> {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                responseDataBody
            ))
        }
    }
}