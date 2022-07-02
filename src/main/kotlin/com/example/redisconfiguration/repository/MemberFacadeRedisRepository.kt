package com.example.redisconfiguration.repository

import com.example.redisconfiguration.domain.Member

interface MemberFacadeRedisRepository {
    fun setMemberCache(value: Member)
    fun setMembersCache(values: List<Member>)
    fun getMemberCache(id: Long): Member?
    fun getMembersCache(ids: List<Long>): List<Member?>
}
