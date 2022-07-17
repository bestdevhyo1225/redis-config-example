package com.example.redisconfiguration.repository

import java.util.concurrent.TimeUnit

interface MemberRedisServerRepository {
    fun <T : Any> set(key: String, value: T, expireTime: Long, timeUnit: TimeUnit)
    fun <T : Any> setUsingPipeline(keysAndValues: List<Pair<String, T>>, expireTime: Long, timeUnit: TimeUnit)
    fun <T> get(key: String, clazz: Class<T>): T?
    fun <T> getUsingPipeline(keys: List<String>, clazz: Class<T>): List<T?>
}
