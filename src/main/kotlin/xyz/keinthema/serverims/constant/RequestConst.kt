package xyz.keinthema.serverims.constant

import jakarta.mail.internet.AddressException
import jakarta.mail.internet.InternetAddress

class RequestConst {
    companion object {
        const val NAME_LENGTH_LIMIT = 64
        const val HASHED_PASSWORD_LENGTH = 128
        const val EMAIL_LENGTH_LIMIT = 128
        fun String.isLegalEmail(): Boolean =
             try {
                 InternetAddress(this).validate()
                 true
             } catch (exp: AddressException) {
                 false
             }
        const val DESCRIPTION_LENGTH_LIMIT = 3 * 500
    }
}