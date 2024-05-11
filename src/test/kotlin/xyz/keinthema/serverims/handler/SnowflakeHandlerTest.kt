package xyz.keinthema.serverims.handler

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SnowflakeHandlerTest(
//    private val snowflakeHandler: SnowflakeHandler
) {

    @Autowired
    private lateinit var snowflakeHandler: SnowflakeHandler
//    @Test
//    fun getSnowflakeId() {
////        Thread.sleep(10*1000)
//        runBlocking {
//            repeat(100) {
//                println("${System.currentTimeMillis().toString(8)} " +
//                        "No. $it sid: ${snowflakeHandler.getSnowflakeId().toString(8)}")
//            }
//        }
//    }

    @Test
    fun getNewThread() {
        snowflakeHandler
    }
}