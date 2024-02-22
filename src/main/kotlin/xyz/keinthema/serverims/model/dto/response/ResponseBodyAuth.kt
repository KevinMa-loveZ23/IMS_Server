package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
sealed interface ResponseBodyAuth<T>: ResponseDataBody<T> {
}

data class LogInBody(
    val jwtToken: String
): ResponseBodyAuth<LogInBody> {
//    override fun void(): LogInBody {
//        return LogInBody("")
//    }
    companion object{
        fun void(): LogInBody {
            return LogInBody("")
        }
    }

    override fun isVoid(): Boolean {
        return jwtToken.isEmpty()
    }
}