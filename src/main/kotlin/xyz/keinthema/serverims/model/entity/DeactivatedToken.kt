package xyz.keinthema.serverims.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import xyz.keinthema.serverims.constant.ServiceConst.Companion.DEACTIVATED_TOKEN_COLL_NAME
import java.util.Date
import java.util.UUID

@Document(collection = DEACTIVATED_TOKEN_COLL_NAME)
data class DeactivatedToken(
    @Id val id: UUID,
    val expireAt: Date
)
