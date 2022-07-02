package com.example.redisconfiguration.service

import com.example.redisconfiguration.domain.Member
import com.example.redisconfiguration.repository.MemberFacadeRepository
import com.example.redisconfiguration.service.dto.CreateMemberCacheDto
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

internal class MemberServiceTests : DescribeSpec({

    val mockMemberFacadeRepository = mockk<MemberFacadeRepository>()
    val memberService = MemberService(memberFacadeRepository = mockMemberFacadeRepository)

    describe("set 메소드는") {
        val id = 1L
        val name = "HongGilDong"
        val value = Member.create(id = id, name = name)

        it("Member 캐시를 저장한다.") {
            justRun { mockMemberFacadeRepository.setMemberCache(value = value) }

            memberService.set(id = id, name = name)
        }
    }

    describe("setUsingPipeline 메소드는") {
        val dtos = listOf(
            CreateMemberCacheDto(id = 1L, name = "HongGilDong"),
            CreateMemberCacheDto(id = 2L, name = "JangHyoSeok")
        )
        val values = dtos.map { Member.create(id = it.id, name = it.name) }

        it("Members 캐시를 저장한다.") {
            justRun { mockMemberFacadeRepository.setMembersCache(values = values) }

            memberService.setUsingPipeline(dtos = dtos)
        }
    }

    describe("get 메소드는") {
        val id = 1L

        context("Member 캐시가 존재하는 경우") {
            it("Member 캐시를 반환한다.") {
                // given
                every {
                    mockMemberFacadeRepository.getMemberCache(id = id)
                }.returns(Member.create(id = id, name = "KimDongSu"))

                // when
                memberService.get(id = id)

                // then
                verify { mockMemberFacadeRepository.getMemberCache(id = id) }
            }
        }

        context("Member 캐시가 존재하지 않는 경우") {
            val value = Member.create(id = id, name = "member name retrieved from rdbms")

            it("Member 캐시를 저장한 후, Member 캐시를 반환한다.") {
                // given
                every {
                    mockMemberFacadeRepository.getMemberCache(id = id)
                }.returns(null)
                justRun { mockMemberFacadeRepository.setMemberCache(value = value) }

                // when
                memberService.get(id = id)

                // then
                verify { mockMemberFacadeRepository.getMemberCache(id = id) }
            }
        }
    }

    describe("getUsingPipeline 메소드는") {
        val start = 0
        val count = 10
        val ids = (1..100).map { it.toLong() }.slice(start until (start + count))

        context("Members 캐시가 존재하는 경우") {
            it("Member 캐시를 반환한다.") {
                // given
                every {
                    mockMemberFacadeRepository.getMembersCache(ids = ids)
                }.returns(ids.map { Member.create(id = it, name = "KimDongSu") })

                // when
                memberService.getUsingPipeline(start = start, count = count)

                // then
                verify { mockMemberFacadeRepository.getMembersCache(ids = ids) }
            }
        }

        context("Members 캐시가 존재하지 않는 경우") {
            val values = ids.map { Member.create(id = it, name = "member name retrieved from rdbms") }

            it("Member 캐시를 저장한 후, Member 캐시를 반환한다.") {
                // given
                every {
                    mockMemberFacadeRepository.getMembersCache(ids = ids)
                }.returns(listOf(Member.create(id = ids.first(), name = "KimDongSu")))

                justRun { mockMemberFacadeRepository.setMembersCache(values = values) }

                // when
                memberService.getUsingPipeline(start = start, count = count)

                // then
                verify { mockMemberFacadeRepository.getMembersCache(ids = ids) }
            }
        }
    }
})
