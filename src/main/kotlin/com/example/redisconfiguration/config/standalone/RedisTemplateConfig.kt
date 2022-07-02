package com.example.redisconfiguration.config.standalone

import com.example.redisconfiguration.config.RedisTemplateCommon
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
@Profile(value = ["test", "redis-standalone"])
class RedisTemplateConfig {

    @Bean(name = ["redisServer1Template"])
    fun redisServer1Template(
        @Qualifier(value = "redisServer1ConnectionFactory")
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, String?> {
        return RedisTemplateCommon.redisTemplate(redisConnectionFactory = redisConnectionFactory)
    }

    @Bean(name = ["redisServer2Template"])
    fun redisServer2Template(
        @Qualifier(value = "redisServer2ConnectionFactory")
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, String?> {
        return RedisTemplateCommon.redisTemplate(redisConnectionFactory = redisConnectionFactory)
    }

    @Bean(name = ["redisServer3Template"])
    fun redisServer3Template(
        @Qualifier(value = "redisServer3ConnectionFactory")
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, String?> {
        return RedisTemplateCommon.redisTemplate(redisConnectionFactory = redisConnectionFactory)
    }
}
