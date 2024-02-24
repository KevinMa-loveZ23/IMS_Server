package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@Serializable
data class StdResponse<T: ResponseDataBody<T>> (
    val httpStatusCode: Int,
    val info: String,
    val responseBody: T
) {
    constructor(
        httpStatus: HttpStatus,
        info: String,
        responseBody: T
    ): this(
        httpStatusCode = httpStatus.value(),
        info = info,
        responseBody = responseBody
    )
    companion object {
        fun <R: ResponseDataBody<R>> makeResponseEntity(httpStatus: HttpStatus,
                                                     info: String,
                                                     responseBody: R
        ): ResponseEntity<StdResponse<R>> {
            return ResponseEntity
                .status(httpStatus)
                .body(StdResponse(httpStatus, info, responseBody))
        }
        fun <R: ResponseDataBody<R>> makeResponseEntity(httpStatus: HttpStatus,
                                                     headers: HttpHeaders,
                                                     info: String,
                                                     responseBody: R
        ): ResponseEntity<StdResponse<R>> {
            return ResponseEntity
                .status(httpStatus)
                .headers(headers)
                .body(StdResponse(httpStatus, info, responseBody))
        }
    }
}