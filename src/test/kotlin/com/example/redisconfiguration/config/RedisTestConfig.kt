package com.example.redisconfiguration.config

import io.lettuce.core.ReadFrom
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@TestConfiguration
@EnableCaching(proxyTargetClass = true)
@Profile(value = ["test"])
class RedisTestConfig(
    @Value("\${spring.data.redis.mode}")
    private val mode: String,

    @Value("\${spring.data.redis.host}")
    private val host: String,

    @Value("\${spring.data.redis.port}")
    private val port: Int,

    @Value("\${spring.data.redis.command-timeout}")
    private val commandTimeout: Long,

    @Value("\${spring.data.redis.shutdown-timeout}")
    private val shutdownTimeout: Long,
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(RedisStandaloneConfiguration(host, port), lettuceClientConfig())
    }

    private fun lettuceClientConfig(): LettuceClientConfiguration {
        // ClientOptions 설정은 디폴트를 따름. -> 디테일한 설정이 필요하면 설정을 변경하자.
        return LettuceClientConfiguration.builder()
            .clientName("test-api")
            .readFrom(ReadFrom.REPLICA_PREFERRED)
            .commandTimeout(Duration.ofSeconds(commandTimeout)) // Connection Timeout
            .shutdownTimeout(Duration.ofMillis(shutdownTimeout))
            .build()
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String?> {
        val stringRedisSerializer = StringRedisSerializer()
        val redisTemplate = RedisTemplate<String, String?>()
        redisTemplate.setConnectionFactory(redisConnectionFactory())
        redisTemplate.keySerializer = stringRedisSerializer
        redisTemplate.valueSerializer = stringRedisSerializer
        redisTemplate.hashKeySerializer = stringRedisSerializer
        redisTemplate.hashValueSerializer = stringRedisSerializer
        return redisTemplate
    }
}
