package com.example.redisconfiguration.controller

import com.example.redisconfiguration.controller.response.SuccessResponse
import com.example.redisconfiguration.service.ApiService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/apis")
class ApiController(
    private val apiService: ApiService
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    fun call(): ResponseEntity<SuccessResponse<Map<String, Any>>> = runBlocking {
        logger.info("current thread name: {}", Thread.currentThread().name)

        val results: List<Long> = apiService.call()
        ResponseEntity.ok(SuccessResponse(data = mapOf("results" to results)))
    }
}
