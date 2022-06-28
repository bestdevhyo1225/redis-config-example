package com.example.redisconfiguration.service

import com.example.redisconfiguration.config.RedisKey
import com.example.redisconfiguration.domain.Member
import com.example.redisconfiguration.repository.MemberRepository
import com.example.redisconfiguration.service.dto.CreateMemberCacheDto
import com.example.redisconfiguration.service.dto.CreateMemberCacheResultDto
import com.example.redisconfiguration.service.dto.FindMemberCacheResultDto
import org.slf4j.LoggerFactory
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

        memberRepository.set(key = key, value = value, expireTime = expireTime, timeUnit = timeUnit)

        return CreateMemberCacheResultDto(memberId = id)
    }

    fun setByPipeline(dtos: List<CreateMemberCacheDto>): List<CreateMemberCacheResultDto> {
        val keysAndValues = dtos.map {
            Pair(first = RedisKey.getMemberKey(id = it.id), second = Member.create(id = it.id, name = it.name))
        }
        val expireTimeSeconds = 60L

        memberRepository.setByPipeline(keysAndValues = keysAndValues, expireTimeSeconds = expireTimeSeconds)

        return dtos.map { CreateMemberCacheResultDto(memberId = it.id) }
    }

    fun get(id: Long): FindMemberCacheResultDto {
        return try {
            val key = RedisKey.getMemberKey(id = id)
            val member = memberRepository.get(key = key, clazz = Member::class.java)!!
            FindMemberCacheResultDto(name = member.name)
        } catch (exception: NullPointerException) {
            throw NoSuchElementException("해당 회원이 존재하지 않습니다.")
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
            // 나중에는 RDMBS의 조회 결과로 대체할 것
            FindMemberCacheResultDto(name = "fallback")
        }
    }
}
