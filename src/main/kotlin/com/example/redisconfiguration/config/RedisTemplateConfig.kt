package com.example.redisconfiguration.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisTemplateConfig {

    @Bean(name = ["redisServer1Template"])
    fun redisServer1Template(
        @Qualifier(value = "redisServer1ConnectionFactory")
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, String?> {
        return redisTemplate(redisConnectionFactory = redisConnectionFactory)
    }

    @Bean(name = ["redisServer2Template"])
    fun redisServer2Template(
        @Qualifier(value = "redisServer2ConnectionFactory")
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, String?> {
        return redisTemplate(redisConnectionFactory = redisConnectionFactory)
    }

    private fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String?> {
        val stringRedisSerializer = StringRedisSerializer()
        val redisTemplate = RedisTemplate<String, String?>()
        redisTemplate.setConnectionFactory(redisConnectionFactory)
        redisTemplate.keySerializer = stringRedisSerializer
        redisTemplate.valueSerializer = stringRedisSerializer
        redisTemplate.hashKeySerializer = stringRedisSerializer
        redisTemplate.hashValueSerializer = stringRedisSerializer
        return redisTemplate
    }
}
