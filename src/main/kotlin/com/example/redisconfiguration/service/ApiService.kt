package com.example.redisconfiguration.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApiService {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun call() = runBlocking {
        logger.info("call()")

        val startTime = System.currentTimeMillis()

        val deferredMember = async(context = Dispatchers.IO) { callMemberApi() }
        val deferredProduct = async(context = Dispatchers.IO) { callProductApi() }
        val deferredOrder = async(context = Dispatchers.IO) { callOrderApi() }

        deferredMember.await()
        deferredProduct.await()
        deferredOrder.await()

        val endTime = System.currentTimeMillis()

        logger.info("execution time: {}", (endTime - startTime))
    }

    suspend fun callMemberApi() {
        val timeMillis: Long = 152
        delay(timeMillis = timeMillis)
        logger.info("callMemberApi() after ${timeMillis}ms")
    }

    suspend fun callProductApi() {
        val timeMillis: Long = 215
        delay(timeMillis = timeMillis)
        logger.info("callProductApi() after ${timeMillis}ms")
    }

    suspend fun callOrderApi() {
        val timeMillis: Long = 283
        delay(timeMillis = timeMillis)
        logger.info("callOrderApi() after ${timeMillis}ms")
    }
}
