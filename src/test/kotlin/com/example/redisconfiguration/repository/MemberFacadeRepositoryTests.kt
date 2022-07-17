package com.example.redisconfiguration.repository

import com.example.redisconfiguration.config.RedisExpireTime
import com.example.redisconfiguration.config.RedisKey
import com.example.redisconfiguration.config.property.RedisServers
import com.example.redisconfiguration.domain.Member
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.RedisConnectionFailureException
import java.util.concurrent.TimeUnit

internal class MemberFacadeRepositoryTests : DescribeSpec({

    val mockRedisServers = mockk<RedisServers>()
    val mockMemberRedisServer1Repository = mockk<MemberRedisServer1Repository>()
    val mockMemberRedisServer2Repository = mockk<MemberRedisServer2Repository>()
    val mockMemberRedisServer3Repository = mockk<MemberRedisServer3Repository>()
    val memberFacadeRedisRepositoryImpl = MemberFacadeRedisRepositoryImpl(
        redisServers = mockRedisServers,
        memberRedisServer1Repository = mockMemberRedisServer1Repository,
        memberRedisServer2Repository = mockMemberRedisServer2Repository,
        memberRedisServer3Repository = mockMemberRedisServer3Repository,
    )

    describe("setMemberCache 메소드는") {
        val id = 1L
        val name = "KimChunGyu"
        val key = RedisKey.getMemberKey(id = id)
        val value = Member.create(id = id, name = name)

        it("모든 Redis 서버에 Member 캐시를 저장한다.") {
            justRun {
                mockMemberRedisServer1Repository.set(
                    key = key,
                    value = value,
                    expireTime = RedisExpireTime.MEMBER,
                    timeUnit = TimeUnit.SECONDS
                )
            }
            justRun {
                mockMemberRedisServer2Repository.set(
                    key = key,
                    value = value,
                    expireTime = RedisExpireTime.MEMBER,
                    timeUnit = TimeUnit.SECONDS
                )
            }
            justRun {
                mockMemberRedisServer3Repository.set(
                    key = key,
                    value = value,
                    expireTime = RedisExpireTime.MEMBER,
                    timeUnit = TimeUnit.SECONDS
                )
            }

            memberFacadeRedisRepositoryImpl.setMemberCache(value = value)
        }

        context("1번 Redis Server가 장애로 인해 RedisConnectionFailureException 예외를 던지는 경우") {
            it("나머지 Redis Server에는 Member 캐시가 정상적으로 저장된다.") {
                // given
                every {
                    mockMemberRedisServer1Repository.set(
                        key = key,
                        value = value,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }.throws(RedisConnectionFailureException("RedisConnectionFailureException"))
                justRun {
                    mockMemberRedisServer2Repository.set(
                        key = key,
                        value = value,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
                justRun {
                    mockMemberRedisServer3Repository.set(
                        key = key,
                        value = value,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }

                // when
                memberFacadeRedisRepositoryImpl.setMemberCache(value = value)

                // then
                verify {
                    mockMemberRedisServer1Repository.set(
                        key = key,
                        value = value,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
            }
        }

        context("2번 Redis Server가 장애로 인해 QueryTimeoutException 예외를 던지는 경우") {
            it("나머지 Redis Server에는 Member 캐시가 정상적으로 저장된다.") {
                // given
                justRun {
                    mockMemberRedisServer1Repository.set(
                        key = key,
                        value = value,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
                every {
                    mockMemberRedisServer2Repository.set(
                        key = key,
                        value = value,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }.throws(QueryTimeoutException("QueryTimeoutException"))
                justRun {
                    mockMemberRedisServer3Repository.set(
                        key = key,
                        value = value,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }

                // when
                memberFacadeRedisRepositoryImpl.setMemberCache(value = value)

                // then
                verify {
                    mockMemberRedisServer2Repository.set(
                        key = key,
                        value = value,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
            }
        }
    }

    describe("setMembersCache 메소드는") {
        val ids = listOf(1L, 2L, 3L)
        val names = listOf("KimChunGyu", "HongGilDong", "KooJunPyo")
        val values = ids.mapIndexed { index, id -> Member.create(id = id, name = names[index]) }
        val keysAndValues = values.map {
            Pair(first = RedisKey.getMemberKey(id = it.id), second = Member.create(id = it.id, name = it.name))
        }

        it("모든 Redis 서버에 Members 캐시를 저장한다.") {
            justRun {
                mockMemberRedisServer1Repository.setUsingPipeline(
                    keysAndValues = keysAndValues,
                    expireTime = RedisExpireTime.MEMBER,
                    timeUnit = TimeUnit.SECONDS
                )
            }
            justRun {
                mockMemberRedisServer2Repository.setUsingPipeline(
                    keysAndValues = keysAndValues,
                    expireTime = RedisExpireTime.MEMBER,
                    timeUnit = TimeUnit.SECONDS
                )
            }
            justRun {
                mockMemberRedisServer3Repository.setUsingPipeline(
                    keysAndValues = keysAndValues,
                    expireTime = RedisExpireTime.MEMBER,
                    timeUnit = TimeUnit.SECONDS
                )
            }

            memberFacadeRedisRepositoryImpl.setMembersCache(values = values)
        }

        context("1, 2번 Redis Server가 장애로 인해 RedisConnectionFailureException 예외를 던지는 경우") {
            it("3번 Redis Server에는 Members 캐시가 정상적으로 저장된다.") {
                // given
                every {
                    mockMemberRedisServer1Repository.setUsingPipeline(
                        keysAndValues = keysAndValues,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }.throws(RedisConnectionFailureException("RedisConnectionFailureException"))
                justRun {
                    mockMemberRedisServer2Repository.setUsingPipeline(
                        keysAndValues = keysAndValues,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
                justRun {
                    mockMemberRedisServer3Repository.setUsingPipeline(
                        keysAndValues = keysAndValues,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }

                // when
                memberFacadeRedisRepositoryImpl.setMembersCache(values = values)

                // then
                verify {
                    mockMemberRedisServer1Repository.setUsingPipeline(
                        keysAndValues = keysAndValues,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
            }
        }

        context("3번 Redis Server가 장애로 인해 QueryTimeoutException 예외를 던지는 경우") {
            it("나머지 Redis Server에는 Members 캐시가 정상적으로 저장된다.") {
                // given
                justRun {
                    mockMemberRedisServer1Repository.setUsingPipeline(
                        keysAndValues = keysAndValues,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
                justRun {
                    mockMemberRedisServer2Repository.setUsingPipeline(
                        keysAndValues = keysAndValues,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
                every {
                    mockMemberRedisServer3Repository.setUsingPipeline(
                        keysAndValues = keysAndValues,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }.throws(QueryTimeoutException("QueryTimeoutException"))

                // when
                memberFacadeRedisRepositoryImpl.setMembersCache(values = values)

                // then
                verify {
                    mockMemberRedisServer3Repository.setUsingPipeline(
                        keysAndValues = keysAndValues,
                        expireTime = RedisExpireTime.MEMBER,
                        timeUnit = TimeUnit.SECONDS
                    )
                }
            }
        }
    }
})
