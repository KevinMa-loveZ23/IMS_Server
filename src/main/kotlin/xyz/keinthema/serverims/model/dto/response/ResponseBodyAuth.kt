@file:UseSerializers(LongAsStringSerializer::class)
package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.LongAsStringSerializer

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

@Serializable
data class RenewRefreshTokenBody(
    val newRefreshToken: String
): ResponseBodyAuth<RenewRefreshTokenBody> {
    companion object {
        fun void(): RenewRefreshTokenBody {
            return RenewRefreshTokenBody("")
        }
    }
    override fun isVoid(): Boolean {
        return newRefreshToken.isEmpty()
    }
}

@Serializable
data class LogOutBody(
    val success: Boolean
): ResponseBodyAuth<LogOutBody> {
    companion object {
        fun void(): LogOutBody {
            return LogOutBody(false)
        }
    }
    override fun isVoid(): Boolean {
        return !success
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