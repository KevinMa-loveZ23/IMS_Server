package xyz.keinthema.serverims.constant

class TimeConst {
    companion object {
        enum class TimeInMsec(val msecTime: Long) {
            SECOND(1000L), // 1000 msec = 1 sec
            MINUTE(SECOND.msecTime * 60L), // 60 sec = 1 min
            HOUR(MINUTE.msecTime * 60L), // 60 min = 1 h
            DAY(HOUR.msecTime * 24L), // 24 h = 1 d
            WEEK(DAY.msecTime * 7L), // 7 d = 1 week
            MONTH(DAY.msecTime * 30L) // 30 d = 1 month
        }

//        fun getMillSecond(): Long {
//            return System.currentTimeMillis()
//        }
    }
}