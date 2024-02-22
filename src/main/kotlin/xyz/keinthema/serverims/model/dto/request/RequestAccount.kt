package xyz.keinthema.serverims.model.dto.request

import kotlinx.serialization.Serializable
import xyz.keinthema.serverims.constant.RequestConst.Companion.EMAIL_LENGTH_LIMIT
import xyz.keinthema.serverims.constant.RequestConst.Companion.HASHED_PASSWORD_LENGTH
import xyz.keinthema.serverims.constant.RequestConst.Companion.NAME_LENGTH_LIMIT
import xyz.keinthema.serverims.constant.RequestConst.Companion.isLegalEmail
import xyz.keinthema.serverims.model.entity.Account

@Serializable
sealed interface RequestAccount {
    fun isLegal(): Boolean
}

data class RequestCreateAccount(
    val name: String,
    val hashedPw: String,
    val email: String
): RequestAccount {
    override fun isLegal(): Boolean {
        return name.length < NAME_LENGTH_LIMIT
                && hashedPw.length == HASHED_PASSWORD_LENGTH
                && email.length < EMAIL_LENGTH_LIMIT
                && email.isLegalEmail()
    }
}

data class RequestModifyAccount(
    val previousPw: String?,
    val hashedOnePw: String?,
    val accountModifiablePart: Account.Companion.AccountModifiablePart?
): RequestAccount {
    override fun isLegal(): Boolean {
        return ((previousPw != null && hashedOnePw != null) || (accountModifiablePart != null))
                && (if (previousPw != null) previousPw.length == HASHED_PASSWORD_LENGTH else true)
                && (if (hashedOnePw != null) hashedOnePw.length == HASHED_PASSWORD_LENGTH else true)
                && (accountModifiablePart?.isLegal() ?: true)
    }
    private fun Account.Companion.AccountModifiablePart.isLegal(): Boolean =
        (if (this.name != null) this.name.length < NAME_LENGTH_LIMIT else true)
                && (if (this.email != null) this.email.isLegalEmail()
                    && this.email.length < EMAIL_LENGTH_LIMIT else true)
}

data class RequestDeleteAccount(
    val hashedPw: String?
): RequestAccount {
    override fun isLegal(): Boolean {
        return if (hashedPw != null) hashedPw.length == HASHED_PASSWORD_LENGTH else true
    }
}