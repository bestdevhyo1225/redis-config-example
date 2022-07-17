package com.example.redisconfiguration.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApiService {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun call(): List<Long> {
        logger.info("call()")

        val startTime = System.currentTimeMillis()

        val deferredMember = CoroutineScope(context = Dispatchers.IO).async { callMemberApi() }
        val deferredProduct = CoroutineScope(context = Dispatchers.IO).async { callProductApi() }
        val deferredOrder = CoroutineScope(context = Dispatchers.IO).async { callOrderApi() }

        deferredMember.await()
        deferredProduct.await()
        deferredOrder.await()

        val endTime = System.currentTimeMillis()

        logger.info("execution time: {}", (endTime - startTime))

        return listOf()
    }

    suspend fun callMemberApi() {
        logger.info("callMemberApi()")
        val timeMillis: Long = 100
        delay(timeMillis = timeMillis)
        logger.info("callMemberApi() after ${timeMillis}ms")
    }

    suspend fun callProductApi() {
        logger.info("callProductApi()")
        val timeMillis: Long = 100
        delay(timeMillis = timeMillis)
        logger.info("callProductApi() after ${timeMillis}ms")
    }

    suspend fun callOrderApi() {
        logger.info("callOrderApi()")
        val timeMillis: Long = 100
        delay(timeMillis = timeMillis)
        logger.info("callOrderApi() after ${timeMillis}ms")
    }
}
