package com.example.redisconfiguration.config

import io.lettuce.core.ClientOptions
import io.lettuce.core.ReadFrom.REPLICA_PREFERRED
import io.lettuce.core.TimeoutOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching(proxyTargetClass = true)
class RedisConfig(
    @Value("\${spring.data.redis.mode}")
    private val mode: String,

    @Value("\${spring.data.redis.nodes}")
    private val nodes: List<String>,

    @Value("\${spring.data.redis.command-timeout}")
    private val commandTimeout: Long,

    @Value("\${spring.data.redis.shutdown-timeout}")
    private val shutdownTimeout: Long,
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        if (mode == "standalone") {
            return LettuceConnectionFactory(standaloneConfig(), lettuceClientConfig())
        }

        if (mode == "cluster") {
            return LettuceConnectionFactory(clusterConfig(), lettuceClientConfig())
        }

        throw IllegalArgumentException("'$mode'는 존재하지 않는 모드입니다.")
    }

    private fun standaloneConfig(): RedisStandaloneConfiguration {
        val splitNodes = nodes.first().split(":")
        return RedisStandaloneConfiguration(splitNodes[0], splitNodes[1].toInt())
    }

    private fun clusterConfig(): RedisClusterConfiguration {
        return RedisClusterConfiguration(nodes)
    }

    private fun lettuceClientConfig(): LettuceClientConfiguration {
        // ClientOptions 설정은 디폴트를 따름. -> 디테일한 설정이 필요하면 설정을 변경하자.
        return LettuceClientConfiguration.builder()
            .clientName("my-server")
            .clientOptions(clientOptions())
            .readFrom(REPLICA_PREFERRED)
            .shutdownTimeout(Duration.ofMillis(shutdownTimeout))
            .build()
    }

    private fun clientOptions(): ClientOptions {
        return ClientOptions.builder()
            .timeoutOptions(timeoutOptions())
            .build()
    }

    private fun timeoutOptions(): TimeoutOptions {
        return TimeoutOptions.builder()
            .timeoutCommands()
            .fixedTimeout(Duration.ofMillis(commandTimeout))
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
