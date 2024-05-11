package xyz.keinthema.serverims.service.impl

import lombok.extern.java.Log
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import xyz.keinthema.serverims.service.intf.ServerService
import java.util.logging.Logger

@SpringBootTest
@Log
class ServerServiceImplTest {
    @Autowired
    lateinit var serverService: ServerService
    val logger = Logger.getLogger("testLog")

    @Test
    fun createServer() {
        serverService.createServer(
            0,
            "name of server",
            "description"
        ).block()
    }
}