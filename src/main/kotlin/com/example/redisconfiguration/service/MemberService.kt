package com.example.redisconfiguration.service

import com.example.redisconfiguration.config.RedisKey
import com.example.redisconfiguration.domain.Member
import com.example.redisconfiguration.repository.MemberRepository
import com.example.redisconfiguration.service.dto.CreateMemberCacheDto
import com.example.redisconfiguration.service.dto.CreateMemberCacheResultDto
import com.example.redisconfiguration.service.dto.FindMemberCacheResultDto
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
        val expireTime = 60L
        val timeUnit = TimeUnit.SECONDS

        try {
            memberRepository.set(key = key, value = value, expireTime = expireTime, timeUnit = timeUnit)
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
        val expireTime = 60L
        val timeUnit = TimeUnit.SECONDS

        try {
            memberRepository.setByPipeline(keysAndValues = keysAndValues, expireTime = expireTime, timeUnit = timeUnit)
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
            val member = memberRepository.get(key = key, clazz = Member::class.java)!!
            FindMemberCacheResultDto(memberId = member.id, name = member.name)
        } catch (exception: NullPointerException) {
            throw NoSuchElementException("해당 회원이 존재하지 않습니다.")
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
            FindMemberCacheResultDto(memberId = 0, name = "redis connection failure fallback")
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
            FindMemberCacheResultDto(memberId = 0, name = "query timeout fallback")
        }
    }

    fun getByPipeline(start: Int, count: Int): List<FindMemberCacheResultDto> {
        val ids = (1..100).map { it.toLong() }.slice(start until (start + count))
        val keys = ids.map { RedisKey.getMemberKey(id = it) }

        return try {
            val members = memberRepository.getByPipeline(keys = keys, clazz = Member::class.java)
            members.filterNotNull().map { FindMemberCacheResultDto(memberId = it.id, name = it.name) }
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
            ids.map { FindMemberCacheResultDto(memberId = it, name = "redis connection failure fallback") }
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
            ids.map { FindMemberCacheResultDto(memberId = it, name = "query timeout fallback") }
        }
    }
}
