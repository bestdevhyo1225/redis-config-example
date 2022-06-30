package com.example.redisconfiguration.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
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
        redisTemplate.opsForValue().set(key, jacksonObjectMapper().writeValueAsString(value), expireTime, timeUnit)
    }

    fun <T : Any> setByPipeline(keysAndValues: List<Pair<String, T>>, expireTime: Long, timeUnit: TimeUnit) {
        redisTemplate.executePipelined {
            keysAndValues.forEach { keyAndValue ->
                set(key = keyAndValue.first, value = keyAndValue.second, expireTime = expireTime, timeUnit = timeUnit)
            }
            return@executePipelined null
        }
    }

    fun <T> get(key: String, clazz: Class<T>): T? {
        if (shouldRefreshKey(key = key)) {
            logger.info("current key is need refresh")
            return null
        }

        val value = redisTemplate.opsForValue().get(key)

        if (value.isNullOrBlank()) {
            logger.info("value is null or blank")
            return null
        }

        return jacksonObjectMapper().readValue(value, clazz)
    }

    fun <T> getByPipeline(keys: List<String>, clazz: Class<T>): List<T?> {
        val results = mutableListOf<T?>()

        redisTemplate.executePipelined {
            runBlocking {
                results.addAll(
                    keys.map { key -> async(context = Dispatchers.IO) { get(key = key, clazz = clazz) } }
                        .awaitAll()
                )
            }
            return@executePipelined null
        }

        return results
    }

    private fun shouldRefreshKey(key: String, expireTimeGapMs: Long = 3_000L): Boolean {
        val remainingExpiryTimeMS: Long = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS)
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
        return remainingExpiryTimeMS - abs(delta * beta * ln(Math.random()))
    }
}
