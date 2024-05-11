package xyz.keinthema.serverims.service.impl

import lombok.extern.java.Log
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.test.StepVerifier
import xyz.keinthema.serverims.constant.ServiceConst
import xyz.keinthema.serverims.service.intf.JwsService
import java.util.*
import java.util.logging.Logger

@SpringBootTest
@Log
class JwsServiceImplTest {

    val logger = Logger.getLogger("testLog")
    @Autowired
    lateinit var jwsService: JwsService

    @Autowired
    lateinit var reactiveRedisTemplate: ReactiveRedisTemplate<String,String>
    @Test
    fun isRevoked() {
        val testUUID = UUID.randomUUID()
        val res = jwsService.revokeJws(testUUID,
            Date(Date().time.plus(60*1000)))
            .flatMap {
                jwsService.isRevoked(testUUID)
                    .map {
//                        logger.info("here is $it")
                        println("here is $it: $testUUID")
                        it
                    }
            }
//        val resBool = res.toFuture().get()
//        println("it is $resBool")
        StepVerifier.create(res)
            .expectNext(true)
            .verifyComplete()
        val another = UUID.randomUUID()
        StepVerifier.create(
            jwsService.isRevoked(another)
                .map {
                    println(another)
                    it
                }
        )
            .expectNext(false)
            .verifyComplete()
//        res.block()?.let { check(it) }
//        res.subscribe()
    }

    @Test
    fun revokeJws() {
        val testUUID = UUID.randomUUID()
        val res = jwsService.revokeJws(testUUID,
            Date(Date().time.plus(60*1000)))
            .flatMap {
                logger.info("hier bin ich $it")
                reactiveRedisTemplate.hasKey(ServiceConst.getKeyForRevokedUUID(testUUID))
            }
        res.log().block()

//        jwsService.revokeJws(UUID.randomUUID(),
//            Date(Date().time.plus(60*1000)))
//            .subscribe {
//                logger.info("${it.id} and ${it.expireAt}")
//            }
    }
}