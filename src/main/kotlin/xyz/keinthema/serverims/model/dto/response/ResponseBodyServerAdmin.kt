package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable

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
    val adminIdNamePairs: MutableSet<Pair<Long, String>>?
): ResponseBodyServerAdmin<AdminServerInfoBody> {
    companion object {
        fun void(): AdminServerInfoBody {
            return AdminServerInfoBody(null)
        }
    }

    override fun isVoid(): Boolean = adminIdNamePairs == null
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