package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable
import xyz.keinthema.serverims.model.entity.Server

@Serializable
sealed interface ResponseBodyServer<T>: ResponseDataBody<T> {
}

@Serializable
data class ServerCreateBody(
    val serverId: Long,
    val name: String,
    val owner: Long
): ResponseBodyServer<ServerCreateBody> {
    companion object {
        fun void(): ServerCreateBody {
            return ServerCreateBody(-1, "", -1)
        }
    }

    override fun isVoid(): Boolean = serverId == -1L
}

@Serializable
data class ServerInfoBody(
    val serverId: Long,
    val name: String,
    val owner: Long,
    val description: String
): ResponseBodyServer<ServerInfoBody> {
    companion object {
        fun void(): ServerInfoBody {
            return ServerInfoBody(
                serverId = -1,
                name = "",
                owner = -1,
                description = ""
            )
        }
    }

    override fun isVoid(): Boolean = serverId == -1L
}

@Serializable
data class ServerModifyBody(
    val serverModifiablePart: Server.Companion.ServerModifiablePart
): ResponseBodyServer<ServerModifyBody> {
    companion object {
        fun void(): ServerModifyBody {
            return ServerModifyBody(Server.Companion.ServerModifiablePart(
                name = null,
                owner = null,
//                addAdmin = null,
//                minusAdmin = null,
                description = null
            ))
        }
    }

    override fun isVoid(): Boolean {
        return serverModifiablePart.name == null
                && serverModifiablePart.owner == null
                && serverModifiablePart.description == null
    }
}

@Serializable
data class ServerDeleteBody(
    val serverId: Long
): ResponseBodyServer<ServerDeleteBody> {
    companion object {
        fun void(): ServerDeleteBody {
            return ServerDeleteBody(-1)
        }
    }

    override fun isVoid(): Boolean = serverId == -1L
}