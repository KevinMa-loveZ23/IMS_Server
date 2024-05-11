package xyz.keinthema.serverims.service.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import xyz.keinthema.serverims.service.intf.AccountService
import xyz.keinthema.serverims.service.intf.ServerMemberService

@SpringBootTest
class AccountServiceImplTest {

    @Autowired
    private lateinit var accountService: AccountService
    @Autowired
    private lateinit var serverMemberService: ServerMemberService
    @Test
    fun deleteServerFromMultiAccount() {
//        accountService.deleteServerFromMultiAccount(listOf(2L), 0L)
//            .block()
//        accountService.deleteServerFromAccount(2L, 0L)
//            .block()
        val names = accountService.getNamesFromMultiAccount(listOf(0L,1L,2L))
            .block()
        println(names)
//        serverMemberService.deleteMembersFromServer(0L, listOf(2L), 2L)
    }
}