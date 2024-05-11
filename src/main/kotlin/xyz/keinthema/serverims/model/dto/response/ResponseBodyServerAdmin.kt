@file:UseSerializers(LongAsStringSerializer::class)
package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.LongAsStringSerializer

@Serializable
sealed interface ResponseBodyServerAdmin<T>: ResponseDataBody<T> {
}

@Serializable
data class AdminServerAddBody(
    val success: Boolean
): ResponseBodyServerAdmin<AdminServerAddBody> {
    companion object {
        fun void(): AdminServerAddBody {
            return AdminServerAddBody(false)
        }
    }

    override fun isVoid(): Boolean = !success
}

@Serializable
data class AdminServerInfoBody(
    val adminIdNameMap: Map<Long, String>?
): ResponseBodyServerAdmin<AdminServerInfoBody> {
    companion object {
        fun void(): AdminServerInfoBody {
            return AdminServerInfoBody(null)
        }
    }

    override fun isVoid(): Boolean = adminIdNameMap == null
}

@Serializable
data class AdminServerDeleteBody(
    val success: Boolean
): ResponseBodyServerAdmin<AdminServerDeleteBody> {
    companion object {
        fun void(): AdminServerDeleteBody {
            return AdminServerDeleteBody(false)
        }
    }

    override fun isVoid(): Boolean = !success
}