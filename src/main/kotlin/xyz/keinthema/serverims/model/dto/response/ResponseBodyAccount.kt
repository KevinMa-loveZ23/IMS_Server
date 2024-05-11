@file:UseSerializers(LongAsStringSerializer::class)
package xyz.keinthema.serverims.model.dto.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.LongAsStringSerializer
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.ServerId
import xyz.keinthema.serverims.model.entity.Account

@Serializable
sealed interface ResponseBodyAccount<T>: ResponseDataBody<T> {
}

@Serializable
data class AccountCreateBody(
    val id: Long,
    val name: String
): ResponseBodyAccount<AccountCreateBody> {
    companion object {
        fun void(): AccountCreateBody {
            return AccountCreateBody(-1, "VOID")
        }
    }

    override fun isVoid(): Boolean = id == -1L
}

@Serializable
data class AccountInfoBody(
    val id: Long,
    val name: String,
    val email: String? = null,
    val publishEmail: Boolean? = null,
    val servers: MutableSet<Long>? = null,
    val publishServer: Boolean? = null,
    val serverCreateTimes: Int? = null
): ResponseBodyAccount<AccountInfoBody> {
    constructor(account: Account) : this(
        id = account.id,
        name = account.name,
        email = account.email,
        publishEmail = account.publishEmail,
        servers = account.servers,
        publishServer = account.publishServer,
        serverCreateTimes = account.serverCreateTimes
    )
    companion object {
        fun publishedInfo(account: Account): AccountInfoBody {
            return AccountInfoBody(
                id = account.id,
                name = account.name,
                email = if (account.publishEmail) account.email else null,
                servers = if (account.publishServer) account.servers else null
            )
        }

        fun void(): AccountInfoBody {
            return AccountInfoBody(-1L,"VOID")
        }
    }

    override fun isVoid(): Boolean = id == -1L
}

@Serializable
data class AccountModifyBody(
    val passwordModified: Boolean,
    val modifiablePart: Account.Companion.AccountModifiablePart?
): ResponseBodyAccount<AccountModifyBody> {
    companion object {
        fun void(): AccountModifyBody {
            return AccountModifyBody(false, null)
        }
    }

    override fun isVoid(): Boolean = !passwordModified && modifiablePart == null
}

@Serializable
data class AccountDeleteBody(
    val id: Long
): ResponseBodyAccount<AccountDeleteBody> {
    companion object {
        fun void(): AccountDeleteBody {
            return AccountDeleteBody(-1)
        }
    }

    override fun isVoid(): Boolean = id == -1L
}

@Serializable
data class AccountNamesBody(
    val nameMap: Map<AccountId, String>
): ResponseBodyAccount<AccountNamesBody> {
    companion object {
        fun void(): AccountNamesBody {
            return AccountNamesBody(mapOf())
        }
    }
    override fun isVoid(): Boolean = nameMap.isEmpty()
}

@Serializable
data class AccountServersBody(
    val serverNameMap: Map<ServerId, String>
): ResponseBodyAccount<AccountServersBody> {
    companion object {
        fun void(): AccountServersBody {
            return AccountServersBody(mapOf())
        }
    }

    override fun isVoid(): Boolean = serverNameMap.isEmpty()
}