package xyz.keinthema.serverims.constant

class PasswordConst {
    companion object {
        const val ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        const val AN_STR_LEN = ALPHA_NUMERIC_STRING.length
        const val SALT_LENGTH = 10

        fun ByteArray.toLowerCaseHexString() = joinToString("") { "%02x".format(it) }

        fun CharSequence.plus(str: String) = "$this$str"

    }
}