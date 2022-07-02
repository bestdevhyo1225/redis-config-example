package com.example.redisconfiguration.config

import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

object RedisTemplateCommon {

    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String?> {
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
