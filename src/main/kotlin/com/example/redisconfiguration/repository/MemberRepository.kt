package com.example.redisconfiguration.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class MemberRepository(
    private val redisTemplate: RedisTemplate<String, String?>
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun <T : Any> set(key: String, value: T, expireTime: Long, timeUnit: TimeUnit) {
        logger.debug("Redis set key: {}, value: {}", key, value)

        redisTemplate.opsForValue().set(key, jacksonObjectMapper().writeValueAsString(value))
        redisTemplate.expire(key, expireTime, timeUnit)
    }

    fun <T> get(key: String, clazz: Class<T>): T? {
        val value = redisTemplate.opsForValue().get(key)

        logger.debug("Redis get value: {}", value)

        if (value == null || value.isBlank()) {
            return null
        }

        return jacksonObjectMapper().readValue(value, clazz)
    }
}
