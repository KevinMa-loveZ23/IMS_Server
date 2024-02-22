package xyz.keinthema.serverims.handler

import org.springframework.security.crypto.password.PasswordEncoder
import xyz.keinthema.serverims.constant.PasswordConst
import xyz.keinthema.serverims.constant.PasswordConst.Companion.plus
import xyz.keinthema.serverims.constant.PasswordConst.Companion.toLowerCaseHexString
import java.security.MessageDigest
import java.security.SecureRandom

class ShaSaltedPasswordEncoder : PasswordEncoder {

    private val digest = MessageDigest.getInstance("SHA-512")

    override fun encode(rawPassword: CharSequence?): String {
        if (rawPassword == null) {
            throw IllegalArgumentException("Password must not be null")
        }
        val salt = generateSalt()
        val saltedPassword = digest.digest(rawPassword.plus(salt).toByteArray()).toLowerCaseHexString()
        return salt.plus(saltedPassword)
    }

    override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
        if (rawPassword == null || encodedPassword == null) {
            return false
        }
        val salt = getSaltFromEncodedPassword(encodedPassword)
        val saltedPassword = digest.digest(rawPassword.plus(salt).toByteArray()).toLowerCaseHexString()
        return salt.plus(saltedPassword) == encodedPassword
    }

    private fun generateSalt(saltLength: Int = PasswordConst.SALT_LENGTH): String {
        val secureRandom = SecureRandom()
        return (0 until saltLength)
            .map { PasswordConst.ALPHA_NUMERIC_STRING[secureRandom.nextInt(PasswordConst.AN_STR_LEN)] }
            .joinToString("")
    }

    private fun getSaltFromEncodedPassword(encodedPassword: String, saltLength: Int = PasswordConst.SALT_LENGTH): String {
        return encodedPassword.substring(0, saltLength)
    }
}