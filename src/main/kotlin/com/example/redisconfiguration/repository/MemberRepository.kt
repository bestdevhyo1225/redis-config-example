package com.example.redisconfiguration.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
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

    fun <T : Any> setByPipeline(keysAndValues: List<Pair<String, T>>, expireTime: Long) {
        redisTemplate.executePipelined { redisConnection ->
            val stringRedisSerializer = StringRedisSerializer()

            keysAndValues.forEach { keyAndValue ->
                redisConnection.setEx(
                    stringRedisSerializer.serialize(keyAndValue.first),
                    expireTime,
                    stringRedisSerializer.serialize(jacksonObjectMapper().writeValueAsString(keyAndValue.second))
                )
            }

            return@executePipelined null
        }
    }

    fun <T> get(key: String, clazz: Class<T>): T? {
        val value = redisTemplate.opsForValue().get(key)

        logger.debug("Redis get value: {}", value)

        if (value.isNullOrBlank()) return null

        return jacksonObjectMapper().readValue(value, clazz)
    }
}
