package com.example.redisconfiguration.config

import com.example.redisconfiguration.config.property.RedisServers
import io.lettuce.core.ClientOptions
import io.lettuce.core.ReadFrom
import io.lettuce.core.TimeoutOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

@Configuration
@Profile(value = ["redis-cluster"])
@EnableConfigurationProperties(value = [RedisServers::class])
@EnableCaching(proxyTargetClass = true)
class RedisClusterConfig(
    @Value("\${spring.data.redis.mode}")
    private val mode: String,

    private val redisServers: RedisServers,

    @Value("\${spring.data.redis.command-timeout}")
    private val commandTimeout: Long,

    @Value("\${spring.data.redis.shutdown-timeout}")
    private val shutdownTimeout: Long,
) {

    @Bean(name = ["redisServer1ConnectionFactory"])
    fun redisServer1ConnectionFactory(): RedisConnectionFactory {
        if (mode == RedisMode.CLUSTER) {
            return LettuceConnectionFactory(clusterConfig(), lettuceClientConfig())
        }

        throw IllegalArgumentException("'$mode'는 존재하지 않는 모드입니다.")
    }

    private fun clusterConfig(): RedisClusterConfiguration {
        return RedisClusterConfiguration(redisServers.nodes[RedisNodesKey.SERVER_1]!!)
    }

    private fun lettuceClientConfig(): LettuceClientConfiguration {
        return LettuceClientConfiguration.builder()
            .clientName("my-server")
            .clientOptions(clientOptions())
            .readFrom(ReadFrom.REPLICA_PREFERRED)
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
