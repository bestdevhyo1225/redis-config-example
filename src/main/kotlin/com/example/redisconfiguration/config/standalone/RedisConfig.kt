package com.example.redisconfiguration.config.standalone

import com.example.redisconfiguration.config.RedisMode
import com.example.redisconfiguration.config.RedisNodesKey
import com.example.redisconfiguration.config.property.RedisServers
import io.lettuce.core.ClientOptions
import io.lettuce.core.ReadFrom.REPLICA_PREFERRED
import io.lettuce.core.TimeoutOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
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
@Profile(value = ["test", "redis-standalone"])
@EnableConfigurationProperties(value = [RedisServers::class])
@EnableCaching(proxyTargetClass = true)
class RedisConfig(
    @Value("\${spring.data.redis.mode}")
    private val mode: String,

    private val redisServers: RedisServers,

    @Value("\${spring.data.redis.command-timeout}")
    private val commandTimeout: Long,

    @Value("\${spring.data.redis.shutdown-timeout}")
    private val shutdownTimeout: Long,
) {

    @Primary
    @Bean(name = ["redisServer1ConnectionFactory"])
    fun redisServer1ConnectionFactory(): RedisConnectionFactory {
        if (mode == RedisMode.STANDALONE) {
            val splitNodes = redisServers.nodes[RedisNodesKey.SERVER_1]!!.first().split(":")
            val redisStandaloneConfig = standaloneConfig(host = splitNodes[0], port = splitNodes[1].toInt())
            return LettuceConnectionFactory(redisStandaloneConfig, lettuceClientConfig())
        }

        throw IllegalArgumentException("'$mode'??? ???????????? ?????? ???????????????.")
    }

    @Bean(name = ["redisServer2ConnectionFactory"])
    fun redisServer2ConnectionFactory(): RedisConnectionFactory {
        if (mode == RedisMode.STANDALONE) {
            val splitNodes = redisServers.nodes[RedisNodesKey.SERVER_2]!!.first().split(":")
            val redisStandaloneConfig = standaloneConfig(host = splitNodes[0], port = splitNodes[1].toInt())
            return LettuceConnectionFactory(redisStandaloneConfig, lettuceClientConfig())
        }

        throw IllegalArgumentException("'$mode'??? ???????????? ?????? ???????????????.")
    }

    @Bean(name = ["redisServer3ConnectionFactory"])
    fun redisServer3ConnectionFactory(): RedisConnectionFactory {
        if (mode == RedisMode.STANDALONE) {
            val splitNodes = redisServers.nodes[RedisNodesKey.SERVER_3]!!.first().split(":")
            val redisStandaloneConfig = standaloneConfig(host = splitNodes[0], port = splitNodes[1].toInt())
            return LettuceConnectionFactory(redisStandaloneConfig, lettuceClientConfig())
        }

        throw IllegalArgumentException("'$mode'??? ???????????? ?????? ???????????????.")
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
