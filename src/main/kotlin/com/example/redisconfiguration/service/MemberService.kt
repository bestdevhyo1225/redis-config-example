package com.example.redisconfiguration.service

import com.example.redisconfiguration.domain.Member
import com.example.redisconfiguration.repository.MemberFacadeRepository
import com.example.redisconfiguration.service.dto.CreateMemberCacheDto
import com.example.redisconfiguration.service.dto.CreateMemberCacheResultDto
import com.example.redisconfiguration.service.dto.FindMemberCacheResultDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberFacadeRepository: MemberFacadeRepository
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun set(id: Long, name: String): CreateMemberCacheResultDto {
        logger.info("set() start")

        val value = Member.create(id = id, name = name)

        memberFacadeRepository.setMemberCache(value = value)

        return CreateMemberCacheResultDto(memberId = id)
    }

    fun setUsingPipeline(dtos: List<CreateMemberCacheDto>): List<CreateMemberCacheResultDto> {
        logger.info("setUsingPipeline() start")

        val values = dtos.map { Member.create(id = it.id, name = it.name) }

        memberFacadeRepository.setMembersCache(values = values)

        return dtos.map { CreateMemberCacheResultDto(memberId = it.id) }
    }

    fun get(id: Long): FindMemberCacheResultDto {
        logger.info("get() start")

        memberFacadeRepository.getMemberCache(id = id)
            ?.let { return FindMemberCacheResultDto(memberId = it.id, name = it.name) }

        val value = Member.create(id = id, name = "member name retrieved from rdbms")

        memberFacadeRepository.setMemberCache(value = value)

        return FindMemberCacheResultDto(memberId = value.id, name = value.name)
    }

    fun getUsingPipeline(start: Int, count: Int): List<FindMemberCacheResultDto> {
        logger.info("getUsingPipeline() start")

        val ids = (1..100).map { it.toLong() }.slice(start until (start + count))

        val cacheValues = memberFacadeRepository.getMembersCache(ids = ids)
            .filterNotNull()

        if (cacheValues.size == count) {
            return cacheValues.map { FindMemberCacheResultDto(memberId = it.id, name = it.name) }
        }

        val values = ids.map { Member.create(id = it, name = "member name retrieved from rdbms") }

        memberFacadeRepository.setMembersCache(values = values)

        return values.map { FindMemberCacheResultDto(memberId = it.id, name = it.name) }
    }
}
