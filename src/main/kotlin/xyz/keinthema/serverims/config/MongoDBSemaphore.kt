package xyz.keinthema.serverims.config

import kotlinx.coroutines.sync.Semaphore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MongoDBSemaphore {
    @Bean
    fun accountSemaphore(): MongoDBAccountsSemaphore {
        return MongoDBAccountsSemaphore()
    }
    @Bean
    fun serversSemaphore(): MongoDBServersSemaphore {
        return MongoDBServersSemaphore()
    }
}

open class MongoDBDefaultSemaphore {
    private val semaphore = Semaphore(1,0)
    suspend fun acquire() {
        semaphore.acquire()
    }
    fun release() {
        semaphore.release()
    }
}

class MongoDBAccountsSemaphore: MongoDBDefaultSemaphore()
class MongoDBServersSemaphore: MongoDBDefaultSemaphore()