package xyz.keinthema.serverims.model.entity

import kotlinx.serialization.Serializable
import xyz.keinthema.serverims.constant.EntityConst

@Serializable
sealed interface ServerEntryStrategy {
    val type: StrategyType
    enum class StrategyType {
        CodeCheck,
        AdminCheck
    }
}

@Serializable
data class CodeCheckStrategy(
    override val type: ServerEntryStrategy.StrategyType,
    val codeList: MutableSet<String>
): ServerEntryStrategy {
    constructor(codeList: MutableSet<String>): this(
        type = ServerEntryStrategy.StrategyType.CodeCheck,
        codeList = codeList
    )
}

data class AdminCheckStrategy(
    override val type: ServerEntryStrategy.StrategyType,
    val waitingTimeInMsec: Long
): ServerEntryStrategy {
    constructor(waitingTimeInMsec: Long): this(
        type = ServerEntryStrategy.StrategyType.AdminCheck,
        waitingTimeInMsec = waitingTimeInMsec
    )
    constructor(): this(
        waitingTimeInMsec = EntityConst.WAITING_TIME_IN_MSEC
    )
}