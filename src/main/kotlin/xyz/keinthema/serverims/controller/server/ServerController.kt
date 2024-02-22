package xyz.keinthema.serverims.controller.server

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_ID_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_ID_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.badRequestMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.internalServerErrorMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.notFoundMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.unauthorizedMonoResponse
import xyz.keinthema.serverims.constant.JwtConst
import xyz.keinthema.serverims.constant.MonoResponse
import xyz.keinthema.serverims.model.dto.request.RequestCreateServer
import xyz.keinthema.serverims.model.dto.request.RequestDeleteServer
import xyz.keinthema.serverims.model.dto.request.RequestModifyServer
import xyz.keinthema.serverims.model.dto.response.*
import xyz.keinthema.serverims.service.intf.ServerService

@RestController
@RequestMapping(SERVER_PATH)
class ServerController(private val serverService: ServerService) {

    @PostMapping
    fun createServer(
        @RequestBody requestCreateServer: RequestCreateServer,
        @RequestAttribute(JwtConst.JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ServerCreateBody> {
        if ( !requestCreateServer.isLegal()) {
            return badRequestMonoResponse(ServerCreateBody.void())
        }
        val jwtId = claims.payload.subject.toLong()
        return serverService
            .createServer(jwtId, requestCreateServer.name, requestCreateServer.description)
            .flatMap { server ->
                if (server == null) {
                    badRequestMonoResponse(ServerCreateBody.void())
                } else {
                    Mono.just(StdResponse.makeResponseEntity(
                        HttpStatus.CREATED,
                        "Server Created",
                        ServerCreateBody(serverId = server.id, name = server.name, owner = jwtId)
                    ))
                }
            }
    }

    @GetMapping(SERVER_ID_PATH)
    fun getServerInfo(
        @PathVariable(SERVER_ID_STR) serverId: Long,
        @RequestAttribute(JwtConst.JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ServerInfoBody> {
        val jwtId = claims.payload.subject.toLong()
        return serverService.getServerById(serverId)
            .flatMap { server ->
                if (server == null) {
                    notFoundMonoResponse(ServerInfoBody.void())
                } else {
                    Mono.just(StdResponse.makeResponseEntity(
                        HttpStatus.OK,
                        "Get Server Info Success",
                        ServerInfoBody(
                            serverId = server.id,
                            name = server.name,
                            owner = server.owner,
                            description = server.description
                        )
                    ))
                }
            }
    }

    @PutMapping(SERVER_ID_PATH)
    fun modifyServer(
        @RequestBody requestModifyServer: RequestModifyServer,
        @PathVariable(SERVER_ID_STR) serverId: Long,
        @RequestAttribute(JwtConst.JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ServerModifyBody> {
        if ( !requestModifyServer.isLegal()) {
            badRequestMonoResponse(ServerModifyBody.void())
        }
        val jwtId = claims.payload.subject.toLong()

        return serverService
            .isLegalToModifyServerInfo(
                jwtId,
                serverId,
                requestModifyServer.serverModifiablePart
            )
            .flatMap { ok ->
                if ( !ok) {
                    unauthorizedMonoResponse(ServerModifyBody.void())
                } else {
                    serverService
                        .modifyServerInfo(
                            serverId,
                            jwtId,
                            requestModifyServer.serverModifiablePart
                        )
                        .flatMap { server ->
                            if (server == null || server.isVoid()) {
                                internalServerErrorMonoResponse(ServerModifyBody.void())
                            } else {
                                Mono.just(StdResponse.makeResponseEntity(
                                    HttpStatus.OK,
                                    "Modify Success",
                                    ServerModifyBody(
                                        requestModifyServer.serverModifiablePart
                                    )
                                ))
                            }
                        }
                }
            }
    }

    @DeleteMapping(SERVER_ID_PATH)
    fun deleteServer(
        @RequestBody requestDeleteServer: RequestDeleteServer,
        @PathVariable(SERVER_ID_STR) serverId: Long,
        @RequestAttribute(JwtConst.JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ServerDeleteBody> {
        if ( !requestDeleteServer.isLegal()) {
            return badRequestMonoResponse(ServerDeleteBody.void())
        }
        val jwtId = claims.payload.subject.toLong()

        return serverService.isLegalToDeleteServer(jwtId, serverId)
            .flatMap { ok ->
                if (ok) {
                    serverService.deleteServer(serverId)
                        .flatMap { success ->
                            if (success) {
                                internalServerErrorMonoResponse(ServerDeleteBody.void())
                            } else {
                                Mono.just(StdResponse.makeResponseEntity(
                                    HttpStatus.OK,
                                    "Delete Success",
                                    ServerDeleteBody(serverId)
                                ))
                            }
                        }
                } else {
                    unauthorizedMonoResponse(ServerDeleteBody.void())
                }
            }
    }
}