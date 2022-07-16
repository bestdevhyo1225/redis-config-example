package com.example.redisconfiguration.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApiService {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun call() = runBlocking {
        val deferredMember = async(context = Dispatchers.IO) { callMemberApi() }
        val deferredProduct = async(context = Dispatchers.IO) { callProductApi() }
        val deferredOrder = async(context = Dispatchers.IO) { callOrderApi() }

        val startTime = System.currentTimeMillis()

        deferredMember.await()
        deferredProduct.await()
        deferredOrder.await()

        val endTime = System.currentTimeMillis()

        logger.info("execution time: {}", (endTime - startTime))
    }

    suspend fun callMemberApi() {
        delay(timeMillis = 152)
        logger.info("callMemberApi() after 152ms")
    }

    suspend fun callProductApi() {
        delay(timeMillis = 178)
        logger.info("callProductApi() after 178ms")
    }

    suspend fun callOrderApi() {
        delay(timeMillis = 283)
        logger.info("callOrderApi() after 283ms")
    }
}
