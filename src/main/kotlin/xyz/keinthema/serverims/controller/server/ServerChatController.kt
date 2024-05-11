package xyz.keinthema.serverims.controller.server

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.*
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_CHAT_ID_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_ID_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_IMG_NAME_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_IMG_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_IMG_WITH_NAME_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_MESSAGE_NUMBER_DEFAULT
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_MESSAGE_NUMBER_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_MESSAGE_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_MESSAGE_UNTIL_DEFAULT
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_MESSAGE_UNTIL_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.SERVER_CHAT_SERVER_ID_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.badRequestMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.forbiddenMonoResponse
import xyz.keinthema.serverims.model.dto.request.RequestCreateChat
import xyz.keinthema.serverims.model.dto.request.RequestModifyChatInfo
import xyz.keinthema.serverims.model.dto.response.*
import xyz.keinthema.serverims.service.intf.MessageService

@RestController
@RequestMapping(SERVER_CHAT_PATH)
class ServerChatController(
    private val messageService: MessageService
) {
    @PostMapping(SERVER_CHAT_IMG_PATH)
    fun uploadImageMessage(
        @RequestPart("file") file: FilePart,//Flux<FilePart>
        @PathVariable(SERVER_CHAT_SERVER_ID_STR) serverId: ServerId,
        @PathVariable(SERVER_CHAT_CHAT_ID_STR) chatId: ChatId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ImageUploadBody> {
        val jwtId = claims.payload.subject.toLong()
        return messageService.isLegalToMessaging(
            serverId = serverId,
            userId = jwtId
        ).flatMap { isLegal ->
            if (isLegal) {
                messageService.saveFile(serverId, file)
                    .flatMap { idAndName ->
                        val id = idAndName.first
                        val fileName = idAndName.second
                        val metaMessage = messageService.newMediaMessage(
                            messageId = id,
                            serverId = serverId,
                            chatId = chatId,
                            userId = jwtId,
                            fileName = fileName
                        )
                        Mono.zip(
                            messageService.storeMessage(metaMessage),
                            messageService.sendMessageByServer(metaMessage, null)
                        ).subscribe()
                        Mono.just(
                            StdResponse.makeResponseEntity(
                            HttpStatus.OK,
                            "Upload Success",
                            ImageUploadBody(id, fileName)
                        ))
                    }
            } else {
                forbiddenMonoResponse(ImageUploadBody.void())
            }
        }
    }
    @GetMapping(SERVER_CHAT_IMG_WITH_NAME_PATH)
    fun getImage(
        @PathVariable(SERVER_CHAT_SERVER_ID_STR) serverId: ServerId,
        @PathVariable(SERVER_CHAT_CHAT_ID_STR) chatId: ChatId,
        @PathVariable(SERVER_CHAT_IMG_NAME_STR) fileName: String,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): Mono<ResponseEntity<ByteArray>> {
        return messageService.isLegalToMessaging(
            serverId = serverId,
            userId = claims.payload.subject.toLong()
        ).flatMap { isLegal ->
            if (isLegal) {
                messageService.getFile(serverId, fileName)
            } else {
                Mono.just(Pair(byteArrayOf(), null))
            }
        }.map {
            val bytes = it.first
            val mediaType = it.second
            if (bytes.isEmpty()) {
                ResponseEntity.badRequest().body(bytes)
            } else if (mediaType == null) {
                ResponseEntity.internalServerError().body(bytes)
            } else {
                ResponseEntity.ok().header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"$fileName\""
                ).contentType(mediaType).body(bytes)
            }
        }
    }

    @PostMapping
    fun createChat(
        @RequestBody requestCreateChat: RequestCreateChat,
        @PathVariable(SERVER_CHAT_SERVER_ID_STR) serverId: ServerId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ChatCreateBody> {
        return messageService.isLegalToChangeChat(
            serverId = serverId,
            userId = claims.payload.subject.toLong()
        ).flatMap { isLegal ->
            if (isLegal) {
                messageService.createChatInServer(
                    serverId = serverId,
                    chatName = requestCreateChat.name
                ).flatMap {
                    if (it == -1) {
                        badRequestMonoResponse(ChatCreateBody.void())
                    } else {
                        Mono.just(StdResponse.makeResponseEntity(
                            HttpStatus.OK,
                            "Create Chat Success",
                            ChatCreateBody(it)
                        ))
                    }
                }
            } else {
                forbiddenMonoResponse(ChatCreateBody.void())
            }
        }
    }

    @GetMapping
    fun getChatList(
        @PathVariable(SERVER_CHAT_SERVER_ID_STR) serverId: ServerId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ListChatGetBody> {
        return messageService.isLegalToMessaging(
            serverId = serverId,
            userId = claims.payload.subject.toLong()
        ).flatMap { isLegal ->
            if (isLegal) {
                messageService.getChatListOfServer(serverId)
                    .map {
                        StdResponse.makeResponseEntity(
                            HttpStatus.OK,
                            "Get Chat List Success",
                            ListChatGetBody(it)
                        )
                    }
            } else {
                forbiddenMonoResponse(ListChatGetBody.void())
            }
        }
    }

//    @GetMapping(SERVER_CHAT_ID_PATH)
//    fun getChatInfo(
//        @PathVariable(SERVER_CHAT_SERVER_ID_STR) serverId: ServerId,
//        @PathVariable(SERVER_CHAT_SERVER_ID_STR) chatId: ChatId,
//        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
//    ): MonoResponse<> {
//        //
//    }

    @PutMapping(SERVER_CHAT_ID_PATH)
    fun modifyChatInfo(
        @RequestBody requestModifyChatInfo: RequestModifyChatInfo,
        @PathVariable(SERVER_CHAT_SERVER_ID_STR) serverId: ServerId,
        @PathVariable(SERVER_CHAT_CHAT_ID_STR) chatId: ChatId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<InfoChatModifyBody> {
        return messageService.isLegalToChangeChat(
            serverId = serverId,
            userId = claims.payload.subject.toLong()
        ).flatMap { isLegal ->
            if (isLegal) {
                messageService.modifyChatInfoInServer(
                    serverId = serverId,
                    chatId = chatId,
                    chatName = requestModifyChatInfo.name
                ).flatMap { success ->
                    if (success) {
                        Mono.just(StdResponse.makeResponseEntity(
                            HttpStatus.OK,
                            "Modify Chat Info Success",
                            InfoChatModifyBody(requestModifyChatInfo.name)
                        ))
                    } else {
                        badRequestMonoResponse(InfoChatModifyBody.void())
                    }
                }
            } else {
                forbiddenMonoResponse(InfoChatModifyBody.void())
            }
        }
    }

    @DeleteMapping(SERVER_CHAT_ID_PATH)
    fun deleteChat(
        @PathVariable(SERVER_CHAT_SERVER_ID_STR) serverId: ServerId,
        @PathVariable(SERVER_CHAT_CHAT_ID_STR) chatId: ChatId,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<ChatDeleteBody> {
        return messageService.isLegalToChangeChat(
            serverId = serverId,
            userId = claims.payload.subject.toLong()
        ).flatMap { isLegal ->
            if (isLegal) {
                messageService.deleteChatInServer(
                    serverId = serverId,
                    chatId = chatId
                ).flatMap { success ->
                    if (success) {
                        Mono.just(StdResponse.makeResponseEntity(
                            HttpStatus.OK,
                            "Delete Chat Success",
                            ChatDeleteBody(success)
                        ))
                    } else {
                        badRequestMonoResponse(ChatDeleteBody.void())
                    }
                }
            } else {
                forbiddenMonoResponse(ChatDeleteBody.void())
            }
        }
    }

    @GetMapping(SERVER_CHAT_MESSAGE_PATH)
    fun getHistoryMessage(
        @PathVariable(SERVER_CHAT_SERVER_ID_STR) serverId: ServerId,
        @PathVariable(SERVER_CHAT_CHAT_ID_STR) chatId: ChatId,
        @RequestParam(
            name = SERVER_CHAT_MESSAGE_UNTIL_STR,
            required = false,
            defaultValue = SERVER_CHAT_MESSAGE_UNTIL_DEFAULT
        ) until: Long,
        @RequestParam(
            name = SERVER_CHAT_MESSAGE_NUMBER_STR,
            required = false,
            defaultValue = SERVER_CHAT_MESSAGE_NUMBER_DEFAULT
        ) number: Int,
        @RequestAttribute(JwtConst.JWT_CLAIMS_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<MessageHistoryGetBody> {
        return messageService.isLegalToMessaging(
            serverId = serverId,
            userId = claims.payload.subject.toLong()
        ).flatMap { isLegal ->
            if (isLegal) {
                messageService.getMessageByServerChat(
                    serverId = serverId,
                    chatId = chatId,
                    beforeTimestampInMsec =
                        if (until == SERVER_CHAT_MESSAGE_UNTIL_DEFAULT.toLong()) System.currentTimeMillis() else until,
                    number = number
                ).map {
                    StdResponse.makeResponseEntity(
                        HttpStatus.OK,
                        "Get History Message Success",
                        MessageHistoryGetBody(it)
                    )
                }
            } else {
                forbiddenMonoResponse(MessageHistoryGetBody.void())
            }
        }
    }
}