package com.example.redisconfiguration

import com.example.redisconfiguration.controller.response.ErrorResponse
import com.example.redisconfiguration.controller.response.FailResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.validation.ConstraintViolationException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handle(exception: IllegalArgumentException): ResponseEntity<FailResponse> {
        logger.error("exception", exception)
        return ResponseEntity(FailResponse(message = exception.localizedMessage), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handle(exception: ConstraintViolationException): ResponseEntity<FailResponse> {
        logger.error("exception", exception)
        return ResponseEntity(FailResponse(message = exception.localizedMessage), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(exception: MethodArgumentNotValidException): ResponseEntity<FailResponse> {
        logger.error("exception", exception)
        return ResponseEntity(
            FailResponse(message = exception.bindingResult.allErrors.first().defaultMessage ?: ""),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handle(exception: NoSuchElementException): ResponseEntity<FailResponse> {
        logger.error("exception", exception)
        return ResponseEntity(FailResponse(message = exception.localizedMessage), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handle(exception: Exception): ResponseEntity<ErrorResponse> {
        logger.error("exception", exception)
        return ResponseEntity(
            ErrorResponse(message = HttpStatus.INTERNAL_SERVER_ERROR.name),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
