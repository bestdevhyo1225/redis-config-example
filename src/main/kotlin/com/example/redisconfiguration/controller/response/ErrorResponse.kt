package com.example.redisconfiguration.controller.response

data class ErrorResponse(
    val status: String = "error",
    val message: String,
)
