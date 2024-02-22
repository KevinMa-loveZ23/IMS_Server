package xyz.keinthema.serverims.controller.server

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_ADMIN_ACCOUNT_ID_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_ADMIN_ACCOUNT_ID_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_ADMIN_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_ADMIN_SERVER_ID_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.internalServerErrorMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.unauthorizedMonoResponse
import xyz.keinthema.serverims.constant.JwtConst
import xyz.keinthema.serverims.constant.MonoResponse
import xyz.keinthema.serverims.model.dto.response.AdminServerAddBody
import xyz.keinthema.serverims.model.dto.response.AdminServerDeleteBody
import xyz.keinthema.serverims.model.dto.response.AdminServerInfoBody
import xyz.keinthema.serverims.model.dto.response.StdResponse
import xyz.keinthema.serverims.service.intf.ServerAdminService

@RestController
@RequestMapping(SERVER_ADMIN_PATH)
class ServerAdminController(private val serverAdminService: ServerAdminService) {
    @PostMapping(SERVER_ADMIN_ACCOUNT_ID_PATH)
    fun addAdmin(
        @PathVariable(SERVER_ADMIN_SERVER_ID_STR) serverId: Long,
        @PathVariable(SERVER_ADMIN_ACCOUNT_ID_STR) accountId: Long,
        @RequestAttribute(JwtConst.JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<AdminServerAddBody> {
        val jwtId = claims.payload.subject.toLong()
        return serverAdminService.isLegalToModifyAdmin(
            operator = jwtId,
            variedId = accountId,
            serverId = serverId
        ).flatMap { ok ->
            if (ok) {
                serverAdminService.addAdminToServer(
                    serverId = serverId,
                    adminId = accountId
                )
                    .flatMap { newAdminSet ->
                        if (newAdminSet == null) {
                            internalServerErrorMonoResponse(AdminServerAddBody.void())
                        } else {
                            Mono.just(StdResponse.makeResponseEntity(
                                HttpStatus.OK,
                                "Admin Added",
                                AdminServerAddBody(true)
                            ))
                        }
                    }
            } else {
                unauthorizedMonoResponse(AdminServerAddBody.void())
            }
        }
    }

    @GetMapping
    fun getAdmins(
        @PathVariable(SERVER_ADMIN_SERVER_ID_STR) serverId: Long,
        @RequestAttribute(JwtConst.JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<AdminServerInfoBody> {
        val jwtId = claims.payload.subject.toLong()

        return serverAdminService.isLegalToGetAdmin(
            operator = jwtId,
            serverId = serverId
        ).flatMap { ok ->
            if (ok) {
                serverAdminService.getAdminsFromServer(serverId)
                    .flatMap { adminSet ->
                        if (adminSet == null) {
                            internalServerErrorMonoResponse(AdminServerInfoBody.void())
                        } else {
                            Mono.just(StdResponse.makeResponseEntity(
                                HttpStatus.OK,
                                "Get Admins",
                                AdminServerInfoBody(adminSet)
                            ))
                        }
                    }
            } else {
                unauthorizedMonoResponse(AdminServerInfoBody.void())
            }
        }
    }

    @DeleteMapping(SERVER_ADMIN_ACCOUNT_ID_PATH)
    fun deleteAdmin(
        @PathVariable(SERVER_ADMIN_SERVER_ID_STR) serverId: Long,
        @PathVariable(SERVER_ADMIN_ACCOUNT_ID_STR) accountId: Long,
        @RequestAttribute(JwtConst.JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<AdminServerDeleteBody> {
        val jwtId = claims.payload.subject.toLong()

        return serverAdminService.isLegalToModifyAdmin(
            operator = jwtId,
            variedId = accountId,
            serverId = serverId
        )
            .flatMap { ok ->
                if (ok) {
                    serverAdminService.deleteAdminFromServer(
                        serverId = serverId,
                        adminId = accountId
                    )
                        .flatMap { newAdminSet ->
                            if (newAdminSet == null) {
                                internalServerErrorMonoResponse(AdminServerDeleteBody.void())
                            } else {
                                Mono.just(StdResponse.makeResponseEntity(
                                    HttpStatus.OK,
                                    "Delete Success",
                                    AdminServerDeleteBody(true)
                                ))
                            }
                        }
                } else {
                    unauthorizedMonoResponse(AdminServerDeleteBody.void())
                }
            }
    }
}