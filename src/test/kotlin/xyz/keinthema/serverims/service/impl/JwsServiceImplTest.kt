package xyz.keinthema.serverims.service.impl

import lombok.extern.java.Log
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import xyz.keinthema.serverims.service.intf.JwsService
import java.util.*
import java.util.logging.Logger

@SpringBootTest
@Log
class JwsServiceImplTest {

    val logger = Logger.getLogger("testLog")
    @Autowired
    lateinit var jwsService: JwsService
    @Test
    fun isDeactivated() {
        val testUUID = UUID.randomUUID()
        val res = jwsService.deactivateJws(testUUID,
            Date(Date().time.plus(60*1000)))
            .flatMap { de ->
                jwsService.isDeactivated(testUUID)
                    .map {
                        logger.info("here is $it")
                        it
                    }
            }
        res.block()?.let { check(it) }
//        res.subscribe()
    }

    @Test
    fun deactivateJws() {
        jwsService.deactivateJws(UUID.randomUUID(),
            Date(Date().time.plus(60*1000)))
            .subscribe {
                logger.info("${it.id} and ${it.expireAt}")
            }
    }
}