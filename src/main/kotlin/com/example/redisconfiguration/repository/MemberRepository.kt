package com.example.redisconfiguration.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.ln

@Repository
class MemberRepository(
    private val redisTemplate: RedisTemplate<String, String?>
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun <T : Any> set(key: String, value: T, expireTime: Long, timeUnit: TimeUnit) {
        logger.debug("set key: {}, value: {}", key, value)

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
        if (isExpiredKey(key = key)) return null

        val value = redisTemplate.opsForValue().get(key)

        logger.debug("get value: {}", value)

        if (value.isNullOrBlank()) return null

        return jacksonObjectMapper().readValue(value, clazz)
    }

    private fun isExpiredKey(key: String, expireTimeGapMs: Long = 3_000L): Boolean {
        val remainingExpiryTimeMS: Long = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS)

        logger.debug("remainingExpiryTimeMS: {}", remainingExpiryTimeMS)

        return remainingExpiryTimeMS >= 0
            && getExpiryTimeBasedOnPER(remainingExpiryTimeMS = remainingExpiryTimeMS, delta = expireTimeGapMs) <= 0.0f
    }

    /**
     * [ remainingExpiryTimeMS ]
     * - 남은 만료 시간
     * [ delta ]
     * - 캐시를 다시 계산하기 위한 시간 범위 (단위: MS)
     * [ beta ]
     * - 가중치 (기본 값으로 1.0을 사용한다.)
     * - ex) beta < 1.0 => 조금 더 소극적으로 재 계산한다.
     * - ex) beta > 1.0 => 조금 더 적극적으로 재 계산한다.
     * */
    private fun getExpiryTimeBasedOnPER(remainingExpiryTimeMS: Long, delta: Long, beta: Float = 1.0f): Double {
        val resultExpiryTime = remainingExpiryTimeMS - abs(delta * beta * ln(Math.random()))

        logger.debug("resultExpiryTime: {}", resultExpiryTime)

        return resultExpiryTime
    }
}
