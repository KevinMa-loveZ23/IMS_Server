package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ResponseBodyAuth<T>: ResponseDataBody<T> {
}

@Serializable
data class LogInBody(
    val refreshToken: String
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
        return refreshToken.isEmpty()
    }
}

/**
 * Extremely weird when removing below @Serializable
 * Json.encodeToString() fails to find serializer of below class
 *
 * It turns out that Spring using Jackson for serialize object
 * and is much better than Json.encodeToString() ......
 * */
@Serializable
data class RequireLegalTokenBody(
    val illegalOrExpiredToken: Boolean
): ResponseBodyAuth<RequireLegalTokenBody> {
    constructor(): this(true)
    companion object {
        fun void(): RequireLegalTokenBody {
            return RequireLegalTokenBody(false)
        }
    }

    override fun isVoid(): Boolean {
        return !illegalOrExpiredToken
    }
}