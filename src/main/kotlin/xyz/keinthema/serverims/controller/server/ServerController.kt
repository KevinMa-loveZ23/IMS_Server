package xyz.keinthema.serverims.controller.server

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_ID_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_ID_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.badRequestMonoResponse
import xyz.keinthema.serverims.constant.JwtConst
import xyz.keinthema.serverims.constant.MonoResponse
import xyz.keinthema.serverims.model.dto.request.RequestCreateServer
import xyz.keinthema.serverims.model.dto.request.RequestDeleteServer
import xyz.keinthema.serverims.model.dto.request.RequestModifyServer
import xyz.keinthema.serverims.model.dto.response.*
import xyz.keinthema.serverims.service.intf.ServerService

@RestController
@RequestMapping("/server")
class ServerController(private val serverService: ServerService) {

    @PostMapping
    fun createServer(
        @RequestBody requestCreateServer: RequestCreateServer,
        @RequestAttribute(JwtConst.JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ServerCreateBody> {
        if ( !requestCreateServer.isLegal()) {
            return Mono.just(StdResponse.makeResponseEntity(
                HttpStatus.BAD_REQUEST,
                "Illegal Parameters",
                ServerCreateBody.void()
            ))
        }
        val jwtId = claims.payload.subject.toLong()
    }

    @GetMapping(SERVER_ID_PATH)
    fun getServerInfo(
        @PathVariable(SERVER_ID_STR) serverId: Long,
        @RequestAttribute(JwtConst.JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ServerInfoBody> {
        val jwtId = claims.payload.subject.toLong()
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
    }
}