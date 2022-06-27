package com.example.redisconfiguration.service

import com.example.redisconfiguration.domain.Member
import com.example.redisconfiguration.repository.MemberRepository
import com.example.redisconfiguration.service.dto.CreateMemberCacheResult
import com.example.redisconfiguration.service.dto.FindMemberCacheResult
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class MemberService(
    private val memberRepository: MemberRepository
) {

    fun set(id: Long, name: String): CreateMemberCacheResult {
        val key = "member:${id}"
        val value = Member.create(id = id, name = name)
        val expireTime = 60L
        val timeUnit = TimeUnit.SECONDS
        memberRepository.set(key = key, value = value, expireTime = expireTime, timeUnit = timeUnit)
        return CreateMemberCacheResult(memberId = id)
    }

    fun get(id: Long): FindMemberCacheResult {
        val key = "member:${id}"
        val member = memberRepository.get(key = key, clazz = Member::class.java)
            ?: throw NoSuchElementException("해당 회원이 존재하지 않습니다.")
        return FindMemberCacheResult(name = member.name)
    }
}
