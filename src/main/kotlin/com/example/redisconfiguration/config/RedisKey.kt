package com.example.redisconfiguration.config

object RedisKey {

    fun getMemberKey(id: Long): String = "member:${id}"
}
