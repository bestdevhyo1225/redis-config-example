package com.example.redisconfiguration.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class MemberRedisServer3Repository(
    @Qualifier(value = "redisServer3Template")
    private val redisServer3Template: RedisTemplate<String, String?>,
) : MemberRedisServerRepository, AbstractRedisServerRepository() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun <T : Any> set(key: String, value: T, expireTime: Long, timeUnit: TimeUnit) {
        redisServer3Template.opsForValue().set(key, jacksonObjectMapper().writeValueAsString(value), expireTime, timeUnit)
    }

    override fun <T : Any> setUsingPipeline(keysAndValues: List<Pair<String, T>>, expireTime: Long, timeUnit: TimeUnit) {
        redisServer3Template.executePipelined {
            keysAndValues.forEach { keyAndValue: Pair<String, T> ->
                set(key = keyAndValue.first, value = keyAndValue.second, expireTime = expireTime, timeUnit = timeUnit)
            }
            return@executePipelined null
        }
    }

    override fun <T> get(key: String, clazz: Class<T>): T? {
        if (shouldRefreshKey(key = key)) {
            logger.info("current key is need refresh")
            return null
        }

        val value = redisServer3Template.opsForValue().get(key)

        if (value.isNullOrBlank()) {
            logger.info("value is null or blank")
            return null
        }

        return jacksonObjectMapper().readValue(value, clazz)
    }

    override fun <T> getUsingPipeline(keys: List<String>, clazz: Class<T>): List<T?> {
        val values = mutableListOf<T?>()

        redisServer3Template.executePipelined {
            runBlocking {
                values.addAll(
                    keys.map { key: String ->
                        async(context = Dispatchers.IO) { getUsingCoroutine(key = key, clazz = clazz) }
                    }.awaitAll()
                )
            }
            return@executePipelined null
        }

        return values
    }

    suspend fun <T> getUsingCoroutine(key: String, clazz: Class<T>): T? = get(key = key, clazz = clazz)

    private fun shouldRefreshKey(key: String, expireTimeGapMs: Long = 3_000L): Boolean {
        val remainingExpiryTimeMS = redisServer3Template.getExpire(key, TimeUnit.MILLISECONDS)
        return remainingExpiryTimeMS >= 0
            && getExpiryTimeBasedOnPER(remainingExpiryTimeMS = remainingExpiryTimeMS, delta = expireTimeGapMs) <= 0.0f
    }
}
