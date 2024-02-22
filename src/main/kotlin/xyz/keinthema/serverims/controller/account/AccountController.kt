package xyz.keinthema.serverims.controller.account

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.ControllerConst.Companion.ACCOUNT_ID_PATH
import xyz.keinthema.serverims.constant.ControllerConst.Companion.ACCOUNT_ID_STR
import xyz.keinthema.serverims.constant.ControllerConst.Companion.badRequestMonoResponse
import xyz.keinthema.serverims.constant.ControllerConst.Companion.unauthorizedMonoResponse
import xyz.keinthema.serverims.constant.JwtConst.Companion.JWT_ATTR_NAME
import xyz.keinthema.serverims.constant.MonoResponse
import xyz.keinthema.serverims.model.dto.request.RequestCreateAccount
import xyz.keinthema.serverims.model.dto.request.RequestDeleteAccount
import xyz.keinthema.serverims.model.dto.request.RequestModifyAccount
import xyz.keinthema.serverims.model.dto.response.*
import xyz.keinthema.serverims.model.entity.Account
import xyz.keinthema.serverims.service.intf.AccountService

@RestController
@RequestMapping("/account")
class AccountController(private val accountService: AccountService) {

    @PostMapping
    fun createAccount(
        @RequestBody requestCreateAccount: RequestCreateAccount
    ): MonoResponse<AccountCreateBody> {
        if ( !requestCreateAccount.isLegal()) {
            return badRequestMonoResponse(AccountCreateBody.void())
        }

        return accountService.createNewAccount(
            requestCreateAccount.name,
            requestCreateAccount.hashedPw,
            requestCreateAccount.email
        ).flatMap { newAccount ->
            if (newAccount != null && newAccount.id != -1L) {
                Mono.just(StdResponse.makeResponseEntity(
                    HttpStatus.CREATED,
                    "Account Created",
                    AccountCreateBody(newAccount.id, newAccount.name)
                ))
            } else {
                Mono.just(StdResponse.makeResponseEntity(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to Create Account",
                    AccountCreateBody.void()
                ))
            }
        }
    }

    @GetMapping(ACCOUNT_ID_PATH)
    fun getAccountInfo(
        @PathVariable(ACCOUNT_ID_STR) id: Long,
        @RequestAttribute(JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<AccountInfoBody> {
        val jwtId = claims.payload.subject.toLong()

        return accountService.getAccountById(id)
            .map { account ->
                 if (account == null) {
                    StdResponse.makeResponseEntity(
                        HttpStatus.NOT_FOUND,
                        "Get Account Info Failed",
                        AccountInfoBody.void()
                    )
                } else if (accountService.isLegalToAccessAllAccountInfo(jwtId, id)) {
                    StdResponse.makeResponseEntity(
                        HttpStatus.OK,
                        "Get Account Info Success",
                        AccountInfoBody(account)
                    )
                } else {
                    StdResponse.makeResponseEntity(
                        HttpStatus.OK,
                        "Get Account Info Success",
                        AccountInfoBody.publishedInfo(account)
                    )
                }
            }
    }

    @PutMapping(ACCOUNT_ID_PATH)
    fun modifyAccount(
        @RequestBody requestModifyAccount: RequestModifyAccount,
        @PathVariable(ACCOUNT_ID_STR) id: Long,
        @RequestAttribute(JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<AccountModifyBody> {
        if ( !requestModifyAccount.isLegal()) {
            return badRequestMonoResponse(AccountModifyBody.void())
        }

        val jwtId = claims.payload.subject.toLong()
        if ( !accountService.isLegalToModifyAccountInfo(jwtId, id)) {
            return unauthorizedMonoResponse(AccountModifyBody.void())
        }

        val infoMono = if (requestModifyAccount.accountModifiablePart != null) {
            accountService
                .modifyAccountInfo(id, requestModifyAccount.accountModifiablePart)
        } else {
            Mono.just(Account.void())
        }

        val passwordMono = if (requestModifyAccount.previousPw != null && requestModifyAccount.hashedOnePw != null) {
            accountService
                .modifyAccountPassword(id,
                    requestModifyAccount.previousPw,
                    requestModifyAccount.hashedOnePw)
        } else {
            Mono.just(false)
        }

        return infoMono.flatMap { info ->
            passwordMono.map { password ->
                val success = !(info == null || info.isVoid()) || password
                val message = if (success) "Modify Success" else "Failed to Modify"
                StdResponse.makeResponseEntity(
                    if (success) HttpStatus.OK else HttpStatus.NOT_MODIFIED,
                    message,
                    AccountModifyBody(
                        passwordModified = password,
                        modifiablePart = Account.Companion.AccountModifiablePart(info)
                    )
                )
            }
        }
    }

    @DeleteMapping(ACCOUNT_ID_PATH)
    fun deleteAccount(
        @RequestBody requestDeleteAccount: RequestDeleteAccount,
        @PathVariable(ACCOUNT_ID_STR) id: Long,
        @RequestAttribute(JWT_ATTR_NAME) claims: Jws<Claims>
    ): MonoResponse<AccountDeleteBody> {
        if ( !requestDeleteAccount.isLegal()) {
            return badRequestMonoResponse(AccountDeleteBody.void())
        }

        val jwtId = claims.payload.subject.toLong()

        return accountService.isLegalToDeleteAccount(jwtId, id, requestDeleteAccount.hashedPw)
            .flatMap { ok ->
                if (ok) {
                    accountService.deleteAccount(id).map { StdResponse.makeResponseEntity(
                        HttpStatus.OK,
                        "Delete Success",
                        AccountDeleteBody(id)
                    ) }
                } else {
                    Mono.just(StdResponse.makeResponseEntity(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        AccountDeleteBody.void()
                    ))
                }
            }
    }
}