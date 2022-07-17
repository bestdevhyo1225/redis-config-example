package com.example.redisconfiguration.controller

import com.example.redisconfiguration.controller.response.SuccessResponse
import com.example.redisconfiguration.service.ApiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/apis")
class ApiController(
    private val apiService: ApiService
) {

    @GetMapping
    fun call(): ResponseEntity<SuccessResponse<Map<String, Any>>> {
        apiService.call()
        return ResponseEntity.ok(SuccessResponse(data = mapOf()))
    }
}
