package com.example.redisconfiguration.config

import io.lettuce.core.ClientOptions
import io.lettuce.core.ReadFrom.REPLICA_PREFERRED
import io.lettuce.core.TimeoutOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

@Configuration
@EnableCaching(proxyTargetClass = true)
@Profile(value = ["redis-standalone"])
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

    @Primary
    @Bean(name = ["redisServer1ConnectionFactory"])
    fun redisServer1ConnectionFactory(): RedisConnectionFactory {
        if (mode == "standalone") {
            val splitNodes = nodes.first().split(":")
            return LettuceConnectionFactory(
                standaloneConfig(host = splitNodes[0], port = splitNodes[1].toInt()),
                lettuceClientConfig()
            )
        }

        throw IllegalArgumentException("'$mode'는 존재하지 않는 모드입니다.")
    }

    @Bean(name = ["redisServer2ConnectionFactory"])
    fun redisServer2ConnectionFactory(): RedisConnectionFactory {
        if (mode == "standalone") {
            val splitNodes = nodes[1].split(":")
            return LettuceConnectionFactory(
                standaloneConfig(host = splitNodes[0], port = splitNodes[1].toInt()),
                lettuceClientConfig()
            )
        }

        throw IllegalArgumentException("'$mode'는 존재하지 않는 모드입니다.")
    }

    private fun standaloneConfig(host: String, port: Int): RedisStandaloneConfiguration {
        return RedisStandaloneConfiguration(host, port)
    }

    private fun lettuceClientConfig(): LettuceClientConfiguration {
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
}
