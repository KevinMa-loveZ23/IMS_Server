package xyz.keinthema.serverims.handler

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.constant.MessageId
import java.util.Date
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger


@Component
class SnowflakeHandler {

    companion object {

        private val twepoch = 1704038400000L //2024-01-01 00:00:00
        private val sequenceBits = 10L
        private val maxSequence = -1L xor (-1L shl sequenceBits.toInt())

        private val workerIdBits = 5L
        private val maxWorkerId = -1L xor (-1L shl workerIdBits.toInt())
        private val maxWorkerNumber: Int = (maxWorkerId + 1).toInt()

        private val workerIdShift = (sequenceBits).toInt()
        private val timestampShift = (sequenceBits + workerIdBits).toInt()
        fun getMinSnowflake(timestamp: Long): Long {
            return (timestamp - twepoch) shl timestampShift
        }
        fun getMinSnowflake(date: Date): Long {
            return getMinSnowflake(date.time)
        }
    }

//    private



//    private val workerId
//    private val sequence = 0L
//    private val lastTimestamp = -1L

    private val contents = Array(maxWorkerNumber) {
        WorkerContent(it.toLong())
    }

//    protected fun getWorkerContents(maxWorkerNumber: Int): Array<WorkerContent> {
//        return Array(maxWorkerNumber) {
//            WorkerContent(it.toLong())
//        }
//    }

    class WorkerContent(private val id: Long) {
        var sequence = 0L
        var lastTimestamp = -1L
        val mutex = Mutex()
    }

    private val executorService = ThreadPoolExecutor(
        0, maxWorkerNumber,
        60L, TimeUnit.SECONDS,
        SynchronousQueue()
    )

//    @Synchronized
//    private fun nextId(): MessageId {
//        val workerId = Thread.currentThread().id
//    }

    private val threadIdGenerator = AtomicInteger(0)

    fun getSnowflakeId(): Mono<MessageId> {

        val newWorkerId = threadIdGenerator.getAndUpdate { (it + 1) % maxWorkerNumber }

        val newIdInfoFuture = executorService.submit(SnowflakeWorker(
            workerId = newWorkerId, contents = contents, maxSequence = maxSequence
        ))
        return mono { coroutineScope {
            val newIdInfo = newIdInfoFuture.get()
            ((newIdInfo.timestamp - twepoch) shl timestampShift) or
                    (newWorkerId.toLong() shl workerIdShift) or
                    (newIdInfo.sequence)
        } }
//        return withContext(Dispatchers.IO) {
//            val newIdInfo = newIdInfoFuture.get()
//            ((newIdInfo.timestamp - twepoch) shl timestampShift) or
//                    (newWorkerId.toLong() shl workerIdShift) or
//                    (newIdInfo.sequence)
//        }
    }
}

class SnowflakeWorker(
    private val workerId: Int,
    private val contents: Array<SnowflakeHandler.WorkerContent>,
    private val maxSequence: Long
): Callable<SnowflakeWorker.NewIdInfo> {

    override fun call(): NewIdInfo {
//        val workerId = Thread.currentThread().id

        var newTs: Long
        var newSeq: Long

        runBlocking {
            var timestamp = System.currentTimeMillis()
            val content = contents[workerId]
            content.mutex.withLock {
                if (timestamp < content.lastTimestamp) {
                    throw RuntimeException(
                        "Clock moves backwards, generating id for $timestamp is refused."
                    )
                } else if (timestamp == content.lastTimestamp) {
                    content.sequence++
                    if (content.sequence > maxSequence) {
                        timestamp = System.currentTimeMillis()
                        while (timestamp <= content.lastTimestamp) {
                            timestamp = System.currentTimeMillis()
                        }
                        content.sequence = 0L
                    }
                } else {
                    content.sequence = 0L
                }
                content.lastTimestamp = timestamp
                newTs = content.lastTimestamp
                newSeq = content.sequence
            }
        }
        return NewIdInfo(
            timestamp = newTs,
            sequence = newSeq,
//            workerId = workerId
        )
    }

    data class NewIdInfo(
        val timestamp: Long,
        val sequence: Long,
//        val workerId: Long
    )
}

//class SnowflakeWorkerThreadFactory(
//    private val maxThreadNumber: Int
//): ThreadFactory {
//
//    private val threadIdGenerator = AtomicInteger(0)
//    private val mutex = Mutex()
//    private var threadId = 0
//    override fun newThread(r: Runnable): Thread {
//        val newId = threadIdGenerator.getAndUpdate { (it + 1) % maxThreadNumber }
//        val ret = Thread(r, "SnowflakeWorker-$newId")
//        println("new thread ${ret.id}")
//        return ret
//    }
//
//}