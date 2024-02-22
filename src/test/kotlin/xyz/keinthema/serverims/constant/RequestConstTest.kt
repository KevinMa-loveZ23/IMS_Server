package xyz.keinthema.serverims.constant

import lombok.extern.java.Log
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import xyz.keinthema.serverims.constant.RequestConst.Companion.isLegalEmail
import java.util.logging.Logger

@SpringBootTest
@Log
class RequestConstTest{
    val logger = Logger.getLogger("testLog")
    @Test
    fun testEmailRegex() {
        val isValid = "1285122489.qq.com".isLegalEmail()
        logger.info("test email regex: 1285122489.qq.com is $isValid")
    }

}