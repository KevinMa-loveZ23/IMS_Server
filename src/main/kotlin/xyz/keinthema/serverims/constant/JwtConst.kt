package xyz.keinthema.serverims.constant

class JwtConst {
    companion object {

        enum class TimeInMsec(val msecTime: Long) {
            SECOND(1000L), // 1000 msec = 1 sec
            MINUTE(SECOND.msecTime * 60L), // 60 sec = 1 min
            HOUR(MINUTE.msecTime * 60L), // 60 min = 1 h
            DAY(HOUR.msecTime * 24L), // 24 h = 1 d
            WEEK(DAY.msecTime * 7L), // 7 d = 1 week
            MONTH(DAY.msecTime * 30L) // 30 d = 1 month
        }

        const val ISSUER = "ims.kein-thema.xyz"
        const val TOKEN_TYPE = "token_type"
        enum class TokenType(val str: String, val validityMsec: Long) {
            ACCESS("access", TimeInMsec.MINUTE.msecTime * 30L), // 30 min
            REFRESH("refresh", TimeInMsec.WEEK.msecTime) // 1 week
        }

        const val JWT_CLAIMS_ATTR_NAME = "jwtClaims"
    }
}