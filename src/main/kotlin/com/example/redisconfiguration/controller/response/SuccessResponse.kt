package com.example.redisconfiguration.controller.response

data class SuccessResponse<T>(
    val status: String = "success",
    val data: T,
)
