package xyz.keinthema.serverims.constant

import java.util.UUID

class ServiceConst {
    companion object {
        const val ACCOUNT_COLL_NAME = "accounts"
        const val SERVER_COLL_NAME = "servers"
        const val REVOKED_TOKEN_COLL_NAME = "revokedTokens"

        const val REDIS_REVOKED_TOKEN_PREFIX = "revokedToken:"
        fun getKeyForRevokedUUID(uuid: UUID): String
        = REDIS_REVOKED_TOKEN_PREFIX + uuid.toString()

        const val REDIS_SERVER_ENTRY_PREFIX = "serverEntry:"
        fun getKeyPatternBaseForServerEntryWaiting(serverId: ServerId): String
        = "$REDIS_SERVER_ENTRY_PREFIX$serverId"
        fun getKeyPatternForServerEntryWaiting(serverId: ServerId): String
        = getKeyPatternBaseForServerEntryWaiting(serverId) + ":*"
        fun getKeyForServerEntryWaiting(serverId: ServerId, userId: AccountId): String
        = "${getKeyPatternBaseForServerEntryWaiting(serverId)}:$userId"

//        const val IMAGE_DIR = "img/"
    }
}