package xyz.keinthema.serverims.model.entity

import kotlinx.serialization.Serializable
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Update
import xyz.keinthema.serverims.constant.ServiceConst.Companion.ACCOUNT_COLL_NAME


@Document(collection = ACCOUNT_COLL_NAME)
data class Account(
    @Id val id: Long,
    val name: String,
    val password: String,
    val email: String,
    val publishEmail: Boolean = true,
    val servers: MutableSet<Long>,
    val publishServer: Boolean = true,
    val serverCreateTimes: Int = 0
) {
    companion object {
        fun void(): Account {
            return Account(-1, "nobody", "no password", "no@em.ail", servers = mutableSetOf())
        }
        @Serializable
        data class AccountModifiablePart(
            val name: String?,
            val email: String?,
            val publishEmail: Boolean?,
            val publishServer: Boolean?
        ) {
            constructor(account: Account?) : this(
                name = account?.name,
                email = account?.email,
                publishEmail = account?.publishEmail,
                publishServer = account?.publishServer
            )

            fun getUpdateObj(): Update {
                val update = Update()
                if (name != null) update.set("name", name)
                if (email != null) update.set("email", email)
                if (publishEmail != null) update.set("publishEmail", publishEmail)
                if (publishServer != null) update.set("publishServer", publishServer)
                return update
            }
        }
    }
    fun isVoid(): Boolean = (id == -1L)
}
