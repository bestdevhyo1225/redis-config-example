package com.example.redisconfiguration.service

import com.example.redisconfiguration.config.RedisExpireTime
import com.example.redisconfiguration.config.RedisKey
import com.example.redisconfiguration.domain.Member
import com.example.redisconfiguration.repository.MemberRepository
import com.example.redisconfiguration.service.dto.CreateMemberCacheDto
import com.example.redisconfiguration.service.dto.CreateMemberCacheResultDto
import com.example.redisconfiguration.service.dto.FindMemberCacheResultDto
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

    fun get(id: Long): FindMemberCacheResultDto {
        val key = RedisKey.getMemberKey(id = id)

        return try {
            memberRepository.get(key = key, clazz = Member::class.java)
                ?.let { return FindMemberCacheResultDto(memberId = it.id, name = it.name) }

            val value = Member.create(id = id, name = "member name retrieved from rdbms")

            runBlocking {
                launch(Dispatchers.IO) {
                    memberRepository.set(
                        key = key,
                        value = value,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
            }

            FindMemberCacheResultDto(memberId = value.id, name = value.name)
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
            FindMemberCacheResultDto(memberId = id, name = "redis connection failure fallback")
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
            FindMemberCacheResultDto(memberId = id, name = "query timeout fallback")
        }
    }

    fun getByPipeline(start: Int, count: Int): List<FindMemberCacheResultDto> {
        val ids = (1..100).map { it.toLong() }.slice(start until (start + count))
        val keys = ids.map { RedisKey.getMemberKey(id = it) }

        return try {
            val cacheValues = memberRepository.getByPipeline(keys = keys, clazz = Member::class.java)
                .filterNotNull()

            if (cacheValues.size == count) {
                return cacheValues.map { FindMemberCacheResultDto(memberId = it.id, name = it.name) }
            }

            val keysAndValues = ids.map {
                Pair(
                    first = RedisKey.getMemberKey(id = it),
                    second = Member.create(id = it, name = "member name retrieved from rdbms")
                )
            }

            runBlocking {
                launch(Dispatchers.IO) {
                    memberRepository.setByPipeline(
                        keysAndValues = keysAndValues,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
            }

            keysAndValues.map { FindMemberCacheResultDto(memberId = it.second.id, name = it.second.name) }
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
            ids.map { FindMemberCacheResultDto(memberId = it, name = "redis connection failure fallback") }
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
            ids.map { FindMemberCacheResultDto(memberId = it, name = "query timeout fallback") }
        }
    }
}
