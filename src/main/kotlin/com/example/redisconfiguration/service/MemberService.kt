package com.example.redisconfiguration.service

import com.example.redisconfiguration.config.RedisExpireTime
import com.example.redisconfiguration.config.RedisKey
import com.example.redisconfiguration.domain.Member
import com.example.redisconfiguration.repository.MemberRepository
import com.example.redisconfiguration.service.dto.CreateMemberCacheDto
import com.example.redisconfiguration.service.dto.CreateMemberCacheResultDto
import com.example.redisconfiguration.service.dto.FindMemberCacheResultDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class MemberService(
    private val memberRepository: MemberRepository
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun set(id: Long, name: String): CreateMemberCacheResultDto {
        val key = RedisKey.getMemberKey(id = id)
        val value = Member.create(id = id, name = name)

        try {
            memberRepository.set(
                key = key,
                value = value,
                expireTime = RedisExpireTime.MEMBER,
                timeUnit = TimeUnit.SECONDS
            )
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
        }

        return CreateMemberCacheResultDto(memberId = id)
    }

    fun setByPipeline(dtos: List<CreateMemberCacheDto>): List<CreateMemberCacheResultDto> {
        val keysAndValues = dtos.map {
            Pair(first = RedisKey.getMemberKey(id = it.id), second = Member.create(id = it.id, name = it.name))
        }

        try {
            memberRepository.setByPipeline(
                keysAndValues = keysAndValues,
                expireTime = RedisExpireTime.MEMBER,
                timeUnit = TimeUnit.SECONDS
            )
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
        }

        return dtos.map { CreateMemberCacheResultDto(memberId = it.id) }
    }

    fun get(id: Long): FindMemberCacheResultDto = runBlocking {
        val key = RedisKey.getMemberKey(id = id)

        try {
            memberRepository.get(key = key, clazz = Member::class.java)
                ?.let { return@runBlocking FindMemberCacheResultDto(memberId = it.id, name = it.name) }
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
            return@runBlocking FindMemberCacheResultDto(memberId = id, name = "redis connection failure fallback")
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
            return@runBlocking FindMemberCacheResultDto(memberId = id, name = "query timeout fallback")
        }

        val value = Member.create(id = id, name = "member name retrieved from rdbms")

        launch(context = Dispatchers.IO) { setMemberCache(key = key, value = value) }

        FindMemberCacheResultDto(memberId = value.id, name = value.name)
    }

    fun getByPipeline(start: Int, count: Int): List<FindMemberCacheResultDto> = runBlocking {
        val ids = (1..100).map { it.toLong() }.slice(start until (start + count))
        val keys = ids.map { RedisKey.getMemberKey(id = it) }

        try {
            val cacheValues = memberRepository.getByPipeline(keys = keys, clazz = Member::class.java)
                .filterNotNull()

            if (cacheValues.size == count) {
                return@runBlocking cacheValues.map { FindMemberCacheResultDto(memberId = it.id, name = it.name) }
            }
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
            return@runBlocking ids.map { FindMemberCacheResultDto(memberId = it, name = "redis connection failure fallback") }
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
            return@runBlocking ids.map { FindMemberCacheResultDto(memberId = it, name = "query timeout fallback") }
        }

        val keysAndValues = ids.map {
            Pair(
                first = RedisKey.getMemberKey(id = it),
                second = Member.create(id = it, name = "member name retrieved from rdbms")
            )
        }

        launch(context = Dispatchers.IO) { setMemberCaches(keysAndValues = keysAndValues) }

        keysAndValues.map { FindMemberCacheResultDto(memberId = it.second.id, name = it.second.name) }
    }

    suspend fun <T : Any> setMemberCache(key: String, value: T) {
        memberRepository.set(key = key, value = value, expireTime = RedisExpireTime.MEMBER, timeUnit = TimeUnit.SECONDS)
    }

    suspend fun <T : Any> setMemberCaches(keysAndValues: List<Pair<String, T>>) {
        memberRepository.setByPipeline(
            keysAndValues = keysAndValues,
            expireTime = RedisExpireTime.MEMBER,
            timeUnit = TimeUnit.SECONDS
        )
    }
}
