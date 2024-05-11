package xyz.keinthema.serverims.constant

import org.springframework.stereotype.Component

@Component
class WebSocketConst {
//    final val userIdStr = "userId"
    final val websocketPath = "/websocket"
//    final val websocketPathWithUserId = "$websocketPath/{$userIdStr}"
    companion object {
        fun getIdAndToken(customProtocolString: String?): Pair<Long, String>? {
            if (customProtocolString == null) {
                return null
            }
            val reg = Regex("^(\\d+),\\s*(\\S+)$")
            val regResult = reg.find(customProtocolString)
            if (regResult != null) {
//                for (i in regResult.groupValues) {
//                    println(i)
//                }
//                println("end")
                return Pair(regResult.groupValues[1].toLong(), regResult.groupValues[2])
            }
            return null
        }
    }
}