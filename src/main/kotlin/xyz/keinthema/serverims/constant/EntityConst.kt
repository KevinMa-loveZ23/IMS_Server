package xyz.keinthema.serverims.constant

import org.springframework.stereotype.Component

typealias AccountId = Long
typealias ServerId = Long
typealias ChatId = Int
typealias MessageId = Long
//typealias MessageType = Int
//typealias
@Component
class EntityConst {
    companion object {
        val WAITING_TIME_IN_MSEC = TimeConst.Companion.TimeInMsec.WEEK.msecTime * 2L
        enum class MessageType {
            TextMessage,
//            SubscribeMessage,
            MediaMessage
        }

        const val DEFAULT_CHAT_NAME = "default chat"
    }
//    final val waitingTimeInMsec = TimeConst.Companion.TimeInMsec.WEEK.msecTime * 2L
}