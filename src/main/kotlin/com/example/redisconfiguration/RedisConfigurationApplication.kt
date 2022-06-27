package com.example.redisconfiguration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedisConfigurationApplication

fun main(args: Array<String>) {
	runApplication<RedisConfigurationApplication>(*args)
}
