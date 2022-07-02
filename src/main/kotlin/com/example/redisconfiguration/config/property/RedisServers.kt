package com.example.redisconfiguration.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisServers(
    val nodes: Map<String, List<String>>
)
