package xyz.keinthema.serverims.controller.server

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.AccountId
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_MEMBER_ACCOUNT_ID_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_MEMBER_ACCOUNT_ID_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_MEMBER_CODE_MESSAGE_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_MEMBER_CODE_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_MEMBER_JOIN_CODE_DEFAULT
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_MEMBER_JOIN_CODE_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_MEMBER_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_MEMBER_SERVER_ID_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_MEMBER_WAITING_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.badRequestMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.forbiddenMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.internalServerErrorMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.unauthorizedMonoResponse
import xyz.keinthema.serverims.constant.JwtConst
import xyz.keinthema.serverims.constant.MonoResponse
import xyz.keinthema.serverims.constant.ServerId
import xyz.keinthema.serverims.model.dto.request.RequestDeleteMultiMembers
import xyz.keinthema.serverims.model.dto.request.RequestJoinServer
import xyz.keinthema.serverims.model.dto.response.*
import xyz.keinthema.serverims.service.intf.ServerMemberService

@RestController
@RequestMapping(SERVER_MEMBER_PATH)
class ServerMemberController(private val serverMemberService: ServerMemberService) {
    @PutMapping
    fun requestJoinServer(
        @RequestBody requestJoinServer: RequestJoinServer,
        @PathVariable(SERVER_MEMBER_SERVER_ID_STR) serverId: ServerId,
        @RequestParam(
            name = SERVER_MEMBER_JOIN_CODE_STR,
            required = false,
            defaultValue = SERVER_MEMBER_JOIN_CODE_DEFAULT
        ) code: Int, // StrategyType?
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ServerJoinRequestBody> {
        if ( !requestJoinServer.isLegal()) {
            return badRequestMonoResponse(ServerJoinRequestBody.void())
        }
        val jwtId = claims.payload.subject.toLong()
        return serverMemberService.processJoinRequestToServer(
            serverId = serverId,
            userId = jwtId,
            code = code,
            message = requestJoinServer.message
        ).flatMap { success ->
            if (success) {
                Mono.just(StdResponse.makeResponseEntity(
                    HttpStatus.OK,
                    "Send Request Success",
                    ServerJoinRequestBody(success)
                ))
            } else {
                internalServerErrorMonoResponse(ServerJoinRequestBody.void())
            }
        }
    }

    @GetMapping(SERVER_MEMBER_WAITING_PATH)
    fun getWaitingList(
        @PathVariable(SERVER_MEMBER_SERVER_ID_STR) serverId: ServerId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ListWaitingGetBody> {
        val jwtId = claims.payload.subject.toLong()
        return serverMemberService.isLegalToGetWaitingList(jwtId, serverId)
            .flatMap { ok ->
                if ( !ok) {
                    forbiddenMonoResponse(ListWaitingGetBody.void())
                } else {
                    serverMemberService.getJoinRequestList(serverId)
                        .flatMap {
                            if (it == null) {
                                internalServerErrorMonoResponse(ListWaitingGetBody.void())
                            } else {
                                Mono.just(StdResponse.makeResponseEntity(
                                    HttpStatus.OK,
                                    "Get Waiting List Success",
                                    ListWaitingGetBody(it)
                                ))
                            }
                        }
                }
            }
    }

    @PutMapping(SERVER_MEMBER_ACCOUNT_ID_PATH)
    fun allowJoinServer(
        @PathVariable(SERVER_MEMBER_SERVER_ID_STR) serverId: ServerId,
        @PathVariable(SERVER_MEMBER_ACCOUNT_ID_STR) accountId: AccountId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ServerJoinAllowBody> {
        val jwtId = claims.payload.subject.toLong()
        return serverMemberService.isLegalToAllowJoinRequest(jwtId, serverId)
            .flatMap { ok ->
                if ( !ok) {
                    forbiddenMonoResponse(ServerJoinAllowBody.void())
                } else {
                    serverMemberService.allowJoinRequestToServer(
                        serverId = serverId,
                        userId = accountId,
                        operator = jwtId
                    ).flatMap { success ->
                        if (success) {
                            Mono.just(StdResponse.makeResponseEntity(
                                HttpStatus.OK,
                                "Allow Join Server Success",
                                ServerJoinAllowBody(success)
                            ))
                        } else {
                            internalServerErrorMonoResponse(ServerJoinAllowBody.void())
                        }
                    }
                }
            }
    }

    @GetMapping
    fun getMembers(
        @PathVariable(SERVER_MEMBER_SERVER_ID_STR) serverId: ServerId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<MembersGetBody> {
        val jwtId = claims.payload.subject.toLong()
        return serverMemberService.isLegalToGetMembers(jwtId, serverId)
            .flatMap { ok ->
                if ( !ok) {
                    forbiddenMonoResponse(MembersGetBody.void())
                } else {
                    serverMemberService.getServerMembersFromServer(serverId)
                        .flatMap { userSet ->
                            if (userSet == null) {
                                internalServerErrorMonoResponse(MembersGetBody.void())
                            } else {
                                Mono.just(StdResponse.makeResponseEntity(
                                    HttpStatus.OK,
                                    "Get Server Members Success",
                                    MembersGetBody(userSet.toList())
                                ))
                            }
                        }
                }
            }
    }

    @DeleteMapping(SERVER_MEMBER_ACCOUNT_ID_PATH)
    fun deleteMember(
        @PathVariable(SERVER_MEMBER_SERVER_ID_STR) serverId: ServerId,
        @PathVariable(SERVER_MEMBER_ACCOUNT_ID_STR) accountId: AccountId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<MemberDeleteBody> {
        val jwtId = claims.payload.subject.toLong()
        return serverMemberService.isLegalToDeleteMembers(jwtId, serverId, accountId)
            .flatMap { ok ->
                if ( !ok) {
                    forbiddenMonoResponse(MemberDeleteBody.void())
                } else {
                    serverMemberService.deleteMembersFromServer(
                        serverId = serverId,
                        userIdList = listOf(accountId),
                        operator = jwtId
                    ).flatMap { success ->
                        if (success) {
                            Mono.just(StdResponse.makeResponseEntity(
                                HttpStatus.OK,
                                "Delete Member Success",
                                MemberDeleteBody(success)
                            ))
                        } else {
                            internalServerErrorMonoResponse(MemberDeleteBody.void())
                        }
                    }
                }
            }
    }

    @DeleteMapping
    fun deleteMultiMembers(
        @RequestBody requestDeleteMultiMembers: RequestDeleteMultiMembers,
        @PathVariable(SERVER_MEMBER_SERVER_ID_STR) serverId: ServerId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<MemberMultiDeleteBody> {
        if ( !requestDeleteMultiMembers.isLegal()) {
            return badRequestMonoResponse(MemberMultiDeleteBody.void())
        }
        val jwtId = claims.payload.subject.toLong()
        return serverMemberService.isLegalToDeleteMuitiMembers(jwtId, serverId, requestDeleteMultiMembers.userIdList)
            .flatMap { ok ->
                if ( !ok) {
                    forbiddenMonoResponse(MemberMultiDeleteBody.void())
                } else {
                    serverMemberService.deleteMembersFromServer(
                        serverId = serverId,
                        userIdList = requestDeleteMultiMembers.userIdList,
                        operator = jwtId
                    ).flatMap { success ->
                        if (success) {
                            Mono.just(StdResponse.makeResponseEntity(
                                HttpStatus.OK,
                                "Delete Multiple Members Success",
                                MemberMultiDeleteBody(success)
                            ))
                        } else {
                            internalServerErrorMonoResponse(MemberMultiDeleteBody.void())
                        }
                    }
                }
            }
    }

    @GetMapping(SERVER_MEMBER_CODE_PATH)
    fun getEntryMessageList(
        @PathVariable(SERVER_MEMBER_SERVER_ID_STR) serverId: ServerId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>,
    ): MonoResponse<ListMessageEntryGetBody> {
        return serverMemberService.isLegalToAllowJoinRequest(
            operator = claims.payload.subject.toLong(),
            serverId = serverId
        ).flatMap { ok ->
            if (!ok) {
                forbiddenMonoResponse(ListMessageEntryGetBody.void())
            } else {
                serverMemberService.getEntryMessageList(serverId)
                    .map { StdResponse.makeResponseEntity(
                        HttpStatus.OK,
                        "Get Entry Message List Success",
                        ListMessageEntryGetBody(it.toList())
                    ) }
            }
        }
    }

    @PostMapping(SERVER_MEMBER_CODE_PATH)
    fun addEntryMessage(
        @PathVariable(SERVER_MEMBER_SERVER_ID_STR) serverId: ServerId,
        @RequestParam(
            name = SERVER_MEMBER_CODE_MESSAGE_STR,
            required = true,
        ) msg: String,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<MessageEntryAddBody> {
        return serverMemberService.isLegalToAllowJoinRequest(
            operator = claims.payload.subject.toLong(),
            serverId = serverId
        ).flatMap { ok ->
            if (!ok) {
                forbiddenMonoResponse(MessageEntryAddBody.void())
            } else {
                serverMemberService.addEntryMessage(serverId, msg)
                    .flatMap {
                        if (it) {
                            Mono.just(StdResponse.makeResponseEntity(
                                HttpStatus.OK,
                                "Add Entry Message Success",
                                MessageEntryAddBody(it)
                            ))
                        } else {
                            internalServerErrorMonoResponse(MessageEntryAddBody.void())
                        }
                    }
            }
        }
    }

    @DeleteMapping(SERVER_MEMBER_CODE_PATH)
    fun deleteEntryMessage(
        @PathVariable(SERVER_MEMBER_SERVER_ID_STR) serverId: ServerId,
        @RequestParam(
            name = SERVER_MEMBER_CODE_MESSAGE_STR,
            required = true,
        ) msg: String,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<MessageEntryDeleteBody> {
        return serverMemberService.isLegalToAllowJoinRequest(
            operator = claims.payload.subject.toLong(),
            serverId = serverId
        ).flatMap { ok ->
            if (!ok) {
                forbiddenMonoResponse(MessageEntryDeleteBody.void())
            } else {
                serverMemberService.deleteEntryMessage(serverId, msg)
                    .flatMap {
                        if (it) {
                            Mono.just(StdResponse.makeResponseEntity(
                                HttpStatus.OK,
                                "Delete Entry Message Success",
                                MessageEntryDeleteBody(it)
                            ))
                        } else {
                            internalServerErrorMonoResponse(MessageEntryDeleteBody.void())
                        }
                    }
            }
        }
    }
}